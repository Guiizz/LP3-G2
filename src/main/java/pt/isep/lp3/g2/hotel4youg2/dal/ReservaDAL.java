package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Implementação do {@link IReservaDAL} sobre a tabela Reserva do SQL Server.
 */
public class ReservaDAL implements IReservaDAL {

    private static final String CONFLITO_DE_DATAS =
            "((r.dataEntrada < ? AND r.dataSaida > ?) "
            + "OR (? = 1 AND r.dataSaida = ?) "
            + "OR (? = 1 AND r.dataEntrada = ?) "
            + "OR (r.lateCheckout = 1 AND r.dataSaida = ?) "
            + "OR (r.earlyCheckin = 1 AND r.dataEntrada = ?))";

    private static final String INSERIR_SE_LIVRE =
            "INSERT Reserva (clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, estado, precoTotal, "
                    + "earlyCheckin, lateCheckout, taxaHorario) "
                    + "OUTPUT INSERTED.id "
                    + "SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
                    + "WHERE NOT EXISTS (SELECT 1 FROM Reserva r WITH (UPDLOCK, HOLDLOCK) "
                    + "WHERE r.quartoNumero = ? AND r.estado <> 'CANCELADA' "
                    + "AND " + CONFLITO_DE_DATAS + ")";

    private static final String SELECT_RESERVA =
            "SELECT id, clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, estado, "
            + "precoTotal, checkinEm, checkoutEm, cartao, earlyCheckin, lateCheckout, taxaHorario, "
            + "(SELECT COALESCE(SUM(rs.quantidade * rs.precoUnitario), 0) FROM ReservaServico rs "
            + "WHERE rs.reservaId = Reserva.id) AS totalServicos, "
            + "(SELECT p.nome FROM Pessoa p WHERE p.id = Reserva.clienteId) AS nomeCliente, "
            + "(SELECT p.nif FROM Pessoa p WHERE p.id = Reserva.clienteId) AS nifCliente FROM Reserva ";

    private static final String PROCURAR_POR_ID = SELECT_RESERVA + "WHERE id = ?";

    private static final String PROCURAR_EM_CURSO_POR_CARTAO =
            SELECT_RESERVA + "WHERE cartao = ? AND estado = 'EM_CURSO' ORDER BY checkinEm DESC";

    private static final String LISTAR = SELECT_RESERVA + "ORDER BY dataEntrada DESC";

    private static final String LISTAR_POR_CLIENTE =
            SELECT_RESERVA + "WHERE clienteId = ? ORDER BY dataEntrada DESC, id DESC";

    private static final String ATUALIZAR_ESTADO = "UPDATE Reserva SET estado = ? WHERE id = ?";

    private static final String APAGAR = "DELETE FROM Reserva WHERE id = ?";

    private static final String CANCELAR =
            "UPDATE Reserva SET estado = 'CANCELADA' "
                    + "WHERE id = ? AND estado = 'CONFIRMADA' AND checkinEm IS NULL";

    private static final String REGISTAR_CHECKIN =
            "UPDATE Reserva SET estado = 'EM_CURSO', checkinEm = ?, cartao = ? "
            + "WHERE id = ? AND estado = 'CONFIRMADA' AND checkinEm IS NULL";

    private static final String REGISTAR_CHECKOUT =
            "UPDATE Reserva SET estado = 'CONCLUIDA', checkoutEm = ? WHERE id = ? AND estado = 'EM_CURSO'";

    private static final String ALTERAR_DATAS_SE_LIVRE =
            "UPDATE Reserva SET dataEntrada = ?, dataSaida = ?, precoTotal = ? "
            + "WHERE id = ? AND estado = 'CONFIRMADA' AND checkinEm IS NULL "
            + "AND NOT EXISTS (SELECT 1 FROM Reserva r WITH (UPDLOCK, HOLDLOCK) "
            + "WHERE r.quartoNumero = ? AND r.id <> ? AND r.estado <> 'CANCELADA' "
            + "AND " + CONFLITO_DE_DATAS + ")";

    @Override
    public boolean inserirSeLivre(Reserva reserva) {
        try (Connection conn = ConexaoBD.abrir();
             PreparedStatement ps = conn.prepareStatement(INSERIR_SE_LIVRE)) {
            ps.setInt(1, reserva.getClienteId());
            ps.setInt(2, reserva.getQuartoNumero());
            ps.setObject(3, reserva.getDataEntrada());
            ps.setObject(4, reserva.getDataSaida());
            ps.setInt(5, reserva.getNumHospedes());
            ps.setString(6, reserva.getEstado().name());
            ps.setBigDecimal(7, reserva.getPrecoTotal());
            ps.setBoolean(8, reserva.isEarlyCheckin());
            ps.setBoolean(9, reserva.isLateCheckout());
            ps.setBigDecimal(10, reserva.getTaxaHorario());
            ps.setInt(11, reserva.getQuartoNumero());
            definirParametros(ps, 12, conflitoDeDatas(reserva));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    reserva.setId(rs.getInt(1));
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao gravar a reserva", e);
        }
    }

    @Override
    public Reserva procurarPorId(int id) {
        List<Reserva> reservas = ConexaoBD.select(PROCURAR_POR_ID, new ReservaMapper(), id);
        return reservas.isEmpty() ? null : reservas.get(0);
    }

    @Override
    public Reserva procurarEmCursoPorCartao(String cartao) {
        List<Reserva> reservas = ConexaoBD.select(PROCURAR_EM_CURSO_POR_CARTAO, new ReservaMapper(), cartao);
        return reservas.isEmpty() ? null : reservas.get(0);
    }

    @Override
    public List<Reserva> listar() {
        return ConexaoBD.select(LISTAR, new ReservaMapper());
    }

    @Override
    public List<Reserva> listarPorCliente(int clienteId) {
        return ConexaoBD.select(LISTAR_POR_CLIENTE, new ReservaMapper(), clienteId);
    }

    @Override
    public boolean atualizarEstado(int id, EstadoReserva estado) {
        return ConexaoBD.executar(ATUALIZAR_ESTADO, estado.name(), id) > 0;
    }

    @Override
    public boolean apagar(int id) {
        return ConexaoBD.executar(APAGAR, id) > 0;
    }

    @Override
    public boolean cancelar(int id) {
        return ConexaoBD.executar(CANCELAR, id) > 0;
    }

    private static class ReservaMapper implements RowMapper<Reserva> {
        @Override
        public Reserva map(ResultSet rs) throws SQLException {
            Timestamp checkin = rs.getTimestamp("checkinEm");
            Timestamp checkout = rs.getTimestamp("checkoutEm");
            Reserva reserva = new Reserva(rs.getInt("id"), rs.getInt("clienteId"),
                    rs.getInt("quartoNumero"),
                    rs.getDate("dataEntrada").toLocalDate(), rs.getDate("dataSaida").toLocalDate(),
                    rs.getInt("numHospedes"), EstadoReserva.valueOf(rs.getString("estado")),
                    rs.getBigDecimal("precoTotal"),
                    checkin != null ? checkin.toLocalDateTime() : null,
                    checkout != null ? checkout.toLocalDateTime() : null);
            reserva.setCartao(rs.getString("cartao"));
            reserva.setTotalServicos(rs.getBigDecimal("totalServicos"));
            reserva.setEarlyCheckin(rs.getBoolean("earlyCheckin"));
            reserva.setLateCheckout(rs.getBoolean("lateCheckout"));
            reserva.setTaxaHorario(rs.getBigDecimal("taxaHorario"));
            reserva.setNomeCliente(rs.getString("nomeCliente"));
            reserva.setNifCliente(rs.getString("nifCliente"));
            return reserva;
        }
    }

    @Override
    public boolean alterarDatasSeLivre(Reserva reserva) {
        Object[] conflito = conflitoDeDatas(reserva);
        return ConexaoBD.executar(ALTERAR_DATAS_SE_LIVRE,
                Date.valueOf(reserva.getDataEntrada()),
                Date.valueOf(reserva.getDataSaida()),
                reserva.getPrecoTotal(), reserva.getId(),
                reserva.getQuartoNumero(), reserva.getId(),
                conflito[0], conflito[1], conflito[2], conflito[3],
                conflito[4], conflito[5], conflito[6], conflito[7]) > 0;
    }

    private static Object[] conflitoDeDatas(Reserva reserva) {
        Date entrada = Date.valueOf(reserva.getDataEntrada());
        Date saida = Date.valueOf(reserva.getDataSaida());
        return new Object[] {saida, entrada, reserva.isEarlyCheckin(), entrada,
                reserva.isLateCheckout(), saida, entrada, saida};
    }

    private static void definirParametros(PreparedStatement ps, int primeiro, Object[] valores) throws SQLException {
        for (int i = 0; i < valores.length; i++) {
            ps.setObject(primeiro + i, valores[i]);
        }
    }

    @Override
    public boolean registarCheckin(Reserva reserva) {
        return ConexaoBD.executar(REGISTAR_CHECKIN, Timestamp.valueOf(reserva.getCheckinEm()),
                reserva.getCartao(), reserva.getId()) > 0;
    }

    @Override
    public boolean registarCheckout(Reserva reserva) {
        return ConexaoBD.executar(REGISTAR_CHECKOUT, Timestamp.valueOf(reserva.getCheckoutEm()),
                reserva.getId()) > 0;
    }
}