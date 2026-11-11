package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.ServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio dos serviços extra das reservas (US37): juntar e retirar serviços
 * com o preço da tabela de preços (US46). Só o rececionista e o gestor o podem fazer,
 * e só em reservas confirmadas ou em curso.
 */
public class ServicoReservaBLL {

    /** A maior quantidade de um serviço que se pode juntar de uma só vez. */
    public static final int QUANTIDADE_MAXIMA = 99;

    private final IServicoReservaDAL servicoReservaDAL;
    private final IServicoDAL servicoDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public ServicoReservaBLL() {
        this(new ServicoReservaDAL(), new ServicoDAL());
    }

    /**
     * Cria o BLL com outros DAL (usado nos testes).
     *
     * @param servicoReservaDAL onde ficam os serviços das reservas
     * @param servicoDAL        onde estão os serviços e os seus preços
     */
    public ServicoReservaBLL(IServicoReservaDAL servicoReservaDAL, IServicoDAL servicoDAL) {
        this.servicoReservaDAL = servicoReservaDAL;
        this.servicoDAL = servicoDAL;
    }

    /**
     * Lista os serviços que se podem juntar a uma reserva.
     *
     * @return só os serviços ativos
     */
    public List<Servico> listarServicosAtivos() {
        List<Servico> ativos = new ArrayList<>();
        for (Servico servico : servicoDAL.listar()) {
            if (servico.isAtivo()) {
                ativos.add(servico);
            }
        }
        return ativos;
    }

    /**
     * Lista os serviços já juntos a uma reserva.
     *
     * @param reserva a reserva
     * @return os serviços da reserva
     * @throws ValidacaoException se não for escolhida nenhuma reserva
     */
    public List<ServicoReserva> listarDaReserva(Reserva reserva) {
        if (reserva == null) {
            throw new ValidacaoException("Escolha uma reserva");
        }
        return servicoReservaDAL.listarPorReserva(reserva.getId());
    }

    /**
     * Junta um serviço à reserva, com o preço que tem agora na tabela de preços,
     * e atualiza o total dos serviços da reserva.
     *
     * @param quemAge    o utilizador com a sessão iniciada (rececionista ou gestor)
     * @param reserva    a reserva (confirmada ou em curso)
     * @param servico    o serviço a juntar (tem de estar ativo)
     * @param quantidade quantas vezes (de 1 a {@value #QUANTIDADE_MAXIMA})
     * @return o serviço junto, já gravado
     * @throws IllegalStateException se quem age não for rececionista nem gestor
     * @throws ValidacaoException    se a reserva já não aceitar serviços, o serviço estiver
     *                               desativado ou a quantidade não for válida
     */
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

    /**
     * Retira um serviço da reserva e desconta-o do total dos serviços.
     *
     * @param quemAge        o utilizador com a sessão iniciada (rececionista ou gestor)
     * @param reserva        a reserva (confirmada ou em curso)
     * @param servicoReserva o serviço a retirar
     * @throws IllegalStateException se quem age não for rececionista nem gestor
     * @throws ValidacaoException    se a reserva já não aceitar alterações ou o serviço já tiver sido retirado
     */
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

    /**
     * Soma o total de uma lista de serviços da reserva.
     *
     * @param servicos os serviços
     * @return a soma de quantidade × preço unitário de todos; zero se a lista estiver vazia
     */
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
