package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.ITarifaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação do {@link ITarifaDAL} sobre a tabela Tarifa do SQL Server.
 */
public class TarifaDAL implements ITarifaDAL {

    private static final String LISTAR = "SELECT tipo, vista, valorNoite FROM Tarifa ORDER BY tipo, vista";
    private static final String ATUALIZAR = "UPDATE Tarifa SET valorNoite = ? WHERE tipo = ? AND vista = ?";

    @Override
    public List<Tarifa> listar() {
        return ConexaoBD.select(LISTAR, new TarifaMapper());
    }
    @Override
    public boolean atualizarValor(TipoQuarto tipo, Vista vista, BigDecimal novoValor) {
        return ConexaoBD.executar(ATUALIZAR, novoValor, tipo.name(), vista.name()) > 0;
    }

    private static class TarifaMapper implements RowMapper<Tarifa> {
        @Override
        public Tarifa map(ResultSet rs) throws SQLException {
            return new Tarifa(TipoQuarto.valueOf(rs.getString("tipo")), Vista.valueOf(rs.getString("vista")),
                    rs.getBigDecimal("valorNoite"));
        }
    }
}
