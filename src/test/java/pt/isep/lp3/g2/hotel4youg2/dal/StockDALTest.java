package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.MovimentoStock;
import pt.isep.lp3.g2.hotel4youg2.model.SaidaStock;
import pt.isep.lp3.g2.hotel4youg2.model.enums.MotivoSaida;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoMovimento;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StockDALTest {

    private StockDAL instance;
    private String codigo;
    private int fornecedorId;
    private int materialId;
    private int funcionarioId;

    @BeforeAll
    public static void limparRestosDosTestes() {
        RestosDosTestes.limpar();
    }

    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabelas = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('Stock') IS NOT NULL "
                    + "AND OBJECT_ID('MovimentoStock') IS NOT NULL THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabelas.get(0) == 1, "Falta correr o 16_movimentos_stock.sql");
            List<Integer> tipoSaida = ConexaoBD.select("SELECT CASE WHEN COL_LENGTH('MovimentoStock', 'tipo') "
                    + "IS NOT NULL THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tipoSaida.get(0) == 1, "Falta correr o 24_tipo_saida_materiais.sql");
            List<Integer> funcionarios = ConexaoBD.select("SELECT TOP 1 pessoaId FROM Funcionario", new InteiroMapper());
            Assumptions.assumeTrue(!funcionarios.isEmpty(), "Faltam funcionários na BD");
            funcionarioId = funcionarios.get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new StockDAL();
        codigo = "TS" + Math.floorMod(System.nanoTime(), 100_000_000L);
        fornecedorId = ConexaoBD.select("INSERT INTO Fornecedor (codigo, nome) OUTPUT INSERTED.id VALUES (?, ?)",
                new InteiroMapper(), codigo, "Fornecedor " + codigo).get(0);
        materialId = ConexaoBD.select("INSERT INTO Material (fornecedorId, codigo, descricao) OUTPUT INSERTED.id "
                + "VALUES (?, ?, ?)", new InteiroMapper(), fornecedorId, "M-1", "Material " + codigo).get(0);
        ConexaoBD.executar("INSERT INTO Stock (materialId, quantidade) VALUES (?, 10)", materialId);
    }

    @AfterEach
    public void tearDown() {
        if (materialId > 0) {
            ConexaoBD.executar("DELETE FROM MovimentoStock WHERE materialId = ?", materialId);
            ConexaoBD.executar("DELETE FROM Stock WHERE materialId = ?", materialId);
            ConexaoBD.executar("DELETE FROM Entrega WHERE fornecedorId = ?", fornecedorId);
            ConexaoBD.executar("DELETE FROM Material WHERE id = ?", materialId);
        }
        if (fornecedorId > 0) {
            ConexaoBD.executar("DELETE FROM Fornecedor WHERE id = ?", fornecedorId);
        }
    }

    private int quantidadeEmStock() {
        return ConexaoBD.select("SELECT quantidade FROM Stock WHERE materialId = ?", new InteiroMapper(),
                materialId).get(0);
    }

    private int contarMovimentos() {
        return ConexaoBD.select("SELECT COUNT(*) FROM MovimentoStock WHERE materialId = ?", new InteiroMapper(),
                materialId).get(0);
    }

    @Test
    public void testListarTrazOMaterial() {
        System.out.println("listar (material, fornecedor e quantidade)");
        MaterialStock expResult = new MaterialStock(materialId, "M-1", "Material " + codigo, "Fornecedor " + codigo, 10);
        MaterialStock result = null;
        for (MaterialStock material : instance.listar()) {
            if (material.getMaterialId() == materialId) {
                result = material;
            }
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaida() {
        System.out.println("registarSaida");
        boolean expResult = true;
        boolean result = instance.registarSaida(materialId, 3, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaTiraDoStock() {
        System.out.println("registarSaida (tira do stock)");
        instance.registarSaida(materialId, 3, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = 7;
        int result = quantidadeEmStock();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaGuardaOFuncionario() {
        System.out.println("registarSaida (fica registado o funcionário)");
        instance.registarSaida(materialId, 3, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = funcionarioId;
        int result = ConexaoBD.select("SELECT funcionarioId FROM MovimentoStock WHERE materialId = ? AND quantidade = 3",
                new InteiroMapper(), materialId).get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaGuardaAData() {
        System.out.println("registarSaida (data posta pela BD)");
        instance.registarSaida(materialId, 3, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = 1;
        int result = ConexaoBD.select("SELECT CASE WHEN ABS(DATEDIFF(MINUTE, registadoEm, SYSDATETIME())) < 5 "
                + "THEN 1 ELSE 0 END FROM MovimentoStock WHERE materialId = ?", new InteiroMapper(), materialId).get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaDeTudo() {
        System.out.println("registarSaida (pode tirar tudo o que existe)");
        instance.registarSaida(materialId, 10, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = 0;
        int result = quantidadeEmStock();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAMaisDevolveFalso() {
        System.out.println("registarSaida (mais do que existe)");
        boolean expResult = false;
        boolean result = instance.registarSaida(materialId, 11, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAMaisNaoMudaOStock() {
        System.out.println("registarSaida (mais do que existe não muda o stock)");
        instance.registarSaida(materialId, 11, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = 10;
        int result = quantidadeEmStock();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAMaisNaoRegistaMovimento() {
        System.out.println("registarSaida (mais do que existe não fica registado)");
        instance.registarSaida(materialId, 11, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = 0;
        int result = contarMovimentos();
        assertEquals(expResult, result);
    }

    private void registarEntrega(String estado, int unidades) {
        int entregaId = ConexaoBD.select("INSERT INTO Entrega (fornecedorId, numeroEncomenda, dataEncomenda, total, "
                + "registadaPor, estado, decididaPor, decididaEm) OUTPUT INSERTED.id "
                + "VALUES (?, ?, CAST(SYSDATETIME() AS DATE), 0, ?, ?, ?, DATEADD(HOUR, -1, SYSDATETIME()))",
                new InteiroMapper(), fornecedorId, "E" + System.nanoTime() % 1_000_000_000L, funcionarioId, estado,
                funcionarioId).get(0);
        ConexaoBD.executar("INSERT INTO LinhaEntrega (entregaId, numeroLinha, materialId, quantidade, unidade, unidades, "
                + "precoUnitario, valorSemIva, taxaIva, valorIva, total) VALUES (?, 1, ?, ?, 'UNIT', ?, 0, 0, 23, 0, 0)",
                entregaId, materialId, unidades, unidades);
    }

    private MaterialStock oMaterialDoTeste() {
        for (MaterialStock material : instance.listar()) {
            if (material.getMaterialId() == materialId) {
                return material;
            }
        }
        return null;
    }

    private String nomeDoFuncionario() {
        return ConexaoBD.select("SELECT nome FROM Pessoa WHERE id = ?", new TextoMapper(), funcionarioId).get(0);
    }

    private static class TextoMapper implements RowMapper<String> {
        @Override
        public String map(ResultSet rs) throws SQLException {
            return rs.getString(1);
        }
    }

    @Test
    public void testListarTrazOQueEntrou() {
        System.out.println("listar (o que entrou nas entregas aceites)");
        registarEntrega("ACEITE", 5);
        int expResult = 5;
        int result = oMaterialDoTeste().getEntrou();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarTrazOQueSaiu() {
        System.out.println("listar (o que saiu)");
        instance.registarSaida(materialId, 3, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        instance.registarSaida(materialId, 2, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int expResult = 5;
        int result = oMaterialDoTeste().getSaiu();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarNaoContaEntregasRecusadas() {
        System.out.println("listar (as entregas recusadas não contam como entrada)");
        registarEntrega("RECUSADA", 5);
        int expResult = 0;
        int result = oMaterialDoTeste().getEntrou();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarMovimentos() {
        System.out.println("listarMovimentos (do mais recente para o mais antigo)");
        registarEntrega("ACEITE", 5);
        instance.registarSaida(materialId, 3, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        List<MovimentoStock> movimentos = instance.listarMovimentos(materialId);
        String expResult = "[Saída definitiva 3, Entrada 5]";
        List<String> obtido = new ArrayList<>();
        for (MovimentoStock movimento : movimentos) {
            obtido.add(movimento.getTipo() + " " + movimento.getQuantidade());
        }
        String result = "" + obtido.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarMovimentosTrazOFuncionario() {
        System.out.println("listarMovimentos (com o nome do funcionário)");
        registarEntrega("ACEITE", 5);
        String expResult = nomeDoFuncionario();
        String result = instance.listarMovimentos(materialId).get(0).getFuncionario();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarMovimentosSemRecusadas() {
        System.out.println("listarMovimentos (sem as entregas recusadas)");
        registarEntrega("RECUSADA", 5);
        int expResult = 0;
        int result = instance.listarMovimentos(materialId).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarMovimentosDeOutroMaterial() {
        System.out.println("listarMovimentos (material sem movimentos)");
        int expResult = 0;
        int result = instance.listarMovimentos(-1).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaFalhaDesfazOStock() {
        System.out.println("registarSaida (se o movimento falhar, o stock volta atrás)");
        try {
            instance.registarSaida(materialId, 3, -1, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        } catch (DataAccessException esperado) {
            System.out.println("o movimento foi recusado pela BD");
        }
        int expResult = 10;
        int result = quantidadeEmStock();
        assertEquals(expResult, result);
    }

    private SaidaStock aSaidaPorDevolver() {
        for (SaidaStock saida : instance.listarPorDevolver()) {
            if (saida.getMaterialId() == materialId) {
                return saida;
            }
        }
        return null;
    }

    private String textoGuardado(String coluna) {
        return ConexaoBD.select("SELECT " + coluna + " FROM MovimentoStock WHERE materialId = ?", new TextoMapper(),
                materialId).get(0);
    }

    @Test
    public void testRegistarSaidaGuardaOTipoEOMotivo() {
        System.out.println("registarSaida (guarda o tipo, o motivo e o texto)");
        instance.registarSaida(materialId, 3, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.OUTRO, "caiu ao chão");
        String expResult = "DEFINITIVA / OUTRO / caiu ao chão";
        String result = textoGuardado("tipo")
                + " / "
                + textoGuardado("motivo")
                + " / "
                + textoGuardado("descricaoMotivo");
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaTemporariaSemMotivo() {
        System.out.println("registarSaida (temporária, sem motivo da lista)");
        instance.registarSaida(materialId, 2, funcionarioId, TipoSaida.TEMPORARIA, null, "Reparação");
        String expResult = "TEMPORARIA / null / 8";
        String result = textoGuardado("tipo") + " / " + textoGuardado("motivo") + " / " + quantidadeEmStock();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorDevolver() {
        System.out.println("listarPorDevolver (a temporária aparece com o motivo e o funcionário)");
        instance.registarSaida(materialId, 2, funcionarioId, TipoSaida.TEMPORARIA, null, "Reparação");
        SaidaStock obtido = aSaidaPorDevolver();
        List<Object> expResult = Arrays.asList(2, "M-1", "Reparação", nomeDoFuncionario());
        List<Object> result = Arrays.asList(obtido.getQuantidade(),
                obtido.getCodigo(),
                obtido.getMotivo(),
                obtido.getFuncionario());
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorDevolverSemDefinitivas() {
        System.out.println("listarPorDevolver (as definitivas não aparecem)");
        instance.registarSaida(materialId, 2, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ROUBADO, null);
        SaidaStock expResult = null;
        SaidaStock result = aSaidaPorDevolver();
        assertEquals(expResult, result);
    }

    @Test
    public void testDevolver() {
        System.out.println("devolver (o material volta ao stock)");
        instance.registarSaida(materialId, 2, funcionarioId, TipoSaida.TEMPORARIA, null, "Reparação");
        List<Object> expResult = Arrays.asList(true, 10, null);
        boolean devolvida = instance.devolver(aSaidaPorDevolver().getId(), funcionarioId);
        List<Object> result = Arrays.asList(devolvida, quantidadeEmStock(), aSaidaPorDevolver());
        assertEquals(expResult, result);
    }

    @Test
    public void testDevolverDuasVezes() {
        System.out.println("devolver (a segunda vez não soma outra vez ao stock)");
        instance.registarSaida(materialId, 2, funcionarioId, TipoSaida.TEMPORARIA, null, "Reparação");
        int saidaId = aSaidaPorDevolver().getId();
        instance.devolver(saidaId, funcionarioId);
        boolean obtido = instance.devolver(saidaId, funcionarioId);
        String expResult = "false / 10";
        String result = obtido + " / " + quantidadeEmStock();
        assertEquals(expResult, result);
    }

    @Test
    public void testDevolverDefinitiva() {
        System.out.println("devolver (uma saída definitiva não volta)");
        instance.registarSaida(materialId, 2, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        int saidaId = ConexaoBD.select("SELECT id FROM MovimentoStock WHERE materialId = ?", new InteiroMapper(),
                materialId).get(0);
        boolean obtido = instance.devolver(saidaId, funcionarioId);
        String expResult = "false / 8";
        String result = obtido + " / " + quantidadeEmStock();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarNaoContaAsDevolvidas() {
        System.out.println("listar (uma saída temporária devolvida já não conta como saída)");
        instance.registarSaida(materialId, 2, funcionarioId, TipoSaida.TEMPORARIA, null, "Reparação");
        instance.registarSaida(materialId, 3, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ESTRAGADO, null);
        instance.devolver(aSaidaPorDevolver().getId(), funcionarioId);
        int expResult = 3;
        int result = oMaterialDoTeste().getSaiu();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarMovimentosComTiposEDevolucao() {
        System.out.println("listarMovimentos (saída temporária, devolução e definitiva em separado)");
        instance.registarSaida(materialId, 2, funcionarioId, TipoSaida.TEMPORARIA, null, "Reparação");
        instance.devolver(aSaidaPorDevolver().getId(), funcionarioId);
        instance.registarSaida(materialId, 3, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.ROUBADO, null);
        List<TipoMovimento> obtido = new ArrayList<>();
        for (MovimentoStock movimento : instance.listarMovimentos(materialId)) {
            obtido.add(movimento.getTipo());
        }
        String expResult = "3 / true / true / true";
        String result = obtido.size()
                + " / "
                + obtido.contains(TipoMovimento.SAIDA_TEMPORARIA)
                + " / "
                + obtido.contains(TipoMovimento.DEVOLUCAO)
                + " / "
                + obtido.contains(TipoMovimento.SAIDA_DEFINITIVA);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarMovimentosTrazOMotivo() {
        System.out.println("listarMovimentos (com o motivo da saída)");
        instance.registarSaida(materialId, 3, funcionarioId, TipoSaida.DEFINITIVA, MotivoSaida.OUTRO, "caiu ao chão");
        String expResult = "Outro: caiu ao chão";
        String result = instance.listarMovimentos(materialId).get(0).getMotivo();
        assertEquals(expResult, result);
    }
}
