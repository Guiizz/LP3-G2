package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.ITarifaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe TarifaBLL.
 */
public class TarifaBLLTest {

    /** Repositório de teste: guarda as tarifas em memória em vez de ir à base de dados. */
    private static class TarifaDALFalsa implements ITarifaDAL {
        private List<Tarifa> tarifas = new ArrayList<>();
        private int alteracoes = 0;

        @Override
        public List<Tarifa> listar() {
            return tarifas;
        }

        @Override
        public boolean atualizarValor(TipoQuarto tipo, Vista vista, BigDecimal novoValor) {
            for (Tarifa tarifa : tarifas) {
                if (tarifa.getTipo() == tipo && tarifa.getVista() == vista) {
                    tarifa.setValorNoite(novoValor);
                    alteracoes++;
                    return true;
                }
            }
            return false;
        }
    }

    private TarifaDALFalsa dal;
    private TarifaBLL instance;
    private Tarifa duploMar;
    private Tarifa individualNenhuma;

    @BeforeEach
    public void setUp() {
        dal = new TarifaDALFalsa();
        duploMar = new Tarifa(TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("100.00"));
        individualNenhuma = new Tarifa(TipoQuarto.INDIVIDUAL, Vista.NENHUMA, new BigDecimal("50.00"));
        dal.tarifas.add(duploMar);
        dal.tarifas.add(individualNenhuma);
        instance = new TarifaBLL(dal);
    }

    @AfterEach
    public void tearDown() {
        dal = null;
        instance = null;
    }

    // devolve a mensagem de erro ao alterar o preço, ou null se o preço foi alterado
    private String mensagemAoAlterar(Perfil perfil, TipoQuarto tipo, Vista vista, BigDecimal novoValor) {
        try {
            instance.alterarPreco(perfil, tipo, vista, novoValor);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testListar() {
        System.out.println("listar");
        List<Tarifa> expResult = new ArrayList<>();
        expResult.add(new Tarifa(TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("100.00")));
        expResult.add(new Tarifa(TipoQuarto.INDIVIDUAL, Vista.NENHUMA, new BigDecimal("50.00")));
        List<Tarifa> result = instance.listar();
        assertEquals(expResult, result);
    }

    @Test
    public void testAlterarPreco() {
        System.out.println("alterarPreco");
        Tarifa expResult = new Tarifa(TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("120.50"));
        instance.alterarPreco(Perfil.GESTOR, duploMar.getTipo(), duploMar.getVista(), new BigDecimal("120.50"));
        Tarifa result = dal.tarifas.get(0);
        assertEquals(expResult, result);
        assertEquals(1, dal.alteracoes);
    }

    @Test
    public void testAlterarPrecoNaoMexeNasOutrasTarifas() {
        System.out.println("alterarPreco (as outras tarifas ficam iguais)");
        Tarifa expResult = new Tarifa(TipoQuarto.INDIVIDUAL, Vista.NENHUMA, new BigDecimal("50.00"));
        instance.alterarPreco(Perfil.GESTOR, duploMar.getTipo(), duploMar.getVista(), new BigDecimal("120.50"));
        Tarifa result = dal.tarifas.get(1);
        assertEquals(expResult, result);
    }

    @Test
    public void testAlterarPrecoComoRececionista() {
        System.out.println("alterarPreco (rececionista não pode)");
        String expResult = "Só o gestor pode alterar tarifas";
        String result = mensagemAoAlterar(Perfil.RECECIONISTA, duploMar.getTipo(), duploMar.getVista(),
                new BigDecimal("100"));
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testAlterarPrecoSemPerfil() {
        System.out.println("alterarPreco (sem perfil)");
        String expResult = "Só o gestor pode alterar tarifas";
        String result = mensagemAoAlterar(null, duploMar.getTipo(), duploMar.getVista(), new BigDecimal("100"));
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testAlterarPrecoNegativo() {
        System.out.println("alterarPreco (preço negativo)");
        String expResult = "O preço tem de ser maior que zero";
        String result = mensagemAoAlterar(Perfil.GESTOR, duploMar.getTipo(), duploMar.getVista(),
                new BigDecimal("-5"));
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testAlterarPrecoZero() {
        System.out.println("alterarPreco (preço zero)");
        String expResult = "O preço tem de ser maior que zero";
        String result = mensagemAoAlterar(Perfil.GESTOR, duploMar.getTipo(), duploMar.getVista(), BigDecimal.ZERO);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testAlterarPrecoVazio() {
        System.out.println("alterarPreco (sem preço)");
        String expResult = "O preço é obrigatório";
        String result = mensagemAoAlterar(Perfil.GESTOR, duploMar.getTipo(), duploMar.getVista(), null);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testAlterarPrecoTarifaInexistente() {
        System.out.println("alterarPreco (a tarifa não existe)");
        String expResult = "A tarifa não existe";
        String result = mensagemAoAlterar(Perfil.GESTOR, TipoQuarto.FAMILIA, Vista.MONTANHA, new BigDecimal("80"));
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }
}
