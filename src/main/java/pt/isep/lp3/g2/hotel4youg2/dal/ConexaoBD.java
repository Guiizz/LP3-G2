package pt.isep.lp3.g2.hotel4youg2.dal;

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

    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    private ConexaoBD() {}

    private static String config(String chave) {
        String valor = DOTENV.get(chave);
        if (valor == null || valor.isBlank()) {
            throw new DataAccessException("Falta a configuração '" + chave + "' no ficheiro .env");
        }
        return valor;
    }

    /**
     * Abre uma ligação nova à base de dados. Quem a abre tem de a fechar.
     *
     * @return a ligação aberta
     * @throws DataAccessException se faltar configuração no .env ou não for possível ligar
     */
    public static Connection abrir() {
        String url = "jdbc:sqlserver://" + config("DB_SERVER")
                + ";databaseName=" + config("DB_DATABASE")
                + ";encrypt=false;trustServerCertificate=true"
                + ";loginTimeout=5";
        try {
            return DriverManager.getConnection(url, config("DB_USER"), config("DB_PASSWORD"));
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível ligar à base de dados", e);
        }
    }

    /**
     * Executa um SELECT e converte cada linha do resultado num objeto.
     *
     * @param sql    a consulta, com {@code ?} no lugar de cada parâmetro
     * @param mapper converte uma linha do resultado num objeto
     * @param params os valores dos parâmetros, pela ordem dos {@code ?}
     * @param <T>    o tipo de objeto devolvido
     * @return a lista de objetos (vazia se não houver resultados)
     * @throws DataAccessException se a consulta falhar
     */
    public static <T> List<T> select(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection conn = abrir();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            definirParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> resultados = new ArrayList<>();
                while (rs.next()) {
                    resultados.add(mapper.map(rs));
                }
                return resultados;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao executar a consulta", e);
        }
    }

    /**
     * Executa um INSERT, UPDATE ou DELETE.
     *
     * @param sql    a instrução, com {@code ?} no lugar de cada parâmetro
     * @param params os valores dos parâmetros, pela ordem dos {@code ?}
     * @return o número de linhas alteradas
     * @throws DataAccessException se a instrução falhar
     */
    public static int executar(String sql, Object... params) {
        try (Connection conn = abrir();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            definirParametros(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao executar a instrução", e);
        }
    }

    private static void definirParametros(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
