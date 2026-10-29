package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinhaEntregaTest {

    private LinhaEntrega instance;

    // a linha 1 do XML do professor: 50 caixas de toalhas a 2,51 €, IVA 23%
    @BeforeEach
    public void setUp() {
        instance = new LinhaEntrega(1, "PZ-1989", "Toalha de banho 400g cinza", 50, Unidade.BOX,
                new BigDecimal("2.51"), new BigDecimal("125.50"), 23, new BigDecimal("28.87"), new BigDecimal("154.37"));
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }

    @Test
    public void testIsValorSemIvaCorreto() {
        System.out.println("isValorSemIvaCorreto");
        boolean expResult = true;
        boolean result = instance.isValorSemIvaCorreto();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsIvaCorreto() {
        System.out.println("isIvaCorreto");
        boolean expResult = true;
        boolean result = instance.isIvaCorreto();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsTotalCorreto() {
        System.out.println("isTotalCorreto");
        boolean expResult = true;
        boolean result = instance.isTotalCorreto();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsTotalCorretoComTotalErrado() {
        System.out.println("isTotalCorreto (total errado)");
        LinhaEntrega errada = new LinhaEntrega(1, "PZ-1989", "Toalha", 50, Unidade.BOX, new BigDecimal("2.51"),
                new BigDecimal("125.50"), 23, new BigDecimal("28.87"), new BigDecimal("999.99"));
        boolean expResult = false;
        boolean result = errada.isTotalCorreto();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsContasCertas() {
        System.out.println("isContasCertas");
        boolean expResult = true;
        boolean result = instance.isContasCertas();
        assertEquals(expResult, result);
    }

    @Test
    public void testDoXml() {
        System.out.println("Unidade.doXml");
        Unidade expResult = Unidade.KILOGRAM;
        Unidade result = Unidade.doXml("Kilogram");
        assertEquals(expResult, result);
    }
}
