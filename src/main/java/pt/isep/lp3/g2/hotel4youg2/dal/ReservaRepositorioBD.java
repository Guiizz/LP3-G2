package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Implementação do {@link ReservaRepositorio} sobre a tabela Reserva do SQL Server.
 */
public class ReservaRepositorioBD implements ReservaRepositorio {

    private static final String INSERIR_SE_LIVRE =
            "INSERT Reserva (clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, estado, precoTotal) "
                    + "OUTPUT INSERTED.id "
                    + "SELECT ?, ?, ?, ?, ?, ?, ? "
                    + "WHERE NOT EXISTS (SELECT 1 FROM Reserva WITH (UPDLOCK, HOLDLOCK) "
                    + "WHERE quartoNumero = ? AND estado <> 'CANCELADA' "
                    + "AND dataEntrada < ? AND dataSaida > ?)";

    private static final String SELECT_RESERVA =
            "SELECT id, clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, estado, "
            + "precoTotal, checkinEm, checkoutEm FROM Reserva ";

    private static final String PROCURAR_POR_ID = SELECT_RESERVA + "WHERE id = ?";

    private static final String LISTAR = SELECT_RESERVA + "ORDER BY dataEntrada DESC";

    private static final String ATUALIZAR_ESTADO = "UPDATE Reserva SET estado = ? WHERE id = ?";

    private static final String APAGAR = "DELETE FROM Reserva WHERE id = ?";

    private static final String CANCELAR =
            "UPDATE Reserva SET estado = 'CANCELADA' "
                    + "WHERE id = ? AND estado = 'CONFIRMADA' AND checkinEm IS NULL";

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
            ps.setInt(8, reserva.getQuartoNumero());
            ps.setObject(9, reserva.getDataSaida());
            ps.setObject(10, reserva.getDataEntrada());
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
    public List<Reserva> listar() {
        return ConexaoBD.select(LISTAR, new ReservaMapper());
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
            return new Reserva(rs.getInt("id"), rs.getInt("clienteId"),
                    rs.getInt("quartoNumero"),
                    rs.getDate("dataEntrada").toLocalDate(), rs.getDate("dataSaida").toLocalDate(),
                    rs.getInt("numHospedes"), EstadoReserva.valueOf(rs.getString("estado")),
                    rs.getBigDecimal("precoTotal"),
                    checkin != null ? checkin.toLocalDateTime() : null,
                    checkout != null ? checkout.toLocalDateTime() : null);

        }
    }
}