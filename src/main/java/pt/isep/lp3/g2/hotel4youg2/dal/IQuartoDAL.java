package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Quarto;

import java.time.LocalDate;
import java.util.List;

/**
 * Acesso aos dados dos quartos.
 */
public interface IQuartoDAL {

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

    /**
     * Procura os quartos ativos livres entre as duas datas, contando também com as horas de early
     * check-in e de late check-out (US47): com early check-in, não pode haver outra reserva a sair
     * do quarto no dia de entrada; com late check-out, não pode haver outra a entrar no dia de saída.
     * O late check-out e o early check-in das outras reservas também contam.
     *
     * @param entrada      o dia de chegada
     * @param saida        o dia de partida
     * @param earlyCheckin se o cliente quer early check-in
     * @param lateCheckout se o cliente quer late check-out
     * @return os quartos livres, cada um com a sua tarifa
     */
    List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida, boolean earlyCheckin, boolean lateCheckout);

    /**
     * @return todos os quartos, ativos e desativados, ordenados pelo número, cada um com a sua tarifa
     */
    List<Quarto> listarTodos();

    /**
     * @param numero o número do quarto
     * @return {@code true} se o quarto tiver reservas não canceladas que ainda não terminaram
     */
    boolean temReservasFuturas(int numero);

    /**
     * Ativa ou desativa um quarto.
     *
     * @param numero o número do quarto
     * @param ativo  {@code true} para ativar, {@code false} para desativar
     * @return {@code true} se o quarto existia e foi alterado
     */
    boolean atualizarAtivo(int numero, boolean ativo);
}
