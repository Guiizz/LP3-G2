package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.HorarioReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IHorarioReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

/**
 * Regras de negócio das horas e das taxas das reservas (US47): as horas standard de check-in
 * e de check-out, as horas de early check-in e de late check-out, e as taxas de cada uma.
 */
public class HorarioReservaBLL {

    private final IHorarioReservaDAL horarioReservaDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public HorarioReservaBLL() {
        this(new HorarioReservaDAL());
    }

    /**
     * Cria o BLL com outro DAL (usado nos testes).
     *
     * @param horarioReservaDAL onde estão as horas e as taxas
     */
    public HorarioReservaBLL(IHorarioReservaDAL horarioReservaDAL) {
        this.horarioReservaDAL = horarioReservaDAL;
    }

    /**
     * Devolve as horas e as taxas em vigor.
     *
     * @return as horas e as taxas das reservas
     */
    public HorarioReserva obter() {
        return horarioReservaDAL.obter();
    }

    /**
     * Altera as horas e as taxas das reservas. Só o gestor o pode fazer.
     * O early check-in tem de ser antes do check-in standard, o late check-out depois do check-out
     * standard, e as taxas não podem ser negativas.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param novo    as horas e as taxas novas
     * @throws IllegalStateException se quem age não for o gestor
     * @throws ValidacaoException    se faltar algum valor, se as horas não estiverem pela ordem certa,
     *                               se alguma taxa for negativa ou se não existirem horas na base de dados
     */
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
        if (!horarioReservaDAL.atualizar(novo)) {
            throw new ValidacaoException("Faltam as horas e as taxas das reservas na base de dados");
        }
    }
}
