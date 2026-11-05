package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FuncionarioClienteDALTest {

    private static final String PESSOAS_TESTE = "(SELECT id FROM Pessoa WHERE nome LIKE 'Teste FC %')";

    private ClienteDAL instance;
    private String nif;
    private String nome;
    private String nomeUtilizador;
    private int pessoaId;

    private static class TextoMapper implements RowMapper<String> {
        @Override
        public String map(ResultSet rs) throws SQLException {
            return rs.getString(1);
        }
    }

    @BeforeAll
    public static void limparRestosDeTestes() {
        try {
            limpar();
        } catch (DataAccessException e) {
        }
    }

    private static void limpar() {
        ConexaoBD.executar("DELETE FROM Reserva WHERE clienteId IN " + PESSOAS_TESTE);
        ConexaoBD.executar("DELETE FROM Utilizador WHERE funcionarioId IN " + PESSOAS_TESTE);
        ConexaoBD.executar("DELETE FROM Cliente WHERE pessoaId IN " + PESSOAS_TESTE);
        ConexaoBD.executar("DELETE FROM Funcionario WHERE pessoaId IN " + PESSOAS_TESTE);
        ConexaoBD.executar("DELETE FROM Pessoa WHERE nome LIKE 'Teste FC %'");
    }

    @BeforeEach
    public void setUp() {
        long marca = System.nanoTime();
        nif = String.format("%09d", Math.floorMod(marca, 1_000_000_000L));
        nome = "Teste FC " + marca;
        nomeUtilizador = "tfc" + marca;
        try {
            pessoaId = ConexaoBD.select("INSERT INTO Pessoa (nome, nif) OUTPUT INSERTED.id VALUES (?, ?)",
                    new InteiroMapper(), nome, nif).get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        ConexaoBD.executar("INSERT INTO Funcionario (pessoaId, numeroFuncionario) VALUES (?, ?)", pessoaId,
                1_000_000_000 + (int) Math.floorMod(marca, 1_000_000_000L));
        ConexaoBD.executar("INSERT INTO Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil) "
                + "VALUES (?, ?, ?, ?, 'RECECIONISTA')", pessoaId, nomeUtilizador, "h".repeat(64), "s".repeat(32));
        instance = new ClienteDAL();
    }

    @AfterEach
    public void tearDown() {
        limpar();
    }

    private Cliente comoCliente() {
        return new Cliente(pessoaId, nome, "12345678", nif, "Rua A", "912345678", LocalDate.of(1990, 5, 1), "t@mail.pt");
    }

    @Test
    public void testProcurarFuncionarioNaoCliente() {
        System.out.println("procurarFuncionarioNaoCliente");
        int expResult = pessoaId;
        int result = instance.procurarFuncionarioNaoCliente(nif).getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarFuncionarioJaCliente() {
        System.out.println("procurarFuncionarioNaoCliente (já é cliente)");
        instance.tornarCliente(comoCliente());
        Cliente expResult = null;
        Cliente result = instance.procurarFuncionarioNaoCliente(nif);
        assertEquals(expResult, result);
    }

    @Test
    public void testTornarCliente() {
        System.out.println("tornarCliente");
        instance.tornarCliente(comoCliente());
        String expResult = "912345678";
        String result = instance.procurarPorNif(nif).getTelemovel();
        assertEquals(expResult, result);
    }

    @Test
    public void testTornarClienteAparecePesquisa() {
        System.out.println("tornarCliente (aparece na pesquisa de clientes)");
        instance.tornarCliente(comoCliente());
        int expResult = 1;
        int result = instance.pesquisarClientes(nif).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testTornarClienteMantemAConta() {
        System.out.println("tornarCliente (a conta e o funcionário não mudam)");
        instance.tornarCliente(comoCliente());
        String expResult = nomeUtilizador;
        String result = ConexaoBD.select("SELECT nomeUtilizador FROM Utilizador WHERE funcionarioId = ? AND ativo = 1",
                new TextoMapper(), pessoaId).get(0);
        assertEquals(expResult, result);
        assertEquals(1, ConexaoBD.select("SELECT 1 FROM Funcionario WHERE pessoaId = ?", new InteiroMapper(), pessoaId).size());
    }

    @Test
    public void testTornarClientePodeTerReservas() {
        System.out.println("tornarCliente (pode ter reservas)");
        instance.tornarCliente(comoCliente());
        List<Integer> quartos = ConexaoBD.select("SELECT TOP 1 numero FROM Quarto WHERE ativo = 1", new InteiroMapper());
        Assumptions.assumeTrue(!quartos.isEmpty(), "Não há quartos na BD");
        LocalDate entrada = LocalDate.of(2098, 1, 1).plusDays(Math.floorMod(System.nanoTime(), 3000L));
        Reserva reserva = new Reserva(0, pessoaId, quartos.get(0), entrada, entrada.plusDays(1), 1,
                EstadoReserva.CONFIRMADA, new BigDecimal("100.00"), null, null);
        boolean expResult = true;
        boolean result = new ReservaDAL().inserirSeLivre(reserva);
        assertEquals(expResult, result);
    }
}
