package pt.isep.lp3.g2.hotel4youg2.model;

/**
 * Estados de uma reserva.
 */
public enum EstadoReserva {
    /** Reserva feita, o cliente ainda não chegou. */
    CONFIRMADA,
    /** O cliente fez check-in e está no hotel. */
    EM_CURSO,
    /** O cliente fez check-out. */
    CONCLUIDA,
    /** A reserva foi cancelada e o quarto fica livre para essas datas. */
    CANCELADA
}
