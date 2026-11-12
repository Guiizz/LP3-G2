package pt.isep.lp3.g2.hotel4youg2.model.enums;

public enum SeparadorReserva {
    POR_FAZER("Por fazer"),
    EM_CURSO("Em curso"),
    HISTORICO("Histórico");

    private final String descricao;
    SeparadorReserva(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

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
