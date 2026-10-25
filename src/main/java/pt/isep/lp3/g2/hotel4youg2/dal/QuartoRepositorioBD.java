package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class QuartoRepositorioBD implements QuartoRepositorio {

    private static final String LISTAR_COM_TARIFA =
            "SELECT q.numero, q.piso, q.tipo, q.vista, q.capacidade, t.valorNoite "
            + "FROM Quarto q JOIN Tarifa t ON t.tipo = q.tipo AND t.vista = q.vista "
            + "WHERE q.ativo = 1 "
            + "ORDER BY q.numero";

    @Override
    public List<Quarto> listarComTarifa() {
        return ConexaoBD.select(LISTAR_COM_TARIFA, new QuartoComTarifaMapper());
    }

   private static class QuartoComTarifaMapper implements  RowMapper<Quarto> {
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
