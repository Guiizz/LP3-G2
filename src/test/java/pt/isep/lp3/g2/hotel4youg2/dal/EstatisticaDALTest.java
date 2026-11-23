package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.bll.EstatisticaBLL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EstatisticaDALTest {

    private static final LocalDate INICIO_ANTIGO = LocalDate.of(1950, 1, 1);
    private static final LocalDate FIM_ANTIGO = LocalDate.of(1950, 12, 31);
    private static final LocalDate SEMPRE_INICIO = LocalDate.of(1900, 1, 1);
    private static final LocalDate SEMPRE_FIM = LocalDate.of(2999, 12, 31);

    private EstatisticaDAL instance;

    @BeforeEach
    public void setUp() {
        try {
            ConexaoBD.select("SELECT 1", new InteiroMapper());
        } catch (DataAccessException e) {
            Assumptions.assumeTrue(false, "Sem ligação à base de dados");
        }
        instance = new EstatisticaDAL();
    }

    @Test
    public void testContarQuartosAtivos() {
        System.out.println("contarQuartosAtivos");
        int expResult = ConexaoBD.select("SELECT COUNT(*) FROM Quarto WHERE ativo = 1", new InteiroMapper()).get(0);
        int result = instance.contarQuartosAtivos();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarEstadiasSoReservasFeitas() {
        System.out.println("listarEstadias (só as reservas em curso e concluídas, como a receita)");
        int expResult = ConexaoBD.select("SELECT COUNT(*) FROM Reserva WHERE estado IN ('EM_CURSO', 'CONCLUIDA')",
                new InteiroMapper()).get(0);
        int result = instance.listarEstadias(SEMPRE_INICIO, SEMPRE_FIM).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarEstadiasPeriodoSemReservas() {
        System.out.println("listarEstadias (período sem reservas)");
        int expResult = 0;
        int result = instance.listarEstadias(INICIO_ANTIGO, FIM_ANTIGO).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testReceitaPorTipoQuartoPeriodoSemReservas() {
        System.out.println("receitaPorTipoQuarto (período sem reservas)");
        int expResult = 0;
        int result = instance.receitaPorTipoQuarto(INICIO_ANTIGO, FIM_ANTIGO).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testReceitaPorTipoQuartoSoReservasFeitas() {
        System.out.println("receitaPorTipoQuarto (soma só as reservas em curso e concluídas)");
        BigDecimal expResult = ConexaoBD.select("SELECT ISNULL(SUM(precoTotal + taxaHorario), 0) FROM Reserva "
                + "WHERE estado IN ('EM_CURSO', 'CONCLUIDA')", new ValorTesteMapper()).get(0).setScale(2);
        BigDecimal result = EstatisticaBLL.somar(instance.receitaPorTipoQuarto(SEMPRE_INICIO, SEMPRE_FIM))
                .setScale(2);
        assertEquals(expResult, result);
    }

    @Test
    public void testReceitaPorServicoPeriodoSemReservas() {
        System.out.println("receitaPorServico (período sem reservas)");
        int expResult = 0;
        int result = instance.receitaPorServico(INICIO_ANTIGO, FIM_ANTIGO).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testReceitaPrevistaPeriodoSemReservas() {
        System.out.println("receitaPrevista (período sem reservas dá 0)");
        int expResult = 0;
        int result = instance.receitaPrevista(INICIO_ANTIGO, FIM_ANTIGO).signum();
        assertEquals(expResult, result);
    }

    @Test
    public void testNoitesVendidasPeriodoSemReservas() {
        System.out.println("noitesVendidas (período sem reservas dá 0)");
        int expResult = 0;
        int result = instance.noitesVendidas(INICIO_ANTIGO, FIM_ANTIGO);
        assertEquals(expResult, result);
    }

    @Test
    public void testDuracaoMediaPeriodoSemReservas() {
        System.out.println("duracaoMedia (período sem reservas não tem média)");
        BigDecimal expResult = null;
        BigDecimal result = instance.duracaoMedia(INICIO_ANTIGO, FIM_ANTIGO);
        assertEquals(expResult, result);
    }

    @Test
    public void testGuardaDataDeCriacao() {
        System.out.println("guardaDataDeCriacao (diz se a coluna criadaEm já existe)");
        boolean expResult = ConexaoBD.select("SELECT COUNT(*) FROM sys.columns WHERE object_id = OBJECT_ID('Reserva') "
                + "AND name = 'criadaEm'", new InteiroMapper()).get(0) == 1;
        boolean result = instance.guardaDataDeCriacao();
        assertEquals(expResult, result);
    }

    @Test
    public void testAntecedenciaMediaPeriodoSemReservas() {
        System.out.println("antecedenciaMedia (período sem reservas não tem média)");
        Assumptions.assumeTrue(instance.guardaDataDeCriacao(), "Falta correr o 28_data_criacao_reservas.sql");
        BigDecimal expResult = null;
        BigDecimal result = instance.antecedenciaMedia(INICIO_ANTIGO, FIM_ANTIGO);
        assertEquals(expResult, result);
    }

    @Test
    public void testReservasPorEstado() {
        System.out.println("reservasPorEstado (a soma dá todas as reservas)");
        int expResult = ConexaoBD.select("SELECT COUNT(*) FROM Reserva", new InteiroMapper()).get(0);
        int result = EstatisticaBLL.somar(instance.reservasPorEstado(SEMPRE_INICIO, SEMPRE_FIM)).intValue();
        assertEquals(expResult, result);
    }

    @Test
    public void testServicosMaisUsadosPeriodoSemReservas() {
        System.out.println("servicosMaisUsados (período sem reservas)");
        int expResult = 0;
        int result = instance.servicosMaisUsados(INICIO_ANTIGO, FIM_ANTIGO).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testMateriaisMaisGastosPeriodoSemSaidas() {
        System.out.println("materiaisMaisGastos (período sem saídas de stock)");
        int expResult = 0;
        int result = instance.materiaisMaisGastos(INICIO_ANTIGO, FIM_ANTIGO).size();
        assertEquals(expResult, result);
    }

    private static class ValorTesteMapper implements RowMapper<BigDecimal> {
        @Override
        public BigDecimal map(ResultSet rs) throws SQLException {
            return rs.getBigDecimal(1);
        }
    }
}
