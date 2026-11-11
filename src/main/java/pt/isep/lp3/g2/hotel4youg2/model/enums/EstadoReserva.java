package pt.isep.lp3.g2.hotel4youg2.model.enums;

/**
 * Estados de uma reserva.
 */
public enum EstadoReserva {
    /** Reserva feita, o cliente ainda não chegou. */
    CONFIRMADA("Confirmada"),
    /** O cliente fez check-in e está no hotel. */
    EM_CURSO("Em curso"),
    /** O cliente fez check-out. */
    CONCLUIDA("Concluída"),
    /** A reserva foi cancelada e o quarto fica livre para essas datas. */
    CANCELADA("Cancelada");

    private final String descricao;

    EstadoReserva(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
