package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ResumoDoDiaTest {

    @Test
    public void testGetters() {
        System.out.println("getters");
        ResumoDoDia instance = new ResumoDoDia(3, 2, 1, 4, 5);
        assertEquals(3, instance.getCheckins());
        assertEquals(2, instance.getCheckouts());
        assertEquals(1, instance.getChegadas());
        assertEquals(4, instance.getPedidos());
        assertEquals(5, instance.getEntregas());
    }

    @Test
    public void testTemTarefas() {
        System.out.println("temTarefas (há um pedido por decidir)");
        ResumoDoDia instance = new ResumoDoDia(0, 0, 0, 1, 0);
        assertTrue(instance.temTarefas());
    }

    @Test
    public void testSoChegadasNaoSaoTarefas() {
        System.out.println("temTarefas (só chegadas não é trabalho por fazer)");
        ResumoDoDia instance = new ResumoDoDia(0, 0, 3, 0, 0);
        assertFalse(instance.temTarefas());
    }

    @Test
    public void testContagemNegativa() {
        System.out.println("contagem negativa é recusada");
        try {
            new ResumoDoDia(-1, 0, 0, 0, 0);
            fail("Devia recusar uma contagem negativa");
        } catch (IllegalArgumentException e) {
            assertEquals("As contagens não podem ser negativas.", e.getMessage());
        }
    }
}
