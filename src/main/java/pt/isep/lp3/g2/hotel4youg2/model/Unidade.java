package pt.isep.lp3.g2.hotel4youg2.model;

/**
 * Unidades em que o fornecedor entrega o material.
 * Cada uma sabe o nome que tem no ficheiro XML e o nome que se mostra no ecrã.
 */
public enum Unidade {
    /** Caixa. */
    BOX("Box", "Caixa"),
    /** Unidade, uma peça avulsa. */
    UNIT("Unit", "Unidade"),
    /** Quilograma. */
    KILOGRAM("Kilogram", "Kg");

    private final String nomeXml;
    private final String descricao;

    Unidade(String nomeXml, String descricao) {
        this.nomeXml = nomeXml;
        this.descricao = descricao;
    }

    public String getNomeXml() {
        return nomeXml;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Procura a unidade pelo nome que aparece no ficheiro XML do fornecedor.
     *
     * @param nomeXml o nome da unidade no XML (por exemplo, {@code "Box"})
     * @return a unidade correspondente
     * @throws IllegalArgumentException se o nome não corresponder a nenhuma unidade
     */
    public static Unidade doXml(String nomeXml) {
        for (Unidade unidade : values()) {
            if (unidade.nomeXml.equals(nomeXml)) {
                return unidade;
            }
        }
        throw new IllegalArgumentException("Unidade desconhecida: " + nomeXml);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
