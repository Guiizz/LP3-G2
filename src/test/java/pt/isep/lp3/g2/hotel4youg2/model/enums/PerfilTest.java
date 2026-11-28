package pt.isep.lp3.g2.hotel4youg2.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários do enum Perfil.
 */
public class PerfilTest {

    @Test
    public void testGetDescricaoGestor() {
        System.out.println("getDescricao (gestor)");
        String expResult = "Gestor";
        String result = Perfil.GESTOR.getDescricao();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDescricaoRececionista() {
        System.out.println("getDescricao (rececionista)");
        String expResult = "Rececionista";
        String result = Perfil.RECECIONISTA.getDescricao();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString (nome legível, com acentos)");
        String expResult = "Restauração";
        String result = Perfil.RESTAURACAO.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testNomeNaBaseDeDados() {
        System.out.println("name (o que fica na base de dados não muda)");
        String expResult = "RESTAURACAO";
        String result = Perfil.RESTAURACAO.name();
        assertEquals(expResult, result);
    }
}