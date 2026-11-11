package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;

import java.util.List;

/**
 * Acesso à base de dados dos serviços juntos às reservas (US37).
 */
public interface IServicoReservaDAL {

    /**
     * Grava o serviço na reserva, só se a reserva ainda aceitar serviços (confirmada ou em curso)
     * e o serviço estiver ativo. Preenche o id e a data em que foi junto.
     *
     * @param servicoReserva o serviço a juntar
     * @return {@code true} se foi gravado; {@code false} se a reserva já não aceita serviços
     *         ou o serviço foi desativado entretanto
     */
    boolean inserirSeAberta(ServicoReserva servicoReserva);

    /**
     * Lista os serviços de uma reserva, pela ordem em que foram juntos.
     *
     * @param reservaId o id da reserva
     * @return os serviços da reserva; vazia se não tiver nenhum
     */
    List<ServicoReserva> listarPorReserva(int reservaId);

    /**
     * Retira um serviço da reserva, só se a reserva ainda aceitar alterações.
     *
     * @param id        o id do serviço da reserva
     * @param reservaId o id da reserva a que tem de pertencer
     * @return {@code true} se foi retirado; {@code false} se já não existia ou a reserva já não aceita alterações
     */
    boolean apagarSeAberta(int id, int reservaId);
}
