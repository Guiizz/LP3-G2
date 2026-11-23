package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import pt.isep.lp3.g2.hotel4youg2.bll.EstatisticaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Estatistica;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EstatisticasController {

    private static final String SEM_DADOS = "sem dados";

    private final EstatisticaBLL estatisticaBLL = new EstatisticaBLL();

    @FXML
    private DatePicker inicioPicker;
    @FXML
    private DatePicker fimPicker;
    @FXML
    private Label ocupacaoLabel;
    @FXML
    private Label precoMedioLabel;
    @FXML
    private Label revparLabel;
    @FXML
    private Label receitaLabel;
    @FXML
    private Label previstaLabel;
    @FXML
    private Label duracaoLabel;
    @FXML
    private Label antecedenciaLabel;
    @FXML
    private Label cancelamentoLabel;
    @FXML
    private BarChart<String, Number> ocupacaoChart;
    @FXML
    private PieChart receitaQuartosChart;
    @FXML
    private PieChart receitaServicosChart;
    @FXML
    private BarChart<Number, String> servicosChart;
    @FXML
    private BarChart<Number, String> materiaisChart;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        aoRepor();
    }

    @FXML
    private void aoRepor() {
        LocalDate hoje = LocalDate.now();
        escolher(inicioPicker, EstatisticaBLL.inicioPorOmissao(hoje));
        escolher(fimPicker, EstatisticaBLL.fimPorOmissao(hoje));
        aoMostrar();
    }

    @FXML
    private void aoMostrar() {
        limpar();
        try {
            Utilizador quemAge = Sessao.getUtilizador();
            LocalDate inicio = dataEscolhida(inicioPicker);
            LocalDate fim = dataEscolhida(fimPicker);
            mostrarNumeros(quemAge, inicio, fim);
            mostrarGraficos(quemAge, inicio, fim);
            Mensagens.info(mensagemLabel, "A ocupação, a receita e o RevPAR contam as reservas em curso e "
                    + "concluídas; as confirmadas só entram na receita prevista. Passe o rato por cima de uma barra "
                    + "ou de uma fatia para ver o valor.");
        } catch (DateTimeParseException e) {
            limpar();
            Mensagens.erro(mensagemLabel, "A data não é válida. Escolha-a no calendário.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            limpar();
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            limpar();
            Mensagens.erro(mensagemLabel, "Não foi possível carregar as estatísticas. Tente novamente.");
        }
    }

    private void mostrarNumeros(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        BigDecimal receitaQuartos = EstatisticaBLL.somar(estatisticaBLL.receitaPorTipoQuarto(quemAge, inicio, fim));
        BigDecimal receitaServicos = EstatisticaBLL.somar(estatisticaBLL.receitaPorServico(quemAge, inicio, fim));
        ocupacaoLabel.setText(Graficos.numero(estatisticaBLL.ocupacaoMedia(quemAge, inicio, fim))
                + Graficos.PERCENTAGEM);
        precoMedioLabel.setText(Graficos.euros(estatisticaBLL.precoMedioPorNoite(quemAge, inicio, fim)));
        revparLabel.setText(Graficos.euros(estatisticaBLL.receitaPorQuartoDisponivel(quemAge, inicio, fim)));
        receitaLabel.setText(Graficos.euros(receitaQuartos.add(receitaServicos)));
        previstaLabel.setText("quartos " + Graficos.euros(receitaQuartos) + " + serviços "
                + Graficos.euros(receitaServicos) + "; prevista das confirmadas: "
                + Graficos.euros(estatisticaBLL.receitaPrevista(quemAge, inicio, fim)));
        duracaoLabel.setText(textoOuSemDados(estatisticaBLL.duracaoMedia(quemAge, inicio, fim), " noite(s)"));
        antecedenciaLabel.setText(textoOuSemDados(estatisticaBLL.antecedenciaMedia(quemAge, inicio, fim),
                " dia(s)"));
        cancelamentoLabel.setText(Graficos.numero(estatisticaBLL.taxaDeCancelamento(quemAge, inicio, fim))
                + Graficos.PERCENTAGEM);
    }

    private void mostrarGraficos(Utilizador quemAge, LocalDate inicio, LocalDate fim) {
        List<Estatistica> ocupacao = estatisticaBLL.ocupacaoPorMes(quemAge, inicio, fim);
        Graficos.mostrarBarras(ocupacaoChart, ocupacao, Graficos.PERCENTAGEM);
        Graficos.mostrarCircular(receitaQuartosChart, estatisticaBLL.receitaPorTipoQuarto(quemAge, inicio, fim),
                Graficos.EUROS);
        Graficos.mostrarCircular(receitaServicosChart, estatisticaBLL.receitaPorServico(quemAge, inicio, fim),
                Graficos.EUROS);
        Graficos.mostrarRanking(servicosChart, estatisticaBLL.servicosMaisUsados(quemAge, inicio, fim), " vez(es)");
        Graficos.mostrarRanking(materiaisChart, estatisticaBLL.materiaisMaisGastos(quemAge, inicio, fim),
                " unidade(s)");
    }

    private void limpar() {
        ocupacaoLabel.setText("-");
        precoMedioLabel.setText("-");
        revparLabel.setText("-");
        receitaLabel.setText("-");
        previstaLabel.setText("");
        duracaoLabel.setText("-");
        antecedenciaLabel.setText("-");
        cancelamentoLabel.setText("-");
        Graficos.limpar(ocupacaoChart);
        Graficos.limpar(receitaQuartosChart);
        Graficos.limpar(receitaServicosChart);
        Graficos.limpar(servicosChart);
        Graficos.limpar(materiaisChart);
        Mensagens.info(mensagemLabel, "");
    }

    private static String textoOuSemDados(BigDecimal valor, String unidade) {
        return valor == null ? SEM_DADOS : Graficos.numero(valor) + unidade;
    }

    private static void escolher(DatePicker picker, LocalDate dia) {
        picker.setValue(dia);
        picker.getEditor().setText(picker.getConverter().toString(dia));
    }

    private static LocalDate dataEscolhida(DatePicker picker) {
        String texto = picker.getEditor().getText();
        if (texto == null || texto.isBlank()) {
            picker.setValue(null);
            return null;
        }
        LocalDate dia = picker.getConverter().fromString(texto.trim());
        picker.setValue(dia);
        return dia;
    }
}
