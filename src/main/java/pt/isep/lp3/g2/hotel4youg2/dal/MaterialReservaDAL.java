package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IMaterialReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Material;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.MotivoSaida;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação em SQL Server de {@link IMaterialReservaDAL} (US76). Também tem os passos de stock do check-in
 * e do check-out, que o {@link ReservaDAL} corre na mesma transação em que muda o estado da reserva: no check-in
 * os consumíveis saem do stock de vez e os não consumíveis saem como temporários, ligados à reserva; no
 * check-out todas as saídas temporárias da reserva voltam ao stock (também as dos pedidos extra da US73).
 */
public class MaterialReservaDAL implements IMaterialReservaDAL {

    private static final String COLUNAS =
            "SELECT q.tipo AS tipoQuarto, %s AS quantidade, m.id, m.codigo, m.descricao, f.nome AS fornecedor, "
            + "m.consumivel ";
    private static final String LISTAR_AJUSTADOS = String.format(COLUNAS, "mr.quantidade")
            + "FROM MaterialReserva mr JOIN Reserva r ON r.id = mr.reservaId "
            + "JOIN Quarto q ON q.numero = r.quartoNumero JOIN Material m ON m.id = mr.materialId "
            + "JOIN Fornecedor f ON f.id = m.fornecedorId WHERE mr.reservaId = ? ORDER BY m.descricao, f.nome, m.id";
    private static final String LISTAR_DA_CONFIGURACAO = String.format(COLUNAS, "c.quantidade")
            + "FROM Reserva r JOIN Quarto q ON q.numero = r.quartoNumero "
            + "JOIN ConfiguracaoQuarto c ON c.tipoQuarto = q.tipo JOIN Material m ON m.id = c.materialId "
            + "JOIN Fornecedor f ON f.id = m.fornecedorId WHERE r.id = ? ORDER BY m.descricao, f.nome, m.id";
    private static final String COPIAR_CONFIGURACAO =
            "INSERT INTO MaterialReserva (reservaId, materialId, quantidade) "
            + "SELECT r.id, c.materialId, c.quantidade FROM Reserva r JOIN Quarto q ON q.numero = r.quartoNumero "
            + "JOIN ConfiguracaoQuarto c ON c.tipoQuarto = q.tipo WHERE r.id = ? "
            + "AND NOT EXISTS (SELECT 1 FROM MaterialReserva mr WITH (UPDLOCK, HOLDLOCK) WHERE mr.reservaId = r.id)";
    private static final String GUARDAR_QUANTIDADE =
            "MERGE MaterialReserva WITH (HOLDLOCK) AS mr "
            + "USING (SELECT ? AS reservaId, ? AS materialId, ? AS quantidade) AS n "
            + "ON mr.reservaId = n.reservaId AND mr.materialId = n.materialId "
            + "WHEN MATCHED THEN UPDATE SET quantidade = n.quantidade "
            + "WHEN NOT MATCHED THEN INSERT (reservaId, materialId, quantidade) "
            + "VALUES (n.reservaId, n.materialId, n.quantidade);";
    private static final String RETIRAR_DO_STOCK =
            "UPDATE Stock SET quantidade = quantidade - ? WHERE materialId = ? AND quantidade >= ?";
    private static final String QUANTIDADE_EM_STOCK = "SELECT quantidade FROM Stock WHERE materialId = ?";
    private static final String REGISTAR_SAIDA =
            "INSERT INTO MovimentoStock (materialId, quantidade, funcionarioId, tipo, motivo, descricaoMotivo, "
            + "reservaId) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String DEVOLVER_AO_STOCK =
            "UPDATE s SET s.quantidade = s.quantidade + t.soma FROM Stock s JOIN "
            + "(SELECT materialId, SUM(quantidade) AS soma FROM MovimentoStock WHERE reservaId = ? "
            + "AND tipo = 'TEMPORARIA' AND devolvidaEm IS NULL GROUP BY materialId) t ON t.materialId = s.materialId";
    private static final String MARCAR_DEVOLVIDAS =
            "UPDATE MovimentoStock SET devolvidaEm = SYSDATETIME(), devolvidaPor = ? "
            + "WHERE reservaId = ? AND tipo = 'TEMPORARIA' AND devolvidaEm IS NULL";

    @Override
    public List<MaterialQuarto> listar(int reservaId) {
        List<MaterialQuarto> ajustados = ConexaoBD.select(LISTAR_AJUSTADOS, new MaterialReservaMapper(), reservaId);
        if (!ajustados.isEmpty()) {
            return ajustados;
        }
        return ConexaoBD.select(LISTAR_DA_CONFIGURACAO, new MaterialReservaMapper(), reservaId);
    }

    @Override
    public void ajustar(int reservaId, int materialId, int quantidade) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try {
                executar(conn, COPIAR_CONFIGURACAO, reservaId);
                executar(conn, GUARDAR_QUANTIDADE, reservaId, materialId, quantidade);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao ajustar os materiais da reserva", e);
        }
    }

    /**
     * Tira do stock os materiais da reserva, no check-in, dentro da transação de quem chama: os consumíveis
     * saem de vez (motivo "Consumo da reserva") e os não consumíveis saem como temporários. Se algum material
     * não tiver stock suficiente, não regista nada e diz todos os que faltam.
     *
     * @param conn          a ligação com a transação aberta
     * @param reservaId     o id da reserva
     * @param materiais     os materiais e as quantidades da reserva
     * @param funcionarioId o id de quem faz o check-in
     * @throws SQLException          se a base de dados der erro
     * @throws IllegalStateException se faltar stock de algum material; quem chama desfaz a transação
     */
    public void darBaixa(Connection conn, int reservaId, List<MaterialQuarto> materiais, int funcionarioId)
            throws SQLException {
        List<String> emFalta = new ArrayList<>();
        String descricao = "Check-in da reserva " + reservaId;
        for (MaterialQuarto linha : materiais) {
            int quantidade = linha.getQuantidade();
            if (quantidade == 0) {
                continue;
            }
            if (executar(conn, RETIRAR_DO_STOCK, quantidade, linha.getMaterialId(), quantidade) == 0) {
                emFalta.add(linha.getDescricao() + " (precisa de " + quantidade + ", há "
                        + quantidadeEmStock(conn, linha.getMaterialId()) + ")");
            } else if (linha.getMaterial().isConsumivel()) {
                executar(conn, REGISTAR_SAIDA, linha.getMaterialId(), quantidade, funcionarioId,
                        TipoSaida.DEFINITIVA.name(), MotivoSaida.CONSUMO_RESERVA.name(), descricao, reservaId);
            } else {
                executar(conn, REGISTAR_SAIDA, linha.getMaterialId(), quantidade, funcionarioId,
                        TipoSaida.TEMPORARIA.name(), null, descricao, reservaId);
            }
        }
        if (!emFalta.isEmpty()) {
            throw new IllegalStateException("Não há stock suficiente para o check-in: " + String.join("; ", emFalta)
                    + ". Ajuste os materiais da reserva ou espere por uma entrega.");
        }
    }

    /**
     * Devolve ao stock todas as saídas temporárias da reserva que ainda estão fora, no check-out, dentro da
     * transação de quem chama, e marca-as como devolvidas por quem faz o check-out.
     *
     * @param conn          a ligação com a transação aberta
     * @param reservaId     o id da reserva
     * @param funcionarioId o id de quem faz o check-out
     * @throws SQLException se a base de dados der erro
     */
    public void devolverTemporarias(Connection conn, int reservaId, int funcionarioId) throws SQLException {
        executar(conn, DEVOLVER_AO_STOCK, reservaId);
        executar(conn, MARCAR_DEVOLVIDAS, funcionarioId, reservaId);
    }

    private static int quantidadeEmStock(Connection conn, int materialId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(QUANTIDADE_EM_STOCK)) {
            ps.setInt(1, materialId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static int executar(Connection conn, String sql, Object... valores) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setObject(i + 1, valores[i]);
            }
            return ps.executeUpdate();
        }
    }

    private static class MaterialReservaMapper implements RowMapper<MaterialQuarto> {
        @Override
        public MaterialQuarto map(ResultSet rs) throws SQLException {
            Material material = new Material(rs.getInt("id"), rs.getString("codigo"), rs.getString("descricao"),
                    rs.getString("fornecedor"), rs.getBoolean("consumivel"));
            return new MaterialQuarto(TipoQuarto.valueOf(rs.getString("tipoQuarto")), material,
                    rs.getInt("quantidade"));
        }
    }
}
