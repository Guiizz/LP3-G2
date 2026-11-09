package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Horas e taxas das reservas (US47): as horas standard de check-in e de check-out, as horas de
 * early check-in e de late check-out, e o que se paga a mais por cada uma.
 */
public class HorarioReserva {

    private LocalTime horaCheckin;
    private LocalTime horaCheckout;
    private LocalTime horaEarlyCheckin;
    private LocalTime horaLateCheckout;
    private BigDecimal taxaEarlyCheckin;
    private BigDecimal taxaLateCheckout;

    /**
     * Cria as horas e as taxas das reservas.
     *
     * @param horaCheckin      a hora standard de check-in
     * @param horaCheckout     a hora standard de check-out
     * @param horaEarlyCheckin a hora a partir da qual se pode fazer o early check-in
     * @param horaLateCheckout a hora até à qual se pode fazer o late check-out
     * @param taxaEarlyCheckin o que se paga a mais pelo early check-in
     * @param taxaLateCheckout o que se paga a mais pelo late check-out
     */
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

    /**
     * @param earlyCheckin se a reserva tem early check-in
     * @return a hora a partir da qual o check-in é permitido
     */
    public LocalTime horaMinimaCheckin(boolean earlyCheckin) {
        return earlyCheckin ? horaEarlyCheckin : horaCheckin;
    }

    /**
     * Soma as taxas pedidas.
     *
     * @param earlyCheckin se a reserva tem early check-in
     * @param lateCheckout se a reserva tem late check-out
     * @return a soma das taxas; zero se não tiver nenhum
     */
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
