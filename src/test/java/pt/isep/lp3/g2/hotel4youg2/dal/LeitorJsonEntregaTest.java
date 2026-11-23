package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.exception.ErrosFicheiroException;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Unidade;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class LeitorJsonEntregaTest {

    private static final String PASTA = "/pt/isep/lp3/g2/hotel4youg2/json/";

    private LeitorJsonEntrega instance;
    private List<String> erros;

    @BeforeEach
    public void setUp() {
        instance = new LeitorJsonEntrega();
        erros = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        instance = null;
        erros = null;
    }

    private InputStream ficheiro(String nome) {
        return LeitorJsonEntregaTest.class.getResourceAsStream(PASTA + nome);
    }

    @Test
    public void testLer() {
        System.out.println("ler (JSON de exemplo do enunciado)");
        String expResult = "X012X";
        Entrega obtido = instance.ler(ficheiro("entrega-valida.json"), erros);
        String result = "" + obtido.getNumeroEncomenda();
        assertEquals(expResult, result);
    }

    @Test
    public void testLerData() {
        System.out.println("ler (data da encomenda)");
        LocalDate expResult = LocalDate.of(2026, 11, 6);
        LocalDate result = instance.ler(ficheiro("entrega-valida.json"), erros).getDataEncomenda();
        assertEquals(expResult, result);
    }

    @Test
    public void testLerFornecedor() {
        System.out.println("ler (fornecedor)");
        Fornecedor expResult = new Fornecedor(0, "964070328", "L'industrie Alimentaire",
                "Rue internationale de Paris 96", null, "Paris", "75001", "France", "FR");
        Fornecedor result = instance.ler(ficheiro("entrega-valida.json"), erros).getFornecedor();
        assertEquals(expResult, result);
    }

    @Test
    public void testLerPrimeiraLinha() {
        System.out.println("ler (primeira linha)");
        LinhaEntrega expResult = new LinhaEntrega(1, "63279196", "Croissant Brioche délifrance", 10000, Unidade.UNIT,
                10000, new BigDecimal("0.25"), new BigDecimal("2500"), 20, new BigDecimal("500"),
                new BigDecimal("3000"));
        LinhaEntrega result = instance.ler(ficheiro("entrega-valida.json"), erros).getLinhas().get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testLerTotal() {
        System.out.println("ler (total da entrega)");
        BigDecimal expResult = new BigDecimal("3508.80").stripTrailingZeros();
        BigDecimal result = instance.ler(ficheiro("entrega-valida.json"), erros).getTotal().stripTrailingZeros();
        assertEquals(expResult, result);
    }

    @Test
    public void testLerMoeda() {
        System.out.println("ler (moeda)");
        String expResult = "EUR";
        String result = instance.ler(ficheiro("entrega-valida.json"), erros).getMoeda();
        assertEquals(expResult, result);
    }

    @Test
    public void testLerAcentosCorrigidos() {
        System.out.println("ler (os acentos mal gravados no ficheiro de exemplo ficam certos)");
        String expResult = "Pão Campagne Francês";
        String result = instance.ler(ficheiro("entrega-valida.json"), erros).getLinhas().get(1).getDescricao();
        assertEquals(expResult, result);
    }

    @Test
    public void testLerCaixasComUnidades() {
        System.out.println("ler (caixas com o total em unidades em InformationalQuantity)");
        int expResult = 10000;
        int result = instance.ler(ficheiro("entrega-caixas-com-unidades.json"), erros).getLinhas().get(0).getUnidades();
        assertEquals(expResult, result);
    }

    @Test
    public void testLerSemErrosNoFicheiroValido() {
        System.out.println("ler (o ficheiro de exemplo não tem erros)");
        instance.ler(ficheiro("entrega-valida.json"), erros);
        List<String> expResult = new ArrayList<>();
        List<String> result = erros;
        assertEquals(expResult, result);
    }

    @Test
    public void testLerDataInvalida() {
        System.out.println("ler (data que não existe)");
        List<String> expResult = List.of("Cabeçalho, campo OrderDate: a data da encomenda não existe ou não está "
                + "no formato AAAA-MM-DD (2026-02-30).");
        instance.ler(ficheiro("entrega-data-invalida.json"), erros);
        List<String> result = erros;
        assertEquals(expResult, result);
    }

    @Test
    public void testLerDuasMoedas() {
        System.out.println("ler (uma linha noutra moeda)");
        List<String> expResult = List.of("Linha 2, campo LineBaseAmount: o valor está em USD, mas a encomenda "
                + "está em EUR.");
        instance.ler(ficheiro("entrega-duas-moedas.json"), erros);
        List<String> result = erros;
        assertEquals(expResult, result);
    }

    @Test
    public void testLerCaixasSemUnidades() {
        System.out.println("ler (caixas sem o total em unidades)");
        List<String> expResult = List.of("Linha 1, campo InformationalQuantity: a quantidade vem em Caixa e o "
                + "ficheiro não indica quantas unidades são.");
        instance.ler(ficheiro("entrega-caixas-sem-unidades.json"), erros);
        List<String> result = erros;
        assertEquals(expResult, result);
    }

    @Test
    public void testLerMalFormado() {
        System.out.println("ler (mal formado, diz a linha do ficheiro)");
        List<String> expResult = List.of("Linha 5, coluna 8 do ficheiro: o JSON não está bem formado.");
        List<String> result = null;
        try {
            instance.ler(ficheiro("entrega-mal-formada.json"), erros);
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            result = expected.getErros();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testLerCamposEmFalta() {
        System.out.println("ler (campos em falta, aparecem todos juntos)");
        List<String> expResult = List.of("Cabeçalho: falta o campo OrderNumber.", "Linha 2: falta o campo Quantity.");
        List<String> result = null;
        try {
            instance.ler(ficheiro("entrega-campos-em-falta.json"), erros);
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            result = expected.getErros();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testLerTextoNumNumero() {
        System.out.println("ler (texto onde devia estar um número)");
        List<String> expResult = List.of("Linha 1, Quantity.Value, campo Value: tem de ser um número inteiro.");
        List<String> result = null;
        try {
            instance.ler(ficheiro("entrega-texto-num-numero.json"), erros);
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            result = expected.getErros();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testLerNumeroEnorme() {
        System.out.println("ler (número demasiado grande para uma quantidade)");
        List<String> expResult = List.of("Linha 1, Quantity.Value, campo Value: tem de ser um número inteiro.");
        List<String> result = null;
        try {
            instance.ler(ficheiro("entrega-numero-enorme.json"), erros);
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            result = expected.getErros();
        }
        assertEquals(expResult, result);
    }
}