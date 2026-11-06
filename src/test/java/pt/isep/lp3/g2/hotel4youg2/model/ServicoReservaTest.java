package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServicoReservaTest {

    private Servico massagem;
    private Reserva reserva;

    @BeforeEach
    public void setUp() {
        massagem = new Servico(4, "Massagem", new BigDecimal("45.00"), new Categoria(2, "Spa"), true);
        reserva = new Reserva(10, 5, 201, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), 2,
                EstadoReserva.CONFIRMADA, new BigDecimal("200.00"), null, null);
    }

    @AfterEach
    public void tearDown() {
        massagem = null;
        reserva = null;
    }

    @Test
    public void testGetTotal() {
        System.out.println("getTotal (serviço)");
        ServicoReserva instance = new ServicoReserva(1, 10, massagem, 2, new BigDecimal("45.00"),
                LocalDateTime.of(2026, 11, 4, 10, 0));
        BigDecimal expResult = new BigDecimal("90.00");
        BigDecimal result = instance.getTotal();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTotalReserva() {
        System.out.println("getTotal (reserva: quarto + serviços)");
        reserva.setTotalServicos(new BigDecimal("90.00"));
        BigDecimal expResult = new BigDecimal("290.00");
        BigDecimal result = reserva.getTotal();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTotalReservaSemServicos() {
        System.out.println("getTotal (reserva sem serviços)");
        BigDecimal expResult = new BigDecimal("200.00");
        BigDecimal result = reserva.getTotal();
        assertEquals(expResult, result);
    }

    @Test
    public void testAceitaServicos() {
        System.out.println("aceitaServicos");
        boolean expResult = true;
        reserva.setEstado(EstadoReserva.EM_CURSO);
        boolean result = reserva.aceitaServicos();
        assertEquals(expResult, result);
    }

    @Test
    public void testAceitaServicosCancelada() {
        System.out.println("aceitaServicos (cancelada)");
        boolean expResult = false;
        reserva.setEstado(EstadoReserva.CANCELADA);
        boolean result = reserva.aceitaServicos();
        assertEquals(expResult, result);
    }

    @Test
    public void testAceitaServicosConcluida() {
        System.out.println("aceitaServicos (concluída)");
        boolean expResult = false;
        reserva.setEstado(EstadoReserva.CONCLUIDA);
        boolean result = reserva.aceitaServicos();
        assertEquals(expResult, result);
    }
}
