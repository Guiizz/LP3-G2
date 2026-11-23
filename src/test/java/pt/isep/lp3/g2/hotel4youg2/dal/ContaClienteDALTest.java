package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContaClienteDALTest {

    private static final String HASH = "a".repeat(64);
    private static final String SALT = "b".repeat(32);
    private static final String HASH_NOVO = "c".repeat(64);
    private static final String SALT_NOVO = "d".repeat(32);

    private ContaClienteDAL instance;
    private ContaCliente conta;
    private Cliente cliente;
    private String nif;
    private String nome;

    @BeforeAll
    public static void limparRestosDosTestes() {
        RestosDosTestes.limpar();
    }

    @BeforeEach
    public void setUp() {
        int clienteId;
        try {
            List<Integer> tabela = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('ContaCliente') IS NOT NULL "
                    + "THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabela.get(0) == 1, "Falta correr o 21_contas_clientes.sql");
            long marca = System.nanoTime();
            nome = "Teste " + marca;
            nif = String.valueOf(900000000L + marca % 100000000L);
            cliente = new Cliente(0, nome, null, nif, null, null, null, null);
            new ClienteDAL().inserirCliente(cliente);
            clienteId = cliente.getId();
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
        if (cliente != null && cliente.getId() > 0) {
            ConexaoBD.executar("DELETE FROM Cliente WHERE pessoaId = ?", cliente.getId());
            ConexaoBD.executar("DELETE FROM Pessoa WHERE id = ?", cliente.getId());
        }
        cliente = null;
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
