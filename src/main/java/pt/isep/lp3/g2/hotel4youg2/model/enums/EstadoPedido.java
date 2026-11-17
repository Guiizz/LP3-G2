package pt.isep.lp3.g2.hotel4youg2.model.enums;

public enum EstadoPedido {
    PENDENTE("Pendente"),
    ACEITE("Aceite"),
    RECUSADO("Recusado");

    private final String descricao;

    EstadoPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
