package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.Avaliacao;

import java.time.LocalDate;
import java.util.List;

public interface IAvaliacaoDAL {

    boolean inserir(Avaliacao avaliacao);

    Avaliacao procurarPorReserva(int reservaId);

    List<Avaliacao> listar(LocalDate inicio, LocalDate fim);
}
