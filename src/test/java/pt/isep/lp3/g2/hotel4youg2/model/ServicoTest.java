package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ServicoTest {

    private Categoria lazer;

    @BeforeEach
    public void setUp() {
        lazer = new Categoria(1, "Lazer");
    }

    @AfterEach
    public void tearDown() {
        lazer = null;
    }

    @Test
    public void testEquals() {
        System.out.println("equals");
        Servico instance = new Servico(1, "Ginásio", new BigDecimal("5.00"), lazer, true);
        Servico outro = new Servico(1, "Ginásio", new BigDecimal("5.0"), lazer, true);
        boolean expResult = true;
        boolean result = instance.equals(outro);
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsPrecoDiferente() {
        System.out.println("equals (preço diferente)");
        Servico instance = new Servico(1, "Ginásio", new BigDecimal("5.00"), lazer, true);
        Servico outro = new Servico(1, "Ginásio", new BigDecimal("7.50"), lazer, true);
        assertNotEquals(instance, outro);
    }

    @Test
    public void testSetPreco() {
        System.out.println("setPreco");
        Servico instance = new Servico(1, "Sauna", new BigDecimal("15.00"), lazer, true);
        BigDecimal expResult = new BigDecimal("18.00");
        instance.setPreco(new BigDecimal("18.00"));
        BigDecimal result = instance.getPreco();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetAtivo() {
        System.out.println("setAtivo");
        Servico instance = new Servico(1, "Sauna", new BigDecimal("15.00"), lazer, true);
        boolean expResult = false;
        instance.setAtivo(false);
        boolean result = instance.isAtivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        Servico instance = new Servico(1, "Sauna", new BigDecimal("15.00"), lazer, true);
        String expResult = "Sauna (15.00 €)";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
