package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IEstatisticaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Estadia;
import pt.isep.lp3.g2.hotel4youg2.model.Estatistica;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class EstatisticaBLLTest {

    private static class EstatisticaDALFalsa implements IEstatisticaDAL {
        private int quartosAtivos;
        private int noitesVendidas;
        private boolean guardaData = true;
        private BigDecimal prevista = BigDecimal.ZERO;
        private BigDecimal duracao;
        private BigDecimal antecedencia;
        private final List<Estadia> estadias = new ArrayList<>();
        private final List<Estatistica> receitaQuartos = new ArrayList<>();
        private final List<Estatistica> receitaServicos = new ArrayList<>();
        private final List<Estatistica> porEstado = new ArrayList<>();
        private final List<Estatistica> servicos = new ArrayList<>();
        private final List<Estatistica> materiais = new ArrayList<>();

        @Override
        public int contarQuartosAtivos() {
            return quartosAtivos;
        }

        @Override
        public List<Estadia> listarEstadias(LocalDate inicio, LocalDate fim) {
            return estadias;
        }

        @Override
        public List<Estatistica> receitaPorTipoQuarto(LocalDate inicio, LocalDate fim) {
            return receitaQuartos;
        }

        @Override
        public List<Estatistica> receitaPorServico(LocalDate inicio, LocalDate fim) {
            return receitaServicos;
        }

        @Override
        public BigDecimal receitaPrevista(LocalDate inicio, LocalDate fim) {
            return prevista;
        }

        @Override
        public int noitesVendidas(LocalDate inicio, LocalDate fim) {
            return noitesVendidas;
        }

        @Override
        public BigDecimal duracaoMedia(LocalDate inicio, LocalDate fim) {
            return duracao;
        }

        @Override
        public boolean guardaDataDeCriacao() {
            return guardaData;
        }

        @Override
        public BigDecimal antecedenciaMedia(LocalDate inicio, LocalDate fim) {
            return antecedencia;
        }

        @Override
        public List<Estatistica> reservasPorEstado(LocalDate inicio, LocalDate fim) {
            return porEstado;
        }

        @Override
        public List<Estatistica> servicosMaisUsados(LocalDate inicio, LocalDate fim) {
            return servicos;
        }

        @Override
        public List<Estatistica> materiaisMaisGastos(LocalDate inicio, LocalDate fim) {
            return materiais;
        }
    }

    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);
    private static final LocalDate INICIO_NOVEMBRO = LocalDate.of(2026, 11, 1);
    private static final LocalDate FIM_NOVEMBRO = LocalDate.of(2026, 11, 30);

    private EstatisticaDALFalsa estatisticaDAL;
    private EstatisticaBLL instance;

    @BeforeEach
    public void setUp() {
        estatisticaDAL = new EstatisticaDALFalsa();
        estatisticaDAL.quartosAtivos = 2;
        instance = new EstatisticaBLL(estatisticaDAL);
    }

    @AfterEach
    public void tearDown() {
        estatisticaDAL.estadias.clear();
        estatisticaDAL.receitaQuartos.clear();
        estatisticaDAL.receitaServicos.clear();
        estatisticaDAL.porEstado.clear();
        estatisticaDAL.servicos.clear();
        estatisticaDAL.materiais.clear();
        instance = null;
    }

    private static Estatistica estatistica(String rotulo, String valor) {
        return new Estatistica(rotulo, new BigDecimal(valor));
    }

    private static List<Estatistica> numerados(int quantos) {
        List<Estatistica> lista = new ArrayList<>();
        for (int i = 1; i <= quantos; i++) {
            lista.add(estatistica("Item " + i, String.valueOf(100 - i)));
        }
        return lista;
    }

    private void receitaDosQuartos() {
        estatisticaDAL.receitaQuartos.add(estatistica("DUPLO", "300.00"));
        estatisticaDAL.receitaQuartos.add(estatistica("INDIVIDUAL", "100.00"));
    }

    @Test
    public void testOcupacaoPorMes() {
        System.out.println("ocupacaoPorMes");
        estatisticaDAL.estadias.add(new Estadia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4)));
        List<Estatistica> expResult = List.of(estatistica("11/2026", "5.0"));
        List<Estatistica> result = instance.ocupacaoPorMes(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testOcupacaoPorMesEstadiaEntreDoisMeses() {
        System.out.println("ocupacaoPorMes (a estadia conta as noites em cada mês)");
        estatisticaDAL.quartosAtivos = 1;
        estatisticaDAL.estadias.add(new Estadia(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 2)));
        List<Estatistica> expResult = List.of(estatistica("10/2026", "50.0"), estatistica("11/2026", "50.0"));
        List<Estatistica> result = instance.ocupacaoPorMes(GESTOR, LocalDate.of(2026, 10, 30),
                LocalDate.of(2026, 11, 2));
        assertEquals(expResult, result);
    }

    @Test
    public void testOcupacaoPorMesMostraOsMesesVazios() {
        System.out.println("ocupacaoPorMes (os meses sem reservas ficam a 0%)");
        List<Estatistica> expResult = List.of(estatistica("12/2026", "0.0"), estatistica("01/2027", "0.0"));
        List<Estatistica> result = instance.ocupacaoPorMes(GESTOR, LocalDate.of(2026, 12, 15),
                LocalDate.of(2027, 1, 15));
        assertEquals(expResult, result);
    }

    @Test
    public void testOcupacaoPorMesSemQuartos() {
        System.out.println("ocupacaoPorMes (sem quartos ativos fica a 0%)");
        estatisticaDAL.quartosAtivos = 0;
        List<Estatistica> expResult = List.of(estatistica("11/2026", "0.0"));
        List<Estatistica> result = instance.ocupacaoPorMes(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testOcupacaoMedia() {
        System.out.println("ocupacaoMedia (3 noites em 2 quartos durante 30 dias)");
        estatisticaDAL.estadias.add(new Estadia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4)));
        BigDecimal expResult = new BigDecimal("5.0");
        BigDecimal result = instance.ocupacaoMedia(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testOcupacaoMediaSoNoitesDoPeriodo() {
        System.out.println("ocupacaoMedia (só conta as noites dentro do período)");
        estatisticaDAL.quartosAtivos = 1;
        estatisticaDAL.estadias.add(new Estadia(LocalDate.of(2026, 10, 25), LocalDate.of(2026, 11, 6)));
        BigDecimal expResult = new BigDecimal("50.0");
        BigDecimal result = instance.ocupacaoMedia(GESTOR, INICIO_NOVEMBRO, LocalDate.of(2026, 11, 10));
        assertEquals(expResult, result);
    }

    @Test
    public void testPrecoMedioPorNoite() {
        System.out.println("precoMedioPorNoite (ADR: 400 € de quartos em 5 noites)");
        receitaDosQuartos();
        estatisticaDAL.noitesVendidas = 5;
        BigDecimal expResult = new BigDecimal("80.00");
        BigDecimal result = instance.precoMedioPorNoite(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testPrecoMedioPorNoiteSemNoites() {
        System.out.println("precoMedioPorNoite (sem noites vendidas fica a 0)");
        BigDecimal expResult = new BigDecimal("0.00");
        BigDecimal result = instance.precoMedioPorNoite(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testReceitaPorQuartoDisponivel() {
        System.out.println("receitaPorQuartoDisponivel (RevPAR: 400 € em 2 quartos durante 30 dias)");
        receitaDosQuartos();
        BigDecimal expResult = new BigDecimal("6.67");
        BigDecimal result = instance.receitaPorQuartoDisponivel(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testReceitaPorQuartoDisponivelBateComPrecoMedioVezesOcupacao() {
        System.out.println("receitaPorQuartoDisponivel (é o preço médio × a ocupação: 100 € × 1 % = 1 €)");
        estatisticaDAL.quartosAtivos = 10;
        estatisticaDAL.estadias.add(new Estadia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4)));
        estatisticaDAL.receitaQuartos.add(estatistica("DUPLO", "300.00"));
        estatisticaDAL.noitesVendidas = 3;
        BigDecimal expResult = instance.precoMedioPorNoite(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO)
                .multiply(instance.ocupacaoMedia(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal result = instance.receitaPorQuartoDisponivel(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testReceitaPorQuartoDisponivelSemQuartos() {
        System.out.println("receitaPorQuartoDisponivel (sem quartos ativos fica a 0)");
        estatisticaDAL.quartosAtivos = 0;
        receitaDosQuartos();
        BigDecimal expResult = new BigDecimal("0.00");
        BigDecimal result = instance.receitaPorQuartoDisponivel(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testReceitaPorTipoQuarto() {
        System.out.println("receitaPorTipoQuarto (os três tipos, mesmo sem receita)");
        estatisticaDAL.receitaQuartos.add(estatistica("DUPLO", "300.00"));
        List<Estatistica> expResult = List.of(estatistica("Individual", "0"), estatistica("Duplo", "300"),
                estatistica("Família", "0"));
        List<Estatistica> result = instance.receitaPorTipoQuarto(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testReceitaPorServico() {
        System.out.println("receitaPorServico (mostra todos os serviços)");
        estatisticaDAL.receitaServicos.addAll(numerados(12));
        List<Estatistica> expResult = numerados(12);
        List<Estatistica> result = instance.receitaPorServico(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testReceitaPrevista() {
        System.out.println("receitaPrevista");
        estatisticaDAL.prevista = new BigDecimal("520.00");
        BigDecimal expResult = new BigDecimal("520.00");
        BigDecimal result = instance.receitaPrevista(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testDuracaoMedia() {
        System.out.println("duracaoMedia (arredonda a uma casa decimal)");
        estatisticaDAL.duracao = new BigDecimal("2.333333");
        BigDecimal expResult = new BigDecimal("2.3");
        BigDecimal result = instance.duracaoMedia(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testDuracaoMediaSemReservas() {
        System.out.println("duracaoMedia (sem reservas não há média)");
        BigDecimal expResult = null;
        BigDecimal result = instance.duracaoMedia(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testAntecedenciaMedia() {
        System.out.println("antecedenciaMedia (arredonda a uma casa decimal)");
        estatisticaDAL.antecedencia = new BigDecimal("12.35");
        BigDecimal expResult = new BigDecimal("12.4");
        BigDecimal result = instance.antecedenciaMedia(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testAntecedenciaMediaSemReservasComData() {
        System.out.println("antecedenciaMedia (sem reservas com a data em que foram feitas)");
        BigDecimal expResult = null;
        BigDecimal result = instance.antecedenciaMedia(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testAntecedenciaMediaSemAColunaNaBD() {
        System.out.println("antecedenciaMedia (a BD ainda não guarda a data em que a reserva foi feita)");
        estatisticaDAL.guardaData = false;
        estatisticaDAL.antecedencia = new BigDecimal("12.35");
        BigDecimal expResult = null;
        BigDecimal result = instance.antecedenciaMedia(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testTaxaDeCancelamento() {
        System.out.println("taxaDeCancelamento (1 cancelada em 4 reservas)");
        estatisticaDAL.porEstado.add(estatistica("CANCELADA", "1"));
        estatisticaDAL.porEstado.add(estatistica("CONCLUIDA", "2"));
        estatisticaDAL.porEstado.add(estatistica("CONFIRMADA", "1"));
        BigDecimal expResult = new BigDecimal("25.0");
        BigDecimal result = instance.taxaDeCancelamento(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testTaxaDeCancelamentoSemReservas() {
        System.out.println("taxaDeCancelamento (sem reservas fica a 0%)");
        BigDecimal expResult = new BigDecimal("0.0");
        BigDecimal result = instance.taxaDeCancelamento(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testServicosMaisUsados() {
        System.out.println("servicosMaisUsados");
        estatisticaDAL.servicos.add(estatistica("Pequeno-almoço", "8"));
        estatisticaDAL.servicos.add(estatistica("Massagem", "2"));
        List<Estatistica> expResult = List.of(estatistica("Pequeno-almoço", "8"), estatistica("Massagem", "2"));
        List<Estatistica> result = instance.servicosMaisUsados(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testServicosMaisUsadosSoOsPrimeiros() {
        System.out.println("servicosMaisUsados (só mostra os 10 primeiros)");
        estatisticaDAL.servicos.addAll(numerados(12));
        List<Estatistica> expResult = numerados(10);
        List<Estatistica> result = instance.servicosMaisUsados(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testMateriaisMaisGastos() {
        System.out.println("materiaisMaisGastos (só mostra os 10 primeiros)");
        estatisticaDAL.materiais.addAll(numerados(11));
        List<Estatistica> expResult = numerados(10);
        List<Estatistica> result = instance.materiaisMaisGastos(GESTOR, INICIO_NOVEMBRO, FIM_NOVEMBRO);
        assertEquals(expResult, result);
    }

    @Test
    public void testSomar() {
        System.out.println("somar");
        BigDecimal expResult = new BigDecimal("350.50");
        BigDecimal result = EstatisticaBLL.somar(List.of(estatistica("Duplo", "300.00"),
                estatistica("Família", "50.50")));
        assertEquals(expResult, result);
    }

    @Test
    public void testPercentagemDoTotal() {
        System.out.println("percentagemDoTotal");
        BigDecimal expResult = new BigDecimal("61.8");
        BigDecimal result = EstatisticaBLL.percentagemDoTotal(new BigDecimal("890.00"), new BigDecimal("1440.00"));
        assertEquals(expResult, result);
    }

    @Test
    public void testPercentagemDoTotalSemTotal() {
        System.out.println("percentagemDoTotal (total a zero dá 0%)");
        BigDecimal expResult = new BigDecimal("0.0");
        BigDecimal result = EstatisticaBLL.percentagemDoTotal(BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(expResult, result);
    }

    @Test
    public void testInicioPorOmissao() {
        System.out.println("inicioPorOmissao (os últimos 12 meses começam no dia 1)");
        LocalDate expResult = LocalDate.of(2025, 12, 1);
        LocalDate result = EstatisticaBLL.inicioPorOmissao(LocalDate.of(2026, 11, 19));
        assertEquals(expResult, result);
    }

    @Test
    public void testFimPorOmissao() {
        System.out.println("fimPorOmissao (acaba no fim do mês atual)");
        LocalDate expResult = LocalDate.of(2026, 11, 30);
        LocalDate result = EstatisticaBLL.fimPorOmissao(LocalDate.of(2026, 11, 19));
        assertEquals(expResult, result);
    }

    @Test
    public void testSoOGestorVe() {
        System.out.println("ocupacaoMedia (o rececionista não pode ver)");
        String expResult = "Só o gestor pode ver as estatísticas do hotel.";
        String result = "";
        try {
            instance.ocupacaoMedia(RECECAO, INICIO_NOVEMBRO, FIM_NOVEMBRO);
            fail("Devia ter recusado o rececionista");
        } catch (IllegalStateException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testSemSessao() {
        System.out.println("receitaPorTipoQuarto (sem sessão iniciada)");
        String expResult = "Só o gestor pode ver as estatísticas do hotel.";
        String result = "";
        try {
            instance.receitaPorTipoQuarto(null, INICIO_NOVEMBRO, FIM_NOVEMBRO);
            fail("Devia ter recusado sem sessão");
        } catch (IllegalStateException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testSemDataDeInicio() {
        System.out.println("precoMedioPorNoite (sem data de início)");
        String expResult = "Escolha a data de início e a data de fim do período.";
        String result = "";
        try {
            instance.precoMedioPorNoite(GESTOR, null, FIM_NOVEMBRO);
            fail("Devia ter pedido a data de início");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testSemDataDeFim() {
        System.out.println("materiaisMaisGastos (sem data de fim)");
        String expResult = "Escolha a data de início e a data de fim do período.";
        String result = "";
        try {
            instance.materiaisMaisGastos(GESTOR, INICIO_NOVEMBRO, null);
            fail("Devia ter pedido a data de fim");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testInicioDepoisDoFim() {
        System.out.println("taxaDeCancelamento (início depois do fim)");
        String expResult = "A data de início não pode ser depois da data de fim.";
        String result = "";
        try {
            instance.taxaDeCancelamento(GESTOR, FIM_NOVEMBRO, INICIO_NOVEMBRO);
            fail("Devia ter recusado o período");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }
}
