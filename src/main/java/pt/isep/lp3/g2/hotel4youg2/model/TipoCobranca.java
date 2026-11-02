package pt.isep.lp3.g2.hotel4youg2.model;

/**
 * Formas de cobrar um serviço extra.
 */
public enum TipoCobranca {
    /** Cobrado por cada noite da estadia. */
    POR_NOITE("Por noite"),
    /** Cobrado uma só vez por estadia. */
    POR_ESTADIA("Por estadia"),
    /** Cobrado de cada vez que o cliente usa o serviço. */
    POR_UTILIZACAO("Por utilização");

    private final String descricao;

    TipoCobranca(String descricao) {
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
