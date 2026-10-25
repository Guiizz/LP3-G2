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

public final class ConexaoBD {

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

        public static Connection abrir() {
            String url = "jdbc:sqlserver://" + config("DB_SERVER")
                    + ";databaseName=" + config("DB_DATABASE")
                    + ";encrypt=false;trustServerCertificate=true"
                    + ";loginTimeout=5";
            try {
                return DriverManager.getConnection(url, config("DB_USER"), config("DB_PASSWORD"));
            } catch (SQLException e) {
                throw  new DataAccessException("Nâo foi possivel ligar à base de dados", e);
            }
        }

        public static <T> List<T> select(String sql, RowMapper<T> mapper,Object... params) {
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
