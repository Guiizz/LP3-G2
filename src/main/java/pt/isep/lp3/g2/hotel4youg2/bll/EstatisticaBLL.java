package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.EstatisticaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IEstatisticaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Estadia;
import pt.isep.lp3.g2.hotel4youg2.model.Estatistica;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio das estatísticas do hotel. Só o gestor as pode ver.
 */
public class EstatisticaBLL {

    /** Número máximo de linhas nos gráficos dos serviços e dos materiais. */
    public static final int MAXIMO_NO_GRAFICO = 10;

    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final IEstatisticaDAL estatisticaDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public EstatisticaBLL() {
        this(new EstatisticaDAL());
    }

    /**
     * Cria o BLL com outro DAL (usado nos testes).
     *
     * @param estatisticaDAL de onde vêm os dados
     */
    public EstatisticaBLL(IEstatisticaDAL estatisticaDAL) {
        this.estatisticaDAL = estatisticaDAL;
    }

    /**
     * @param hoje o dia de hoje
     * @return o primeiro dia de há 11 meses (o período mostra os últimos 12 meses)
     */
    public static LocalDate inicioPorOmissao(LocalDate hoje) {
        return hoje.minusMonths(11).withDayOfMonth(1);
    }

    /**
     * @param hoje o dia de hoje
     * @return o último dia do mês atual
     */
    public static LocalDate fimPorOmissao(LocalDate hoje) {
        return YearMonth.from(hoje).atEndOfMonth();
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return a ocupação de cada mês do período, em percentagem
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public List<Estatistica> ocupacaoPorMes(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        int quartos = estatisticaDAL.contarQuartosAtivos();
        List<Estadia> estadias = estatisticaDAL.listarEstadias(inicio, fim);
        List<Estatistica> meses = new ArrayList<>();
        YearMonth mes = YearMonth.from(inicio);
        YearMonth ultimo = YearMonth.from(fim);
        while (!mes.isAfter(ultimo)) {
            LocalDate primeiroDia = maisTarde(mes.atDay(1), inicio);
            LocalDate ultimoDia = maisCedo(mes.atEndOfMonth(), fim);
            int dias = diasEntre(primeiroDia, ultimoDia);
            int noites = noitesOcupadas(estadias, primeiroDia, ultimoDia);
            meses.add(new Estatistica(mes.format(MES), percentagem(noites, dias * quartos)));
            mes = mes.plusMonths(1);
        }
        return meses;
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return a ocupação média do período, em percentagem
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public BigDecimal ocupacaoMedia(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        int quartos = estatisticaDAL.contarQuartosAtivos();
        int noites = noitesOcupadas(estatisticaDAL.listarEstadias(inicio, fim), inicio, fim);
        return percentagem(noites, diasEntre(inicio, fim) * quartos);
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return o preço médio por noite vendida (ADR)
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public BigDecimal precoMedioPorNoite(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        int noites = estatisticaDAL.noitesVendidas(inicio, fim);
        if (noites == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal receitaQuartos = somar(estatisticaDAL.receitaPorTipoQuarto(inicio, fim));
        return receitaQuartos.divide(BigDecimal.valueOf(noites), 2, RoundingMode.HALF_UP);
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return a receita por quarto disponível por noite (RevPAR)
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public BigDecimal receitaPorQuartoDisponivel(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        int disponiveis = estatisticaDAL.contarQuartosAtivos() * diasEntre(inicio, fim);
        if (disponiveis == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal receitaQuartos = somar(estatisticaDAL.receitaPorTipoQuarto(inicio, fim));
        return receitaQuartos.divide(BigDecimal.valueOf(disponiveis), 2, RoundingMode.HALF_UP);
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return a receita de cada tipo de quarto (os três, mesmo sem receita)
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public List<Estatistica> receitaPorTipoQuarto(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        List<Estatistica> daBD = estatisticaDAL.receitaPorTipoQuarto(inicio, fim);
        List<Estatistica> porTipo = new ArrayList<>();
        for (TipoQuarto tipo : TipoQuarto.values()) {
            porTipo.add(new Estatistica(nomeDoTipo(tipo), valorDe(daBD, tipo.name())));
        }
        return porTipo;
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return a receita de cada serviço extra
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public List<Estatistica> receitaPorServico(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        return estatisticaDAL.receitaPorServico(inicio, fim);
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return a receita das reservas confirmadas do período
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public BigDecimal receitaPrevista(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        return estatisticaDAL.receitaPrevista(inicio, fim);
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return a média de noites por reserva, ou {@code null} se não houver reservas
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public BigDecimal duracaoMedia(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        BigDecimal media = estatisticaDAL.duracaoMedia(inicio, fim);
        return media == null ? null : media.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return a média de dias entre criar a reserva e a entrada, ou {@code null} se não houver dados
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public BigDecimal antecedenciaMedia(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        if (!estatisticaDAL.guardaDataDeCriacao()) {
            return null;
        }
        BigDecimal media = estatisticaDAL.antecedenciaMedia(inicio, fim);
        return media == null ? null : media.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return a percentagem de reservas canceladas
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public BigDecimal taxaDeCancelamento(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        List<Estatistica> porEstado = estatisticaDAL.reservasPorEstado(inicio, fim);
        int canceladas = valorDe(porEstado, "CANCELADA").intValue();
        return percentagem(canceladas, somar(porEstado).intValue());
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return os serviços mais pedidos (no máximo {@link #MAXIMO_NO_GRAFICO})
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public List<Estatistica> servicosMaisUsados(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        return primeiros(estatisticaDAL.servicosMaisUsados(inicio, fim));
    }

    /**
     * @param quemAge o utilizador com a sessão iniciada
     * @param inicio  o primeiro dia do período
     * @param fim     o último dia do período
     * @return os materiais que mais saíram de vez (no máximo {@link #MAXIMO_NO_GRAFICO})
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar uma data ou o início for depois do fim
     */
    public List<Estatistica> materiaisMaisGastos(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        return primeiros(estatisticaDAL.materiaisMaisGastos(inicio, fim));
    }

    /**
     * @param estatisticas os valores a somar
     * @return a soma dos valores
     */
    public static BigDecimal somar(List<Estatistica> estatisticas) {
        BigDecimal total = BigDecimal.ZERO;
        for (Estatistica estatistica : estatisticas) {
            total = total.add(estatistica.getValor());
        }
        return total;
    }

    /**
     * @param parte a parte
     * @param total o total
     * @return a parte em percentagem do total, com uma casa decimal; 0 se o total for 0
     */
    public static BigDecimal percentagemDoTotal(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return parte.multiply(CEM).divide(total, 1, RoundingMode.HALF_UP);
    }

    static String nomeDoTipo(TipoQuarto tipo) {
        switch (tipo) {
            case INDIVIDUAL:
                return "Individual";
            case DUPLO:
                return "Duplo";
            default:
                return "Família";
        }
    }

    private static void validar(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode ver as estatísticas do hotel.");
        }
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Escolha a data de início e a data de fim do período.");
        }
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data de início não pode ser depois da data de fim.");
        }
    }

    private static int diasEntre(LocalDate inicio, LocalDate fim) {
        return (int) ChronoUnit.DAYS.between(inicio, fim) + 1;
    }

    private static int noitesOcupadas(List<Estadia> estadias, LocalDate inicio, LocalDate fim) {
        int noites = 0;
        for (LocalDate dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
            for (Estadia estadia : estadias) {
                if (estadia.ocupaNoite(dia)) {
                    noites++;
                }
            }
        }
        return noites;
    }

    private static BigDecimal percentagem(int parte, int total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return BigDecimal.valueOf(parte).multiply(CEM).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal valorDe(List<Estatistica> estatisticas, String rotulo) {
        for (Estatistica estatistica : estatisticas) {
            if (rotulo.equals(estatistica.getRotulo())) {
                return estatistica.getValor();
            }
        }
        return BigDecimal.ZERO;
    }

    private static List<Estatistica> primeiros(List<Estatistica> estatisticas) {
        if (estatisticas.size() <= MAXIMO_NO_GRAFICO) {
            return estatisticas;
        }
        return new ArrayList<>(estatisticas.subList(0, MAXIMO_NO_GRAFICO));
    }

    private static LocalDate maisTarde(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDate maisCedo(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
