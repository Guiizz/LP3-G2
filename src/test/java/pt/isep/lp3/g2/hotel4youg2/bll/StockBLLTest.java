package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IStockDAL;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.MovimentoStock;
import pt.isep.lp3.g2.hotel4youg2.model.SaidaStock;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.MotivoSaida;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoMovimento;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

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
        private TipoSaida ultimoTipo;
        private MotivoSaida ultimoMotivo;
        private String ultimoTexto;
        private final List<SaidaStock> porDevolver = new ArrayList<>();
        private int ultimoQueRecebeu;

        @Override
        public List<MaterialStock> listar() {
            return materiais;
        }

        @Override
        public boolean registarSaida(int materialId, int quantidade, int funcionarioId, TipoSaida tipo,
                                     MotivoSaida motivo, String descricaoMotivo) {
            if (quantidade > emStock) {
                return false;
            }
            emStock -= quantidade;
            movimentos++;
            ultimoFuncionario = funcionarioId;
            ultimoTipo = tipo;
            ultimoMotivo = motivo;
            ultimoTexto = descricaoMotivo;
            return true;
        }

        @Override
        public List<SaidaStock> listarPorDevolver() {
            return porDevolver;
        }

        @Override
        public boolean devolver(int saidaId, int funcionarioId) {
            for (SaidaStock saida : porDevolver) {
                if (saida.getId() == saidaId) {
                    porDevolver.remove(saida);
                    emStock += saida.getQuantidade();
                    ultimoQueRecebeu = funcionarioId;
                    return true;
                }
            }
            return false;
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

    @Test
    public void testListar() {
        System.out.println("listar");
        List<MaterialStock> expResult = List.of(TOALHAS);
        List<MaterialStock> result = instance.listar(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaida() {
        System.out.println("registarSaida (tira do stock)");
        instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = 7;
        int result = stockDAL.emStock;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaGuardaOFuncionario() {
        System.out.println("registarSaida (fica registado o funcionário)");
        instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = 7;
        int result = stockDAL.ultimoFuncionario;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAceitaEspacos() {
        System.out.println("registarSaida (espaços na quantidade)");
        instance.registarSaida(RECECAO, TOALHAS, " 4 ", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = 6;
        int result = stockDAL.emStock;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaDeTudo() {
        System.out.println("registarSaida (pode tirar tudo o que existe)");
        instance.registarSaida(RECECAO, TOALHAS, "10", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = 0;
        int result = stockDAL.emStock;
        assertEquals(expResult, result);
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

    private static final MaterialStock CHAMPO = new MaterialStock(5, "M-2", "Champô 200ml", "Rituals", 30);
    private static final MovimentoStock SAIDA_RECENTE = new MovimentoStock(TipoMovimento.SAIDA_DEFINITIVA, 4, 3, "Ana Silva",
            LocalDateTime.of(2026, 11, 16, 10, 0));
    private static final MovimentoStock ENTRADA = new MovimentoStock(TipoMovimento.ENTRADA, 4, 20, "Rui Costa",
            LocalDateTime.of(2026, 11, 10, 9, 30));
    private static final MovimentoStock SAIDA_ANTIGA = new MovimentoStock(TipoMovimento.SAIDA_DEFINITIVA, 4, 7, "Ana Silva",
            LocalDateTime.of(2026, 11, 5, 18, 15));

    private void juntarMovimentos() {
        stockDAL.historico.add(SAIDA_RECENTE);
        stockDAL.historico.add(ENTRADA);
        stockDAL.historico.add(SAIDA_ANTIGA);
    }

    @Test
    public void testHistoricoSoSaidas() {
        System.out.println("historico (só as saídas definitivas)");
        juntarMovimentos();
        List<MovimentoStock> expResult = List.of(SAIDA_RECENTE, SAIDA_ANTIGA);
        List<MovimentoStock> result = instance.historico(RECECAO, TOALHAS, TipoMovimento.SAIDA_DEFINITIVA);
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
    public void testRegistarSaidaTemporaria() {
        System.out.println("registarSaida (temporária, com o texto do motivo)");
        instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.TEMPORARIA, null, "Reparação");
        TipoSaida expResult = TipoSaida.TEMPORARIA;
        TipoSaida result = stockDAL.ultimoTipo;
        assertEquals(expResult, result);
        assertEquals("Reparação", stockDAL.ultimoTexto);
        assertEquals(7, stockDAL.emStock);
    }

    @Test
    public void testRegistarSaidaTemporariaSemTexto() {
        System.out.println("registarSaida (temporária sem motivo também pode)");
        instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.TEMPORARIA, null, "  ");
        String expResult = null;
        String result = stockDAL.ultimoTexto;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaTemporariaIgnoraOMotivoDaLista() {
        System.out.println("registarSaida (temporária não guarda o motivo da lista)");
        instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.TEMPORARIA, MotivoSaida.ROUBADO, null);
        MotivoSaida expResult = null;
        MotivoSaida result = stockDAL.ultimoMotivo;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaDefinitiva() {
        System.out.println("registarSaida (definitiva com o motivo)");
        instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.DEFINITIVA, MotivoSaida.DEVOLVIDO_FORNECEDOR, null);
        TipoSaida expResult = TipoSaida.DEFINITIVA;
        TipoSaida result = stockDAL.ultimoTipo;
        assertEquals(expResult, result);
        assertEquals(MotivoSaida.DEVOLVIDO_FORNECEDOR, stockDAL.ultimoMotivo);
    }

    @Test
    public void testRegistarSaidaDefinitivaOutroComTexto() {
        System.out.println("registarSaida (definitiva com \"Outro\" e o motivo escrito)");
        instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.DEFINITIVA, MotivoSaida.OUTRO, "  caiu ao chão ");
        MotivoSaida expResult = MotivoSaida.OUTRO;
        MotivoSaida result = stockDAL.ultimoMotivo;
        assertEquals(expResult, result);
        assertEquals("caiu ao chão", stockDAL.ultimoTexto);
    }

    @Test
    public void testRegistarSaidaMotivoComOLimiteDeCaracteres() {
        System.out.println("registarSaida (motivo com 200 caracteres)");
        String texto = "a".repeat(StockBLL.TAMANHO_MAXIMO_MOTIVO);
        instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.TEMPORARIA, null, texto);
        String expResult = texto;
        String result = stockDAL.ultimoTexto;
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorDevolver() {
        System.out.println("listarPorDevolver");
        stockDAL.porDevolver.add(SECADORES);
        List<SaidaStock> expResult = List.of(SECADORES);
        List<SaidaStock> result = instance.listarPorDevolver(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testDevolver() {
        System.out.println("devolver (o material volta ao stock)");
        stockDAL.porDevolver.add(SECADORES);
        instance.devolver(RECECAO, SECADORES);
        int expResult = 12;
        int result = stockDAL.emStock;
        assertEquals(expResult, result);
        assertEquals(0, stockDAL.porDevolver.size());
        assertEquals(7, stockDAL.ultimoQueRecebeu);
    }

    @Test
    public void testHistoricoSoTemporarias() {
        System.out.println("historico (só as saídas temporárias)");
        MovimentoStock temporaria = new MovimentoStock(TipoMovimento.SAIDA_TEMPORARIA, 4, 2, "Ana Silva", QUANDO,
                "Reparação");
        stockDAL.historico.add(temporaria);
        stockDAL.historico.add(SAIDA_RECENTE);
        List<MovimentoStock> expResult = List.of(temporaria);
        List<MovimentoStock> result = instance.historico(RECECAO, TOALHAS, TipoMovimento.SAIDA_TEMPORARIA);
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
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testRegistarSaidaAMais() {
        System.out.println("registarSaida (mais do que existe)");
        String expResult = "Não é possível retirar mais do que existe em stock.";
        try {
            instance.registarSaida(RECECAO, TOALHAS, "11", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testRegistarSaidaSemMaterial() {
        System.out.println("registarSaida (sem material)");
        String expResult = "Escolha o material na tabela.";
        try {
            instance.registarSaida(RECECAO, null, "3", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    private static final LocalDateTime QUANDO = LocalDateTime.of(2026, 11, 16, 9, 5);
    private static final SaidaStock SECADORES = new SaidaStock(7, 4, "X569P", "Secador", 2, "Reparação", "Ana Silva",
            QUANDO);

    @Test
    public void testRegistarSaidaSemQuantidade() {
        System.out.println("registarSaida (sem quantidade)");
        String expResult = "Indique a quantidade.";
        try {
            instance.registarSaida(RECECAO, TOALHAS, "  ", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testRegistarSaidaQuantidadeNula() {
        System.out.println("registarSaida (quantidade nula)");
        String expResult = "Indique a quantidade.";
        try {
            instance.registarSaida(RECECAO, TOALHAS, null, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testRegistarSaidaQuantidadeZero() {
        System.out.println("registarSaida (quantidade zero)");
        String expResult = "A quantidade tem de ser um número inteiro maior que zero.";
        try {
            instance.registarSaida(RECECAO, TOALHAS, "0", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testRegistarSaidaQuantidadeNegativa() {
        System.out.println("registarSaida (quantidade negativa)");
        String expResult = "A quantidade tem de ser um número inteiro maior que zero.";
        try {
            instance.registarSaida(RECECAO, TOALHAS, "-3", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testRegistarSaidaQuantidadeComLetras() {
        System.out.println("registarSaida (quantidade com letras)");
        String expResult = "A quantidade tem de ser um número inteiro maior que zero.";
        try {
            instance.registarSaida(RECECAO, TOALHAS, "abc", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testRegistarSaidaSemSessao() {
        System.out.println("registarSaida (sem sessão iniciada)");
        String expResult = "Entre na aplicação para registar saídas de stock.";
        try {
            instance.registarSaida(null, TOALHAS, "3", TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testPesquisarSemSessao() {
        System.out.println("pesquisar (sem sessão iniciada)");
        String expResult = "Entre na aplicação para consultar o stock.";
        try {
            instance.pesquisar(null, "");
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testHistoricoSemMaterial() {
        System.out.println("historico (sem material escolhido)");
        try {
            instance.historico(RECECAO, null, null);
            fail("Devia ter pedido o material");
        } catch (IllegalArgumentException expected) {
            String expResult = "Escolha o material na tabela.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testHistoricoSemSessao() {
        System.out.println("historico (sem sessão iniciada)");
        try {
            instance.historico(null, TOALHAS, null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            String expResult = "Entre na aplicação para consultar o stock.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testRegistarSaidaSemTipo() {
        System.out.println("registarSaida (sem tipo)");
        String expResult = "Escolha o tipo de saída: temporária ou definitiva.";
        try {
            instance.registarSaida(RECECAO, TOALHAS, "3", null, MotivoSaida.ESTRAGADO, null);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testRegistarSaidaDefinitivaSemMotivo() {
        System.out.println("registarSaida (definitiva sem motivo)");
        String expResult = "Escolha o motivo da saída definitiva.";
        try {
            instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.DEFINITIVA, null, null);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testRegistarSaidaDefinitivaOutroSemTexto() {
        System.out.println("registarSaida (\"Outro\" sem o motivo escrito)");
        String expResult = "Escreva o motivo da saída.";
        try {
            instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.DEFINITIVA, MotivoSaida.OUTRO, "   ");
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testRegistarSaidaMotivoComMuitosCaracteres() {
        System.out.println("registarSaida (motivo com mais de 200 caracteres)");
        String expResult = "O motivo pode ter no máximo 200 caracteres.";
        try {
            instance.registarSaida(RECECAO, TOALHAS, "3", TipoSaida.DEFINITIVA, MotivoSaida.OUTRO, "a".repeat(StockBLL.TAMANHO_MAXIMO_MOTIVO + 1));
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testListarPorDevolverSemSessao() {
        System.out.println("listarPorDevolver (sem sessão iniciada)");
        try {
            instance.listarPorDevolver(null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            String expResult = "Entre na aplicação para consultar o stock.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testDevolverJaDevolvida() {
        System.out.println("devolver (já tinha sido devolvida)");
        try {
            instance.devolver(RECECAO, SECADORES);
            fail("Devia ter recusado a devolução");
        } catch (IllegalArgumentException expected) {
            String expResult = "Esta saída já foi devolvida.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(10, stockDAL.emStock);
        }
    }

    @Test
    public void testDevolverSemSaida() {
        System.out.println("devolver (sem saída escolhida)");
        try {
            instance.devolver(RECECAO, null);
            fail("Devia ter pedido a saída");
        } catch (IllegalArgumentException expected) {
            String expResult = "Escolha a saída na tabela.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testDevolverSemSessao() {
        System.out.println("devolver (sem sessão iniciada)");
        stockDAL.porDevolver.add(SECADORES);
        try {
            instance.devolver(null, SECADORES);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            String expResult = "Entre na aplicação para registar saídas de stock.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(1, stockDAL.porDevolver.size());
        }
    }
}
