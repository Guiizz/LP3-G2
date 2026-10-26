package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FuncionarioDAL implements IFuncionarioDAL {

    private static final String EXISTE_NIF = "SELECT 1 FROM Pessoa WHERE nif = ?";
    private static final String EXISTE_NOME_UTILIZADOR = "SELECT 1 FROM Utilizador WHERE nomeUtilizador = ?";
    private static final String INSERIR_PESSOA = "INSERT INTO Pessoa (nome, nif, dataNascimento, telemovel, email, cc, morada) " + "OUTPUT INSERTED.id VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String PROXIMO_NUMERO = "SELECT ISNULL(MAX(numeroFuncionario), 0) + 1 FROM Funcionario";
    private static final String INSERIR_FUNCIONARIO = "INSERT INTO Funcionario (pessoaId, numeroFuncionario, dataAdmissao) VALUES (?, ?, ?)";
    private static final String INSERIR_UTILIZADOR = "INSERT INTO Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil, ativo) " + "VALUES (?, ?, ?, ?, ?, ?)";

    @Override
    public boolean existeNif(String nif) {
        return !ConexaoBD.select(EXISTE_NIF, rs -> rs.getInt(1), nif).isEmpty();
    }

    @Override
    public boolean existeNomeUtilizador(String nomeUtilizador) {
        return !ConexaoBD.select(EXISTE_NOME_UTILIZADOR, rs -> rs.getInt(1), nomeUtilizador).isEmpty();
    }

    @Override
    public void criarConta(Funcionario funcionario, Utilizador utilizador) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try {
                int pessoaId = inserirPessoa(conn, funcionario);
                int numero = proximoNumero(conn);
                executar(conn, INSERIR_FUNCIONARIO, pessoaId, numero, funcionario.getDataAdmissao());
                executar(conn, INSERIR_UTILIZADOR, pessoaId, utilizador.getNomeUtilizador(),
                        utilizador.getPasswordHash(), utilizador.getPasswordSalt(),
                        utilizador.getPerfil().name(), utilizador.isAtivo());
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao criar a conta", e);
        }
    }

    private static int inserirPessoa(Connection conn, Funcionario funcionario) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERIR_PESSOA)) {
            definirParametros(ps, funcionario.getNome(), funcionario.getNif(), funcionario.getDataNascimento(), funcionario.getTelemovel(),
                    funcionario.getEmail(), funcionario.getCc(), funcionario.getMorada());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static int proximoNumero(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(PROXIMO_NUMERO);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void executar(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            definirParametros(ps, params);
            ps.executeUpdate();
        }
    }

    private static void definirParametros(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
