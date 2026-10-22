package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private void esperaErro(int codigo, String sql) {
        SQLException e = assertThrows(SQLException.class, () -> exec(sql));
        assertEquals(codigo, e.getErrorCode());
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
    void quartoComNumeroRepetidoFalha() throws Exception {
        quarto(9001);
        esperaErro(DUPLICADO, "INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                + "VALUES (9001, 3, 'DUPLO', 'MAR', 2)");
    }

    @Test
    void tarifaNegativaFalha() throws Exception {
        tarifa();
        esperaErro(RESTRICAO, "UPDATE Tarifa SET valorNoite = -5 WHERE tipo = 'DUPLO' AND vista = 'MAR'");
    }

    @Test
    void tipoDeQuartoForaDoEnumFalha() throws Exception {
        tarifa();
        esperaErro(RESTRICAO, "INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                + "VALUES (9002, 2, 'SUITE', 'MAR', 2)");
    }

    @Test
    void vistaForaDoEnumFalha() throws Exception {
        tarifa();
        esperaErro(RESTRICAO, "INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                + "VALUES (9003, 2, 'DUPLO', 'JARDIM', 2)");
    }

    @Test
    void capacidadeZeroFalha() throws Exception {
        tarifa();
        esperaErro(RESTRICAO, "INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                + "VALUES (9004, 2, 'DUPLO', 'MAR', 0)");
    }

    @Test
    void nifRepetidoFalha() throws Exception {
        pessoa("999999991");
        esperaErro(DUPLICADO, "INSERT Pessoa (nome, nif) VALUES ('Outro', '999999991')");
    }

    @Test
    void nifComLetrasFalha() {
        esperaErro(RESTRICAO, "INSERT Pessoa (nome, nif) VALUES ('Teste', '12AB56789')");
    }

    @Test
    void nomeDeUtilizadorRepetidoFalha() throws Exception {
        funcionarioComUtilizador("999999991", 9001, "teste_dup");
        pessoa("999999992");
        exec("INSERT Funcionario (pessoaId, numeroFuncionario) SELECT id, 9002 FROM Pessoa WHERE nif = '999999992'");
        esperaErro(DUPLICADO, "INSERT Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil) "
                + "SELECT id, 'teste_dup', REPLICATE('a', 64), REPLICATE('b', 32), 'GESTOR' "
                + "FROM Pessoa WHERE nif = '999999992'");
    }

    @Test
    void reservaComSaidaIgualOuAnteriorAEntradaFalha() throws Exception {
        quarto(9001);
        pessoa("999999993");
        exec("INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999993'");
        esperaErro(RESTRICAO, reserva("2026-11-05", "2026-11-05"));
        esperaErro(RESTRICAO, reserva("2026-11-05", "2026-11-04"));
    }

    @Test
    void reservaValidaFunciona() throws Exception {
        quarto(9001);
        pessoa("999999993");
        exec("INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999993'");
        assertDoesNotThrow(() -> exec(reserva("2026-11-05", "2026-11-06")));
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
    void todasAsTabelasExistem() throws Exception {
        assertEquals("7", consulta("SELECT COUNT(*) FROM sys.tables WHERE name IN "
                + "('Pessoa', 'Cliente', 'Funcionario', 'Utilizador', 'Tarifa', 'Quarto', 'Reserva')"));
    }


    @Test
    void quartoComPisoNegativoFalha() throws Exception {
        tarifa();
        esperaErro(RESTRICAO, "INSERT Quarto (numero, piso, tipo, vista, capacidade) "
                + "VALUES (9005, -1, 'DUPLO', 'MAR', 2)");
    }


    @Test
    void clienteSemPessoaFalha() {
        esperaErro(RESTRICAO, "INSERT Cliente VALUES (-1)");
    }

    @Test
    void mesmaPessoaNaoPodeSerClienteDuasVezes() throws Exception {
        pessoa("999999991");
        exec("INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999991'");
        esperaErro(DUPLICADO, "INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999991'");
    }

    @Test
    void funcionarioPodeSerTambemCliente() throws Exception {
        funcionario("999999991", 9001);
        assertDoesNotThrow(() -> exec("INSERT Cliente SELECT id FROM Pessoa WHERE nif = '999999991'"));
    }

    @Test
    void numeroDeFuncionarioRepetidoFalha() throws Exception {
        funcionario("999999991", 9001);
        pessoa("999999992");
        esperaErro(DUPLICADO, "INSERT Funcionario (pessoaId, numeroFuncionario) "
                + "SELECT id, 9001 FROM Pessoa WHERE nif = '999999992'");
    }



    @Test
    void perfilForaDoEnumFalha() throws Exception {
        funcionario("999999991", 9001);
        esperaErro(RESTRICAO, utilizador("999999991", "teste_perfil", "ADMIN"));
    }

    @Test
    void funcionarioSoTemUmUtilizador() throws Exception {
        funcionario("999999991", 9001);
        exec(utilizador("999999991", "teste_a", "GESTOR"));
        esperaErro(DUPLICADO, utilizador("999999991", "teste_b", "RECECIONISTA"));
    }

    @Test
    void utilizadorSemFuncionarioFalha() {
        esperaErro(RESTRICAO, "INSERT Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil) "
                + "VALUES (-1, 'teste_x', REPLICATE('a', 64), REPLICATE('b', 32), 'GESTOR')");
    }

    @Test
    void utilizadorFicaAtivoPorOmissao() throws Exception {
        funcionario("999999991", 9001);
        exec(utilizador("999999991", "teste_ativo", "GESTOR"));
        assertEquals("1", consulta("SELECT CAST(ativo AS INT) FROM Utilizador WHERE nomeUtilizador = 'teste_ativo'"));
    }


    @Test
    void reservaFicaConfirmadaPorOmissao() throws Exception {
        clienteEQuarto();
        exec(reservaCom(1, "50", "", ""));
        assertEquals("CONFIRMADA", consulta("SELECT estado FROM Reserva WHERE quartoNumero = 9001"));
    }

    @Test
    void estadoForaDoEnumFalha() throws Exception {
        clienteEQuarto();
        esperaErro(RESTRICAO, reservaCom(1, "50", ", estado", ", 'PERDIDA'"));
    }

    @Test
    void reservaSemHospedesFalha() throws Exception {
        clienteEQuarto();
        esperaErro(RESTRICAO, reservaCom(0, "50", "", ""));
    }

    @Test
    void precoTotalNegativoFalha() throws Exception {
        clienteEQuarto();
        esperaErro(RESTRICAO, reservaCom(1, "-1", "", ""));
    }

    @Test
    void checkoutSemCheckinFalha() throws Exception {
        clienteEQuarto();
        esperaErro(RESTRICAO, reservaCom(1, "50", ", checkoutEm", ", SYSDATETIME()"));
    }

    @Test
    void checkoutAntesDoCheckinFalha() throws Exception {
        clienteEQuarto();
        esperaErro(RESTRICAO, reservaCom(1, "50", ", checkinEm, checkoutEm",
                ", '2026-11-05 15:00', '2026-11-05 10:00'"));
    }

    @Test
    void reservaComClienteInexistenteFalha() throws Exception {
        quarto(9001);
        esperaErro(RESTRICAO, "INSERT Reserva (clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, precoTotal) "
                + "VALUES (-1, 9001, '2026-11-05', '2026-11-06', 1, 50)");
    }

    @Test
    void reservaComQuartoInexistenteFalha() throws Exception {
        clienteEQuarto();
        esperaErro(RESTRICAO, "INSERT Reserva (clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, precoTotal) "
                + "SELECT id, 9998, '2026-11-05', '2026-11-06', 1, 50 FROM Pessoa WHERE nif = '999999993'");
    }
}