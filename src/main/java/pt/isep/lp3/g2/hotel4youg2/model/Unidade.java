package pt.isep.lp3.g2.hotel4youg2.model;

public enum Unidade {
    BOX("Box", "Caixa"),
    UNIT("Unit", "Unidade"),
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
