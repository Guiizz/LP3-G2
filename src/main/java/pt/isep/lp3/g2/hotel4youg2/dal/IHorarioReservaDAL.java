package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;

public interface IHorarioReservaDAL {

    HorarioReserva obter();

    boolean atualizar(HorarioReserva horario);
}
