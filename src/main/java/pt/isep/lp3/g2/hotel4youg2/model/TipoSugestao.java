package pt.isep.lp3.g2.hotel4youg2.model;

public enum TipoSugestao {
    SUGESTAO("Sugestão"), QUEIXA("Queixa");

    private final String descricao;

    TipoSugestao(String descricao) {
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
