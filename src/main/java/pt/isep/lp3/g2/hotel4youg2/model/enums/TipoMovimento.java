package pt.isep.lp3.g2.hotel4youg2.model.enums;

/**
 * Tipo de movimento do stock (US62): entrada (entrega aceite) ou saída (material usado ou retirado).
 */
public enum TipoMovimento {
    ENTRADA("Entrada"),
    SAIDA("Saída");

    private final String descricao;

    TipoMovimento(String descricao) {
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
