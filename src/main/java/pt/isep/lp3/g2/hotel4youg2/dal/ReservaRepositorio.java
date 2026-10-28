package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

/**
 * Acesso aos dados das reservas.
 */
public interface ReservaRepositorio {

    /**
     * Grava a reserva, mas só se o quarto estiver livre nessas datas.
     * Se gravar, atualiza o id da reserva.
     *
     * @param reserva a reserva a gravar
     * @return {@code true} se foi gravada; {@code false} se o quarto já estava reservado
     *         em datas que se sobrepõem
     */
    boolean inserirSeLivre(Reserva reserva);
}