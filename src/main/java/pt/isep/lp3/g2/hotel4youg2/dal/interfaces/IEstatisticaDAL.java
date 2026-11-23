package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.Estadia;
import pt.isep.lp3.g2.hotel4youg2.model.Estatistica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Acesso aos dados das estatísticas do hotel.
 */
public interface IEstatisticaDAL {

    /**
     * @return o número de quartos ativos
     */
    int contarQuartosAtivos();

    /**
     * Lista as estadias em curso e concluídas que têm noites no período.
     *
     * @param inicio o primeiro dia do período
     * @param fim    o último dia do período
     * @return as estadias
     */
    List<Estadia> listarEstadias(LocalDate inicio, LocalDate fim);

    /**
     * Soma o alojamento das reservas em curso e concluídas que entram no período, por tipo de quarto.
     *
     * @param inicio o primeiro dia do período
     * @param fim    o último dia do período
     * @return a receita de cada tipo de quarto
     */
    List<Estatistica> receitaPorTipoQuarto(LocalDate inicio, LocalDate fim);

    /**
     * Soma os serviços extra das reservas em curso e concluídas que entram no período.
     *
     * @param inicio o primeiro dia do período
     * @param fim    o último dia do período
     * @return a receita de cada serviço, da maior para a menor
     */
    List<Estatistica> receitaPorServico(LocalDate inicio, LocalDate fim);

    /**
     * Soma o alojamento e os serviços das reservas confirmadas que entram no período.
     *
     * @param inicio o primeiro dia do período
     * @param fim    o último dia do período
     * @return a receita prevista
     */
    BigDecimal receitaPrevista(LocalDate inicio, LocalDate fim);

    /**
     * @param inicio o primeiro dia do período
     * @param fim    o último dia do período
     * @return as noites das reservas em curso e concluídas que entram no período
     */
    int noitesVendidas(LocalDate inicio, LocalDate fim);

    /**
     * @param inicio o primeiro dia do período
     * @param fim    o último dia do período
     * @return a média de noites por reserva não cancelada, ou {@code null} se não houver
     */
    BigDecimal duracaoMedia(LocalDate inicio, LocalDate fim);

    /**
     * @return {@code true} se a base de dados já guarda quando cada reserva foi criada (script 28)
     */
    boolean guardaDataDeCriacao();

    /**
     * @param inicio o primeiro dia do período
     * @param fim    o último dia do período
     * @return a média de dias entre criar a reserva e a entrada, ou {@code null} se não houver
     */
    BigDecimal antecedenciaMedia(LocalDate inicio, LocalDate fim);

    /**
     * @param inicio o primeiro dia do período
     * @param fim    o último dia do período
     * @return quantas reservas há em cada estado
     */
    List<Estatistica> reservasPorEstado(LocalDate inicio, LocalDate fim);

    /**
     * @param inicio o primeiro dia do período
     * @param fim    o último dia do período
     * @return quantas vezes foi pedido cada serviço, do mais usado para o menos usado
     */
    List<Estatistica> servicosMaisUsados(LocalDate inicio, LocalDate fim);

    /**
     * @param inicio o primeiro dia do período
     * @param fim    o último dia do período
     * @return quantas unidades saíram de vez de cada material, do mais gasto para o menos gasto
     */
    List<Estatistica> materiaisMaisGastos(LocalDate inicio, LocalDate fim);
}
