package pt.isep.lp3.g2.hotel4youg2.model.enums;

/**
 * Tipo de uma saída de material (US77): temporária, quando o material volta ao stock (por exemplo,
 * depois de uma reparação), ou definitiva, quando não volta.
 */
public enum TipoSaida {
    /** O material volta ao stock. */
    TEMPORARIA("Temporária"),
    /** O material não volta ao stock. */
    DEFINITIVA("Definitiva");

    private final String descricao;

    TipoSaida(String descricao) {
        this.descricao = descricao;
    }

    /**
     * @return o nome do tipo para mostrar no ecrã, por exemplo "Temporária"
     */
    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
