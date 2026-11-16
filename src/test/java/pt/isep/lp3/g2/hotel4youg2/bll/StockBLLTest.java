package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IStockDAL;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.MovimentoStock;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoMovimento;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class StockBLLTest {

    private static class StockDALFalsa implements IStockDAL {
        private final List<MaterialStock> materiais = new ArrayList<>();
        private int emStock = 10;
        private int movimentos;
        private int ultimoFuncionario;

        @Override
        public List<MaterialStock> listar() {
            return materiais;
        }

        @Override
        public boolean registarSaida(int materialId, int quantidade, int funcionarioId) {
            if (quantidade > emStock) {
                return false;
            }
            emStock -= quantidade;
            movimentos++;
            ultimoFuncionario = funcionarioId;
            return true;
        }

        private final List<MovimentoStock> historico = new ArrayList<>();
        private int materialPedido;

        @Override
        public List<MovimentoStock> listarMovimentos(int materialId) {
            materialPedido = materialId;
            return historico;
        }
    }

    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);
    private static final MaterialStock TOALHAS = new MaterialStock(4, "M-1", "Toalhas", "Lavandaria Norte", 10);

    private StockDALFalsa stockDAL;
    private StockBLL instance;

    @BeforeEach
    public void setUp() {
        stockDAL = new StockDALFalsa();
        stockDAL.materiais.add(TOALHAS);
        instance = new StockBLL(stockDAL);
    }

    @AfterEach
    public void tearDown() {
        stockDAL.materiais.clear();
    }

    private void esperarErro(MaterialStock material, String quantidade, String expResult) {
        try {
            instance.registarSaida(RECECAO, material, quantidade);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testListar() {
        System.out.println("listar");
        List<MaterialStock> expResult = List.of(TOALHAS);
        List<MaterialStock> result = instance.listar(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarSemSessao() {
        System.out.println("listar (sem sessão iniciada)");
        String expResult = "Entre na aplicação para registar saídas de stock.";
        try {
            instance.listar(null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testRegistarSaida() {
        System.out.println("registarSaida (tira do stock)");
        instance.registarSaida(RECECAO, TOALHAS, "3");
        int expResult = 7;
        int result = stockDAL.emStock;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaGuardaOFuncionario() {
        System.out.println("registarSaida (fica registado o funcionário)");
        instance.registarSaida(RECECAO, TOALHAS, "3");
        int expResult = 7;
        int result = stockDAL.ultimoFuncionario;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAceitaEspacos() {
        System.out.println("registarSaida (espaços na quantidade)");
        instance.registarSaida(RECECAO, TOALHAS, " 4 ");
        int expResult = 6;
        int result = stockDAL.emStock;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaDeTudo() {
        System.out.println("registarSaida (pode tirar tudo o que existe)");
        instance.registarSaida(RECECAO, TOALHAS, "10");
        int expResult = 0;
        int result = stockDAL.emStock;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAMais() {
        System.out.println("registarSaida (mais do que existe)");
        esperarErro(TOALHAS, "11", "Não é possível retirar mais do que existe em stock.");
    }

    @Test
    public void testRegistarSaidaSemMaterial() {
        System.out.println("registarSaida (sem material)");
        esperarErro(null, "3", "Escolha o material na tabela.");
    }

    @Test
    public void testRegistarSaidaSemQuantidade() {
        System.out.println("registarSaida (sem quantidade)");
        esperarErro(TOALHAS, "  ", "Indique a quantidade.");
    }

    @Test
    public void testRegistarSaidaQuantidadeNula() {
        System.out.println("registarSaida (quantidade nula)");
        esperarErro(TOALHAS, null, "Indique a quantidade.");
    }

    @Test
    public void testRegistarSaidaQuantidadeZero() {
        System.out.println("registarSaida (quantidade zero)");
        esperarErro(TOALHAS, "0", "A quantidade tem de ser um número inteiro maior que zero.");
    }

    @Test
    public void testRegistarSaidaQuantidadeNegativa() {
        System.out.println("registarSaida (quantidade negativa)");
        esperarErro(TOALHAS, "-3", "A quantidade tem de ser um número inteiro maior que zero.");
    }

    @Test
    public void testRegistarSaidaQuantidadeComLetras() {
        System.out.println("registarSaida (quantidade com letras)");
        esperarErro(TOALHAS, "abc", "A quantidade tem de ser um número inteiro maior que zero.");
    }

    @Test
    public void testRegistarSaidaSemSessao() {
        System.out.println("registarSaida (sem sessão iniciada)");
        String expResult = "Entre na aplicação para registar saídas de stock.";
        try {
            instance.registarSaida(null, TOALHAS, "3");
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(0, stockDAL.movimentos);
        }
    }

    private static final MaterialStock CHAMPO = new MaterialStock(5, "M-2", "Champô 200ml", "Rituals", 30);
    private static final MovimentoStock SAIDA_RECENTE = new MovimentoStock(TipoMovimento.SAIDA, 4, 3, "Ana Silva",
            LocalDateTime.of(2026, 11, 16, 10, 0));
    private static final MovimentoStock ENTRADA = new MovimentoStock(TipoMovimento.ENTRADA, 4, 20, "Rui Costa",
            LocalDateTime.of(2026, 11, 10, 9, 30));
    private static final MovimentoStock SAIDA_ANTIGA = new MovimentoStock(TipoMovimento.SAIDA, 4, 7, "Ana Silva",
            LocalDateTime.of(2026, 11, 5, 18, 15));

    private void juntarMovimentos() {
        stockDAL.historico.add(SAIDA_RECENTE);
        stockDAL.historico.add(ENTRADA);
        stockDAL.historico.add(SAIDA_ANTIGA);
    }

    @Test
    public void testPesquisarSemTermo() {
        System.out.println("pesquisar (sem texto mostra todos)");
        stockDAL.materiais.add(CHAMPO);
        List<MaterialStock> expResult = List.of(TOALHAS, CHAMPO);
        List<MaterialStock> result = instance.pesquisar(RECECAO, "");
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarTermoNulo() {
        System.out.println("pesquisar (texto nulo mostra todos)");
        stockDAL.materiais.add(CHAMPO);
        int expResult = 2;
        int result = instance.pesquisar(RECECAO, null).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarPeloMaterial() {
        System.out.println("pesquisar (pelo nome do material, sem ligar a maiúsculas)");
        stockDAL.materiais.add(CHAMPO);
        List<MaterialStock> expResult = List.of(CHAMPO);
        List<MaterialStock> result = instance.pesquisar(RECECAO, "  CHAMPÔ ");
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarPeloCodigo() {
        System.out.println("pesquisar (pelo código)");
        stockDAL.materiais.add(CHAMPO);
        List<MaterialStock> expResult = List.of(TOALHAS);
        List<MaterialStock> result = instance.pesquisar(RECECAO, "m-1");
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarPeloFornecedor() {
        System.out.println("pesquisar (pelo fornecedor)");
        stockDAL.materiais.add(CHAMPO);
        List<MaterialStock> expResult = List.of(TOALHAS);
        List<MaterialStock> result = instance.pesquisar(RECECAO, "lavandaria");
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarSemResultados() {
        System.out.println("pesquisar (nenhum material com esse nome)");
        List<MaterialStock> expResult = new ArrayList<>();
        List<MaterialStock> result = instance.pesquisar(RECECAO, "almofadas");
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarSemSessao() {
        System.out.println("pesquisar (sem sessão iniciada)");
        String expResult = "Entre na aplicação para consultar o stock.";
        try {
            instance.pesquisar(null, "");
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testHistorico() {
        System.out.println("historico (entradas e saídas, do mais recente para o mais antigo)");
        juntarMovimentos();
        List<MovimentoStock> expResult = List.of(SAIDA_RECENTE, ENTRADA, SAIDA_ANTIGA);
        List<MovimentoStock> result = instance.historico(RECECAO, TOALHAS, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testHistoricoDoMaterialEscolhido() {
        System.out.println("historico (pede os movimentos do material escolhido)");
        juntarMovimentos();
        instance.historico(RECECAO, TOALHAS, null);
        int expResult = TOALHAS.getMaterialId();
        int result = stockDAL.materialPedido;
        assertEquals(expResult, result);
    }

    @Test
    public void testHistoricoSoEntradas() {
        System.out.println("historico (só as entradas)");
        juntarMovimentos();
        List<MovimentoStock> expResult = List.of(ENTRADA);
        List<MovimentoStock> result = instance.historico(RECECAO, TOALHAS, TipoMovimento.ENTRADA);
        assertEquals(expResult, result);
    }

    @Test
    public void testHistoricoSoSaidas() {
        System.out.println("historico (só as saídas)");
        juntarMovimentos();
        List<MovimentoStock> expResult = List.of(SAIDA_RECENTE, SAIDA_ANTIGA);
        List<MovimentoStock> result = instance.historico(RECECAO, TOALHAS, TipoMovimento.SAIDA);
        assertEquals(expResult, result);
    }

    @Test
    public void testHistoricoSemMovimentos() {
        System.out.println("historico (material sem movimentos)");
        List<MovimentoStock> expResult = new ArrayList<>();
        List<MovimentoStock> result = instance.historico(RECECAO, TOALHAS, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testHistoricoSemMaterial() {
        System.out.println("historico (sem material escolhido)");
        try {
            instance.historico(RECECAO, null, null);
            fail("Devia ter pedido o material");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha o material na tabela.", expected.getMessage());
        }
    }

    @Test
    public void testHistoricoSemSessao() {
        System.out.println("historico (sem sessão iniciada)");
        try {
            instance.historico(null, TOALHAS, null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals("Entre na aplicação para consultar o stock.", expected.getMessage());
        }
    }
}
