package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HorarioReservaTest {

    private HorarioReserva instance;

    @BeforeEach
    public void setUp() {
        instance = new HorarioReserva(LocalTime.of(15, 0), LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalTime.of(14, 0), new BigDecimal("20.00"), new BigDecimal("25.00"));
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }

    @Test
    public void testCalcularTaxaSemPedido() {
        System.out.println("calcularTaxa (nenhum)");
        BigDecimal expResult = BigDecimal.ZERO;
        BigDecimal result = instance.calcularTaxa(false, false);
        assertEquals(expResult, result);
    }

    @Test
    public void testCalcularTaxaSoEarly() {
        System.out.println("calcularTaxa (só early)");
        BigDecimal expResult = instance.getTaxaEarlyCheckin();
        BigDecimal result = instance.calcularTaxa(true, false);
        assertEquals(expResult, result);
    }

    @Test
    public void testCalcularTaxaSoLate() {
        System.out.println("calcularTaxa (só late)");
        BigDecimal expResult = instance.getTaxaLateCheckout();
        BigDecimal result = instance.calcularTaxa(false, true);
        assertEquals(expResult, result);
    }

    @Test
    public void testCalcularTaxaOsDois() {
        System.out.println("calcularTaxa (os dois)");
        BigDecimal expResult = instance.getTaxaEarlyCheckin().add(instance.getTaxaLateCheckout());
        BigDecimal result = instance.calcularTaxa(true, true);
        assertEquals(expResult, result);
    }

    @Test
    public void testHoraMinimaCheckinComEarly() {
        System.out.println("horaMinimaCheckin (com early)");
        LocalTime expResult = instance.getHoraEarlyCheckin();
        LocalTime result = instance.horaMinimaCheckin(true);
        assertEquals(expResult, result);
    }

    @Test
    public void testHoraMinimaCheckinSemEarly() {
        System.out.println("horaMinimaCheckin (sem early)");
        LocalTime expResult = instance.getHoraCheckin();
        LocalTime result = instance.horaMinimaCheckin(false);
        assertEquals(expResult, result);
    }
}
