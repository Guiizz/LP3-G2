package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Testes unitários da classe Tarifa.
 */
public class TarifaTest {

    private Tarifa instance;

    @BeforeEach
    public void setUp() {
        instance = new Tarifa(TipoQuarto.DUPLO, Vista.MONTANHA, new BigDecimal("90.00"));
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }

    @Test
    public void testGetTipo() {
        System.out.println("getTipo");
        TipoQuarto expResult = TipoQuarto.DUPLO;
        TipoQuarto result = instance.getTipo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetVista() {
        System.out.println("getVista");
        Vista expResult = Vista.MONTANHA;
        Vista result = instance.getVista();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetValorNoite() {
        System.out.println("getValorNoite");
        BigDecimal expResult = new BigDecimal("90.00");
        BigDecimal result = instance.getValorNoite();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetTipo() {
        System.out.println("setTipo");
        instance.setTipo(TipoQuarto.FAMILIA);
        TipoQuarto expResult = TipoQuarto.FAMILIA;
        TipoQuarto result = instance.getTipo();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetVista() {
        System.out.println("setVista");
        instance.setVista(Vista.MAR);
        Vista expResult = Vista.MAR;
        Vista result = instance.getVista();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetValorNoite() {
        System.out.println("setValorNoite");
        instance.setValorNoite(new BigDecimal("70.00"));
        BigDecimal expResult = new BigDecimal("70.00");
        BigDecimal result = instance.getValorNoite();
        assertEquals(expResult, result);
    }

    @Test
    public void testEquals() {
        System.out.println("equals (mesmo tipo, vista e preço)");
        Tarifa outra = new Tarifa(TipoQuarto.DUPLO, Vista.MONTANHA, new BigDecimal("90.00"));
        assertEquals(outra, instance);
    }

    @Test
    public void testEqualsPrecoComOutraEscala() {
        System.out.println("equals (90.0 e 90.00 contam como iguais)");
        Tarifa outra = new Tarifa(TipoQuarto.DUPLO, Vista.MONTANHA, new BigDecimal("90.0"));
        assertEquals(outra, instance);
    }

    @Test
    public void testEqualsOutroTipo() {
        System.out.println("equals (tipo diferente)");
        Tarifa outra = new Tarifa(TipoQuarto.FAMILIA, Vista.MONTANHA, new BigDecimal("90.00"));
        assertNotEquals(outra, instance);
    }

    @Test
    public void testEqualsOutraVista() {
        System.out.println("equals (vista diferente)");
        Tarifa outra = new Tarifa(TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("90.00"));
        assertNotEquals(outra, instance);
    }

    @Test
    public void testEqualsOutroPreco() {
        System.out.println("equals (preço diferente)");
        Tarifa outra = new Tarifa(TipoQuarto.DUPLO, Vista.MONTANHA, new BigDecimal("95.00"));
        assertNotEquals(outra, instance);
    }

    @Test
    public void testEqualsComNull() {
        System.out.println("equals (null)");
        assertNotEquals(null, instance);
    }

    @Test
    public void testHashCode() {
        System.out.println("hashCode (tarifas iguais têm o mesmo hashCode)");
        Tarifa outra = new Tarifa(TipoQuarto.DUPLO, Vista.MONTANHA, new BigDecimal("90.0"));
        int expResult = outra.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "Tarifa{DUPLO, MONTANHA, 90.00}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
