package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoMovimento;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MovimentoStockTest {

    private static final LocalDateTime QUANDO = LocalDateTime.of(2026, 11, 16, 9, 5);

    private final MovimentoStock instance = new MovimentoStock(TipoMovimento.SAIDA_DEFINITIVA, 4, 3, "Ana Silva", QUANDO);

    @Test
    public void testGetTipo() {
        System.out.println("getTipo");
        TipoMovimento expResult = TipoMovimento.SAIDA_DEFINITIVA;
        TipoMovimento result = instance.getTipo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetQuantidade() {
        System.out.println("getQuantidade");
        int expResult = 3;
        int result = instance.getQuantidade();
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
        System.out.println("getFuncionario (entrega antiga sem funcionário mostra -)");
        MovimentoStock semFuncionario = new MovimentoStock(TipoMovimento.ENTRADA, 4, 20, null, QUANDO);
        String expResult = "-";
        String result = semFuncionario.getFuncionario();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRegistadoEmTexto() {
        System.out.println("getRegistadoEmTexto");
        String expResult = "16/11/2026 09:05";
        String result = instance.getRegistadoEmTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRegistadoEmTextoSemData() {
        System.out.println("getRegistadoEmTexto (sem data)");
        MovimentoStock semData = new MovimentoStock(TipoMovimento.ENTRADA, 4, 20, "Rui Costa", null);
        String expResult = "";
        String result = semData.getRegistadoEmTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testTipoNoEcra() {
        System.out.println("TipoMovimento (texto no ecrã)");
        String expResult = "Saída definitiva";
        String result = instance.getTipo().toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testEquals() {
        System.out.println("equals (mesmos dados)");
        MovimentoStock outro = new MovimentoStock(TipoMovimento.SAIDA_DEFINITIVA, 4, 3, "Ana Silva", QUANDO);
        assertEquals(outro, instance);
    }

    @Test
    public void testEqualsOutroTipo() {
        System.out.println("equals (tipo diferente)");
        MovimentoStock outro = new MovimentoStock(TipoMovimento.ENTRADA, 4, 3, "Ana Silva", QUANDO);
        assertNotEquals(outro, instance);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "Saída definitiva de 3 em 16/11/2026 09:05 (Ana Silva)";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMotivo() {
        System.out.println("getMotivo (US77)");
        MovimentoStock saida = new MovimentoStock(TipoMovimento.SAIDA_DEFINITIVA, 4, 3, "Ana Silva", QUANDO, "Estragado");
        String expResult = "Estragado";
        String result = saida.getMotivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMotivoSemMotivo() {
        System.out.println("getMotivo (entrada ou saída antiga mostra -)");
        String expResult = "-";
        String result = instance.getMotivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsOutroMotivo() {
        System.out.println("equals (motivo diferente)");
        MovimentoStock comMotivo = new MovimentoStock(TipoMovimento.SAIDA_DEFINITIVA, 4, 3, "Ana Silva", QUANDO, "Roubado");
        boolean expResult = false;
        boolean result = instance.equals(comMotivo);
        assertEquals(expResult, result);
    }
}
