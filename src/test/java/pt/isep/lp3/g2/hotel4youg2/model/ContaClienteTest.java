package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContaClienteTest {

    @Test
    public void testDefinirPassword() {
        System.out.println("definirPassword (guarda a palavra-passe e sai do primeiro acesso)");
        ContaCliente instance = new ContaCliente(5, 15, "123456789", "Rui Costa", "codigo", "salt1", true);
        instance.definirPassword("nova", "salt2", false);
        ContaCliente expResult = new ContaCliente(5, 15, "123456789", "Rui Costa", "nova", "salt2", false);
        assertEquals(expResult, instance);
    }

    @Test
    public void testEqualsPrimeiroAcessoDiferente() {
        System.out.println("equals (primeiro acesso diferente)");
        ContaCliente instance = new ContaCliente(5, 15, "123456789", "Rui Costa", "h", "s", true);
        boolean expResult = false;
        boolean result = instance.equals(new ContaCliente(5, 15, "123456789", "Rui Costa", "h", "s", false));
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        ContaCliente instance = new ContaCliente(5, 15, "123456789", "Rui Costa", "h", "s", false);
        String expResult = "Rui Costa (NIF 123456789)";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
