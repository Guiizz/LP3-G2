package pt.isep.lp3.g2.hotel4youg2.model;

public enum LocalHotel {
    QUARTO("Quarto"),
    RESTAURANTE("Restaurante"),
    PISCINA("Piscina");

    private final String descricao;

    LocalHotel(String descricao) {
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
