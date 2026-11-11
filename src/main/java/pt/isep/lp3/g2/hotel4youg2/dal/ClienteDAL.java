package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do {@link IClienteDAL} sobre as tabelas Pessoa e Cliente do SQL Server.
 */
public class ClienteDAL implements IClienteDAL {

    @Override
    public int inserirCliente(Cliente c) {
        try(Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            int id;
            try(PreparedStatement ps = conn.prepareStatement(
                    "INSERT Pessoa (nome, nif, dataNascimento, telemovel ,email, cc, morada) VALUES (?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getNome());
                ps.setString(2, c.getNif());
                ps.setObject(3, c.getDataNascimento());
                ps.setString(4, c.getTelemovel());
                ps.setString(5, c.getEmail());
                ps.setString(6, c.getCc());
                ps.setString(7, c.getMorada());
                ps.executeUpdate();
                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    chaves.next();
                    id = chaves.getInt(1);
                }
            }
            try (PreparedStatement psCliente = conn.prepareStatement(
                    "INSERT Cliente (pessoaId) VALUES (?)")) {
                psCliente.setInt(1, id);
                psCliente.executeUpdate();
            }
            conn.commit();
            c.setId(id);
            return id;
        }catch (SQLException e) {
            throw new DataAccessException("Erro ao registar o cliente!", e);
        }
    }

    @Override
    public boolean atualizarCliente(Cliente c) {
        return ConexaoBD.executar(
                "UPDATE Pessoa SET nome = ?, dataNascimento = ?, telemovel = ?, email = ?, cc = ?, morada = ? "
                        + "WHERE id = ? AND id IN (SELECT pessoaId FROM Cliente)",
                c.getNome(), Date.valueOf(c.getDataNascimento()), c.getTelemovel(), c.getEmail(), c.getCc(), c.getMorada(),
                c.getId()) > 0;
    }

    @Override
    public boolean existeNif(String nif) {
        try (Connection conn = ConexaoBD.abrir();
             PreparedStatement ps= conn.prepareStatement(
                     "SELECT 1 FROM Pessoa WHERE nif = ?")) {
            ps.setString(1, nif);
            try (ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        }catch (SQLException e) {
            throw new DataAccessException("Erro ao verificar o NIF!", e);
        }
    }

    @Override
    public List<Cliente> pesquisarClientes(String termo) {
        List<Cliente> clientes = new ArrayList<>();
        try(Connection conn = ConexaoBD.abrir();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT p.id, p.nome, p.cc, p.nif, p.morada, p.telemovel, p.dataNascimento, p.email "
                    + "FROM Cliente c JOIN Pessoa p ON p.id = c.pessoaId "
                    + "WHERE p.nome LIKE ? OR p.nif = ? "
                    + "ORDER BY p.nome")) {
            ps.setString(1, "%" + termo + "%");
            ps.setString(2, termo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clientes.add(criarCliente(rs));
                }
            }
        }catch (SQLException e) {
            throw new DataAccessException("Erro ao pesquisar Clientes!", e);
        }
        return clientes;
    }

    @Override
    public Cliente procurarPorNif(String nif) {
        try (Connection conn = ConexaoBD.abrir();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT p.id, p.nome, p.cc, p.nif, p.morada, p.telemovel, p.dataNascimento, p.email "
                     + "FROM Cliente c JOIN Pessoa p ON p.id = c.pessoaId "
                     + "WHERE p.nif = ?")) {
            ps.setString(1, nif);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return criarCliente(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao procurar o cliente", e);
        }
    }

    @Override
    public Cliente procurarFuncionarioNaoCliente(String nif) {
        List<Cliente> encontrados = ConexaoBD.select(
                "SELECT p.id, p.nome, p.cc, p.nif, p.morada, p.telemovel, p.dataNascimento, p.email "
                + "FROM Funcionario f JOIN Pessoa p ON p.id = f.pessoaId "
                + "WHERE p.nif = ? AND NOT EXISTS (SELECT 1 FROM Cliente c WHERE c.pessoaId = p.id)",
                new ClienteMapper(), nif);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    @Override
    public boolean tornarCliente(Cliente c) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psCliente = conn.prepareStatement(
                         "INSERT Cliente (pessoaId) SELECT ? WHERE NOT EXISTS "
                         + "(SELECT 1 FROM Cliente WITH (UPDLOCK, HOLDLOCK) WHERE pessoaId = ?)");
                 PreparedStatement psPessoa = conn.prepareStatement(
                         "UPDATE Pessoa SET dataNascimento = COALESCE(?, dataNascimento), "
                         + "telemovel = COALESCE(?, telemovel), email = COALESCE(?, email), "
                         + "cc = COALESCE(?, cc), morada = COALESCE(?, morada) WHERE id = ?")) {
                psCliente.setInt(1, c.getId());
                psCliente.setInt(2, c.getId());
                if (psCliente.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
                psPessoa.setObject(1, c.getDataNascimento());
                psPessoa.setString(2, c.getTelemovel());
                psPessoa.setString(3, c.getEmail());
                psPessoa.setString(4, c.getCc());
                psPessoa.setString(5, c.getMorada());
                psPessoa.setInt(6, c.getId());
                psPessoa.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao registar o funcionário como cliente!", e);
        }
    }

    private class ClienteMapper implements RowMapper<Cliente> {
        @Override
        public Cliente map(ResultSet rs) throws SQLException {
            return criarCliente(rs);
        }
    }

    private Cliente criarCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("cc"),
                rs.getString("nif"),
                rs.getString("morada"),
                rs.getString("telemovel"),
                rs.getObject("dataNascimento", LocalDate.class),
                rs.getString("email"));
    }
}
