package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe Material.
 */
public class MaterialTest {

    private Material instance;

    // um champô, que é consumível
    @BeforeEach
    public void setUp() {
        instance = new Material(5, "XQF6324", "Rituals Champô 200ml", "ProductHero", true);
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }

    @Test
    public void testGetId() {
        System.out.println("getId");
        int expResult = 5;
        int result = instance.getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetCodigo() {
        System.out.println("getCodigo");
        String expResult = "XQF6324";
        String result = instance.getCodigo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDescricao() {
        System.out.println("getDescricao");
        String expResult = "Rituals Champô 200ml";
        String result = instance.getDescricao();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFornecedor() {
        System.out.println("getFornecedor");
        String expResult = "ProductHero";
        String result = instance.getFornecedor();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsConsumivel() {
        System.out.println("isConsumivel");
        boolean expResult = true;
        boolean result = instance.isConsumivel();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetConsumivel() {
        System.out.println("setConsumivel (passa a não consumível)");
        instance.setConsumivel(false);
        boolean expResult = false;
        boolean result = instance.isConsumivel();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetConsumivelTexto() {
        System.out.println("getConsumivelTexto (consumível)");
        String expResult = "Consumível";
        String result = instance.getConsumivelTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetConsumivelTextoNaoConsumivel() {
        System.out.println("getConsumivelTexto (não consumível)");
        Material secador = new Material(6, "X569P", "Secador de cabelo", "ProductHero", false);
        String expResult = "Não consumível";
        String result = secador.getConsumivelTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testEquals() {
        System.out.println("equals (mesmos dados)");
        Material expResult = new Material(5, "XQF6324", "Rituals Champô 200ml", "ProductHero", true);
        Material result = instance;
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsOutroConsumivel() {
        System.out.println("equals (consumível diferente)");
        Material outro = new Material(5, "XQF6324", "Rituals Champô 200ml", "ProductHero", false);
        boolean expResult = false;
        boolean result = instance.equals(outro);
        assertEquals(expResult, result);
    }

    @Test
    public void testHashCode() {
        System.out.println("hashCode (materiais iguais têm o mesmo hashCode)");
        Material outro = new Material(5, "XQF6324", "Rituals Champô 200ml", "ProductHero", true);
        int expResult = outro.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "Rituals Champô 200ml (ProductHero)";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
