package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoSugestao;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SugestaoQueixaDALTest {

    private SugestaoQueixaDAL instance;
    private SugestaoQueixa sugestao;

    // Prepara o contexto: uma queixa de teste de um cliente e de um funcionário que já existam na BD
    @BeforeEach
    public void setUp() {
        int clienteId = 0;
        int funcionarioId = 0;
        try {
            List<Integer> tabela = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('SugestaoQueixa') IS NOT NULL "
                    + "THEN 1 ELSE 0 END", rs -> rs.getInt(1));
            Assumptions.assumeTrue(tabela.get(0) == 1, "Falta correr o 06_sugestoes.sql");
            List<Integer> clientes = ConexaoBD.select("SELECT TOP 1 pessoaId FROM Cliente", rs -> rs.getInt(1));
            List<Integer> funcionarios = ConexaoBD.select("SELECT TOP 1 pessoaId FROM Funcionario", rs -> rs.getInt(1));
            Assumptions.assumeTrue(!clientes.isEmpty() && !funcionarios.isEmpty(), "Faltam clientes ou funcionários na BD");
            clienteId = clientes.get(0);
            funcionarioId = funcionarios.get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new SugestaoQueixaDAL();
        sugestao = new SugestaoQueixa(0, clienteId, TipoSugestao.QUEIXA, "Teste " + System.nanoTime(),
                funcionarioId, null);
        instance.inserir(sugestao);
    }

    // Desfaz o que o setUp fez: apaga a queixa de teste
    @AfterEach
    public void tearDown() {
        if (sugestao != null && sugestao.getId() > 0) {
            instance.apagar(sugestao.getId());
        }
    }

    @Test
    public void testInserir() {
        System.out.println("inserir");
        boolean expResult = true;
        boolean result = sugestao.getId() > 0;
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirDataAutomatica() {
        System.out.println("inserir (data posta pela BD)");
        boolean expResult = true;
        boolean result = Duration.between(sugestao.getRegistadaEm(), LocalDateTime.now()).abs().toMinutes() < 5;
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorId() {
        System.out.println("procurarPorId");
        SugestaoQueixa expResult = sugestao;
        SugestaoQueixa result = instance.procurarPorId(sugestao.getId()).orElseThrow();
        assertEquals(expResult, result);
    }

    @Test
    public void testApagar() {
        System.out.println("apagar");
        instance.apagar(sugestao.getId());
        Optional<SugestaoQueixa> expResult = Optional.empty();
        Optional<SugestaoQueixa> result = instance.procurarPorId(sugestao.getId());
        assertEquals(expResult, result);
        sugestao.setId(0);
    }
}
