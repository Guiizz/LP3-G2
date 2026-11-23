package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EstatisticaTest {

    @Test
    public void testGetValorSemValor() {
        System.out.println("getValor (sem valor fica 0)");
        BigDecimal expResult = BigDecimal.ZERO;
        BigDecimal result = new Estatistica("Duplo", null).getValor();
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsComCasasDecimaisDiferentes() {
        System.out.println("equals (300 e 300.00 são o mesmo valor)");
        Estatistica instance = new Estatistica("Duplo", new BigDecimal("300.00"));
        boolean expResult = true;
        boolean result = instance.equals(new Estatistica("Duplo", new BigDecimal("300")));
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsComRotuloDiferente() {
        System.out.println("equals (rótulos diferentes)");
        Estatistica instance = new Estatistica("Duplo", new BigDecimal("300"));
        boolean expResult = false;
        boolean result = instance.equals(new Estatistica("Família", new BigDecimal("300")));
        assertEquals(expResult, result);
    }

    @Test
    public void testHashCodeComCasasDecimaisDiferentes() {
        System.out.println("hashCode (300 e 300.00 dão o mesmo código)");
        int expResult = new Estatistica("Duplo", new BigDecimal("300")).hashCode();
        int result = new Estatistica("Duplo", new BigDecimal("300.00")).hashCode();
        assertEquals(expResult, result);
    }
}
