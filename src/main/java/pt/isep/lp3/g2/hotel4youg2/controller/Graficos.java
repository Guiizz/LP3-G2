package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import pt.isep.lp3.g2.hotel4youg2.bll.EstatisticaBLL;
import pt.isep.lp3.g2.hotel4youg2.model.Estatistica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Ajuda a mostrar as estatísticas nos gráficos da JavaFX.
 */
public final class Graficos {

    /** Unidade para valores em euros. */
    public static final String EUROS = " €";
    /** Unidade para percentagens. */
    public static final String PERCENTAGEM = " %";

    private static final String SEM_DADOS = "Não há dados neste período.";

    private Graficos() {
    }

    /**
     * Apaga os dados de um gráfico de barras.
     *
     * @param grafico o gráfico
     */
    public static void limpar(BarChart<?, ?> grafico) {
        grafico.getData().clear();
        grafico.setTitle(null);
    }

    /**
     * Apaga os dados de um gráfico circular.
     *
     * @param grafico o gráfico
     */
    public static void limpar(PieChart grafico) {
        grafico.getData().clear();
        grafico.setTitle(null);
    }

    /**
     * Mostra os valores num gráfico de barras na vertical.
     *
     * @param grafico      o gráfico
     * @param estatisticas os valores a mostrar
     * @param unidade      a unidade dos valores ({@link #EUROS} ou {@link #PERCENTAGEM})
     */
    public static void mostrarBarras(BarChart<String, Number> grafico, List<Estatistica> estatisticas, String unidade) {
        grafico.getData().add(serie(estatisticas));
        explicarSerie(grafico.getData().get(0), estatisticas, unidade);
        grafico.setTitle(semValores(estatisticas) ? SEM_DADOS : null);
    }

    /**
     * Mostra os valores num gráfico de barras na horizontal, do maior para o menor.
     *
     * @param grafico      o gráfico
     * @param estatisticas os valores a mostrar
     * @param unidade      a unidade dos valores ({@link #EUROS} ou {@link #PERCENTAGEM})
     */
    public static void mostrarRanking(BarChart<Number, String> grafico, List<Estatistica> estatisticas, String unidade) {
        XYChart.Series<Number, String> serie = new XYChart.Series<>();
        for (int i = estatisticas.size() - 1; i >= 0; i--) {
            Estatistica estatistica = estatisticas.get(i);
            serie.getData().add(new XYChart.Data<Number, String>(estatistica.getValor().doubleValue(),
                    estatistica.getRotulo()));
        }
        grafico.getData().add(serie);
        for (int i = 0; i < estatisticas.size(); i++) {
            explicar(serie.getData().get(i).getNode(), estatisticas.get(estatisticas.size() - 1 - i), unidade);
        }
        grafico.setTitle(estatisticas.isEmpty() ? SEM_DADOS : null);
    }

    /**
     * Mostra os valores num gráfico circular, com a percentagem de cada fatia.
     *
     * @param grafico      o gráfico
     * @param estatisticas os valores a mostrar
     * @param unidade      a unidade dos valores ({@link #EUROS} ou {@link #PERCENTAGEM})
     */
    public static void mostrarCircular(PieChart grafico, List<Estatistica> estatisticas, String unidade) {
        BigDecimal total = EstatisticaBLL.somar(estatisticas);
        for (Estatistica estatistica : estatisticas) {
            if (estatistica.getValor().signum() > 0) {
                BigDecimal fatia = EstatisticaBLL.percentagemDoTotal(estatistica.getValor(), total);
                PieChart.Data dado = new PieChart.Data(estatistica.getRotulo() + " (" + numero(fatia) + " %)",
                        estatistica.getValor().doubleValue());
                grafico.getData().add(dado);
                explicar(dado.getNode(), estatistica, unidade);
            }
        }
        grafico.setTitle(grafico.getData().isEmpty() ? SEM_DADOS : null);
    }

    /**
     * @param valor o valor
     * @return o valor em euros (ex.: 22,50 €)
     */
    public static String euros(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + EUROS;
    }

    /**
     * @param valor o valor
     * @return o valor sem zeros a mais e com vírgula (ex.: 12,5)
     */
    public static String numero(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private static XYChart.Series<String, Number> serie(List<Estatistica> estatisticas) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Estatistica estatistica : estatisticas) {
            serie.getData().add(new XYChart.Data<String, Number>(estatistica.getRotulo(),
                    estatistica.getValor().doubleValue()));
        }
        return serie;
    }

    private static void explicarSerie(XYChart.Series<String, Number> serie, List<Estatistica> estatisticas,
                                      String unidade) {
        for (int i = 0; i < estatisticas.size(); i++) {
            explicar(serie.getData().get(i).getNode(), estatisticas.get(i), unidade);
        }
    }

    private static void explicar(Node parte, Estatistica estatistica, String unidade) {
        if (parte != null) {
            String valor = EUROS.equals(unidade) ? euros(estatistica.getValor())
                    : numero(estatistica.getValor()) + unidade;
            Tooltip.install(parte, new Tooltip(estatistica.getRotulo() + ": " + valor));
        }
    }

    private static boolean semValores(List<Estatistica> estatisticas) {
        return EstatisticaBLL.somar(estatisticas).signum() == 0;
    }
}
