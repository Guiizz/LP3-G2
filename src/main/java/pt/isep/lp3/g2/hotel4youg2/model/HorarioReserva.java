package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

public class HorarioReserva {

    private LocalTime horaCheckin;
    private LocalTime horaCheckout;
    private LocalTime horaEarlyCheckin;
    private LocalTime horaLateCheckout;
    private BigDecimal taxaEarlyCheckin;
    private BigDecimal taxaLateCheckout;

    public HorarioReserva(LocalTime horaCheckin, LocalTime horaCheckout, LocalTime horaEarlyCheckin,
                          LocalTime horaLateCheckout, BigDecimal taxaEarlyCheckin, BigDecimal taxaLateCheckout) {
        this.horaCheckin = horaCheckin;
        this.horaCheckout = horaCheckout;
        this.horaEarlyCheckin = horaEarlyCheckin;
        this.horaLateCheckout = horaLateCheckout;
        this.taxaEarlyCheckin = taxaEarlyCheckin;
        this.taxaLateCheckout = taxaLateCheckout;
    }

    public LocalTime getHoraCheckin() {
        return horaCheckin;
    }

    public LocalTime getHoraCheckout() {
        return horaCheckout;
    }

    public LocalTime getHoraEarlyCheckin() {
        return horaEarlyCheckin;
    }

    public LocalTime getHoraLateCheckout() {
        return horaLateCheckout;
    }

    public BigDecimal getTaxaEarlyCheckin() {
        return taxaEarlyCheckin;
    }

    public BigDecimal getTaxaLateCheckout() {
        return taxaLateCheckout;
    }

    public LocalTime horaMinimaCheckin(boolean earlyCheckin) {
        return earlyCheckin ? horaEarlyCheckin : horaCheckin;
    }

    public BigDecimal calcularTaxa(boolean earlyCheckin, boolean lateCheckout) {
        BigDecimal taxa = BigDecimal.ZERO;
        if (earlyCheckin) {
            taxa = taxa.add(taxaEarlyCheckin);
        }
        if (lateCheckout) {
            taxa = taxa.add(taxaLateCheckout);
        }
        return taxa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HorarioReserva)) {
            return false;
        }
        HorarioReserva outro = (HorarioReserva) o;
        return Objects.equals(horaCheckin, outro.horaCheckin)
                && Objects.equals(horaCheckout, outro.horaCheckout)
                && Objects.equals(horaEarlyCheckin, outro.horaEarlyCheckin)
                && Objects.equals(horaLateCheckout, outro.horaLateCheckout)
                && mesmoValor(taxaEarlyCheckin, outro.taxaEarlyCheckin)
                && mesmoValor(taxaLateCheckout, outro.taxaLateCheckout);
    }

    private static boolean mesmoValor(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.compareTo(b) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(horaCheckin, horaCheckout, horaEarlyCheckin, horaLateCheckout);
    }

    @Override
    public String toString() {
        return "HorarioReserva{checkin=" + horaCheckin + ", checkout=" + horaCheckout
                + ", early=" + horaEarlyCheckin + " (+" + taxaEarlyCheckin + " €)"
                + ", late=" + horaLateCheckout + " (+" + taxaLateCheckout + " €)}";
    }
}
