package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.AvaliacaoBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Avaliacao;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Controlador do ecrã "Avaliações das estadias": o gestor vê as avaliações de cada mês e a média.
 */
public class AvaliacoesController {

    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MMMM 'de' yyyy",
            Locale.forLanguageTag("pt-PT"));

    private final AvaliacaoBLL avaliacaoBLL = new AvaliacaoBLL();
    private YearMonth mes = YearMonth.now();

    @FXML
    private Label mesLabel;
    @FXML
    private Label mediaLabel;
    @FXML
    private Label mediaEstrelasLabel;
    @FXML
    private Label quantasLabel;
    @FXML
    private TableView<Avaliacao> tabela;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        carregar();
    }

    @FXML
    private void aoMesAnterior() {
        mes = mes.minusMonths(1);
        carregar();
    }

    @FXML
    private void aoMesSeguinte() {
        mes = mes.plusMonths(1);
        carregar();
    }

    @FXML
    private void aoEsteMes() {
        mes = YearMonth.now();
        carregar();
    }

    private void carregar() {
        mesLabel.setText(mes.format(MES));
        tabela.getItems().clear();
        mediaLabel.setText("-");
        mediaEstrelasLabel.setText("");
        quantasLabel.setText("");
        Mensagens.info(mensagemLabel, "");
        try {
            List<Avaliacao> avaliacoes = avaliacaoBLL.doMes(Sessao.getUtilizador(), mes);
            tabela.getItems().setAll(avaliacoes);
            BigDecimal media = AvaliacaoBLL.media(avaliacoes);
            if (media == null) {
                mediaLabel.setText("Sem avaliações");
                return;
            }
            mediaLabel.setText("Média " + media.toPlainString().replace('.', ','));
            mediaEstrelasLabel.setText(Avaliacao.desenharEstrelas(media.setScale(0, RoundingMode.HALF_UP).intValue()));
            quantasLabel.setText(avaliacoes.size() + (avaliacoes.size() == 1 ? " avaliação" : " avaliações"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível carregar as avaliações. Tente novamente.");
        }
    }
}
