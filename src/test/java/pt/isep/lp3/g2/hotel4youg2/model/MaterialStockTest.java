package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaterialStockTest {

    private final MaterialStock instance = new MaterialStock(4, "M-1", "Toalhas", "Lavandaria Norte", 12);

    @Test
    public void testGetEntrouSemMovimentos() {
        System.out.println("getEntrou (ainda sem movimentos)");
        int expResult = 0;
        int result = instance.getEntrou();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetMovimentosEntrou() {
        System.out.println("setMovimentos (entrou)");
        instance.setMovimentos(20, 8);
        int expResult = 20;
        int result = instance.getEntrou();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetMovimentosSaiu() {
        System.out.println("setMovimentos (saiu)");
        instance.setMovimentos(20, 8);
        int expResult = 8;
        int result = instance.getSaiu();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetUnidade() {
        System.out.println("getUnidade (o stock conta-se em unidades)");
        String expResult = "Unidade";
        String result = instance.getUnidade();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetQuantidade() {
        System.out.println("getQuantidade (stock efetivo)");
        int expResult = 12;
        int result = instance.getQuantidade();
        assertEquals(expResult, result);
    }
}
