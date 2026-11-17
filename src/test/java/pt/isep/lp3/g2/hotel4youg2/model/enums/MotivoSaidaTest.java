package pt.isep.lp3.g2.hotel4youg2.model.enums;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários do enum MotivoSaida e do enum TipoSaida.
 */
public class MotivoSaidaTest {

    @Test
    public void testGetDescricao() {
        System.out.println("getDescricao");
        String expResult = "Devolvido ao fornecedor";
        String result = MotivoSaida.DEVOLVIDO_FORNECEDOR.getDescricao();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString (texto da lista no ecrã)");
        String expResult = "Estragado";
        String result = MotivoSaida.ESTRAGADO.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testDescreverMotivoSemTexto() {
        System.out.println("descrever (só o motivo)");
        String expResult = "Roubado";
        String result = MotivoSaida.descrever(MotivoSaida.ROUBADO, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testDescreverMotivoComTexto() {
        System.out.println("descrever (motivo e texto)");
        String expResult = "Outro: caiu ao chão";
        String result = MotivoSaida.descrever(MotivoSaida.OUTRO, "  caiu ao chão ");
        assertEquals(expResult, result);
    }

    @Test
    public void testDescreverSoTexto() {
        System.out.println("descrever (saída temporária só com o texto)");
        String expResult = "Reparação";
        String result = MotivoSaida.descrever(null, "Reparação");
        assertEquals(expResult, result);
    }

    @Test
    public void testDescreverTextoEmBranco() {
        System.out.println("descrever (texto só com espaços conta como sem texto)");
        String expResult = "Estragado";
        String result = MotivoSaida.descrever(MotivoSaida.ESTRAGADO, "   ");
        assertEquals(expResult, result);
    }

    @Test
    public void testDescreverSemNada() {
        System.out.println("descrever (saída antiga, sem motivo nem texto)");
        String expResult = "-";
        String result = MotivoSaida.descrever(null, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testTipoSaidaGetDescricao() {
        System.out.println("TipoSaida.getDescricao");
        String expResult = "Temporária";
        String result = TipoSaida.TEMPORARIA.getDescricao();
        assertEquals(expResult, result);
    }

    @Test
    public void testTipoSaidaToString() {
        System.out.println("TipoSaida.toString (texto da lista no ecrã)");
        String expResult = "Definitiva";
        String result = TipoSaida.DEFINITIVA.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testEscolhiveis() {
        System.out.println("escolhiveis (o consumo da reserva não se escolhe à mão)");
        List<MotivoSaida> expResult = List.of(MotivoSaida.ESTRAGADO, MotivoSaida.ROUBADO,
                MotivoSaida.DEVOLVIDO_FORNECEDOR, MotivoSaida.OUTRO);
        List<MotivoSaida> result = MotivoSaida.escolhiveis();
        assertEquals(expResult, result);
    }

    @Test
    public void testDescreverConsumoDaReserva() {
        System.out.println("descrever (saída de um pedido extra)");
        String expResult = "Consumo da reserva: Pedido extra da reserva 30";
        String result = MotivoSaida.descrever(MotivoSaida.CONSUMO_RESERVA, "Pedido extra da reserva 30");
        assertEquals(expResult, result);
    }
}
