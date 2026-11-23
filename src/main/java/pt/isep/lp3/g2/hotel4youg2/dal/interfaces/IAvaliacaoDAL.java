package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.Avaliacao;

import java.time.LocalDate;
import java.util.List;

/**
 * Acesso aos dados das avaliações das estadias.
 */
public interface IAvaliacaoDAL {

    /**
     * Grava uma avaliação, só se a reserva estiver concluída e ainda não tiver avaliação. Preenche o id.
     *
     * @param avaliacao a avaliação a gravar
     * @return {@code true} se foi gravada
     */
    boolean inserir(Avaliacao avaliacao);

    /**
     * @param reservaId o id da reserva
     * @return a avaliação da reserva, com o nome do cliente e o quarto, ou {@code null} se não houver
     */
    Avaliacao procurarPorReserva(int reservaId);

    /**
     * Lista as avaliações feitas entre dois dias.
     *
     * @param inicio o primeiro dia
     * @param fim    o último dia
     * @return as avaliações, da mais recente para a mais antiga
     */
    List<Avaliacao> listar(LocalDate inicio, LocalDate fim);
}
