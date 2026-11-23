package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EstadiaTest {

    private final Estadia instance = new Estadia(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));

    @Test
    public void testOcupaNoiteDaEntrada() {
        System.out.println("ocupaNoite (noite do dia de entrada)");
        boolean expResult = true;
        boolean result = instance.ocupaNoite(LocalDate.of(2026, 11, 10));
        assertEquals(expResult, result);
    }

    @Test
    public void testOcupaNoiteDoMeio() {
        System.out.println("ocupaNoite (noite antes da saída)");
        boolean expResult = true;
        boolean result = instance.ocupaNoite(LocalDate.of(2026, 11, 11));
        assertEquals(expResult, result);
    }

    @Test
    public void testNaoOcupaNoiteDaSaida() {
        System.out.println("ocupaNoite (no dia de saída o quarto fica livre)");
        boolean expResult = false;
        boolean result = instance.ocupaNoite(LocalDate.of(2026, 11, 12));
        assertEquals(expResult, result);
    }

    @Test
    public void testNaoOcupaNoiteAntesDaEntrada() {
        System.out.println("ocupaNoite (antes da entrada)");
        boolean expResult = false;
        boolean result = instance.ocupaNoite(LocalDate.of(2026, 11, 9));
        assertEquals(expResult, result);
    }
}
