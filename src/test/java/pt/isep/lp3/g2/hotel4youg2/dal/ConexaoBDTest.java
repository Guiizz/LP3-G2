package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ConexaoBDTest {

    private static class FonteQueFalha implements ConexaoBD.FonteDeLigacoes {
        private final int falhas;
        private int chamadas;

        FonteQueFalha(int falhas) {
            this.falhas = falhas;
        }

        @Override
        public Connection ligar() throws SQLException {
            chamadas++;
            if (chamadas <= falhas) {
                throw new SQLException("Connect timed out", "08S01");
            }
            return null;
        }
    }

    private void exigirBD() {
        try {
            ConexaoBD.select("SELECT 1", new InteiroMapper());
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
    }

    @Test
    void ligacaoAbreEFecha() throws Exception {
        try(Connection conn = ConexaoBD.abrir()) {
            assertTrue(conn.isValid(2));
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
    }

    @Test
    public void testAbrirMuitasLigacoesSeguidas() throws SQLException {
        System.out.println("abrir 20 ligações seguidas");
        exigirBD();
        int falhas = 0;
        long maisLenta = 0;
        for (int i = 0; i < 20; i++) {
            long inicio = System.currentTimeMillis();
            try (Connection conn = ConexaoBD.abrir()) {
                if (!conn.isValid(2)) {
                    falhas++;
                }
            } catch (DataAccessException e) {
                falhas++;
            }
            maisLenta = Math.max(maisLenta, System.currentTimeMillis() - inicio);
        }
        System.out.println("a ligação mais lenta demorou " + maisLenta + " ms");
        int expResult = 0;
        int result = falhas;
        assertEquals(expResult, result);
    }

    @Test
    public void testSelectReaproveitaALigacao() {
        System.out.println("select (usa sempre a mesma ligação)");
        exigirBD();
        int expResult = ConexaoBD.select("SELECT @@SPID", new InteiroMapper()).get(0);
        int result = ConexaoBD.select("SELECT @@SPID", new InteiroMapper()).get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testSelectDepoisDeUmErro() {
        System.out.println("select (um erro de SQL não estraga as consultas seguintes)");
        exigirBD();
        try {
            ConexaoBD.select("SELECT * FROM TabelaQueNaoExiste", new InteiroMapper());
            fail("Devia ter recusado uma tabela que não existe");
        } catch (DataAccessException e) {
            assertEquals("Erro ao executar a consulta", e.getMessage());
        }
        List<Integer> expResult = List.of(1);
        List<Integer> result = ConexaoBD.select("SELECT 1", new InteiroMapper());
        assertEquals(expResult, result);
    }

    @Test
    public void testAbrirTentaOutraVez() {
        System.out.println("abrir (a primeira tentativa falha e a segunda liga)");
        FonteQueFalha fonte = new FonteQueFalha(1);
        ConexaoBD.abrir(fonte, ConexaoBD.TENTATIVAS);
        int expResult = 2;
        int result = fonte.chamadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testAbrirLigaAPrimeira() {
        System.out.println("abrir (liga à primeira e não tenta outra vez)");
        FonteQueFalha fonte = new FonteQueFalha(0);
        ConexaoBD.abrir(fonte, ConexaoBD.TENTATIVAS);
        int expResult = 1;
        int result = fonte.chamadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testAbrirDesisteDepoisDasTentativas() {
        System.out.println("abrir (desiste depois das tentativas, com uma mensagem clara)");
        FonteQueFalha fonte = new FonteQueFalha(5);
        try {
            ConexaoBD.abrir(fonte, ConexaoBD.TENTATIVAS);
            fail("Devia ter desistido de ligar");
        } catch (DataAccessException e) {
            assertEquals(ConexaoBD.SEM_LIGACAO, e.getMessage());
        }
        int expResult = ConexaoBD.TENTATIVAS;
        int result = fonte.chamadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testMensagemQuandoALigacaoCai() {
        System.out.println("mensagem (a ligação caiu a meio)");
        String expResult = ConexaoBD.LIGACAO_PERDIDA;
        String result = ConexaoBD.mensagem(new SQLException("Connection reset", "08S01"), "Erro ao executar a consulta");
        assertEquals(expResult, result);
    }

    @Test
    public void testMensagemDeOutroErro() {
        System.out.println("mensagem (erro no SQL, com a ligação boa)");
        String expResult = "Erro ao executar a consulta";
        String result = ConexaoBD.mensagem(new SQLException("Invalid object name", "42S02"), "Erro ao executar a consulta");
        assertEquals(expResult, result);
    }

    @Test
    public void testELeitura() {
        System.out.println("eLeitura (um SELECT pode repetir-se)");
        boolean expResult = true;
        boolean result = ConexaoBD.eLeitura("  select id FROM Reserva");
        assertEquals(expResult, result);
    }

    @Test
    public void testELeituraInsertComOutput() {
        System.out.println("eLeitura (um INSERT com OUTPUT não se repete)");
        boolean expResult = false;
        boolean result = ConexaoBD.eLeitura("INSERT INTO AcessoCartao (cartao) OUTPUT INSERTED.id VALUES (?)");
        assertEquals(expResult, result);
    }

    @Test
    public void testPrepararDeixaALigacaoPronta() throws InterruptedException {
        System.out.println("preparar (a ligação fica aberta antes da primeira consulta)");
        exigirBD();
        ConexaoBD.preparar();
        Thread.sleep(100);
        List<Integer> expResult = List.of(1);
        List<Integer> result = ConexaoBD.select("SELECT 1", new InteiroMapper());
        assertEquals(expResult, result);
    }

    @Test
    public void testMensagemSemEstado() {
        System.out.println("mensagem (erro sem código de estado)");
        String expResult = "Erro ao executar a instrução";
        String result = ConexaoBD.mensagem(new SQLException("erro"), "Erro ao executar a instrução");
        assertEquals(expResult, result);
    }
}
