package pt.isep.lp3.g2.hotel4youg2.model.enums;

/**
 * Separadores do ecrã "Gerir reservas" (US55): cada reserva aparece no separador do seu estado.
 */
public enum SeparadorReserva {
    /** Reservas confirmadas, ainda sem check-in. */
    POR_FAZER("Por fazer"),
    /** Reservas com check-in feito, ainda sem check-out. */
    EM_CURSO("Em curso"),
    /** Reservas concluídas ou canceladas. */
    HISTORICO("Histórico");

    private final String descricao;
    SeparadorReserva(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Diz se uma reserva com este estado aparece neste separador: "Por fazer" tem as confirmadas,
     * "Em curso" as em curso e "Histórico" as concluídas e as canceladas.
     *
     * @param estado o estado da reserva
     * @return {@code true} se a reserva aparece neste separador
     */
    public boolean inclui(EstadoReserva estado) {
        if (this == POR_FAZER) {
            return estado == EstadoReserva.CONFIRMADA;
        }
        if (this == EM_CURSO) {
            return estado == EstadoReserva.EM_CURSO;
        }
        return estado == EstadoReserva.CONCLUIDA || estado == EstadoReserva.CANCELADA;
    }
}
