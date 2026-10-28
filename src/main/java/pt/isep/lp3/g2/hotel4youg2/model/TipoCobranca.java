package pt.isep.lp3.g2.hotel4youg2.model;

public enum TipoCobranca {
    POR_NOITE("Por noite"), POR_ESTADIA("Por estadia"), POR_UTILIZACAO("Por utilização");

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
