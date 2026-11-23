package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IPedidoExtraDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.PedidoExtra;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoPedido;
import pt.isep.lp3.g2.hotel4youg2.model.enums.MotivoSaida;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação em SQL Server de {@link IPedidoExtraDAL}.
 */
public class PedidoExtraDAL implements IPedidoExtraDAL {

    private static final String INSERIR_SE_EM_CURSO =
            "INSERT INTO PedidoExtra (reservaId, servicoId, materialId, quantidade) "
            + "OUTPUT INSERTED.id, INSERTED.pedidoEm "
            + "SELECT ?, NULLIF(?, 0), NULLIF(?, 0), ? "
            + "WHERE EXISTS (SELECT 1 FROM Reserva WHERE id = ? AND estado = 'EM_CURSO')";
    private static final String LISTAR =
            "SELECT p.id, p.reservaId, ISNULL(p.servicoId, 0) AS servicoId, ISNULL(p.materialId, 0) AS materialId, "
            + "COALESCE(s.designacao, m.descricao) AS descricao, p.quantidade, p.estado, p.motivoRecusa, "
            + "p.pedidoEm, CAST(ISNULL(m.consumivel, 0) AS BIT) AS consumivel, r.quartoNumero, c.nome AS cliente "
            + "FROM PedidoExtra p JOIN Reserva r ON r.id = p.reservaId JOIN Pessoa c ON c.id = r.clienteId "
            + "LEFT JOIN Servico s ON s.id = p.servicoId LEFT JOIN Material m ON m.id = p.materialId ";
    private static final String LISTAR_POR_CLIENTE =
            LISTAR + "WHERE r.clienteId = ? ORDER BY p.pedidoEm DESC, p.id DESC";
    private static final String LISTAR_PENDENTES =
            LISTAR + "WHERE p.estado = 'PENDENTE' ORDER BY p.pedidoEm, p.id";
    private static final String MARCAR_ACEITE =
            "UPDATE PedidoExtra SET estado = 'ACEITE', decididoEm = SYSDATETIME(), decididoPor = ? "
            + "WHERE id = ? AND estado = 'PENDENTE' "
            + "AND EXISTS (SELECT 1 FROM Reserva r WITH (UPDLOCK, HOLDLOCK) "
            + "WHERE r.id = PedidoExtra.reservaId AND r.estado = 'EM_CURSO')";
    private static final String JUNTAR_A_LINHA =
            "UPDATE ReservaServico SET quantidade = quantidade + ? WHERE id = ("
            + "SELECT TOP 1 rs.id FROM ReservaServico rs JOIN Servico s ON s.id = rs.servicoId "
            + "WHERE rs.reservaId = ? AND rs.servicoId = ? AND rs.precoUnitario = s.preco ORDER BY rs.id)";
    private static final String NOVA_LINHA =
            "INSERT INTO ReservaServico (reservaId, servicoId, quantidade, precoUnitario) "
            + "SELECT ?, s.id, ?, s.preco FROM Servico s WHERE s.id = ? AND s.ativo = 1";
    private static final String RETIRAR_DO_STOCK =
            "UPDATE Stock SET quantidade = quantidade - ? WHERE materialId = ? AND quantidade >= ?";
    private static final String REGISTAR_SAIDA =
            "INSERT INTO MovimentoStock (materialId, quantidade, funcionarioId, tipo, motivo, descricaoMotivo, "
            + "reservaId) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String RECUSAR =
            "UPDATE PedidoExtra SET estado = 'RECUSADO', motivoRecusa = ?, decididoEm = SYSDATETIME(), "
            + "decididoPor = ? WHERE id = ? AND estado = 'PENDENTE'";
    private static final String APAGAR = "DELETE FROM PedidoExtra WHERE id = ?";

    @Override
    public boolean inserirSeEmCurso(PedidoExtra pedido) {
        List<PedidoExtra> gravado = ConexaoBD.select(INSERIR_SE_EM_CURSO, new DadosGeradosMapper(pedido),
                pedido.getReservaId(), pedido.getServicoId(), pedido.getMaterialId(), pedido.getQuantidade(),
                pedido.getReservaId());
        return !gravado.isEmpty();
    }

    @Override
    public List<PedidoExtra> listarPorCliente(int clienteId) {
        return ConexaoBD.select(LISTAR_POR_CLIENTE, new PedidoExtraMapper(), clienteId);
    }

    @Override
    public List<PedidoExtra> listarPendentes() {
        return ConexaoBD.select(LISTAR_PENDENTES, new PedidoExtraMapper());
    }

    @Override
    public boolean aceitarServico(PedidoExtra pedido, int funcionarioId) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try {
                if (executar(conn, MARCAR_ACEITE, funcionarioId, pedido.getId()) == 0) {
                    conn.rollback();
                    return false;
                }
                int juntou = executar(conn, JUNTAR_A_LINHA, pedido.getQuantidade(), pedido.getReservaId(),
                        pedido.getServicoId());
                if (juntou == 0 && executar(conn, NOVA_LINHA, pedido.getReservaId(), pedido.getQuantidade(),
                        pedido.getServicoId()) == 0) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao aceitar o pedido de serviço", e);
        }
    }

    @Override
    public boolean aceitarMaterial(PedidoExtra pedido, int funcionarioId, TipoSaida tipo) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try {
                if (executar(conn, MARCAR_ACEITE, funcionarioId, pedido.getId()) == 0
                        || executar(conn, RETIRAR_DO_STOCK, pedido.getQuantidade(), pedido.getMaterialId(),
                        pedido.getQuantidade()) == 0) {
                    conn.rollback();
                    return false;
                }
                String motivo = tipo == TipoSaida.DEFINITIVA ? MotivoSaida.CONSUMO_RESERVA.name() : null;
                executar(conn, REGISTAR_SAIDA, pedido.getMaterialId(), pedido.getQuantidade(), funcionarioId,
                        tipo.name(), motivo, "Pedido extra da reserva " + pedido.getReservaId(),
                        pedido.getReservaId());
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao aceitar o pedido de material", e);
        }
    }

    @Override
    public boolean recusar(int pedidoId, int funcionarioId, String motivo) {
        return ConexaoBD.executar(RECUSAR, motivo, funcionarioId, pedidoId) > 0;
    }

    @Override
    public boolean apagar(int pedidoId) {
        return ConexaoBD.executar(APAGAR, pedidoId) > 0;
    }

    private static int executar(Connection conn, String sql, Object... valores) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setObject(i + 1, valores[i]);
            }
            return ps.executeUpdate();
        }
    }

    private static class PedidoExtraMapper implements RowMapper<PedidoExtra> {
        @Override
        public PedidoExtra map(ResultSet rs) throws SQLException {
            PedidoExtra pedido = new PedidoExtra(rs.getInt("id"), rs.getInt("reservaId"), rs.getInt("servicoId"),
                    rs.getInt("materialId"), rs.getString("descricao"), rs.getInt("quantidade"),
                    EstadoPedido.valueOf(rs.getString("estado")));
            pedido.setMotivoRecusa(rs.getString("motivoRecusa"));
            pedido.setPedidoEm(rs.getTimestamp("pedidoEm").toLocalDateTime());
            pedido.setConsumivel(rs.getBoolean("consumivel"));
            pedido.setReserva(rs.getInt("quartoNumero"), rs.getString("cliente"));
            return pedido;
        }
    }

    private static class DadosGeradosMapper implements RowMapper<PedidoExtra> {
        private final PedidoExtra pedido;

        DadosGeradosMapper(PedidoExtra pedido) {
            this.pedido = pedido;
        }

        @Override
        public PedidoExtra map(ResultSet rs) throws SQLException {
            pedido.setId(rs.getInt("id"));
            pedido.setPedidoEm(rs.getTimestamp("pedidoEm").toLocalDateTime());
            return pedido;
        }
    }
}
