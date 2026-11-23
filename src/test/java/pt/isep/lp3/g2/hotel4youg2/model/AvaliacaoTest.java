package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AvaliacaoTest {

    @Test
    public void testGetEstrelasTexto() {
        System.out.println("getEstrelasTexto (3 de 5 estrelas)");
        Avaliacao instance = new Avaliacao(1, 12, 3, null, null);
        String expResult = "★★★☆☆";
        String result = instance.getEstrelasTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testDesenharCincoEstrelas() {
        System.out.println("desenharEstrelas (5 estrelas)");
        String expResult = "★★★★★";
        String result = Avaliacao.desenharEstrelas(5);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetComentarioTextoSemComentario() {
        System.out.println("getComentarioTexto (sem comentário mostra um traço)");
        Avaliacao instance = new Avaliacao(1, 12, 4, null, null);
        String expResult = "-";
        String result = instance.getComentarioTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDataTexto() {
        System.out.println("getDataTexto");
        Avaliacao instance = new Avaliacao(1, 12, 4, "Bom", LocalDateTime.of(2026, 11, 22, 18, 30));
        String expResult = "22/11/2026";
        String result = instance.getDataTexto();
        assertEquals(expResult, result);
    }
}
