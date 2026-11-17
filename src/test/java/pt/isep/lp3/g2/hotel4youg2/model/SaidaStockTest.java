package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe SaidaStock.
 */
public class SaidaStockTest {

    private static final LocalDateTime QUANDO = LocalDateTime.of(2026, 11, 16, 9, 5);

    private SaidaStock instance;

    // 2 secadores levados para reparação pela Ana Silva
    @BeforeEach
    public void setUp() {
        instance = new SaidaStock(7, 4, "X569P", "Secador de cabelo Philips Preto", 2, "Reparação", "Ana Silva",
                QUANDO);
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }

    @Test
    public void testGetId() {
        System.out.println("getId");
        int expResult = 7;
        int result = instance.getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMaterialId() {
        System.out.println("getMaterialId");
        int expResult = 4;
        int result = instance.getMaterialId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetCodigo() {
        System.out.println("getCodigo");
        String expResult = "X569P";
        String result = instance.getCodigo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMaterial() {
        System.out.println("getMaterial");
        String expResult = "Secador de cabelo Philips Preto";
        String result = instance.getMaterial();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetQuantidade() {
        System.out.println("getQuantidade");
        int expResult = 2;
        int result = instance.getQuantidade();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMotivo() {
        System.out.println("getMotivo");
        String expResult = "Reparação";
        String result = instance.getMotivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMotivoSemMotivo() {
        System.out.println("getMotivo (sem motivo mostra -)");
        SaidaStock semMotivo = new SaidaStock(7, 4, "X569P", "Secador", 2, null, "Ana Silva", QUANDO);
        String expResult = "-";
        String result = semMotivo.getMotivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFuncionario() {
        System.out.println("getFuncionario");
        String expResult = "Ana Silva";
        String result = instance.getFuncionario();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFuncionarioSemNome() {
        System.out.println("getFuncionario (sem nome mostra -)");
        SaidaStock semNome = new SaidaStock(7, 4, "X569P", "Secador", 2, "Reparação", " ", QUANDO);
        String expResult = "-";
        String result = semNome.getFuncionario();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRegistadaEmTexto() {
        System.out.println("getRegistadaEmTexto");
        String expResult = "16/11/2026 09:05";
        String result = instance.getRegistadaEmTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRegistadaEmTextoSemData() {
        System.out.println("getRegistadaEmTexto (sem data)");
        SaidaStock semData = new SaidaStock(7, 4, "X569P", "Secador", 2, "Reparação", "Ana Silva", null);
        String expResult = "";
        String result = semData.getRegistadaEmTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testEquals() {
        System.out.println("equals (mesmos dados)");
        SaidaStock expResult = new SaidaStock(7, 4, "X569P", "Secador de cabelo Philips Preto", 2, "Reparação",
                "Ana Silva", QUANDO);
        SaidaStock result = instance;
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsOutraQuantidade() {
        System.out.println("equals (quantidade diferente)");
        SaidaStock outra = new SaidaStock(7, 4, "X569P", "Secador de cabelo Philips Preto", 3, "Reparação",
                "Ana Silva", QUANDO);
        boolean expResult = false;
        boolean result = instance.equals(outra);
        assertEquals(expResult, result);
    }

    @Test
    public void testHashCode() {
        System.out.println("hashCode (saídas iguais têm o mesmo hashCode)");
        SaidaStock outra = new SaidaStock(7, 4, "X569P", "Secador de cabelo Philips Preto", 2, "Reparação",
                "Ana Silva", QUANDO);
        int expResult = outra.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "2 × Secador de cabelo Philips Preto (Reparação)";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
