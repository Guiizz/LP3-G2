package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Quarto;

import java.time.LocalDate;
import java.util.List;

/**
 * Acesso aos dados dos quartos.
 */
public interface QuartoRepositorio {

    /**
     * @return os quartos ativos, ordenados pelo número, cada um com a sua tarifa
     */
    List<Quarto> listarComTarifa();

    /**
     * Procura os quartos ativos que não têm nenhuma reserva entre as duas datas.
     *
     * @param entrada o dia de chegada
     * @param saida   o dia de partida
     * @return os quartos livres, cada um com a sua tarifa
     */
    List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida);

    List<Quarto> listarTodos();

    boolean temReservasFuturas(int numero);

    boolean atualizarAtivo(int numero, boolean ativo);
}
