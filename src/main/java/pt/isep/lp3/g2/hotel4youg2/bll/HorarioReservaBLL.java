package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.HorarioReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IHorarioReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

public class HorarioReservaBLL {

    private final IHorarioReservaDAL repositorio;

    public HorarioReservaBLL() {
        this(new HorarioReservaDAL());
    }

    public HorarioReservaBLL(IHorarioReservaDAL repositorio) {
        this.repositorio = repositorio;
    }

    public HorarioReserva obter() {
        return repositorio.obter();
    }

    public void alterar(Utilizador quemAge, HorarioReserva novo) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode alterar as horas e as taxas das reservas");
        }
        if (novo == null || novo.getHoraCheckin() == null || novo.getHoraCheckout() == null
                || novo.getHoraEarlyCheckin() == null || novo.getHoraLateCheckout() == null
                || novo.getTaxaEarlyCheckin() == null || novo.getTaxaLateCheckout() == null) {
            throw new ValidacaoException("Preencha todas as horas e taxas");
        }
        if (!novo.getHoraEarlyCheckin().isBefore(novo.getHoraCheckin())) {
            throw new ValidacaoException("O early check-in tem de ser antes da hora de check-in ("
                    + novo.getHoraCheckin() + ")");
        }
        if (!novo.getHoraLateCheckout().isAfter(novo.getHoraCheckout())) {
            throw new ValidacaoException("O late check-out tem de ser depois da hora de check-out ("
                    + novo.getHoraCheckout() + ")");
        }
        if (novo.getTaxaEarlyCheckin().signum() < 0 || novo.getTaxaLateCheckout().signum() < 0) {
            throw new ValidacaoException("As taxas não podem ser negativas");
        }
        if (!repositorio.atualizar(novo)) {
            throw new ValidacaoException("Faltam as horas e as taxas das reservas na base de dados");
        }
    }
}
