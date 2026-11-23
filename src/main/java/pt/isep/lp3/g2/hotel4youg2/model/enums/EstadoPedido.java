package pt.isep.lp3.g2.hotel4youg2.model.enums;

/**
 * Estados de um pedido extra.
 */
public enum EstadoPedido {
    /** Ainda não foi decidido pela receção. */
    PENDENTE("Pendente"),
    /** A receção aceitou o pedido. */
    ACEITE("Aceite"),
    /** A receção recusou o pedido. */
    RECUSADO("Recusado");

    private final String descricao;

    EstadoPedido(String descricao) {
        this.descricao = descricao;
    }

    /**
     * @return o nome do estado para mostrar no ecrã
     */
    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
