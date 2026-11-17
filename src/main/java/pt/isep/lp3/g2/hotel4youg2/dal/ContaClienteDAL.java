package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IContaClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ContaClienteDAL implements IContaClienteDAL {

    private static final String INSERIR =
            "INSERT INTO ContaCliente (clienteId, passwordHash, passwordSalt, primeiroAcesso) OUTPUT INSERTED.id "
            + "VALUES (?, ?, ?, 1)";
    private static final String PROCURAR_POR_NIF =
            "SELECT c.id, c.clienteId, p.nif, p.nome, c.passwordHash, c.passwordSalt, c.primeiroAcesso "
            + "FROM ContaCliente c JOIN Pessoa p ON p.id = c.clienteId WHERE p.nif = ?";
    private static final String DEFINIR_PASSWORD =
            "UPDATE ContaCliente SET passwordHash = ?, passwordSalt = ?, primeiroAcesso = 0 "
            + "WHERE id = ? AND primeiroAcesso = 1";
    private static final String NOVO_CODIGO =
            "UPDATE ContaCliente SET passwordHash = ?, passwordSalt = ?, primeiroAcesso = 1 WHERE id = ?";
    private static final String APAGAR = "DELETE FROM ContaCliente WHERE id = ?";

    @Override
    public int inserir(ContaCliente conta) {
        int id = ConexaoBD.select(INSERIR, new InteiroMapper(), conta.getClienteId(), conta.getPasswordHash(),
                conta.getPasswordSalt()).get(0);
        conta.setId(id);
        return id;
    }

    @Override
    public ContaCliente procurarPorNif(String nif) {
        List<ContaCliente> contas = ConexaoBD.select(PROCURAR_POR_NIF, new ContaClienteMapper(), nif);
        return contas.isEmpty() ? null : contas.get(0);
    }

    @Override
    public boolean definirPassword(int contaId, String passwordHash, String passwordSalt) {
        return ConexaoBD.executar(DEFINIR_PASSWORD, passwordHash, passwordSalt, contaId) > 0;
    }

    @Override
    public boolean novoCodigo(int contaId, String passwordHash, String passwordSalt) {
        return ConexaoBD.executar(NOVO_CODIGO, passwordHash, passwordSalt, contaId) > 0;
    }

    @Override
    public boolean apagar(int contaId) {
        return ConexaoBD.executar(APAGAR, contaId) > 0;
    }

    private static class ContaClienteMapper implements RowMapper<ContaCliente> {
        @Override
        public ContaCliente map(ResultSet rs) throws SQLException {
            return new ContaCliente(rs.getInt("id"), rs.getInt("clienteId"), rs.getString("nif"),
                    rs.getString("nome"), rs.getString("passwordHash").trim(), rs.getString("passwordSalt").trim(),
                    rs.getBoolean("primeiroAcesso"));
        }
    }
}
