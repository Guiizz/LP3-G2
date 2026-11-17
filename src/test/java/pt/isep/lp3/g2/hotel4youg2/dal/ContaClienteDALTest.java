package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContaClienteDALTest {

    private static final String HASH = "a".repeat(64);
    private static final String SALT = "b".repeat(32);
    private static final String HASH_NOVO = "c".repeat(64);
    private static final String SALT_NOVO = "d".repeat(32);

    private ContaClienteDAL instance;
    private ContaCliente conta;
    private String nif;
    private String nome;

    @BeforeEach
    public void setUp() {
        int clienteId;
        try {
            List<Integer> tabela = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('ContaCliente') IS NOT NULL "
                    + "THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabela.get(0) == 1, "Falta correr o 21_contas_clientes.sql");
            List<Integer> clientes = ConexaoBD.select("SELECT TOP 1 c.pessoaId FROM Cliente c WHERE NOT EXISTS "
                    + "(SELECT 1 FROM ContaCliente k WHERE k.clienteId = c.pessoaId) ORDER BY c.pessoaId",
                    new InteiroMapper());
            Assumptions.assumeTrue(!clientes.isEmpty(), "Faltam clientes sem conta na BD");
            clienteId = clientes.get(0);
            nif = ConexaoBD.select("SELECT nif FROM Pessoa WHERE id = ?", new TextoMapper(), clienteId).get(0);
            nome = ConexaoBD.select("SELECT nome FROM Pessoa WHERE id = ?", new TextoMapper(), clienteId).get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
            return;
        }
        instance = new ContaClienteDAL();
        conta = new ContaCliente(0, clienteId, nif, nome, HASH, SALT, true);
        instance.inserir(conta);
    }

    @AfterEach
    public void tearDown() {
        if (conta != null && conta.getId() > 0) {
            instance.apagar(conta.getId());
        }
        conta = null;
    }

    private static class TextoMapper implements RowMapper<String> {
        @Override
        public String map(ResultSet rs) throws SQLException {
            return rs.getString(1).trim();
        }
    }

    @Test
    public void testInserir() {
        System.out.println("inserir (fica com o id da BD)");
        boolean expResult = true;
        boolean result = conta.getId() > 0;
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorNif() {
        System.out.println("procurarPorNif");
        ContaCliente expResult = conta;
        ContaCliente result = instance.procurarPorNif(nif);
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorNifSemConta() {
        System.out.println("procurarPorNif (NIF sem conta)");
        ContaCliente expResult = null;
        ContaCliente result = instance.procurarPorNif("000000000");
        assertEquals(expResult, result);
    }

    @Test
    public void testDefinirPassword() {
        System.out.println("definirPassword (sai do primeiro acesso)");
        instance.definirPassword(conta.getId(), HASH_NOVO, SALT_NOVO);
        ContaCliente expResult = new ContaCliente(conta.getId(), conta.getClienteId(), nif, nome, HASH_NOVO,
                SALT_NOVO, false);
        ContaCliente result = instance.procurarPorNif(nif);
        assertEquals(expResult, result);
    }

    @Test
    public void testDefinirPasswordSoUmaVez() {
        System.out.println("definirPassword (só no primeiro acesso)");
        instance.definirPassword(conta.getId(), HASH_NOVO, SALT_NOVO);
        boolean expResult = false;
        boolean result = instance.definirPassword(conta.getId(), HASH, SALT);
        assertEquals(expResult, result);
    }

    @Test
    public void testNovoCodigo() {
        System.out.println("novoCodigo (volta ao primeiro acesso)");
        instance.definirPassword(conta.getId(), HASH_NOVO, SALT_NOVO);
        instance.novoCodigo(conta.getId(), HASH, SALT);
        ContaCliente expResult = conta;
        ContaCliente result = instance.procurarPorNif(nif);
        assertEquals(expResult, result);
    }

    @Test
    public void testApagar() {
        System.out.println("apagar");
        instance.apagar(conta.getId());
        ContaCliente expResult = null;
        ContaCliente result = instance.procurarPorNif(nif);
        assertEquals(expResult, result);
    }
}
