package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ServicoTest {

    private Categoria lazer;

    @BeforeEach
    public void setUp() {
        lazer = new Categoria(1, "Lazer");
    }

    @AfterEach
    public void tearDown() {
        lazer = null;
    }

    @Test
    public void testEquals() {
        System.out.println("equals");
        Servico instance = new Servico(1, "Ginásio", new BigDecimal("5.00"), TipoCobranca.POR_UTILIZACAO, lazer, true);
        Servico outro = new Servico(1, "Ginásio", new BigDecimal("5.0"), TipoCobranca.POR_UTILIZACAO, lazer, true);
        boolean expResult = true;
        boolean result = instance.equals(outro);
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsCobrancaDiferente() {
        System.out.println("equals (cobrança diferente)");
        Servico instance = new Servico(1, "Ginásio", new BigDecimal("5.00"), TipoCobranca.POR_UTILIZACAO, lazer, true);
        Servico outro = new Servico(1, "Ginásio", new BigDecimal("5.00"), TipoCobranca.POR_ESTADIA, lazer, true);
        assertNotEquals(instance, outro);
    }

    @Test
    public void testSetPreco() {
        System.out.println("setPreco");
        Servico instance = new Servico(1, "Sauna", new BigDecimal("15.00"), TipoCobranca.POR_UTILIZACAO, lazer, true);
        BigDecimal expResult = new BigDecimal("18.00");
        instance.setPreco(new BigDecimal("18.00"));
        BigDecimal result = instance.getPreco();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetAtivo() {
        System.out.println("setAtivo");
        Servico instance = new Servico(1, "Sauna", new BigDecimal("15.00"), TipoCobranca.POR_UTILIZACAO, lazer, true);
        boolean expResult = false;
        instance.setAtivo(false);
        boolean result = instance.isAtivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDescricaoTipoCobranca() {
        System.out.println("getDescricao");
        TipoCobranca instance = TipoCobranca.POR_ESTADIA;
        String expResult = "Por estadia";
        String result = instance.getDescricao();
        assertEquals(expResult, result);
    }
}
