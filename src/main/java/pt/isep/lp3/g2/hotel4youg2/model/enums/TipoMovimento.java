package pt.isep.lp3.g2.hotel4youg2.model.enums;

/**
 * Tipo de movimento do stock (US62): entrada (entrega aceite), saída temporária ou definitiva (US77)
 * e devolução de uma saída temporária, quando o material volta ao stock (US77).
 */
public enum TipoMovimento {
    ENTRADA("Entrada"),
    SAIDA_TEMPORARIA("Saída temporária"),
    SAIDA_DEFINITIVA("Saída definitiva"),
    DEVOLUCAO("Devolução");

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
