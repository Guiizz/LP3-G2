package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.math.BigDecimal;
import java.util.List;

/**
 * Acesso aos dados das tarifas.
 */
public interface TarifaRepositorio {

    /**
     * @return todas as tarifas, ordenadas por tipo e vista
     */
    List<Tarifa> listar();

    /**
     * Muda o preço por noite de uma tarifa.
     *
     * @param tipo      o tipo de quarto da tarifa
     * @param vista     a vista da tarifa
     * @param novoValor o novo preço por noite
     * @return {@code true} se a tarifa existia e foi alterada
     */
    boolean atualizarValor(TipoQuarto tipo, Vista vista, BigDecimal novoValor);
}
