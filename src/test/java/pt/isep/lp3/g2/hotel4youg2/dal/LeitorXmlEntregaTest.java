package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Unidade;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class LeitorXmlEntregaTest {

    private LeitorXmlEntrega instance;

    @BeforeEach
    public void setUp() {
        instance = new LeitorXmlEntrega();
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }

    private InputStream ficheiro(String nome) {
        return LeitorXmlEntregaTest.class.getResourceAsStream("/pt/isep/lp3/g2/hotel4youg2/xml/" + nome);
    }

    @Test
    public void testLer() {
        System.out.println("ler");
        Fornecedor fornecedor = new Fornecedor(0, "11PT920", "ProductHero", "Rua da Alegria", "nº1", "Porto",
                "4000-032", "Portugal", "PT");
        Entrega result = instance.ler(ficheiro("entrega-valida.xml"));
        assertEquals("OC56974", result.getNumeroEncomenda());
        assertEquals(LocalDate.of(2026, 10, 5), result.getDataEncomenda());
        assertEquals(fornecedor, result.getFornecedor());
        assertEquals(5, result.getLinhas().size());
    }

    @Test
    public void testLerPrimeiraLinha() {
        System.out.println("ler (primeira linha)");
        LinhaEntrega expResult = new LinhaEntrega(1, "PZ-1989", "Toalha de banho 400g cinza", 50, Unidade.BOX,
                new BigDecimal("2.51"), new BigDecimal("125.50"), 23, new BigDecimal("28.87"), new BigDecimal("154.37"));
        LinhaEntrega result = instance.ler(ficheiro("entrega-valida.xml")).getLinhas().get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testLerTotal() {
        System.out.println("ler (total da entrega)");
        BigDecimal expResult = new BigDecimal("17389.75");
        BigDecimal result = instance.ler(ficheiro("entrega-valida.xml")).getTotal();
        assertEquals(0, expResult.compareTo(result));
    }

    @Test
    public void testLerMalFormado() {
        System.out.println("ler (mal formado)");
        try {
            instance.ler(ficheiro("entrega-mal-formada.xml"));
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().startsWith("O ficheiro não é um XML bem formado"));
        }
    }

    @Test
    public void testLerForaDoXsd() {
        System.out.println("ler (não segue o XSD)");
        try {
            instance.ler(ficheiro("entrega-fora-do-xsd.xml"));
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().startsWith("O ficheiro não segue o formato das entregas (linha 31)"));
        }
    }

    @Test
    public void testLerDataInvalida() {
        System.out.println("ler (data inválida)");
        try {
            instance.ler(ficheiro("entrega-data-invalida.xml"));
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A data da encomenda não existe: 5/13/2026.", expected.getMessage());
        }
    }
}
