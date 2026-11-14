package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.security.Security;

/**
 * Liga a aplicação ao SQL Server e executa as instruções SQL.
 * Os dados da ligação (servidor, base de dados, utilizador e password)
 * são lidos do ficheiro {@code .env}, que não vai para o repositório.
 */
public final class ConexaoBD {

    // O servidor do ISEP só aceita uma versão antiga de TLS, por isso é preciso voltar a permiti-la
    static {
        Security.setProperty("jdk.tls.disabledAlgorithms",
                "SSLv3, DTLSv1.0, RC4, DES, MD5withRSA, DH keySize < 1024, "
                        + "EC keySize < 224, 3DES_EDE_CBC, anon, NULL, ECDH");
    }

    static final int TEMPO_ESPERA_SEGUNDOS = 10;
    static final int TEMPO_RESPOSTA_SEGUNDOS = 30;
    static final int TENTATIVAS = 2;
    static final long VERIFICAR_DEPOIS_DE_MS = 60_000;
    static final String SEM_LIGACAO =
            "Não foi possível ligar à base de dados. Verifique a ligação à internet e tente outra vez.";
    static final String LIGACAO_PERDIDA = "A ligação à base de dados falhou. Tente outra vez.";

    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    private static Connection partilhada;
    private static long usadaEm;

    private ConexaoBD() {}

    interface FonteDeLigacoes {
        Connection ligar() throws SQLException;
    }

    private static class LigacaoAoServidor implements FonteDeLigacoes {
        @Override
        public Connection ligar() throws SQLException {
            String url = "jdbc:sqlserver://" + config("DB_SERVER")
                    + ";databaseName=" + config("DB_DATABASE")
                    + ";encrypt=false;trustServerCertificate=true"
                    + ";loginTimeout=" + TEMPO_ESPERA_SEGUNDOS
                    + ";socketTimeout=" + (TEMPO_RESPOSTA_SEGUNDOS * 1000);
            return DriverManager.getConnection(url, config("DB_USER"), config("DB_PASSWORD"));
        }
    }

    private static String config(String chave) {
        String valor = DOTENV.get(chave);
        if (valor == null || valor.isBlank()) {
            throw new DataAccessException("Falta a configuração '" + chave + "' no ficheiro .env");
        }
        return valor;
    }

    /**
     * Abre uma ligação nova à base de dados. Quem a abre tem de a fechar.
     * Se a primeira tentativa falhar, tenta outra vez antes de desistir.
     *
     * @return a ligação aberta
     * @throws DataAccessException se faltar configuração no .env ou não for possível ligar
     */
    public static Connection abrir() {
        return abrir(new LigacaoAoServidor(), TENTATIVAS);
    }

    static Connection abrir(FonteDeLigacoes fonte, int tentativas) {
        SQLException ultimoErro = null;
        for (int tentativa = 1; tentativa <= tentativas; tentativa++) {
            try {
                return fonte.ligar();
            } catch (SQLException e) {
                ultimoErro = e;
            }
        }
        throw new DataAccessException(SEM_LIGACAO, ultimoErro);
    }

    static boolean ligacaoCaiu(SQLException erro) {
        String estado = erro.getSQLState();
        return estado != null && estado.startsWith("08");
    }

    static String mensagem(SQLException erro, String textoNormal) {
        if (ligacaoCaiu(erro)) {
            return LIGACAO_PERDIDA;
        }
        return textoNormal;
    }

    private static Connection ligacaoPartilhada() throws SQLException {
        long agora = System.currentTimeMillis();
        if (partilhada != null && (partilhada.isClosed()
                || (agora - usadaEm > VERIFICAR_DEPOIS_DE_MS && !partilhada.isValid(2)))) {
            esquecerPartilhada();
        }
        if (partilhada == null) {
            partilhada = abrir();
        }
        usadaEm = agora;
        return partilhada;
    }

    private static void esquecerPartilhada() {
        Connection antiga = partilhada;
        partilhada = null;
        if (antiga != null) {
            try {
                antiga.close();
            } catch (SQLException e) {
                return;
            }
        }
    }

    private static void tratarErro(SQLException erro) {
        if (ligacaoCaiu(erro)) {
            esquecerPartilhada();
        }
    }

    /**
     * Executa um SELECT e converte cada linha do resultado num objeto.
     * Usa sempre a mesma ligação enquanto ela estiver boa, para não perder tempo a ligar outra vez.
     * Se a ligação cair a meio de uma leitura, tenta outra vez com uma ligação nova.
     *
     * @param sql    a consulta, com {@code ?} no lugar de cada parâmetro
     * @param mapper converte uma linha do resultado num objeto
     * @param params os valores dos parâmetros, pela ordem dos {@code ?}
     * @param <T>    o tipo de objeto devolvido
     * @return a lista de objetos (vazia se não houver resultados)
     * @throws DataAccessException se a consulta falhar
     */
    public static synchronized <T> List<T> select(String sql, RowMapper<T> mapper, Object... params) {
        try {
            return consultar(sql, mapper, params);
        } catch (SQLException e) {
            tratarErro(e);
            if (!ligacaoCaiu(e) || !eLeitura(sql)) {
                throw new DataAccessException(mensagem(e, "Erro ao executar a consulta"), e);
            }
        }
        try {
            return consultar(sql, mapper, params);
        } catch (SQLException e) {
            tratarErro(e);
            throw new DataAccessException(mensagem(e, "Erro ao executar a consulta"), e);
        }
    }

    private static <T> List<T> consultar(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        Connection conn = ligacaoPartilhada();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            definirParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> resultados = new ArrayList<>();
                while (rs.next()) {
                    resultados.add(mapper.map(rs));
                }
                return resultados;
            }
        }
    }

    static boolean eLeitura(String sql) {
        return sql.trim().toUpperCase().startsWith("SELECT");
    }

    public static void preparar() {
        Thread ligar = new Thread(new PrepararLigacao(), "preparar-ligacao");
        ligar.setDaemon(true);
        ligar.start();
    }

    private static class PrepararLigacao implements Runnable {
        @Override
        public void run() {
            synchronized (ConexaoBD.class) {
                try {
                    ligacaoPartilhada();
                } catch (SQLException | DataAccessException e) {
                    return;
                }
            }
        }
    }

    /**
     * Executa um INSERT, UPDATE ou DELETE.
     * Usa sempre a mesma ligação enquanto ela estiver boa, para não perder tempo a ligar outra vez.
     *
     * @param sql    a instrução, com {@code ?} no lugar de cada parâmetro
     * @param params os valores dos parâmetros, pela ordem dos {@code ?}
     * @return o número de linhas alteradas
     * @throws DataAccessException se a instrução falhar
     */
    public static synchronized int executar(String sql, Object... params) {
        try {
            Connection conn = ligacaoPartilhada();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                definirParametros(ps, params);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            tratarErro(e);
            throw new DataAccessException(mensagem(e, "Erro ao executar a instrução"), e);
        }
    }

    private static void definirParametros(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
