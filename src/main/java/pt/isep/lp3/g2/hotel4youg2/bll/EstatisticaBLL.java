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

public class EstatisticaBLL {

    public static final int MAXIMO_NO_GRAFICO = 10;

    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final IEstatisticaDAL estatisticaDAL;

    public EstatisticaBLL() {
        this(new EstatisticaDAL());
    }

    public EstatisticaBLL(IEstatisticaDAL estatisticaDAL) {
        this.estatisticaDAL = estatisticaDAL;
    }

    public static LocalDate inicioPorOmissao(LocalDate hoje) {
        return hoje.minusMonths(11).withDayOfMonth(1);
    }

    public static LocalDate fimPorOmissao(LocalDate hoje) {
        return YearMonth.from(hoje).atEndOfMonth();
    }

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

    public BigDecimal ocupacaoMedia(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        int quartos = estatisticaDAL.contarQuartosAtivos();
        int noites = noitesOcupadas(estatisticaDAL.listarEstadias(inicio, fim), inicio, fim);
        return percentagem(noites, diasEntre(inicio, fim) * quartos);
    }

    public BigDecimal precoMedioPorNoite(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        int noites = estatisticaDAL.noitesVendidas(inicio, fim);
        if (noites == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal receitaQuartos = somar(estatisticaDAL.receitaPorTipoQuarto(inicio, fim));
        return receitaQuartos.divide(BigDecimal.valueOf(noites), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal receitaPorQuartoDisponivel(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        int disponiveis = estatisticaDAL.contarQuartosAtivos() * diasEntre(inicio, fim);
        if (disponiveis == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal receitaQuartos = somar(estatisticaDAL.receitaPorTipoQuarto(inicio, fim));
        return receitaQuartos.divide(BigDecimal.valueOf(disponiveis), 2, RoundingMode.HALF_UP);
    }

    public List<Estatistica> receitaPorTipoQuarto(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        List<Estatistica> daBD = estatisticaDAL.receitaPorTipoQuarto(inicio, fim);
        List<Estatistica> porTipo = new ArrayList<>();
        for (TipoQuarto tipo : TipoQuarto.values()) {
            porTipo.add(new Estatistica(nomeDoTipo(tipo), valorDe(daBD, tipo.name())));
        }
        return porTipo;
    }

    public List<Estatistica> receitaPorServico(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        return estatisticaDAL.receitaPorServico(inicio, fim);
    }

    public BigDecimal receitaPrevista(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        return estatisticaDAL.receitaPrevista(inicio, fim);
    }

    public BigDecimal duracaoMedia(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        BigDecimal media = estatisticaDAL.duracaoMedia(inicio, fim);
        return media == null ? null : media.setScale(1, RoundingMode.HALF_UP);
    }

    public BigDecimal antecedenciaMedia(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        if (!estatisticaDAL.guardaDataDeCriacao()) {
            return null;
        }
        BigDecimal media = estatisticaDAL.antecedenciaMedia(inicio, fim);
        return media == null ? null : media.setScale(1, RoundingMode.HALF_UP);
    }

    public BigDecimal taxaDeCancelamento(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        List<Estatistica> porEstado = estatisticaDAL.reservasPorEstado(inicio, fim);
        int canceladas = valorDe(porEstado, "CANCELADA").intValue();
        return percentagem(canceladas, somar(porEstado).intValue());
    }

    public List<Estatistica> servicosMaisUsados(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        return primeiros(estatisticaDAL.servicosMaisUsados(inicio, fim));
    }

    public List<Estatistica> materiaisMaisGastos(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        validar(quemAge, inicio, fim);
        return primeiros(estatisticaDAL.materiaisMaisGastos(inicio, fim));
    }

    public static BigDecimal somar(List<Estatistica> estatisticas) {
        BigDecimal total = BigDecimal.ZERO;
        for (Estatistica estatistica : estatisticas) {
            total = total.add(estatistica.getValor());
        }
        return total;
    }

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
