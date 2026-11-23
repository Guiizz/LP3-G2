package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

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

    @Test
    public void testGetCartaoRestauracao() {
        System.out.println("getCartao (a restauração tem o cartão R)");
        Funcionario instance = new Funcionario(9, "Rita Sousa", null, "234567891", null, null, null, null, 13, null);
        instance.setPerfil(Perfil.RESTAURACAO);
        String expResult = "R13";
        String result = instance.getCartao();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetCartaoRececionista() {
        System.out.println("getCartao (a receção continua com o cartão F)");
        Funcionario instance = new Funcionario(7, "Ana Silva", null, "123456789", null, null, null, null, 12, null);
        instance.setPerfil(Perfil.RECECIONISTA);
        String expResult = "F12";
        String result = instance.getCartao();
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsEmpresaDiferente() {
        System.out.println("equals (empresas diferentes)");
        Funcionario instance = new Funcionario(9, "Rita Sousa", null, "234567891", null, null, null, null, 13, null);
        instance.setEmpresa("Sabores do Norte Lda");
        Funcionario outra = new Funcionario(9, "Rita Sousa", null, "234567891", null, null, null, null, 13, null);
        outra.setEmpresa("Outra Lda");
        boolean expResult = false;
        boolean result = instance.equals(outra);
        assertEquals(expResult, result);
    }
}
