package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.IServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ServicoReservaBLL {

    public static final int QUANTIDADE_MAXIMA = 99;

    private final IServicoReservaDAL servicoReservaDAL;
    private final IServicoDAL servicoDAL;

    public ServicoReservaBLL() {
        this(new ServicoReservaDAL(), new ServicoDAL());
    }

    public ServicoReservaBLL(IServicoReservaDAL servicoReservaDAL, IServicoDAL servicoDAL) {
        this.servicoReservaDAL = servicoReservaDAL;
        this.servicoDAL = servicoDAL;
    }

    public List<Servico> listarServicosAtivos() {
        List<Servico> ativos = new ArrayList<>();
        for (Servico servico : servicoDAL.listar()) {
            if (servico.isAtivo()) {
                ativos.add(servico);
            }
        }
        return ativos;
    }

    public List<ServicoReserva> listarDaReserva(Reserva reserva) {
        if (reserva == null) {
            throw new ValidacaoException("Escolha uma reserva");
        }
        return servicoReservaDAL.listarPorReserva(reserva.getId());
    }

    public ServicoReserva adicionar(Utilizador quemAge, Reserva reserva, Servico servico, int quantidade) {
        exigirRececaoOuGestor(quemAge);
        exigirReservaAberta(reserva, "juntar");
        if (servico == null) {
            throw new ValidacaoException("Escolha um serviço");
        }
        if (!servico.isAtivo()) {
            throw new ValidacaoException("O serviço " + servico.getDesignacao() + " está desativado");
        }
        if (quantidade < 1) {
            throw new ValidacaoException("A quantidade tem de ser pelo menos 1");
        }
        if (quantidade > QUANTIDADE_MAXIMA) {
            throw new ValidacaoException("A quantidade não pode ser maior que " + QUANTIDADE_MAXIMA);
        }
        ServicoReserva novo = new ServicoReserva(0, reserva.getId(), servico, quantidade, servico.getPreco(), null);
        if (!servicoReservaDAL.inserirSeAberta(novo)) {
            throw new ValidacaoException("A reserva já não aceita serviços ou o serviço foi desativado. "
                    + "Atualize a lista e tente outra vez.");
        }
        reserva.setTotalServicos(reserva.getTotalServicos().add(novo.getTotal()));
        return novo;
    }

    public void retirar(Utilizador quemAge, Reserva reserva, ServicoReserva servicoReserva) {
        exigirRececaoOuGestor(quemAge);
        exigirReservaAberta(reserva, "retirar");
        if (servicoReserva == null) {
            throw new ValidacaoException("Escolha um serviço da lista");
        }
        if (!servicoReservaDAL.apagarSeAberta(servicoReserva.getId(), reserva.getId())) {
            throw new ValidacaoException("O serviço já tinha sido retirado ou a reserva já não aceita alterações");
        }
        reserva.setTotalServicos(reserva.getTotalServicos().subtract(servicoReserva.getTotal()));
    }

    public static BigDecimal somar(List<ServicoReserva> servicos) {
        BigDecimal soma = BigDecimal.ZERO;
        for (ServicoReserva servico : servicos) {
            soma = soma.add(servico.getTotal());
        }
        return soma;
    }

    private static void exigirRececaoOuGestor(Utilizador quemAge) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Entre na aplicação como rececionista ou gestor para mudar os serviços da reserva");
        }
    }

    private static void exigirReservaAberta(Reserva reserva, String acao) {
        if (reserva == null) {
            throw new ValidacaoException("Escolha uma reserva");
        }
        if (!reserva.aceitaServicos()) {
            throw new ValidacaoException("A reserva " + reserva.getId() + " está "
                    + reserva.getEstado().getDescricao().toLowerCase() + ": já não é possível " + acao + " serviços");
        }
    }
}
