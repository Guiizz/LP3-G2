package pt.isep.lp3.g2.hotel4youg2.model.enums;

/**
 * Tipos de registo que um cliente pode fazer ao hotel: uma sugestão ou uma queixa.
 */
public enum TipoSugestao {
    /** Ideia do cliente para melhorar o hotel. */
    SUGESTAO("Sugestão"),
    /** Reclamação do cliente sobre algo que correu mal. */
    QUEIXA("Queixa");

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
