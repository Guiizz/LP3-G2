package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.PedidoExtraDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.StockDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IPedidoExtraDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IStockDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.PedidoExtra;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

import java.util.ArrayList;
import java.util.List;

public class PedidoExtraBLL {

    public static final int QUANTIDADE_MAXIMA = 99;
    public static final int TAMANHO_MAXIMO_MOTIVO = 200;

    private final IPedidoExtraDAL pedidoExtraDAL;
    private final IReservaDAL reservaDAL;
    private final IServicoDAL servicoDAL;
    private final IStockDAL stockDAL;

    public PedidoExtraBLL() {
        this(new PedidoExtraDAL(), new ReservaDAL(), new ServicoDAL(), new StockDAL());
    }

    public PedidoExtraBLL(IPedidoExtraDAL pedidoExtraDAL, IReservaDAL reservaDAL, IServicoDAL servicoDAL,
                          IStockDAL stockDAL) {
        this.pedidoExtraDAL = pedidoExtraDAL;
        this.reservaDAL = reservaDAL;
        this.servicoDAL = servicoDAL;
        this.stockDAL = stockDAL;
    }

    public List<Servico> listarServicos() {
        List<Servico> ativos = new ArrayList<>();
        for (Servico servico : servicoDAL.listar()) {
            if (servico.isAtivo()) {
                ativos.add(servico);
            }
        }
        return ativos;
    }

    public List<MaterialStock> listarMateriais() {
        return stockDAL.listar();
    }

    public PedidoExtra pedirServico(ContaCliente conta, Reserva reserva, Servico servico, int quantidade) {
        exigirReservaEmCurso(conta, reserva);
        if (servico == null) {
            throw new ValidacaoException("Escolha o serviço.");
        }
        exigirQuantidade(quantidade);
        return gravar(PedidoExtra.deServico(reserva.getId(), servico, quantidade));
    }

    public PedidoExtra pedirMaterial(ContaCliente conta, Reserva reserva, MaterialStock material, int quantidade) {
        exigirReservaEmCurso(conta, reserva);
        if (material == null) {
            throw new ValidacaoException("Escolha o material.");
        }
        exigirQuantidade(quantidade);
        return gravar(PedidoExtra.deMaterial(reserva.getId(), material, quantidade));
    }

    public List<PedidoExtra> listarDoCliente(ContaCliente conta) {
        if (conta == null) {
            throw new IllegalStateException("Entre na sua conta para ver os seus pedidos.");
        }
        return pedidoExtraDAL.listarPorCliente(conta.getClienteId());
    }

    public List<PedidoExtra> listarPendentes(Utilizador quemAge) {
        exigirRececao(quemAge);
        return pedidoExtraDAL.listarPendentes();
    }

    public void aceitar(Utilizador quemAge, PedidoExtra pedido) {
        exigirRececao(quemAge);
        exigirPendente(pedido);
        boolean aceite;
        if (pedido.isServico()) {
            aceite = pedidoExtraDAL.aceitarServico(pedido, quemAge.getFuncionarioId());
        } else {
            exigirStock(pedido);
            TipoSaida tipo = pedido.isConsumivel() ? TipoSaida.DEFINITIVA : TipoSaida.TEMPORARIA;
            aceite = pedidoExtraDAL.aceitarMaterial(pedido, quemAge.getFuncionarioId(), tipo);
        }
        if (!aceite) {
            throw new ValidacaoException("Não foi possível aceitar: o pedido já foi decidido, a reserva já não está "
                    + "em curso ou já não há stock suficiente.");
        }
        pedido.aceitar();
    }

    public void recusar(Utilizador quemAge, PedidoExtra pedido, String motivo) {
        exigirRececao(quemAge);
        exigirPendente(pedido);
        String texto = motivo == null ? "" : motivo.trim();
        if (texto.isEmpty()) {
            throw new ValidacaoException("Escreva o motivo da recusa.");
        }
        if (texto.length() > TAMANHO_MAXIMO_MOTIVO) {
            throw new ValidacaoException("O motivo tem no máximo " + TAMANHO_MAXIMO_MOTIVO + " caracteres.");
        }
        if (!pedidoExtraDAL.recusar(pedido.getId(), quemAge.getFuncionarioId(), texto)) {
            throw new ValidacaoException("Este pedido já foi decidido.");
        }
        pedido.recusar(texto);
    }

    private PedidoExtra gravar(PedidoExtra pedido) {
        if (!pedidoExtraDAL.inserirSeEmCurso(pedido)) {
            throw new ValidacaoException("A reserva " + pedido.getReservaId() + " já não está em curso.");
        }
        return pedido;
    }

    private void exigirReservaEmCurso(ContaCliente conta, Reserva reserva) {
        if (conta == null) {
            throw new IllegalStateException("Entre na sua conta para pedir extras.");
        }
        if (reserva == null) {
            throw new ValidacaoException("Escolha a reserva em curso.");
        }
        for (Reserva sua : reservaDAL.listarPorCliente(conta.getClienteId())) {
            if (sua.getId() == reserva.getId() && sua.getEstado() == EstadoReserva.EM_CURSO) {
                return;
            }
        }
        throw new ValidacaoException("Só pode pedir extras para uma reserva sua em curso.");
    }

    private void exigirQuantidade(int quantidade) {
        if (quantidade < 1 || quantidade > QUANTIDADE_MAXIMA) {
            throw new ValidacaoException("A quantidade tem de ser entre 1 e " + QUANTIDADE_MAXIMA + ".");
        }
    }

    private void exigirRececao(Utilizador quemAge) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Só a receção e o gestor podem decidir os pedidos dos clientes.");
        }
    }

    private void exigirPendente(PedidoExtra pedido) {
        if (pedido == null) {
            throw new ValidacaoException("Escolha um pedido na tabela.");
        }
        if (!pedido.isPendente()) {
            throw new ValidacaoException("Este pedido já foi decidido.");
        }
    }

    private void exigirStock(PedidoExtra pedido) {
        for (MaterialStock material : stockDAL.listar()) {
            if (material.getMaterialId() == pedido.getMaterialId()) {
                if (material.getQuantidade() < pedido.getQuantidade()) {
                    throw new ValidacaoException("Não há stock suficiente de " + pedido.getDescricao() + ": há "
                            + material.getQuantidade() + " e o cliente pediu " + pedido.getQuantidade() + ".");
                }
                return;
            }
        }
        throw new ValidacaoException("O material " + pedido.getDescricao() + " não está no stock.");
    }
}
