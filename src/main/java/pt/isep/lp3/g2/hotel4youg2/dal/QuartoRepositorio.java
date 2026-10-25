package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Quarto;

import java.time.LocalDate;
import java.util.List;

public interface QuartoRepositorio {

    List<Quarto> listarComTarifa();
    List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida);
}
