package pt.isep.lp3.g2.hotel4youg2.model.enums;

/**
 * Tipos de quarto que o hotel tem, cada um com o número de pessoas que lá cabem.
 */
public enum TipoQuarto {
    /** Quarto para uma pessoa. */
    INDIVIDUAL(1),
    /** Quarto para duas pessoas. */
    DUPLO(2),
    /** Quarto para uma família (até quatro pessoas). */
    FAMILIA(4);

    private final int capacidade;

    TipoQuarto(int capacidade) {
        this.capacidade = capacidade;
    }

    public int getCapacidade() {
        return capacidade;
    }
}
