package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Unidade;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe Entrega.
 */
public class EntregaTest {

    private Entrega instance;

    // as duas primeiras linhas do XML do professor; a linha 2 tem o IVA com 3 cêntimos a menos
    @BeforeEach
    public void setUp() {
        Fornecedor fornecedor = new Fornecedor(0, "11PT920", "ProductHero", "Rua da Alegria", "nº1", "Porto",
                "4000-032", "Portugal", "PT");
        instance = new Entrega(0, "OC56974", LocalDate.of(2026, 10, 5), fornecedor);
        instance.adicionarLinha(new LinhaEntrega(1, "PZ-1989", "Toalha de banho 400g cinza", 50, Unidade.BOX, 500,
                new BigDecimal("2.51"), new BigDecimal("125.50"), 23, new BigDecimal("28.87"),
                new BigDecimal("154.37")));
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }

    private void adicionarLinhaComIvaErrado() {
        instance.adicionarLinha(new LinhaEntrega(2, "XQF6324", "Rituals Condicionador/Champô de banho 200ml", 200,
                Unidade.UNIT, 200, new BigDecimal("3.49"), new BigDecimal("698.00"), 23, new BigDecimal("160.51"),
                new BigDecimal("858.54")));
    }

    @Test
    public void testGetEstadoEntregaNova() {
        System.out.println("getEstado (entrega nova está pendente)");
        EstadoEntrega expResult = EstadoEntrega.PENDENTE;
        EstadoEntrega result = instance.getEstado();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMoedaEntregaNova() {
        System.out.println("getMoeda (entrega nova está em EUR)");
        String expResult = "EUR";
        String result = instance.getMoeda();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetMoeda() {
        System.out.println("setMoeda");
        instance.setMoeda("USD");
        String expResult = "USD";
        String result = instance.getMoeda();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTotalSemIva() {
        System.out.println("getTotalSemIva");
        adicionarLinhaComIvaErrado();
        BigDecimal expResult = new BigDecimal("823.50");
        BigDecimal result = instance.getTotalSemIva();
        assertEquals(0, expResult.compareTo(result));
    }

    @Test
    public void testGetTotalIva() {
        System.out.println("getTotalIva");
        adicionarLinhaComIvaErrado();
        BigDecimal expResult = new BigDecimal("189.38");
        BigDecimal result = instance.getTotalIva();
        assertEquals(0, expResult.compareTo(result));
    }

    @Test
    public void testGetTotal() {
        System.out.println("getTotal");
        adicionarLinhaComIvaErrado();
        BigDecimal expResult = new BigDecimal("1012.91");
        BigDecimal result = instance.getTotal();
        assertEquals(0, expResult.compareTo(result));
    }

    @Test
    public void testGetTotaisSemLinhas() {
        System.out.println("getTotalSemIva e getTotalIva (sem linhas dão zero)");
        Entrega semLinhas = new Entrega(0, "OC-1", LocalDate.of(2026, 10, 5), null);
        assertEquals(0, BigDecimal.ZERO.compareTo(semLinhas.getTotalSemIva()));
        assertEquals(0, BigDecimal.ZERO.compareTo(semLinhas.getTotalIva()));
    }

    @Test
    public void testIsTotalCertoSemTotalGuardado() {
        System.out.println("isTotalCerto (entrega lida do ficheiro, ainda sem total guardado)");
        boolean expResult = true;
        boolean result = instance.isTotalCerto();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsTotalCerto() {
        System.out.println("isTotalCerto (o total guardado é a soma das linhas)");
        instance.setTotalRegistado(new BigDecimal("154.37"));
        boolean expResult = true;
        boolean result = instance.isTotalCerto();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsTotalCertoComDiferenca() {
        System.out.println("isTotalCerto (o total guardado não bate certo)");
        instance.setTotalRegistado(new BigDecimal("160.00"));
        boolean expResult = false;
        boolean result = instance.isTotalCerto();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsContasCertas() {
        System.out.println("isContasCertas (todas as linhas certas)");
        boolean expResult = true;
        boolean result = instance.isContasCertas();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsContasCertasComLinhaErrada() {
        System.out.println("isContasCertas (uma linha com o IVA errado)");
        adicionarLinhaComIvaErrado();
        boolean expResult = false;
        boolean result = instance.isContasCertas();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsContasCertasComTotalErrado() {
        System.out.println("isContasCertas (linhas certas, mas total guardado errado)");
        instance.setTotalRegistado(new BigDecimal("999.99"));
        boolean expResult = false;
        boolean result = instance.isContasCertas();
        assertEquals(expResult, result);
    }
}
