package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FornecedorDALTest {

    private FornecedorDAL instance;
    private String codigo;
    private String codigoCriado;

    // Prepara o contexto: um fornecedor de teste com conta (hash e salt) criado só para o teste
    @BeforeEach
    public void setUp() {
        try {
            List<Integer> colunas = ConexaoBD.select("SELECT CASE WHEN COL_LENGTH('Fornecedor', 'passwordHash') "
                    + "IS NOT NULL THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(colunas.get(0) == 1, "Falta correr o 11_entregas_estado_fornecedor_stock.sql");
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new FornecedorDAL();
        codigo = "T" + System.nanoTime() % 1_000_000_000L;
        ConexaoBD.executar("INSERT INTO Fornecedor (codigo, nome, passwordHash, passwordSalt) VALUES (?, ?, ?, ?)",
                codigo, "Fornecedor de teste", "a".repeat(64), "b".repeat(32));
    }

    // Desfaz o que o setUp fez: apaga o fornecedor de teste
    @AfterEach
    public void tearDown() {
        if (instance == null) {
            return;
        }
        ConexaoBD.executar("DELETE FROM Fornecedor WHERE codigo = ?", codigo);

        if (codigoCriado != null) {
            ConexaoBD.executar("DELETE FROM Fornecedor WHERE codigo = ?", codigoCriado);
        }
    }

    @Test
    public void testProcurarPorCodigo() {
        System.out.println("procurarPorCodigo");
        Fornecedor result = instance.procurarPorCodigo(codigo).orElseThrow();
        assertEquals("Fornecedor de teste", result.getNome());
        assertEquals("a".repeat(64), result.getPasswordHash());
        assertEquals("b".repeat(32), result.getPasswordSalt());
    }

    @Test
    public void testProcurarPorCodigoQueNaoExiste() {
        System.out.println("procurarPorCodigo (não existe)");
        Optional<Fornecedor> expResult = Optional.empty();
        Optional<Fornecedor> result = instance.procurarPorCodigo("NAO-EXISTE");
        assertEquals(expResult, result);
    }

    @Test
    public void testCriar() {
        System.out.println("criar");
        codigoCriado = codigo + "N";
        Fornecedor fornecedor = new Fornecedor(0, codigoCriado, "Fornecedor novo", null, null, null, null, null, null);
        fornecedor.definirPassword("c".repeat(64), "d".repeat(32));
        int id = instance.criar(fornecedor);
        Fornecedor result = instance.procurarPorCodigo(codigoCriado).orElseThrow();
        assertEquals(id, result.getId());
        assertEquals("Fornecedor novo", result.getNome());
        assertEquals("c".repeat(64), result.getPasswordHash());
    }

    @Test
    public void testDefinirPassword() {
        System.out.println("definirPassword");
        codigoCriado = codigo + "N";
        ConexaoBD.executar("INSERT INTO Fornecedor (codigo, nome) VALUES (?, ?)", codigoCriado, "Sem conta");
        int id = instance.procurarPorCodigo(codigoCriado).orElseThrow().getId();
        boolean expResult = true;
        boolean result = instance.definirPassword(id, "e".repeat(64), "f".repeat(32));
        assertEquals(expResult, result);
        Fornecedor atualizado = instance.procurarPorCodigo(codigoCriado).orElseThrow();
        assertEquals("e".repeat(64), atualizado.getPasswordHash());
        assertEquals("f".repeat(32), atualizado.getPasswordSalt());
    }

    @Test
    public void testDefinirPasswordQueJaExiste() {
        System.out.println("definirPassword (já tem password)");
        int id = instance.procurarPorCodigo(codigo).orElseThrow().getId();
        boolean expResult = false;
        boolean result = instance.definirPassword(id, "e".repeat(64), "f".repeat(32));
        assertEquals(expResult, result);
        assertEquals("a".repeat(64), instance.procurarPorCodigo(codigo).orElseThrow().getPasswordHash());
    }
}
