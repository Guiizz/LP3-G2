package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;

import java.util.List;

public interface IServicoReservaDAL {

    boolean inserirSeAberta(ServicoReserva servicoReserva);

    List<ServicoReserva> listarPorReserva(int reservaId);

    boolean apagarSeAberta(int id, int reservaId);
}
