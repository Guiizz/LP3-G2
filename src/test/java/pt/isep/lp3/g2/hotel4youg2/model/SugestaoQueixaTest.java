package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Testes unitários da classe SugestaoQueixa.
 */
public class SugestaoQueixaTest {

    private static final LocalDateTime QUANDO = LocalDateTime.of(2026, 10, 29, 18, 30);

    private SugestaoQueixa instance;

    @BeforeEach
    public void setUp() {
        instance = new SugestaoQueixa(3, 7, TipoSugestao.QUEIXA, "Ar condicionado avariado", 2, QUANDO);
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }

    @Test
    public void testGetId() {
        System.out.println("getId");
        int expResult = 3;
        int result = instance.getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetClienteId() {
        System.out.println("getClienteId");
        int expResult = 7;
        int result = instance.getClienteId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTipo() {
        System.out.println("getTipo");
        TipoSugestao expResult = TipoSugestao.QUEIXA;
        TipoSugestao result = instance.getTipo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDescricao() {
        System.out.println("getDescricao");
        String expResult = "Ar condicionado avariado";
        String result = instance.getDescricao();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRegistadaPor() {
        System.out.println("getRegistadaPor");
        int expResult = 2;
        int result = instance.getRegistadaPor();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRegistadaEm() {
        System.out.println("getRegistadaEm");
        LocalDateTime expResult = QUANDO;
        LocalDateTime result = instance.getRegistadaEm();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetId() {
        System.out.println("setId");
        instance.setId(9);
        int expResult = 9;
        int result = instance.getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetRegistadaEm() {
        System.out.println("setRegistadaEm");
        LocalDateTime outra = LocalDateTime.of(2026, 11, 2, 9, 5);
        instance.setRegistadaEm(outra);
        LocalDateTime expResult = outra;
        LocalDateTime result = instance.getRegistadaEm();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetNomeClienteSemNome() {
        System.out.println("getNomeCliente (ainda sem nome)");
        String expResult = null;
        String result = instance.getNomeCliente();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetNomeCliente() {
        System.out.println("setNomeCliente");
        instance.setNomeCliente("Ana Silva");
        String expResult = "Ana Silva";
        String result = instance.getNomeCliente();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRegistadaEmTexto() {
        System.out.println("getRegistadaEmTexto");
        String expResult = "29/10/2026 18:30";
        String result = instance.getRegistadaEmTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRegistadaEmTextoSemData() {
        System.out.println("getRegistadaEmTexto (sem data mostra texto vazio)");
        SugestaoQueixa semData = new SugestaoQueixa(0, 7, TipoSugestao.SUGESTAO, "Mais toalhas", 2, null);
        String expResult = "";
        String result = semData.getRegistadaEmTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testEquals() {
        System.out.println("equals (mesmos dados)");
        SugestaoQueixa outra = new SugestaoQueixa(3, 7, TipoSugestao.QUEIXA, "Ar condicionado avariado", 2, QUANDO);
        assertEquals(outra, instance);
    }

    @Test
    public void testEqualsNaoDependeDoNomeDoCliente() {
        System.out.println("equals (o nome do cliente não conta)");
        SugestaoQueixa outra = new SugestaoQueixa(3, 7, TipoSugestao.QUEIXA, "Ar condicionado avariado", 2, QUANDO);
        outra.setNomeCliente("Ana Silva");
        assertEquals(outra, instance);
    }

    @Test
    public void testEqualsOutraDescricao() {
        System.out.println("equals (descrição diferente)");
        SugestaoQueixa outra = new SugestaoQueixa(3, 7, TipoSugestao.QUEIXA, "Barulho", 2, QUANDO);
        assertNotEquals(outra, instance);
    }

    @Test
    public void testEqualsOutroTipo() {
        System.out.println("equals (tipo diferente)");
        SugestaoQueixa outra = new SugestaoQueixa(3, 7, TipoSugestao.SUGESTAO, "Ar condicionado avariado", 2,
                QUANDO);
        assertNotEquals(outra, instance);
    }

    @Test
    public void testHashCode() {
        System.out.println("hashCode (registos iguais têm o mesmo hashCode)");
        SugestaoQueixa outra = new SugestaoQueixa(3, 7, TipoSugestao.QUEIXA, "Ar condicionado avariado", 2, QUANDO);
        int expResult = outra.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "Queixa do cliente 7: Ar condicionado avariado";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
