package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Unidade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntregaDALTest {

    private EntregaDAL instance;
    private Entrega entrega;
    private String codigoFornecedor;
    private int funcionarioId;

    // Prepara o contexto: um funcionário que já exista e uma entrega de teste com um fornecedor só para o teste
    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabelas = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('Entrega') IS NOT NULL "
                    + "AND OBJECT_ID('LinhaEntrega') IS NOT NULL THEN 1 ELSE 0 END", rs -> rs.getInt(1));
            Assumptions.assumeTrue(tabelas.get(0) == 1, "Falta correr o 05_entregas.sql");
            List<Integer> funcionarios = ConexaoBD.select("SELECT TOP 1 pessoaId FROM Funcionario", rs -> rs.getInt(1));
            Assumptions.assumeTrue(!funcionarios.isEmpty(), "Não há funcionários na BD");
            funcionarioId = funcionarios.get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new EntregaDAL();
        long marca = System.nanoTime() % 1_000_000_000L;
        codigoFornecedor = "T" + marca;
        Fornecedor fornecedor = new Fornecedor(0, codigoFornecedor, "Fornecedor de teste", "Rua A", "nº1", "Porto",
                "4000-000", "Portugal", "PT");
        entrega = new Entrega(0, "TESTE" + marca, LocalDate.of(2026, 10, 5), fornecedor);
        entrega.adicionarLinha(new LinhaEntrega(1, "M-1", "Toalha de teste", 50, Unidade.BOX, new BigDecimal("2.51"),
                new BigDecimal("125.50"), 23, new BigDecimal("28.87"), new BigDecimal("154.37")));
        entrega.adicionarLinha(new LinhaEntrega(2, "M-2", "Champô de teste", 200, Unidade.UNIT, new BigDecimal("3.49"),
                new BigDecimal("698.00"), 23, new BigDecimal("160.54"), new BigDecimal("858.54")));
        instance.registar(entrega, funcionarioId);
    }

    // Desfaz o que o setUp fez: apaga a entrega (e as linhas), os materiais e o fornecedor de teste
    @AfterEach
    public void tearDown() {
        if (instance == null) {
            return;
        }
        if (entrega != null && entrega.getId() > 0) {
            instance.apagar(entrega.getId());
        }
        ConexaoBD.executar("DELETE FROM Material WHERE fornecedorId IN (SELECT id FROM Fornecedor WHERE codigo = ?)",
                codigoFornecedor);
        ConexaoBD.executar("DELETE FROM Fornecedor WHERE codigo = ?", codigoFornecedor);
    }

    @Test
    public void testRegistar() {
        System.out.println("registar");
        boolean expResult = true;
        boolean result = entrega.getId() > 0;
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorId() {
        System.out.println("procurarPorId");
        Entrega expResult = entrega;
        Entrega result = instance.procurarPorId(entrega.getId()).orElseThrow();
        assertEquals(expResult, result);
    }

    @Test
    public void testExisteEntrega() {
        System.out.println("existeEntrega");
        boolean expResult = true;
        boolean result = instance.existeEntrega(codigoFornecedor, entrega.getNumeroEncomenda());
        assertEquals(expResult, result);
    }

    @Test
    public void testExisteEntregaOutroNumero() {
        System.out.println("existeEntrega (outro número)");
        boolean expResult = false;
        boolean result = instance.existeEntrega(codigoFornecedor, "NAO-EXISTE");
        assertEquals(expResult, result);
    }

    @Test
    public void testApagar() {
        System.out.println("apagar");
        instance.apagar(entrega.getId());
        Optional<Entrega> expResult = Optional.empty();
        Optional<Entrega> result = instance.procurarPorId(entrega.getId());
        assertEquals(expResult, result);
        entrega.setId(0);
    }
}
