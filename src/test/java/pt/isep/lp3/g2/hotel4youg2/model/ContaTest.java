package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContaTest {

    private Fornecedor fornecedor(boolean comPassword) {
        Fornecedor fornecedor = new Fornecedor(1, "F001", "Lavandaria Norte", null, null, null, null, null, null);
        if (comPassword) {
            fornecedor.definirPassword("h", "s");
        }
        return fornecedor;
    }

    @Test
    public void testGetTipoDoGestor() {
        System.out.println("getTipo (gestor)");
        Conta instance = new Conta(new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true));
        String expResult = "Gestor";
        String result = instance.getTipo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTipoDoFornecedor() {
        System.out.println("getTipo (fornecedor)");
        Conta instance = new Conta(fornecedor(true));
        String expResult = "Fornecedor";
        String result = instance.getTipo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetEstadoDesativada() {
        System.out.println("getEstado (conta desativada)");
        Conta instance = new Conta(new Utilizador(3, 3, "antiga", "h", "s", Perfil.RECECIONISTA, false));
        String expResult = "Desativada";
        String result = instance.getEstado();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetEstadoFornecedorSemPassword() {
        System.out.println("getEstado (fornecedor sem password não consegue entrar)");
        Conta instance = new Conta(fornecedor(false));
        String expResult = "Sem password";
        String result = instance.getEstado();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetNomeDoFornecedor() {
        System.out.println("getNome (fornecedor: código e nome)");
        Conta instance = new Conta(fornecedor(true));
        String expResult = "F001 - Lavandaria Norte";
        String result = instance.getNome();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsDeFornecedor() {
        System.out.println("isDeFornecedor");
        Conta instance = new Conta(fornecedor(true));
        boolean expResult = true;
        boolean result = instance.isDeFornecedor();
        assertEquals(expResult, result);
    }
}
