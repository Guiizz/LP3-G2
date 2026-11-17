package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe MaterialQuarto.
 */
public class MaterialQuartoTest {

    private Material toalha;
    private MaterialQuarto instance;

    // um quarto duplo leva 2 toalhas de banho
    @BeforeEach
    public void setUp() {
        toalha = new Material(4, "PZ-1989", "Toalha de banho 400g cinza", "ProductHero", false);
        instance = new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2);
    }

    @AfterEach
    public void tearDown() {
        toalha = null;
        instance = null;
    }

    @Test
    public void testGetTipoQuarto() {
        System.out.println("getTipoQuarto");
        TipoQuarto expResult = TipoQuarto.DUPLO;
        TipoQuarto result = instance.getTipoQuarto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMaterial() {
        System.out.println("getMaterial");
        Material expResult = toalha;
        Material result = instance.getMaterial();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetQuantidade() {
        System.out.println("getQuantidade");
        int expResult = 2;
        int result = instance.getQuantidade();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMaterialId() {
        System.out.println("getMaterialId");
        int expResult = toalha.getId();
        int result = instance.getMaterialId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetCodigo() {
        System.out.println("getCodigo");
        String expResult = toalha.getCodigo();
        String result = instance.getCodigo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDescricao() {
        System.out.println("getDescricao");
        String expResult = toalha.getDescricao();
        String result = instance.getDescricao();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFornecedor() {
        System.out.println("getFornecedor");
        String expResult = toalha.getFornecedor();
        String result = instance.getFornecedor();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetConsumivelTexto() {
        System.out.println("getConsumivelTexto");
        String expResult = "Não consumível";
        String result = instance.getConsumivelTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testEquals() {
        System.out.println("equals (mesmo tipo, material e quantidade)");
        MaterialQuarto expResult = new MaterialQuarto(TipoQuarto.DUPLO,
                new Material(4, "PZ-1989", "Toalha de banho 400g cinza", "ProductHero", false), 2);
        MaterialQuarto result = instance;
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsOutroTipo() {
        System.out.println("equals (tipo de quarto diferente)");
        MaterialQuarto outro = new MaterialQuarto(TipoQuarto.FAMILIA, toalha, 2);
        boolean expResult = false;
        boolean result = instance.equals(outro);
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsOutraQuantidade() {
        System.out.println("equals (quantidade diferente)");
        MaterialQuarto outro = new MaterialQuarto(TipoQuarto.DUPLO, toalha, 3);
        boolean expResult = false;
        boolean result = instance.equals(outro);
        assertEquals(expResult, result);
    }

    @Test
    public void testHashCode() {
        System.out.println("hashCode (linhas iguais têm o mesmo hashCode)");
        MaterialQuarto outro = new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2);
        int expResult = outro.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "2 × Toalha de banho 400g cinza";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
