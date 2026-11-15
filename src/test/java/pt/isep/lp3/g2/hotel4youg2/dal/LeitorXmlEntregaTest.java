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

    // lê um ficheiro que devia ser recusado logo (formato ou XSD) e devolve os erros
    private List<String> errosAoLer(String nome) {
        try {
            instance.ler(ficheiro(nome), erros);
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
            return null;
        } catch (ErrosFicheiroException expected) {
            return expected.getErros();
        }
    }

    // importa um XSD que devia ser recusado e devolve os erros
    private List<String> errosAoImportar(String nome) {
        try {
            LeitorXmlEntrega.importarXsd(ficheiroEmDisco(nome));
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
            return null;
        } catch (ErrosFicheiroException expected) {
            return expected.getErros();
        }
    }

    @Test
    public void testLer() {
        System.out.println("ler");
        Fornecedor fornecedor = new Fornecedor(0, "11PT920", "ProductHero", "Rua da Alegria", "nº1", "Porto",
                "4000-032", "Portugal", "PT");
        Entrega result = instance.ler(ficheiro("entrega-valida.xml"), erros);
        assertEquals("OC56974", result.getNumeroEncomenda());
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
        assertEquals(500, result.getLinhas().get(0).getUnidades());
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
        assertEquals(expResult, erros);
    }

    @Test
    public void testLerDataInvalida() {
        System.out.println("ler (data que não existe)");
        instance.ler(ficheiro("entrega-data-invalida.xml"), erros);
        List<String> expResult = List.of("Cabeçalho, campo OrderDate: a data da encomenda não existe (5/13/2026).");
        assertEquals(expResult, erros);
    }

    @Test
    public void testLerDuasMoedas() {
        System.out.println("ler (uma linha noutra moeda)");
        instance.ler(ficheiro("entrega-duas-moedas.xml"), erros);
        List<String> expResult = List.of("Linha 3, campo PricePerUnit: o valor está em USD, "
                + "mas a encomenda está em EUR.");
        assertEquals(expResult, erros);
    }

    @Test
    public void testLerMalFormado() {
        System.out.println("ler (mal formado)");
        List<String> result = errosAoLer("entrega-mal-formada.xml");
        assertEquals(1, result.size());
        assertEquals(true, result.get(0).contains("do ficheiro: o XML não está bem formado."));
    }

    @Test
    public void testLerForaDoXsd() {
        System.out.println("ler (não segue o XSD, diz a linha do ficheiro)");
        List<String> result = errosAoLer("entrega-fora-do-xsd.xml");
        assertEquals(true, result.get(0).startsWith("Linha 31 do ficheiro: "));
    }

    @Test
    public void testLerForaDoXsdJuntaTodosOsErros() {
        System.out.println("ler (dois erros no XSD aparecem os dois)");
        List<String> result = errosAoLer("entrega-dois-erros-xsd.xml");
        boolean temLinha31 = false;
        boolean temLinha67 = false;
        for (String erro : result) {
            if (erro.startsWith("Linha 31 do ficheiro: ")) {
                temLinha31 = true;
            }
            if (erro.startsWith("Linha 67 do ficheiro: ")) {
                temLinha67 = true;
            }
        }
        assertEquals(true, temLinha31);
        assertEquals(true, temLinha67);
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
    public void testImportarXsdPassaASerUsado() {
        System.out.println("importarXsd (o XML passa a ser validado com o XSD importado)");
        LeitorXmlEntrega.importarXsd(ficheiroEmDisco("xsd-outra-raiz.xsd"));
        // o XSD importado só aceita <Encomenda>, por isso o XML do professor (<Order>) é recusado
        List<String> result = errosAoLer("entrega-valida.xml");
        assertEquals(true, result.get(0).startsWith("Linha 2 do ficheiro: "));
    }

    @Test
    public void testImportarXsdDoProjeto() {
        System.out.println("importarXsd (o XSD do projeto importado aceita o XML do professor)");
        LeitorXmlEntrega.importarXsd(ficheiroEmDisco("Hotel4You.xsd"));
        Entrega result = instance.ler(ficheiro("entrega-valida.xml"), erros);
        assertEquals("OC56974", result.getNumeroEncomenda());
        assertEquals("Hotel4You.xsd", LeitorXmlEntrega.getNomeXsdImportado());
    }

    @Test
    public void testImportarXsdInvalido() {
        System.out.println("importarXsd (XSD inválido é recusado)");
        List<String> result = errosAoImportar("xsd-invalido.xsd");
        assertEquals(true, result.get(0).startsWith("O ficheiro não é um XSD válido. Linha 4 do ficheiro: "));
    }

    @Test
    public void testImportarXsdInvalidoMantemOAnterior() {
        System.out.println("importarXsd (XSD inválido não substitui o que estava em uso)");
        LeitorXmlEntrega.importarXsd(ficheiroEmDisco("xsd-outra-raiz.xsd"));
        errosAoImportar("xsd-invalido.xsd");
        String expResult = "xsd-outra-raiz.xsd";
        String result = LeitorXmlEntrega.getNomeXsdImportado();
        assertEquals(expResult, result);
    }

    @Test
    public void testImportarXsdQueNaoEUmXsd() {
        System.out.println("importarXsd (um XML de entrega não é um XSD)");
        List<String> result = errosAoImportar("entrega-valida.xml");
        assertEquals(1, result.size());
        assertEquals(null, LeitorXmlEntrega.getNomeXsdImportado());
    }

    @Test
    public void testImportarXsdQueNaoExiste() {
        System.out.println("importarXsd (ficheiro que não existe)");
        try {
            LeitorXmlEntrega.importarXsd(new File("nao-existe.xsd"));
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            assertEquals("Não foi possível abrir o ficheiro nao-existe.xsd.", expected.getMessage());
        }
    }

    @Test
    public void testUsarXsdDoProjeto() {
        System.out.println("usarXsdDoProjeto (esquece o XSD importado)");
        LeitorXmlEntrega.importarXsd(ficheiroEmDisco("xsd-outra-raiz.xsd"));
        LeitorXmlEntrega.usarXsdDoProjeto();
        Entrega result = instance.ler(ficheiro("entrega-valida.xml"), erros);
        assertEquals("OC56974", result.getNumeroEncomenda());
        assertEquals(null, LeitorXmlEntrega.getNomeXsdImportado());
    }
}
