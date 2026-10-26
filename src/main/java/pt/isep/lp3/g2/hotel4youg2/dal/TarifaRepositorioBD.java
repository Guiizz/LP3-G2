package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementação do {@link TarifaRepositorio} sobre a tabela Tarifa do SQL Server.
 */
public class TarifaRepositorioBD implements TarifaRepositorio {

    private static final String LISTAR = "SELECT tipo, vista, valorNoite FROM Tarifa ORDER BY tipo, vista";
    private static final String ATUALIZAR = "UPDATE Tarifa SET valorNoite = ? WHERE tipo = ? AND vista = ?";

    @Override
    public List<Tarifa> listar() {
        return ConexaoBD.select(LISTAR, rs -> new Tarifa(TipoQuarto.valueOf(rs.getString("tipo")),Vista.valueOf(rs.getString("vista")),rs.getBigDecimal("valorNoite")));
    }
    @Override
    public boolean atualizarValor(TipoQuarto tipo, Vista vista, BigDecimal novoValor) {
        return ConexaoBD.executar(ATUALIZAR, novoValor, tipo.name(), vista.name()) > 0;
    }
}
