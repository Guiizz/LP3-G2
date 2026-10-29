package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

import java.util.List;

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

    Reserva procurarPorId(int id);

    List<Reserva> listar();

    boolean atualizarEstado(int id, EstadoReserva estado);

    boolean apagar(int id);

    boolean cancelar(int id);
}