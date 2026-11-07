package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    @Test
    void alterarDatasMudaAsDatasERecalculaOPreco() {
        Reserva reserva = novaReserva();
        reserva.alterarDatas(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15));
        assertEquals(LocalDate.of(2026, 11, 10), reserva.getDataEntrada());
        assertEquals(LocalDate.of(2026, 11, 15), reserva.getDataSaida());
        assertEquals(0, new BigDecimal("500.00").compareTo(reserva.getPrecoTotal()));
    }

    @Test
    void reservaComCheckinNaoPodeSerAlterada() {
        Reserva reserva = novaReserva();
        reserva.setEstado(EstadoReserva.EM_CURSO);
        reserva.setCheckinEm(LocalDateTime.now());
        assertFalse(reserva.podeSerAlterada());
    }

    @Test
    void reservaCanceladaNaoPodeSerAlterada() {
        Reserva reserva = novaReserva();
        reserva.cancelar();
        assertFalse(reserva.podeSerAlterada());
    }

    @Test
    void alterarReservaCanceladaDaErroEMantemAsDatas() {
        Reserva reserva = novaReserva();
        reserva.cancelar();
        try {
            reserva.alterarDatas(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15));
            fail("Devia ter recusado alterar");
        } catch (IllegalStateException e) {
            assertEquals(LocalDate.of(2026, 11, 2), reserva.getDataEntrada());
        }
    }

    @Test
    public void testFazerCheckin() {
        System.out.println("fazerCheckin");
        Reserva instance = novaReserva();
        LocalDateTime agora = instance.getDataEntrada().atTime(15, 30);
        Reserva expResult = new Reserva(instance.getId(), instance.getClienteId(), instance.getQuartoNumero(),
                instance.getDataEntrada(), instance.getDataSaida(), instance.getNumHospedes(),
                EstadoReserva.EM_CURSO, instance.getPrecoTotal(), agora, null);
        expResult.setCartao("C" + instance.getQuartoNumero());
        instance.fazerCheckin(agora, LocalTime.of(15, 0));
        Reserva result = instance;
        assertEquals(expResult, result);
    }

    @Test
    public void testFazerCheckinAntesDas15h() {
        System.out.println("fazerCheckin (antes das 15h)");
        Reserva instance = novaReserva();
        try {
            instance.fazerCheckin(instance.getDataEntrada().atTime(14, 59), LocalTime.of(15, 0));
            fail("Devia ter recusado o check-in antes das 15h");
        } catch (IllegalStateException expected) {
            EstadoReserva expResult = EstadoReserva.CONFIRMADA;
            EstadoReserva result = instance.getEstado();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testFazerCheckinForaDoDiaDeEntrada() {
        System.out.println("fazerCheckin (fora do dia de entrada)");
        Reserva instance = novaReserva();
        try {
            instance.fazerCheckin(instance.getDataEntrada().plusDays(1).atTime(16, 0), LocalTime.of(15, 0));
            fail("Devia ter recusado o check-in fora do dia de entrada");
        } catch (IllegalStateException expected) {
            EstadoReserva expResult = EstadoReserva.CONFIRMADA;
            EstadoReserva result = instance.getEstado();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testFazerCheckinReservaCancelada() {
        System.out.println("fazerCheckin (reserva cancelada)");
        Reserva instance = novaReserva();
        instance.cancelar();
        String expResult = "A reserva " + instance.getId() + " foi cancelada e não pode fazer check-in";
        try {
            instance.fazerCheckin(instance.getDataEntrada().atTime(16, 0), LocalTime.of(15, 0));
            fail("Devia ter recusado uma reserva cancelada");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testFazerCheckinRepetido() {
        System.out.println("fazerCheckin (repetido)");
        Reserva instance = novaReserva();
        instance.fazerCheckin(instance.getDataEntrada().atTime(15, 30), LocalTime.of(15, 0));
        String expResult = "A reserva " + instance.getId() + " já tem o check-in feito";
        try {
            instance.fazerCheckin(instance.getDataEntrada().atTime(16, 0), LocalTime.of(15, 0));
            fail("Devia ter recusado um segundo check-in");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testFazerCheckout() {
        System.out.println("fazerCheckout");
        Reserva instance = novaReserva();
        LocalDateTime checkin = instance.getDataEntrada().atTime(15, 30);
        LocalDateTime agora = instance.getDataSaida().atTime(11, 0);
        instance.fazerCheckin(checkin, LocalTime.of(15, 0));
        Reserva expResult = new Reserva(instance.getId(), instance.getClienteId(), instance.getQuartoNumero(),
                instance.getDataEntrada(), instance.getDataSaida(), instance.getNumHospedes(),
                EstadoReserva.CONCLUIDA, instance.getPrecoTotal(), checkin, agora);
        expResult.setCartao("C" + instance.getQuartoNumero());
        instance.fazerCheckout(agora);
        Reserva result = instance;
        assertEquals(expResult, result);
    }

    @Test
    public void testFazerCheckoutSemCheckin() {
        System.out.println("fazerCheckout (sem check-in)");
        Reserva instance = novaReserva();
        try {
            instance.fazerCheckout(instance.getDataSaida().atTime(11, 0));
            fail("Devia ter recusado o check-out de uma reserva sem check-in");
        } catch (IllegalStateException expected) {
            EstadoReserva expResult = EstadoReserva.CONFIRMADA;
            EstadoReserva result = instance.getEstado();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testCartaoDaAcessoDuranteAEstadia() {
        System.out.println("cartaoDaAcesso (durante a estadia)");
        Reserva instance = novaReserva();
        instance.fazerCheckin(instance.getDataEntrada().atTime(15, 30), LocalTime.of(15, 0));
        boolean expResult = true;
        boolean result = instance.cartaoDaAcesso();
        assertEquals(expResult, result);
    }

    @Test
    public void testCartaoDeixaDeDarAcessoDepoisDoCheckout() {
        System.out.println("cartaoDaAcesso (depois do check-out)");
        Reserva instance = novaReserva();
        instance.fazerCheckin(instance.getDataEntrada().atTime(15, 30), LocalTime.of(15, 0));
        instance.fazerCheckout(instance.getDataSaida().atTime(11, 0));
        boolean expResult = false;
        boolean result = instance.cartaoDaAcesso();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTotal() {
        System.out.println("getTotal (quarto + serviços + taxa)");
        Reserva instance = novaReserva();
        instance.setTotalServicos(new BigDecimal("35.50"));
        instance.setTaxaHorario(new BigDecimal("20.00"));
        BigDecimal expResult = instance.getPrecoTotal().add(instance.getTotalServicos()).add(instance.getTaxaHorario());
        BigDecimal result = instance.getTotal();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTotalSemServicosNemTaxa() {
        System.out.println("getTotal (só o quarto)");
        Reserva instance = novaReserva();
        BigDecimal expResult = instance.getPrecoTotal();
        BigDecimal result = instance.getTotal();
        assertEquals(expResult, result);
    }
}
