package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação em SQL Server de {@link IStockDAL}. A saída só tira do stock se houver quantidade
 * suficiente, e o movimento fica registado na mesma transação.
 */
public class StockDAL implements IStockDAL {

    private static final String LISTAR =
            "SELECT m.id, m.codigo, m.descricao, f.nome AS fornecedor, s.quantidade FROM Stock s "
            + "JOIN Material m ON m.id = s.materialId JOIN Fornecedor f ON f.id = m.fornecedorId "
            + "ORDER BY m.descricao, f.nome, m.id";
    private static final String RETIRAR =
            "UPDATE Stock SET quantidade = quantidade - ? WHERE materialId = ? AND quantidade >= ?";
    private static final String REGISTAR_MOVIMENTO =
            "INSERT INTO MovimentoStock (materialId, quantidade, funcionarioId) VALUES (?, ?, ?)";

    @Override
    public List<MaterialStock> listar() {
        return ConexaoBD.select(LISTAR, new MaterialStockMapper());
    }

    @Override
    public boolean registarSaida(int materialId, int quantidade, int funcionarioId) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try {
                if (executar(conn, RETIRAR, quantidade, materialId, quantidade) == 0) {
                    conn.rollback();
                    return false;
                }
                executar(conn, REGISTAR_MOVIMENTO, materialId, quantidade, funcionarioId);
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao registar a saída de stock", e);
        }
    }

    private static int executar(Connection conn, String sql, int... valores) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setInt(i + 1, valores[i]);
            }
            return ps.executeUpdate();
        }
    }

    private static class MaterialStockMapper implements RowMapper<MaterialStock> {
        @Override
        public MaterialStock map(ResultSet rs) throws SQLException {
            return new MaterialStock(rs.getInt("id"), rs.getString("codigo"), rs.getString("descricao"),
                    rs.getString("fornecedor"), rs.getInt("quantidade"));
        }
    }
}
