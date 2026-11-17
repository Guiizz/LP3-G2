package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IStockDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.MovimentoStock;
import pt.isep.lp3.g2.hotel4youg2.model.SaidaStock;
import pt.isep.lp3.g2.hotel4youg2.model.enums.MotivoSaida;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoMovimento;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação em SQL Server de {@link IStockDAL}. A saída só tira do stock se houver quantidade
 * suficiente, e o movimento fica registado na mesma transação. Uma saída temporária que volta soma ao
 * stock na mesma transação em que fica marcada como devolvida (US77).
 */
public class StockDAL implements IStockDAL {

    private static final String LISTAR =
            "SELECT m.id, m.codigo, m.descricao, f.nome AS fornecedor, s.quantidade, "
            + "ISNULL((SELECT SUM(l.unidades) FROM LinhaEntrega l JOIN Entrega e ON e.id = l.entregaId "
            + "WHERE l.materialId = m.id AND e.estado = 'ACEITE'), 0) AS entrou, "
            + "ISNULL((SELECT SUM(ms.quantidade) FROM MovimentoStock ms WHERE ms.materialId = m.id "
            + "AND ms.devolvidaEm IS NULL), 0) AS saiu "
            + "FROM Stock s JOIN Material m ON m.id = s.materialId JOIN Fornecedor f ON f.id = m.fornecedorId "
            + "ORDER BY m.descricao, f.nome, m.id";
    // os NULL levam o tipo VARCHAR: num UNION, um NULL sem tipo é INT e o SQL Server tentaria converter o texto
    private static final String LISTAR_MOVIMENTOS =
            "SELECT 'ENTRADA' AS tipo, l.unidades AS quantidade, COALESCE(e.decididaEm, e.registadaEm) AS registadoEm, "
            + "p.nome AS funcionario, CAST(NULL AS VARCHAR(25)) AS motivo, "
            + "CAST(NULL AS VARCHAR(200)) AS descricaoMotivo FROM LinhaEntrega l JOIN Entrega e ON e.id = l.entregaId "
            + "LEFT JOIN Pessoa p ON p.id = COALESCE(e.decididaPor, e.registadaPor) "
            + "WHERE l.materialId = ? AND e.estado = 'ACEITE' AND l.unidades IS NOT NULL "
            + "UNION ALL "
            + "SELECT CASE WHEN ms.tipo = 'TEMPORARIA' THEN 'SAIDA_TEMPORARIA' ELSE 'SAIDA_DEFINITIVA' END, "
            + "ms.quantidade, ms.registadoEm, p.nome, ms.motivo, ms.descricaoMotivo FROM MovimentoStock ms "
            + "LEFT JOIN Pessoa p ON p.id = ms.funcionarioId WHERE ms.materialId = ? "
            + "UNION ALL "
            + "SELECT 'DEVOLUCAO', ms.quantidade, ms.devolvidaEm, p.nome, NULL, ms.descricaoMotivo "
            + "FROM MovimentoStock ms LEFT JOIN Pessoa p ON p.id = ms.devolvidaPor "
            + "WHERE ms.materialId = ? AND ms.devolvidaEm IS NOT NULL "
            + "ORDER BY registadoEm DESC, tipo";
    private static final String RETIRAR =
            "UPDATE Stock SET quantidade = quantidade - ? WHERE materialId = ? AND quantidade >= ?";
    private static final String REGISTAR_MOVIMENTO =
            "INSERT INTO MovimentoStock (materialId, quantidade, funcionarioId, tipo, motivo, descricaoMotivo) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String LISTAR_POR_DEVOLVER =
            "SELECT ms.id, ms.materialId, m.codigo, m.descricao, ms.quantidade, ms.motivo, ms.descricaoMotivo, "
            + "p.nome AS funcionario, ms.registadoEm FROM MovimentoStock ms JOIN Material m ON m.id = ms.materialId "
            + "LEFT JOIN Pessoa p ON p.id = ms.funcionarioId "
            + "WHERE ms.tipo = 'TEMPORARIA' AND ms.devolvidaEm IS NULL ORDER BY ms.registadoEm, ms.id";
    // só marca a saída se for temporária e ainda estiver fora: se outra pessoa já a devolveu, não muda nada
    private static final String MARCAR_DEVOLVIDA =
            "UPDATE MovimentoStock SET devolvidaEm = SYSDATETIME(), devolvidaPor = ? "
            + "WHERE id = ? AND tipo = 'TEMPORARIA' AND devolvidaEm IS NULL";
    private static final String DEVOLVER_AO_STOCK =
            "UPDATE s SET s.quantidade = s.quantidade + ms.quantidade FROM Stock s "
            + "JOIN MovimentoStock ms ON ms.materialId = s.materialId WHERE ms.id = ?";

    @Override
    public List<MaterialStock> listar() {
        return ConexaoBD.select(LISTAR, new MaterialStockMapper());
    }

    @Override
    public boolean registarSaida(int materialId, int quantidade, int funcionarioId, TipoSaida tipo, MotivoSaida motivo,
                                 String descricaoMotivo) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try {
                if (executar(conn, RETIRAR, quantidade, materialId, quantidade) == 0) {
                    conn.rollback();
                    return false;
                }
                String nomeMotivo = motivo == null ? null : motivo.name();
                executar(conn, REGISTAR_MOVIMENTO, materialId, quantidade, funcionarioId, tipo.name(), nomeMotivo,
                        descricaoMotivo);
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
        return ConexaoBD.select(LISTAR_MOVIMENTOS, new MovimentoMapper(materialId), materialId, materialId,
                materialId);
    }

    @Override
    public List<SaidaStock> listarPorDevolver() {
        return ConexaoBD.select(LISTAR_POR_DEVOLVER, new SaidaMapper());
    }

    @Override
    public boolean devolver(int saidaId, int funcionarioId) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try {
                if (executar(conn, MARCAR_DEVOLVIDA, funcionarioId, saidaId) == 0) {
                    conn.rollback();
                    return false;
                }
                executar(conn, DEVOLVER_AO_STOCK, saidaId);
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao devolver a saída ao stock", e);
        }
    }

    // passa o motivo guardado (nome do enum) e o texto para o texto da tabela, por exemplo "Outro: caiu ao chão"
    private static String motivo(ResultSet rs) throws SQLException {
        String nome = rs.getString("motivo");
        MotivoSaida motivo = nome == null ? null : MotivoSaida.valueOf(nome);
        return MotivoSaida.descrever(motivo, rs.getString("descricaoMotivo"));
    }

    private static int executar(Connection conn, String sql, Object... valores) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setObject(i + 1, valores[i]);
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
                    rs.getTimestamp("registadoEm").toLocalDateTime(), motivo(rs));
        }
    }

    private static class SaidaMapper implements RowMapper<SaidaStock> {
        @Override
        public SaidaStock map(ResultSet rs) throws SQLException {
            return new SaidaStock(rs.getInt("id"), rs.getInt("materialId"), rs.getString("codigo"),
                    rs.getString("descricao"), rs.getInt("quantidade"), motivo(rs), rs.getString("funcionario"),
                    rs.getTimestamp("registadoEm").toLocalDateTime());
        }
    }
}
