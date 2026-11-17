package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalPicagemTest {

    @Test
    public void testToStringQuarto() {
        System.out.println("toString (quarto)");
        LocalPicagem instance = LocalPicagem.quarto(201);
        String expResult = "Quarto 201";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testToStringOutroLocal() {
        System.out.println("toString (piscina)");
        LocalPicagem instance = new LocalPicagem(LocalHotel.PISCINA, 0);
        String expResult = "Piscina";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testQuarto() {
        System.out.println("quarto");
        LocalPicagem expResult = new LocalPicagem(LocalHotel.QUARTO, 305);
        LocalPicagem result = LocalPicagem.quarto(305);
        assertEquals(expResult, result);
    }

    @Test
    public void testSoOQuartoTemNumero() {
        System.out.println("construtor (fora do quarto o número é ignorado)");
        LocalPicagem expResult = new LocalPicagem(LocalHotel.BAR, 0);
        LocalPicagem result = new LocalPicagem(LocalHotel.BAR, 201);
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsQuartosDiferentes() {
        System.out.println("equals (quartos diferentes)");
        boolean expResult = false;
        boolean result = LocalPicagem.quarto(201).equals(LocalPicagem.quarto(202));
        assertEquals(expResult, result);
    }
}
