package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFuncionarioDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementação do {@link IFuncionarioDAL} sobre as tabelas Pessoa, Funcionario e Utilizador do SQL Server.
 * O número de funcionário é o seguinte ao maior que já existe.
 */
public class FuncionarioDAL implements IFuncionarioDAL {

    private static final String EXISTE_NIF = "SELECT 1 FROM Pessoa WHERE nif = ?";
    private static final String EXISTE_NOME_UTILIZADOR = "SELECT 1 FROM Utilizador WHERE nomeUtilizador = ?";
    private static final String INSERIR_PESSOA = "INSERT INTO Pessoa (nome, nif, dataNascimento, telemovel, email, cc, morada) " + "OUTPUT INSERTED.id VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String PROXIMO_NUMERO = "SELECT ISNULL(MAX(numeroFuncionario), 0) + 1 FROM Funcionario";
    private static final String INSERIR_FUNCIONARIO = "INSERT INTO Funcionario (pessoaId, numeroFuncionario, dataAdmissao, empresa) VALUES (?, ?, ?, ?)";
    private static final String INSERIR_UTILIZADOR = "INSERT INTO Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil, ativo) " + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String PROCURAR = "SELECT p.id, p.nome, p.cc, p.nif, p.morada, p.telemovel, p.dataNascimento, p.email, "
            + "f.numeroFuncionario, f.dataAdmissao, f.empresa, "
            + "(SELECT TOP 1 u2.perfil FROM Utilizador u2 WHERE u2.funcionarioId = f.pessoaId) AS perfil "
            + "FROM Funcionario f JOIN Pessoa p ON p.id = f.pessoaId ";
    private static final String PROCURAR_POR_NUMERO = PROCURAR + "WHERE f.numeroFuncionario = ?";
    private static final String PROCURAR_POR_ID = PROCURAR + "WHERE f.pessoaId = ?";
    private static final String EDITAR_PESSOA =
            "UPDATE Pessoa SET nome = ?, telemovel = ?, email = ?, morada = ? WHERE id = ?";
    private static final String EDITAR_EMPRESA = "UPDATE Funcionario SET empresa = ? WHERE pessoaId = ?";
    private static final String EDITAR_PERFIL =
            "UPDATE Utilizador SET perfil = ? WHERE id = ? AND funcionarioId = ?";
    private static final String TEM_CONTA_ATIVA = "SELECT 1 FROM Utilizador WHERE funcionarioId = ? AND ativo = 1";
    private static final String LISTAR_COM_CONTA_ATIVA = PROCURAR
            + "JOIN Utilizador u ON u.funcionarioId = f.pessoaId WHERE u.ativo = 1 ORDER BY f.numeroFuncionario";

    @Override
    public boolean existeNif(String nif) {
        return !ConexaoBD.select(EXISTE_NIF, new InteiroMapper(), nif).isEmpty();
    }

    @Override
    public boolean existeNomeUtilizador(String nomeUtilizador) {
        return !ConexaoBD.select(EXISTE_NOME_UTILIZADOR, new InteiroMapper(), nomeUtilizador).isEmpty();
    }

    @Override
    public void criarConta(Funcionario funcionario, Utilizador utilizador) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try {
                int pessoaId = inserirPessoa(conn, funcionario);
                int numero = proximoNumero(conn);
                executar(conn, INSERIR_FUNCIONARIO, pessoaId, numero, funcionario.getDataAdmissao(),
                        funcionario.getEmpresa());
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

    @Override
    public boolean editar(Funcionario funcionario, int utilizadorId, Perfil perfil) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try {
                int contas = executar(conn, EDITAR_PERFIL, perfil.name(), utilizadorId, funcionario.getId());
                if (contas == 0) {
                    conn.rollback();
                    return false;
                }
                executar(conn, EDITAR_PESSOA, funcionario.getNome(), funcionario.getTelemovel(),
                        funcionario.getEmail(), funcionario.getMorada(), funcionario.getId());
                executar(conn, EDITAR_EMPRESA, funcionario.getEmpresa(), funcionario.getId());
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao editar o funcionário", e);
        }
    }

    @Override
    public Funcionario procurarPorNumero(int numeroFuncionario) {
        return primeiro(ConexaoBD.select(PROCURAR_POR_NUMERO, new FuncionarioMapper(), numeroFuncionario));
    }

    @Override
    public Funcionario procurarPorId(int pessoaId) {
        return primeiro(ConexaoBD.select(PROCURAR_POR_ID, new FuncionarioMapper(), pessoaId));
    }

    @Override
    public boolean temContaAtiva(int pessoaId) {
        return !ConexaoBD.select(TEM_CONTA_ATIVA, new InteiroMapper(), pessoaId).isEmpty();
    }

    @Override
    public List<Funcionario> listarComContaAtiva() {
        return ConexaoBD.select(LISTAR_COM_CONTA_ATIVA, new FuncionarioMapper());
    }

    private static Funcionario primeiro(List<Funcionario> funcionarios) {
        return funcionarios.isEmpty() ? null : funcionarios.get(0);
    }

    private static class FuncionarioMapper implements RowMapper<Funcionario> {
        @Override
        public Funcionario map(ResultSet rs) throws SQLException {
            Funcionario funcionario = new Funcionario(rs.getInt("id"), rs.getString("nome"), rs.getString("cc"),
                    rs.getString("nif"), rs.getString("morada"), rs.getString("telemovel"),
                    rs.getObject("dataNascimento", LocalDate.class), rs.getString("email"),
                    rs.getInt("numeroFuncionario"), rs.getObject("dataAdmissao", LocalDate.class));
            funcionario.setEmpresa(rs.getString("empresa"));
            String perfil = rs.getString("perfil");
            if (perfil != null) {
                funcionario.setPerfil(Perfil.valueOf(perfil));
            }
            return funcionario;
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

    private static int executar(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            definirParametros(ps, params);
            return ps.executeUpdate();
        }
    }

    private static void definirParametros(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
