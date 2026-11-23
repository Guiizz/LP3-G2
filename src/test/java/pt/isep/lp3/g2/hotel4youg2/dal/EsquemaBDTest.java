package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/** Testa que as restrições das tabelas (US09 e US10) rejeitam dados inválidos. */
class EsquemaBDTest {

    private static final int RESTRICAO = 547;   // CHECK ou chave estrangeira violada
    private static final int DUPLICADO = 2627;  // chave primária ou UNIQUE violada

    private Connection conn;

    @BeforeEach
    void abrir() throws Exception {
        try {
            conn = ConexaoBD.abrir();
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        conn.setAutoCommit(false);
    }

    @AfterEach
    void desfazer() throws Exception {
        if (conn != null) {
            conn.rollback();
            conn.close();
        }
    }

    private void exec(String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    private void tarifa() throws SQLException {
        exec("IF NOT EXISTS (SELECT 1 FROM Tarifa WHERE tipo = 'DUPLO' AND vista = 'MAR') "
                + "INSERT Tarifa VALUES ('DUPLO', 'MAR', 100)");
    }

    private void quarto(int numero) throws SQLException {
        tarifa();
        exec("INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                + "VALUES (" + numero + ", 2, 'DUPLO', 'MAR', 2)");
    }

    private void pessoa(String nif) throws SQLException {
        exec("INSERT Pessoa (nome, nif) VALUES ('Teste', '" + nif + "')");
    }

    private void funcionarioComUtilizador(String nif, int numero, String nomeUtilizador) throws SQLException {
        pessoa(nif);
        exec("INSERT Funcionario (pessoaId, numeroFuncionario) "
                + "SELECT id, " + numero + " FROM Pessoa WHERE nif = '" + nif + "'");
        exec("INSERT Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil) "
                + "SELECT id, '" + nomeUtilizador + "', REPLICATE('a', 64), REPLICATE('b', 32), 'GESTOR' "
                + "FROM Pessoa WHERE nif = '" + nif + "'");
    }

    private String reserva(String entrada, String saida) {
        return "INSERT Reserva (clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, precoTotal) "
                + "SELECT id, 9001, '" + entrada + "', '" + saida + "', 1, 50 FROM Pessoa WHERE nif = '999999993'";
    }

    @Test
    void reservaValidaFunciona() throws Exception {
        System.out.println("reservaValidaFunciona");
        quarto(9001);
        pessoa("999999993");
        exec("INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999993'");
        exec(reserva("2026-11-05", "2026-11-06"));
        String expResult = "1";
        String result = consulta("SELECT COUNT(*) FROM Reserva WHERE quartoNumero = 9001");
        assertEquals(expResult, result);
    }

    @Test
    void todasAsTabelasExistem() throws Exception {
        System.out.println("todasAsTabelasExistem");
        String expResult = "7";
        String result = consulta("SELECT COUNT(*) FROM sys.tables WHERE name IN "
                + "('Pessoa', 'Cliente', 'Funcionario', 'Utilizador', 'Tarifa', 'Quarto', 'Reserva')");
        assertEquals(expResult, result);
    }

    @Test
    void funcionarioPodeSerTambemCliente() throws Exception {
        System.out.println("funcionarioPodeSerTambemCliente");
        funcionario("999999991", 9001);
        exec("INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999991'");
        String expResult = "1";
        String result = consulta("SELECT COUNT(*) FROM Cliente c JOIN Funcionario f ON f.pessoaId = c.pessoaId "
                + "JOIN Pessoa p ON p.id = c.pessoaId WHERE p.nif = '999999991'");
        assertEquals(expResult, result);
    }

    @Test
    void utilizadorFicaAtivoPorOmissao() throws Exception {
        System.out.println("utilizadorFicaAtivoPorOmissao");
        funcionario("999999991", 9001);
        exec(utilizador("999999991", "teste_ativo", "GESTOR"));
        String expResult = "1";
        String result = consulta("SELECT CAST(ativo AS INT) FROM Utilizador WHERE nomeUtilizador = 'teste_ativo'");
        assertEquals(expResult, result);
    }

    @Test
    void reservaFicaConfirmadaPorOmissao() throws Exception {
        System.out.println("reservaFicaConfirmadaPorOmissao");
        clienteEQuarto();
        exec(reservaCom(1, "50", "", ""));
        String expResult = "CONFIRMADA";
        String result = consulta("SELECT estado FROM Reserva WHERE quartoNumero = 9001");
        assertEquals(expResult, result);
    }

    @Test
    void quartoComNumeroRepetidoFalha() throws Exception {
        System.out.println("quartoComNumeroRepetidoFalha");
        quarto(9001);
        int expResult = DUPLICADO;
        try {
            exec("INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                        + "VALUES (9001, 3, 'DUPLO', 'MAR', 2)");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void tarifaNegativaFalha() throws Exception {
        System.out.println("tarifaNegativaFalha");
        tarifa();
        int expResult = RESTRICAO;
        try {
            exec("UPDATE Tarifa SET valorNoite = -5 WHERE tipo = 'DUPLO' AND vista = 'MAR'");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void tipoDeQuartoForaDoEnumFalha() throws Exception {
        System.out.println("tipoDeQuartoForaDoEnumFalha");
        tarifa();
        int expResult = RESTRICAO;
        try {
            exec("INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                        + "VALUES (9002, 2, 'SUITE', 'MAR', 2)");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void vistaForaDoEnumFalha() throws Exception {
        System.out.println("vistaForaDoEnumFalha");
        tarifa();
        int expResult = RESTRICAO;
        try {
            exec("INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                        + "VALUES (9003, 2, 'DUPLO', 'JARDIM', 2)");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void capacidadeZeroFalha() throws Exception {
        System.out.println("capacidadeZeroFalha");
        tarifa();
        int expResult = RESTRICAO;
        try {
            exec("INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                        + "VALUES (9004, 2, 'DUPLO', 'MAR', 0)");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    private String consulta(String sql) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getString(1);
        }
    }

    private void funcionario(String nif, int numero) throws SQLException {
        pessoa(nif);
        exec("INSERT Funcionario (pessoaId, numeroFuncionario) "
                + "SELECT id, " + numero + " FROM Pessoa WHERE nif = '" + nif + "'");
    }

    private String utilizador(String nif, String nome, String perfil) {
        return "INSERT Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil) "
                + "SELECT id, '" + nome + "', REPLICATE('a', 64), REPLICATE('b', 32), '" + perfil + "' "
                + "FROM Pessoa WHERE nif = '" + nif + "'";
    }

    private void clienteEQuarto() throws SQLException {
        quarto(9001);
        pessoa("999999993");
        exec("INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999993'");
    }

    private String reservaCom(int hospedes, String preco, String colunas, String valores) {
        return "INSERT Reserva (clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, precoTotal" + colunas + ") "
                + "SELECT id, 9001, '2026-11-05', '2026-11-06', " + hospedes + ", " + preco + valores
                + " FROM Pessoa WHERE nif = '999999993'";
    }

    @Test
    void nifRepetidoFalha() throws Exception {
        System.out.println("nifRepetidoFalha");
        pessoa("999999991");
        int expResult = DUPLICADO;
        try {
            exec("INSERT Pessoa (nome, nif) VALUES ('Outro', '999999991')");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void nifComLetrasFalha() {
        System.out.println("nifComLetrasFalha");
        int expResult = RESTRICAO;
        try {
            exec("INSERT Pessoa (nome, nif) VALUES ('Teste', '12AB56789')");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void nomeDeUtilizadorRepetidoFalha() throws Exception {
        System.out.println("nomeDeUtilizadorRepetidoFalha");
        funcionarioComUtilizador("999999991", 9001, "teste_dup");
        pessoa("999999992");
        exec("INSERT Funcionario (pessoaId, numeroFuncionario) SELECT id, 9002 FROM Pessoa WHERE nif = '999999992'");
        int expResult = DUPLICADO;
        try {
            exec("INSERT Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil) "
                        + "SELECT id, 'teste_dup', REPLICATE('a', 64), REPLICATE('b', 32), 'GESTOR' "
                        + "FROM Pessoa WHERE nif = '999999992'");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void reservaComSaidaIgualOuAnteriorAEntradaFalha() throws Exception {
        System.out.println("reservaComSaidaIgualOuAnteriorAEntradaFalha");
        quarto(9001);
        pessoa("999999993");
        exec("INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999993'");
        String expResult = RESTRICAO + " / " + RESTRICAO;
        int saidaIgual = 0;
        try {
            exec(reserva("2026-11-05", "2026-11-05"));
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            saidaIgual = expected.getErrorCode();
        }
        int saidaAnterior = 0;
        try {
            exec(reserva("2026-11-05", "2026-11-04"));
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            saidaAnterior = expected.getErrorCode();
        }
        String result = saidaIgual + " / " + saidaAnterior;
        assertEquals(expResult, result);
    }

    @Test
    void quartoComPisoNegativoFalha() throws Exception {
        System.out.println("quartoComPisoNegativoFalha");
        tarifa();
        int expResult = RESTRICAO;
        try {
            exec("INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                        + "VALUES (9005, -1, 'DUPLO', 'MAR', 2)");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void clienteSemPessoaFalha() {
        System.out.println("clienteSemPessoaFalha");
        int expResult = RESTRICAO;
        try {
            exec("INSERT Cliente VALUES (-1)");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void mesmaPessoaNaoPodeSerClienteDuasVezes() throws Exception {
        System.out.println("mesmaPessoaNaoPodeSerClienteDuasVezes");
        pessoa("999999991");
        exec("INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999991'");
        int expResult = DUPLICADO;
        try {
            exec("INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999991'");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void numeroDeFuncionarioRepetidoFalha() throws Exception {
        System.out.println("numeroDeFuncionarioRepetidoFalha");
        funcionario("999999991", 9001);
        pessoa("999999992");
        int expResult = DUPLICADO;
        try {
            exec("INSERT Funcionario (pessoaId, numeroFuncionario) "
                        + "SELECT id, 9001 FROM Pessoa WHERE nif = '999999992'");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void perfilForaDoEnumFalha() throws Exception {
        System.out.println("perfilForaDoEnumFalha");
        funcionario("999999991", 9001);
        int expResult = RESTRICAO;
        try {
            exec(utilizador("999999991", "teste_perfil", "ADMIN"));
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void funcionarioSoTemUmUtilizador() throws Exception {
        System.out.println("funcionarioSoTemUmUtilizador");
        funcionario("999999991", 9001);
        exec(utilizador("999999991", "teste_a", "GESTOR"));
        int expResult = DUPLICADO;
        try {
            exec(utilizador("999999991", "teste_b", "RECECIONISTA"));
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void utilizadorSemFuncionarioFalha() {
        System.out.println("utilizadorSemFuncionarioFalha");
        int expResult = RESTRICAO;
        try {
            exec("INSERT Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil) "
                        + "VALUES (-1, 'teste_x', REPLICATE('a', 64), REPLICATE('b', 32), 'GESTOR')");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void estadoForaDoEnumFalha() throws Exception {
        System.out.println("estadoForaDoEnumFalha");
        clienteEQuarto();
        int expResult = RESTRICAO;
        try {
            exec(reservaCom(1, "50", ", estado", ", 'PERDIDA'"));
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void reservaSemHospedesFalha() throws Exception {
        System.out.println("reservaSemHospedesFalha");
        clienteEQuarto();
        int expResult = RESTRICAO;
        try {
            exec(reservaCom(0, "50", "", ""));
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void precoTotalNegativoFalha() throws Exception {
        System.out.println("precoTotalNegativoFalha");
        clienteEQuarto();
        int expResult = RESTRICAO;
        try {
            exec(reservaCom(1, "-1", "", ""));
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void checkoutSemCheckinFalha() throws Exception {
        System.out.println("checkoutSemCheckinFalha");
        clienteEQuarto();
        int expResult = RESTRICAO;
        try {
            exec(reservaCom(1, "50", ", checkoutEm", ", SYSDATETIME()"));
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void checkoutAntesDoCheckinFalha() throws Exception {
        System.out.println("checkoutAntesDoCheckinFalha");
        clienteEQuarto();
        int expResult = RESTRICAO;
        try {
            exec(reservaCom(1, "50", ", checkinEm, checkoutEm",
                        ", '2026-11-05 15:00', '2026-11-05 10:00'"));
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void reservaComClienteInexistenteFalha() throws Exception {
        System.out.println("reservaComClienteInexistenteFalha");
        quarto(9001);
        int expResult = RESTRICAO;
        try {
            exec("INSERT Reserva (clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, precoTotal) "
                        + "VALUES (-1, 9001, '2026-11-05', '2026-11-06', 1, 50)");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }

    @Test
    void reservaComQuartoInexistenteFalha() throws Exception {
        System.out.println("reservaComQuartoInexistenteFalha");
        clienteEQuarto();
        int expResult = RESTRICAO;
        try {
            exec("INSERT Reserva (clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, precoTotal) "
                        + "SELECT id, 9998, '2026-11-05', '2026-11-06', 1, 50 FROM Pessoa WHERE nif = '999999993'");
            fail("A base de dados devia ter recusado a instrução.");
        } catch (SQLException expected) {
            int result = expected.getErrorCode();
            assertEquals(expResult, result);
        }
    }
}