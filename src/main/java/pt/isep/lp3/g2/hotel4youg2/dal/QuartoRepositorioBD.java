package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementação do {@link QuartoRepositorio} sobre as tabelas Quarto, Tarifa e Reserva do SQL Server.
 */
public class QuartoRepositorioBD implements QuartoRepositorio {

    private static final String LISTAR_COM_TARIFA =
            "SELECT q.numero, q.piso, q.tipo, q.vista, q.capacidade, t.valorNoite "
                    + "FROM Quarto q JOIN Tarifa t ON t.tipo = q.tipo AND t.vista = q.vista "
                    + "WHERE q.ativo = 1 "
                    + "ORDER BY q.numero";

    private static final String LISTAR_DISPONIVEIS =
            "SELECT q.numero, q.piso, q.tipo, q.vista, q.capacidade, t.valorNoite "
                    + "FROM Quarto q JOIN Tarifa t ON t.tipo = q.tipo AND t.vista = q.vista "
                    + "WHERE q.ativo = 1 "
                    + "AND NOT EXISTS (SELECT 1 FROM Reserva r "
                    + "WHERE r.quartoNumero = q.numero "
                    + "AND r.estado <> 'CANCELADA' "
                    + "AND r.dataEntrada < ? "
                    + "AND r.dataSaida > ?) "
                    + "ORDER BY q.numero";

    @Override
    public List<Quarto> listarComTarifa() {
        return ConexaoBD.select(LISTAR_COM_TARIFA, new QuartoComTarifaMapper());
    }

    @Override
    public List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida) {
        return ConexaoBD.select(LISTAR_DISPONIVEIS, new QuartoComTarifaMapper(), Date.valueOf(saida), Date.valueOf(entrada));
    }

    private static class QuartoComTarifaMapper implements RowMapper<Quarto> {
        @Override
        public Quarto map(ResultSet rs) throws SQLException {
            TipoQuarto tipo = TipoQuarto.valueOf(rs.getString("tipo"));
            Vista vista = Vista.valueOf(rs.getString("vista"));
            Quarto quarto = new Quarto(rs.getInt("numero"), rs.getInt("piso"), tipo, vista, rs.getInt("capacidade"));
            quarto.setTarifa(new Tarifa(tipo, vista, rs.getBigDecimal("valorNoite")));
            return quarto;
        }
    }
}
