package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;

/**
 * Acesso à base de dados das horas e das taxas das reservas (US47). Há uma só linha com os valores em vigor.
 */
public interface IHorarioReservaDAL {

    /**
     * @return as horas e as taxas em vigor
     * @throws DataAccessException se a linha das horas não existir na base de dados
     */
    HorarioReserva obter();

    /**
     * Grava as horas e as taxas novas.
     *
     * @param horario as horas e as taxas novas
     * @return {@code true} se foram gravadas; {@code false} se a linha das horas não existir
     */
    boolean atualizar(HorarioReserva horario);
}
