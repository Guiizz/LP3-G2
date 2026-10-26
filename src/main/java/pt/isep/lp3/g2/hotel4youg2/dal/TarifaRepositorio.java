package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.math.BigDecimal;
import java.util.List;

public interface TarifaRepositorio {

    List<Tarifa> listar();

    boolean atualizarValor(TipoQuarto tipo, Vista vista, BigDecimal novoValor);
}
