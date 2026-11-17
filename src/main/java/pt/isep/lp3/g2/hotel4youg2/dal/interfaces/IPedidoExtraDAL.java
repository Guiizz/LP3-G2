package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.PedidoExtra;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

import java.util.List;

public interface IPedidoExtraDAL {

    boolean inserirSeEmCurso(PedidoExtra pedido);

    List<PedidoExtra> listarPorCliente(int clienteId);

    List<PedidoExtra> listarPendentes();

    boolean aceitarServico(PedidoExtra pedido, int funcionarioId);

    boolean aceitarMaterial(PedidoExtra pedido, int funcionarioId, TipoSaida tipo);

    boolean recusar(int pedidoId, int funcionarioId, String motivo);

    boolean apagar(int pedidoId);
}
