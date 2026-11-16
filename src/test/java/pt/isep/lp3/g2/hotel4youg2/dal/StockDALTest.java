package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.MovimentoStock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StockDALTest {

    private StockDAL instance;
    private String codigo;
    private int fornecedorId;
    private int materialId;
    private int funcionarioId;

    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabelas = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('Stock') IS NOT NULL "
                    + "AND OBJECT_ID('MovimentoStock') IS NOT NULL THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabelas.get(0) == 1, "Falta correr o 16_movimentos_stock.sql");
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
        boolean result = instance.registarSaida(materialId, 3, funcionarioId);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaTiraDoStock() {
        System.out.println("registarSaida (tira do stock)");
        instance.registarSaida(materialId, 3, funcionarioId);
        int expResult = 7;
        int result = quantidadeEmStock();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaGuardaOFuncionario() {
        System.out.println("registarSaida (fica registado o funcionário)");
        instance.registarSaida(materialId, 3, funcionarioId);
        int expResult = funcionarioId;
        int result = ConexaoBD.select("SELECT funcionarioId FROM MovimentoStock WHERE materialId = ? AND quantidade = 3",
                new InteiroMapper(), materialId).get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaGuardaAData() {
        System.out.println("registarSaida (data posta pela BD)");
        instance.registarSaida(materialId, 3, funcionarioId);
        int expResult = 1;
        int result = ConexaoBD.select("SELECT CASE WHEN ABS(DATEDIFF(MINUTE, registadoEm, SYSDATETIME())) < 5 "
                + "THEN 1 ELSE 0 END FROM MovimentoStock WHERE materialId = ?", new InteiroMapper(), materialId).get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaDeTudo() {
        System.out.println("registarSaida (pode tirar tudo o que existe)");
        instance.registarSaida(materialId, 10, funcionarioId);
        int expResult = 0;
        int result = quantidadeEmStock();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAMaisDevolveFalso() {
        System.out.println("registarSaida (mais do que existe)");
        boolean expResult = false;
        boolean result = instance.registarSaida(materialId, 11, funcionarioId);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAMaisNaoMudaOStock() {
        System.out.println("registarSaida (mais do que existe não muda o stock)");
        instance.registarSaida(materialId, 11, funcionarioId);
        int expResult = 10;
        int result = quantidadeEmStock();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAMaisNaoRegistaMovimento() {
        System.out.println("registarSaida (mais do que existe não fica registado)");
        instance.registarSaida(materialId, 11, funcionarioId);
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
        instance.registarSaida(materialId, 3, funcionarioId);
        instance.registarSaida(materialId, 2, funcionarioId);
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
        instance.registarSaida(materialId, 3, funcionarioId);
        List<MovimentoStock> movimentos = instance.listarMovimentos(materialId);
        String expResult = "[Saída 3, Entrada 5]";
        List<String> result = new ArrayList<>();
        for (MovimentoStock movimento : movimentos) {
            result.add(movimento.getTipo() + " " + movimento.getQuantidade());
        }
        assertEquals(expResult, result.toString());
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
            instance.registarSaida(materialId, 3, -1);
        } catch (DataAccessException esperado) {
            System.out.println("o movimento foi recusado pela BD");
        }
        int expResult = 10;
        int result = quantidadeEmStock();
        assertEquals(expResult, result);
    }
}
