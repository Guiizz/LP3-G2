package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

/**
 * Implementação em SQL Server de {@link IHorarioReservaDAL}: lê e altera a única linha da tabela {@code HorarioReserva}.
 */
public class HorarioReservaDAL implements IHorarioReservaDAL {

    private static final String OBTER =
            "SELECT horaCheckin, horaCheckout, horaEarlyCheckin, horaLateCheckout, taxaEarlyCheckin, taxaLateCheckout "
            + "FROM HorarioReserva WHERE id = 1";

    private static final String ATUALIZAR =
            "UPDATE HorarioReserva SET horaCheckin = ?, horaCheckout = ?, horaEarlyCheckin = ?, horaLateCheckout = ?, "
            + "taxaEarlyCheckin = ?, taxaLateCheckout = ? WHERE id = 1";

    @Override
    public HorarioReserva obter() {
        List<HorarioReserva> resultado = ConexaoBD.select(OBTER, new HorarioMapper());
        if (resultado.isEmpty()) {
            throw new DataAccessException("Faltam as horas e as taxas das reservas na base de dados");
        }
        return resultado.get(0);
    }

    @Override
    public boolean atualizar(HorarioReserva horario) {
        return ConexaoBD.executar(ATUALIZAR,
                Time.valueOf(horario.getHoraCheckin()), Time.valueOf(horario.getHoraCheckout()),
                Time.valueOf(horario.getHoraEarlyCheckin()), Time.valueOf(horario.getHoraLateCheckout()),
                horario.getTaxaEarlyCheckin(), horario.getTaxaLateCheckout()) > 0;
    }

    private static class HorarioMapper implements RowMapper<HorarioReserva> {
        @Override
        public HorarioReserva map(ResultSet rs) throws SQLException {
            return new HorarioReserva(
                    rs.getTime("horaCheckin").toLocalTime(),
                    rs.getTime("horaCheckout").toLocalTime(),
                    rs.getTime("horaEarlyCheckin").toLocalTime(),
                    rs.getTime("horaLateCheckout").toLocalTime(),
                    rs.getBigDecimal("taxaEarlyCheckin"),
                    rs.getBigDecimal("taxaLateCheckout"));
        }
    }
}
