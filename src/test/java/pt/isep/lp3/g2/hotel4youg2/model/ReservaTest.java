package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    private Reserva novaReserva() {
        return new Reserva(1, 7, 305, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 5), 2,
                EstadoReserva.CONFIRMADA, new BigDecimal("300.00"), null, null);
    }

    @Test
    void construtorGuardaOsDados() {
        Reserva reserva = novaReserva();
        assertEquals(1, reserva.getId());
        assertEquals(7, reserva.getClienteId());
        assertEquals(305, reserva.getQuartoNumero());
        assertEquals(LocalDate.of(2026, 11, 2), reserva.getDataEntrada());
        assertEquals(LocalDate.of(2026, 11, 5), reserva.getDataSaida());
        assertEquals(2, reserva.getNumHospedes());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(new BigDecimal("300.00"), reserva.getPrecoTotal());
    }

    @Test
    void reservaNovaAindaNaoTemCheckinNemCheckout() {
        Reserva reserva = novaReserva();
        assertNull(reserva.getCheckinEm());
        assertNull(reserva.getCheckoutEm());
    }

    @Test
    void fazerCheckinGuardaAHoraEMudaOEstado() {
        Reserva reserva = novaReserva();
        LocalDateTime chegada = LocalDateTime.of(2026, 11, 2, 15, 30);
        reserva.setCheckinEm(chegada);
        reserva.setEstado(EstadoReserva.EM_CURSO);
        assertEquals(chegada, reserva.getCheckinEm());
        assertEquals(EstadoReserva.EM_CURSO, reserva.getEstado());
    }

    @Test
    void reservaConfirmadaPodeSerCancelada() {
        Reserva reserva = novaReserva();
        reserva.cancelar();
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }

    @Test
    void reservaComCheckinNaoPodeSerCancelada() {
        Reserva reserva = novaReserva();
        reserva.setCheckinEm(LocalDateTime.now());
        reserva.setEstado(EstadoReserva.EM_CURSO);
        assertFalse(reserva.podeSerCancelada());
    }

    @Test
    void reservaJaCanceladaNaoPodeSerCanceladaOutraVez() {
        Reserva reserva = novaReserva();
        reserva.cancelar();
        assertFalse(reserva.podeSerCancelada());
    }

    @Test
    void cancelarReservaEmCursoDaErroEMantemOEstado() {
        Reserva reserva = novaReserva();
        reserva.setEstado(EstadoReserva.EM_CURSO);
        try {
            reserva.cancelar();
            fail("Devia ter recusado cancelar");
        } catch (IllegalStateException e) {
            assertEquals(EstadoReserva.EM_CURSO, reserva.getEstado());
        }
    }
}
