package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do {@link IFornecedorDAL} sobre a tabela dos fornecedores do SQL Server.
 */
public class FornecedorDAL implements IFornecedorDAL {

    private static final String PROCURAR_POR_CODIGO =
            "SELECT id, codigo, nome, morada1, morada2, cidade, codigoPostal, pais, codigoPais, "
            + "passwordHash, passwordSalt FROM Fornecedor WHERE codigo = ?";

    @Override
    public Optional<Fornecedor> procurarPorCodigo(String codigo) {
        List<Fornecedor> fornecedores = ConexaoBD.select(PROCURAR_POR_CODIGO, new FornecedorMapper(), codigo);
        if (fornecedores.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fornecedores.get(0));
    }

    private static class FornecedorMapper implements RowMapper<Fornecedor> {
        @Override
        public Fornecedor map(ResultSet rs) throws SQLException {
            Fornecedor fornecedor = new Fornecedor(rs.getInt("id"), rs.getString("codigo"), rs.getString("nome"),
                    rs.getString("morada1"), rs.getString("morada2"), rs.getString("cidade"),
                    rs.getString("codigoPostal"), rs.getString("pais"), rs.getString("codigoPais"));
            fornecedor.definirPassword(rs.getString("passwordHash"), rs.getString("passwordSalt"));
            return fornecedor;
        }
    }
}
