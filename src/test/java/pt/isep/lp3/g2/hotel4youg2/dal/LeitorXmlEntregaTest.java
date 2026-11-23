package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.exception.ErrosFicheiroException;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Unidade;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testes unitários da classe LeitorXmlEntrega.
 */
public class LeitorXmlEntregaTest {

    private static final String PASTA = "/pt/isep/lp3/g2/hotel4youg2/xml/";

    private LeitorXmlEntrega instance;
    private List<String> erros;

    @BeforeEach
    public void setUp() {
        LeitorXmlEntrega.usarXsdDoProjeto();
        instance = new LeitorXmlEntrega();
        erros = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        // o XSD importado fica guardado entre testes: volta sempre ao do projeto
        LeitorXmlEntrega.usarXsdDoProjeto();
        instance = null;
        erros = null;
    }

    private InputStream ficheiro(String nome) {
        return LeitorXmlEntregaTest.class.getResourceAsStream(PASTA + nome);
    }

    private File ficheiroEmDisco(String nome) {
        try {
            return new File(LeitorXmlEntregaTest.class.getResource(PASTA + nome).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testLer() {
        System.out.println("ler");
        Fornecedor fornecedor = new Fornecedor(0, "11PT920", "ProductHero", "Rua da Alegria", "nº1", "Porto",
                "4000-032", "Portugal", "PT");
        Entrega result = instance.ler(ficheiro("entrega-valida.xml"), erros);
        String expResult = "OC56974";
        assertEquals(expResult, result.getNumeroEncomenda());
        assertEquals(LocalDate.of(2026, 10, 5), result.getDataEncomenda());
        assertEquals(fornecedor, result.getFornecedor());
        assertEquals(5, result.getLinhas().size());
        assertEquals(0, erros.size());
    }

    @Test
    public void testLerPrimeiraLinha() {
        System.out.println("ler (primeira linha)");
        LinhaEntrega expResult = new LinhaEntrega(1, "PZ-1989", "Toalha de banho 400g cinza", 50, Unidade.BOX, 500,
                new BigDecimal("2.51"), new BigDecimal("125.50"), 23, new BigDecimal("28.87"), new BigDecimal("154.37"));
        LinhaEntrega result = instance.ler(ficheiro("entrega-valida.xml"), erros).getLinhas().get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testLerUnidadesDeCadaLinha() {
        System.out.println("ler (unidades de cada linha)");
        Entrega result = instance.ler(ficheiro("entrega-valida.xml"), erros);
        // linha 1: 50 caixas, o ficheiro diz que são 500 unidades; as outras já vêm em unidades
        int expResult = 500;
        assertEquals(expResult, result.getLinhas().get(0).getUnidades());
        assertEquals(200, result.getLinhas().get(1).getUnidades());
        assertEquals(50, result.getLinhas().get(2).getUnidades());
    }

    @Test
    public void testLerTotal() {
        System.out.println("ler (total da entrega)");
        BigDecimal expResult = new BigDecimal("17389.75");
        BigDecimal result = instance.ler(ficheiro("entrega-valida.xml"), erros).getTotal();
        assertEquals(0, expResult.compareTo(result));
    }

    @Test
    public void testLerMoeda() {
        System.out.println("ler (moeda)");
        String expResult = "EUR";
        String result = instance.ler(ficheiro("entrega-valida.xml"), erros).getMoeda();
        assertEquals(expResult, result);
    }

    @Test
    public void testLerCaixasSemUnidades() {
        System.out.println("ler (caixas sem o total em unidades)");
        instance.ler(ficheiro("entrega-caixas-sem-unidades.xml"), erros);
        List<String> expResult = List.of("Linha 1, campo InformationalQuantity: a quantidade vem em Caixa "
                + "e o ficheiro não indica quantas unidades são.");
        List<String> result = erros;
        assertEquals(expResult, result);
    }

    @Test
    public void testLerDataInvalida() {
        System.out.println("ler (data que não existe)");
        instance.ler(ficheiro("entrega-data-invalida.xml"), erros);
        List<String> expResult = List.of("Cabeçalho, campo OrderDate: a data da encomenda não existe (5/13/2026).");
        List<String> result = erros;
        assertEquals(expResult, result);
    }

    @Test
    public void testLerDuasMoedas() {
        System.out.println("ler (uma linha noutra moeda)");
        instance.ler(ficheiro("entrega-duas-moedas.xml"), erros);
        List<String> expResult = List.of("Linha 3, campo PricePerUnit: o valor está em USD, "
                + "mas a encomenda está em EUR.");
        List<String> result = erros;
        assertEquals(expResult, result);
    }

    @Test
    public void testGetNomeXsdImportadoSemImportar() {
        System.out.println("getNomeXsdImportado (sem importar usa o do projeto)");
        String expResult = null;
        String result = LeitorXmlEntrega.getNomeXsdImportado();
        assertEquals(expResult, result);
    }

    @Test
    public void testImportarXsd() {
        System.out.println("importarXsd");
        LeitorXmlEntrega.importarXsd(ficheiroEmDisco("xsd-outra-raiz.xsd"));
        String expResult = "xsd-outra-raiz.xsd";
        String result = LeitorXmlEntrega.getNomeXsdImportado();
        assertEquals(expResult, result);
    }

    @Test
    public void testImportarXsdDoProjeto() {
        System.out.println("importarXsd (o XSD do projeto importado aceita o XML do professor)");
        LeitorXmlEntrega.importarXsd(ficheiroEmDisco("Hotel4You.xsd"));
        Entrega result = instance.ler(ficheiro("entrega-valida.xml"), erros);
        String expResult = "OC56974";
        assertEquals(expResult, result.getNumeroEncomenda());
        assertEquals("Hotel4You.xsd", LeitorXmlEntrega.getNomeXsdImportado());
    }

    @Test
    public void testUsarXsdDoProjeto() {
        System.out.println("usarXsdDoProjeto (esquece o XSD importado)");
        LeitorXmlEntrega.importarXsd(ficheiroEmDisco("xsd-outra-raiz.xsd"));
        LeitorXmlEntrega.usarXsdDoProjeto();
        Entrega result = instance.ler(ficheiro("entrega-valida.xml"), erros);
        String expResult = "OC56974";
        assertEquals(expResult, result.getNumeroEncomenda());
        assertEquals(null, LeitorXmlEntrega.getNomeXsdImportado());
    }

    @Test
    public void testLerMalFormado() {
        System.out.println("ler (mal formado)");
        List<String> result = null;
        try {
            instance.ler(ficheiro("entrega-mal-formada.xml"), erros);
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            result = expected.getErros();
        }
        int expResult = 1;
        assertEquals(expResult, result.size());
        assertEquals(true, result.get(0).contains("do ficheiro: o XML não está bem formado."));
    }

    @Test
    public void testLerForaDoXsd() {
        System.out.println("ler (não segue o XSD, diz a linha do ficheiro)");
        List<String> lidos = null;
        try {
            instance.ler(ficheiro("entrega-fora-do-xsd.xml"), erros);
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            lidos = expected.getErros();
        }
        boolean expResult = true;
        boolean result = lidos.get(0).startsWith("Linha 31 do ficheiro: ");
        assertEquals(expResult, result);
    }

    @Test
    public void testLerForaDoXsdJuntaTodosOsErros() {
        System.out.println("ler (dois erros no XSD aparecem os dois)");
        List<String> lidos = null;
        try {
            instance.ler(ficheiro("entrega-dois-erros-xsd.xml"), erros);
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            lidos = expected.getErros();
        }
        boolean temLinha31 = false;
        boolean temLinha67 = false;
        for (String erro : lidos) {
            if (erro.startsWith("Linha 31 do ficheiro: ")) {
                temLinha31 = true;
            }
            if (erro.startsWith("Linha 67 do ficheiro: ")) {
                temLinha67 = true;
            }
        }
        boolean expResult = true;
        boolean result = temLinha31 && temLinha67;
        assertEquals(expResult, result);
    }

    @Test
    public void testImportarXsdPassaASerUsado() {
        System.out.println("importarXsd (o XML passa a ser validado com o XSD importado)");
        LeitorXmlEntrega.importarXsd(ficheiroEmDisco("xsd-outra-raiz.xsd"));
        // o XSD importado só aceita <Encomenda>, por isso o XML do professor (<Order>) é recusado
        List<String> lidos = null;
        try {
            instance.ler(ficheiro("entrega-valida.xml"), erros);
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            lidos = expected.getErros();
        }
        boolean expResult = true;
        boolean result = lidos.get(0).startsWith("Linha 2 do ficheiro: ");
        assertEquals(expResult, result);
    }

    @Test
    public void testImportarXsdInvalido() {
        System.out.println("importarXsd (XSD inválido é recusado)");
        List<String> lidos = null;
        try {
            LeitorXmlEntrega.importarXsd(ficheiroEmDisco("xsd-invalido.xsd"));
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            lidos = expected.getErros();
        }
        boolean expResult = true;
        boolean result = lidos.get(0).startsWith("O ficheiro não é um XSD válido. Linha 4 do ficheiro: ");
        assertEquals(expResult, result);
    }

    @Test
    public void testImportarXsdInvalidoMantemOAnterior() {
        System.out.println("importarXsd (XSD inválido não substitui o que estava em uso)");
        LeitorXmlEntrega.importarXsd(ficheiroEmDisco("xsd-outra-raiz.xsd"));
        try {
            LeitorXmlEntrega.importarXsd(ficheiroEmDisco("xsd-invalido.xsd"));
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
        }
        String expResult = "xsd-outra-raiz.xsd";
        String result = LeitorXmlEntrega.getNomeXsdImportado();
        assertEquals(expResult, result);
    }

    @Test
    public void testImportarXsdQueNaoEUmXsd() {
        System.out.println("importarXsd (um XML de entrega não é um XSD)");
        List<String> result = null;
        try {
            LeitorXmlEntrega.importarXsd(ficheiroEmDisco("entrega-valida.xml"));
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            result = expected.getErros();
        }
        int expResult = 1;
        assertEquals(expResult, result.size());
        assertEquals(null, LeitorXmlEntrega.getNomeXsdImportado());
    }

    @Test
    public void testImportarXsdQueNaoExiste() {
        System.out.println("importarXsd (ficheiro que não existe)");
        try {
            LeitorXmlEntrega.importarXsd(new File("nao-existe.xsd"));
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            String expResult = "Não foi possível abrir o ficheiro nao-existe.xsd.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }
}
