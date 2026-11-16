package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.Unidade;
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
        instance = new LinhaEntrega(1, "PZ-1989", "Toalha de banho 400g cinza", 50, Unidade.BOX, 500,
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
        LinhaEntrega errada = new LinhaEntrega(1, "PZ-1989", "Toalha", 50, Unidade.BOX, 500, new BigDecimal("2.51"),
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

    @Test
    public void testGetUnidadeTexto() {
        System.out.println("getUnidadeTexto");
        String expResult = "Caixa";
        String result = instance.getUnidadeTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPrecoUnitarioTexto() {
        System.out.println("getPrecoUnitarioTexto");
        String expResult = "2,51";
        String result = instance.getPrecoUnitarioTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetValorSemIvaTexto() {
        System.out.println("getValorSemIvaTexto");
        String expResult = "125,50";
        String result = instance.getValorSemIvaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTaxaIvaTexto() {
        System.out.println("getTaxaIvaTexto");
        String expResult = "23%";
        String result = instance.getTaxaIvaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetValorIvaTexto() {
        System.out.println("getValorIvaTexto");
        String expResult = "28,87";
        String result = instance.getValorIvaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTotalLinhaTexto() {
        System.out.println("getTotalLinhaTexto");
        String expResult = "154,37";
        String result = instance.getTotalLinhaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testValor() {
        System.out.println("valor (sempre com 2 casas decimais e vírgula)");
        String expResult = "9025,00";
        String result = LinhaEntrega.valor(new BigDecimal("9025"));
        assertEquals(expResult, result);
    }
}
