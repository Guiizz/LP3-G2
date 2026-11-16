package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IStockDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.MovimentoStock;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoMovimento;

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
            "SELECT m.id, m.codigo, m.descricao, f.nome AS fornecedor, s.quantidade, "
            + "ISNULL((SELECT SUM(l.unidades) FROM LinhaEntrega l JOIN Entrega e ON e.id = l.entregaId "
            + "WHERE l.materialId = m.id AND e.estado = 'ACEITE'), 0) AS entrou, "
            + "ISNULL((SELECT SUM(ms.quantidade) FROM MovimentoStock ms WHERE ms.materialId = m.id), 0) AS saiu "
            + "FROM Stock s JOIN Material m ON m.id = s.materialId JOIN Fornecedor f ON f.id = m.fornecedorId "
            + "ORDER BY m.descricao, f.nome, m.id";
    private static final String LISTAR_MOVIMENTOS =
            "SELECT 'ENTRADA' AS tipo, l.unidades AS quantidade, COALESCE(e.decididaEm, e.registadaEm) AS registadoEm, "
            + "p.nome AS funcionario FROM LinhaEntrega l JOIN Entrega e ON e.id = l.entregaId "
            + "LEFT JOIN Pessoa p ON p.id = COALESCE(e.decididaPor, e.registadaPor) "
            + "WHERE l.materialId = ? AND e.estado = 'ACEITE' AND l.unidades IS NOT NULL "
            + "UNION ALL "
            + "SELECT 'SAIDA', ms.quantidade, ms.registadoEm, p.nome FROM MovimentoStock ms "
            + "LEFT JOIN Pessoa p ON p.id = ms.funcionarioId WHERE ms.materialId = ? "
            + "ORDER BY registadoEm DESC, tipo";
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

    @Override
    public List<MovimentoStock> listarMovimentos(int materialId) {
        return ConexaoBD.select(LISTAR_MOVIMENTOS, new MovimentoMapper(materialId), materialId, materialId);
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
            MaterialStock material = new MaterialStock(rs.getInt("id"), rs.getString("codigo"),
                    rs.getString("descricao"), rs.getString("fornecedor"), rs.getInt("quantidade"));
            material.setMovimentos(rs.getInt("entrou"), rs.getInt("saiu"));
            return material;
        }
    }

    private static class MovimentoMapper implements RowMapper<MovimentoStock> {
        private final int materialId;

        MovimentoMapper(int materialId) {
            this.materialId = materialId;
        }

        @Override
        public MovimentoStock map(ResultSet rs) throws SQLException {
            return new MovimentoStock(TipoMovimento.valueOf(rs.getString("tipo")), materialId,
                    rs.getInt("quantidade"), rs.getString("funcionario"),
                    rs.getTimestamp("registadoEm").toLocalDateTime());
        }
    }
}
