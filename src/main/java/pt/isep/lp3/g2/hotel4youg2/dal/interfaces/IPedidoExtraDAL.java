package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.PedidoExtra;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

import java.util.List;

/**
 * Acesso aos dados dos pedidos extra dos clientes.
 */
public interface IPedidoExtraDAL {

    /**
     * Grava o pedido, só se a reserva ainda estiver em curso. Preenche o id e a hora do pedido.
     *
     * @param pedido o pedido a gravar
     * @return {@code true} se foi gravado
     */
    boolean inserirSeEmCurso(PedidoExtra pedido);

    /**
     * @param clienteId o id do cliente
     * @return os pedidos do cliente, do mais recente para o mais antigo
     */
    List<PedidoExtra> listarPorCliente(int clienteId);

    /**
     * @return os pedidos ainda por decidir, do mais antigo para o mais recente
     */
    List<PedidoExtra> listarPendentes();

    /**
     * Aceita um pedido de um serviço e junta-o à reserva.
     *
     * @param pedido        o pedido
     * @param funcionarioId o id de quem aceitou
     * @return {@code true} se foi aceite; {@code false} se já estava decidido ou a reserva já não está em curso
     */
    boolean aceitarServico(PedidoExtra pedido, int funcionarioId);

    /**
     * Aceita um pedido de um material: tira-o do stock e regista a saída, tudo de uma vez.
     *
     * @param pedido        o pedido
     * @param funcionarioId o id de quem aceitou
     * @param tipo          o tipo da saída (definitiva se o material for consumível)
     * @return {@code true} se foi aceite; {@code false} se já estava decidido, a reserva já não está em curso
     *         ou não há stock
     */
    boolean aceitarMaterial(PedidoExtra pedido, int funcionarioId, TipoSaida tipo);

    /**
     * Recusa um pedido, com o motivo.
     *
     * @param pedidoId      o id do pedido
     * @param funcionarioId o id de quem recusou
     * @param motivo        o motivo da recusa
     * @return {@code true} se foi recusado; {@code false} se já estava decidido
     */
    boolean recusar(int pedidoId, int funcionarioId, String motivo);

    /**
     * Apaga um pedido (usado nos testes).
     *
     * @param pedidoId o id do pedido
     * @return {@code true} se o pedido existia e foi apagado
     */
    boolean apagar(int pedidoId);
}
