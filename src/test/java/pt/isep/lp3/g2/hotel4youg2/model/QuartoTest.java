package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe Quarto.
 */
public class QuartoTest {

    private Quarto instance;

    @BeforeEach
    public void setUp() {
        instance = new Quarto(305, 3, TipoQuarto.DUPLO, Vista.MAR, 2);
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }

    @Test
    public void testGetNumero() {
        System.out.println("getNumero");
        int expResult = 305;
        int result = instance.getNumero();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPiso() {
        System.out.println("getPiso");
        int expResult = 3;
        int result = instance.getPiso();
        assertEquals(expResult, result);
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
        Vista expResult = Vista.MAR;
        Vista result = instance.getVista();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetCapacidade() {
        System.out.println("getCapacidade");
        int expResult = 2;
        int result = instance.getCapacidade();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetNumero() {
        System.out.println("setNumero");
        instance.setNumero(306);
        int expResult = 306;
        int result = instance.getNumero();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetPiso() {
        System.out.println("setPiso");
        instance.setPiso(4);
        int expResult = 4;
        int result = instance.getPiso();
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
        instance.setVista(Vista.MONTANHA);
        Vista expResult = Vista.MONTANHA;
        Vista result = instance.getVista();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetCapacidade() {
        System.out.println("setCapacidade");
        instance.setCapacidade(3);
        int expResult = 3;
        int result = instance.getCapacidade();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTarifaSemTarifa() {
        System.out.println("getTarifa (quarto novo, ainda sem tarifa)");
        Tarifa expResult = null;
        Tarifa result = instance.getTarifa();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetTarifa() {
        System.out.println("setTarifa");
        Tarifa expResult = new Tarifa(TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("100.00"));
        instance.setTarifa(new Tarifa(TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("100.00")));
        Tarifa result = instance.getTarifa();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetValorNoiteSemTarifa() {
        System.out.println("getValorNoite (sem tarifa não tem preço)");
        BigDecimal expResult = null;
        BigDecimal result = instance.getValorNoite();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetValorNoite() {
        System.out.println("getValorNoite (o preço vem da tarifa)");
        instance.setTarifa(new Tarifa(TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("150.00")));
        BigDecimal expResult = new BigDecimal("150.00");
        BigDecimal result = instance.getValorNoite();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetValorNoiteMudaComATarifa() {
        System.out.println("getValorNoite (mudar a tarifa muda o preço)");
        instance.setTarifa(new Tarifa(TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("80.00")));
        instance.setTarifa(new Tarifa(TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("85.00")));
        BigDecimal expResult = new BigDecimal("85.00");
        BigDecimal result = instance.getValorNoite();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsAtivoQuartoNovo() {
        System.out.println("isAtivo (quarto novo está ativo)");
        boolean expResult = true;
        boolean result = instance.isAtivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetAtivoDesativar() {
        System.out.println("setAtivo (desativar)");
        instance.setAtivo(false);
        boolean expResult = false;
        boolean result = instance.isAtivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetAtivoReativar() {
        System.out.println("setAtivo (reativar)");
        instance.setAtivo(false);
        instance.setAtivo(true);
        boolean expResult = true;
        boolean result = instance.isAtivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetEstadoAtivo() {
        System.out.println("getEstado (ativo)");
        String expResult = "Ativo";
        String result = instance.getEstado();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetEstadoDesativado() {
        System.out.println("getEstado (desativado)");
        instance.setAtivo(false);
        String expResult = "Desativado";
        String result = instance.getEstado();
        assertEquals(expResult, result);
    }
}
