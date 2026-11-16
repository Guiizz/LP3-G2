package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartaoAtivoTest {

    @Test
    public void testToString() {
        System.out.println("toString");
        CartaoAtivo instance = new CartaoAtivo("C201", "Rui Costa (quarto 201)");
        String expResult = "C201 - Rui Costa (quarto 201)";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testEquals() {
        System.out.println("equals");
        CartaoAtivo instance = new CartaoAtivo("F12", "Ana Silva");
        boolean expResult = true;
        boolean result = instance.equals(new CartaoAtivo("F12", "Ana Silva"));
        assertEquals(expResult, result);
    }
}
