package pt.isep.lp3.g2.hotel4youg2.model.enums;

/**
 * Locais do hotel onde se passa o cartão (US39 e US52). Cada local tem o piso onde fica;
 * os quartos ficam nos pisos 2 e 3 e guardam o primeiro.
 */
public enum LocalHotel {
    /** Um quarto (pisos 2 e 3). */
    QUARTO("Quarto", 2),
    /** Bar (piso 0). */
    BAR("Bar", 0),
    /** Salas de conferência (piso 0). */
    SALA_CONFERENCIAS("Salas de conferência", 0),
    /** Restaurante (piso 1). */
    RESTAURANTE("Restaurante", 1),
    /** Piscina (piso 1). */
    PISCINA("Piscina", 1),
    /** Cozinha (piso 1). */
    COZINHA("Cozinha", 1),
    /** Garagem (piso -1). */
    GARAGEM("Garagem", -1),
    /** Armazém (piso -1). */
    ARMAZEM("Armazém", -1);

    private final String descricao;
    private final int piso;

    LocalHotel(String descricao, int piso) {
        this.descricao = descricao;
        this.piso = piso;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getPiso() {
        return piso;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
