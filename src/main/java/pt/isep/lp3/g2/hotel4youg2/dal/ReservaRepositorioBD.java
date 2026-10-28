package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}