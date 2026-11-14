package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FuncionarioTest {

    @Test
    public void testGetCartao() {
        System.out.println("getCartao");
        Funcionario instance = new Funcionario(7, "Ana Silva", null, "123456789", null, null, null, null, 12, null);
        String expResult = "F12";
        String result = instance.getCartao();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        Funcionario instance = new Funcionario(7, "Ana Silva", null, "123456789", null, null, null, null, 12, null);
        String expResult = "F12 - Ana Silva";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
