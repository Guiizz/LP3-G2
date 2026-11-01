package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.SugestaoQueixaBLL;
import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoSugestao;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.util.List;

public class ConsultarSugestoesController {

    private final SugestaoQueixaBLL sugestaoBLL = new SugestaoQueixaBLL();

    @FXML
    private ComboBox<TipoSugestao> tipoCombo;

    @FXML
    private TableView<SugestaoQueixa> tabela;

    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        tipoCombo.getItems().setAll(TipoSugestao.values());
        carregar();
    }

    @FXML
    private void aoFiltrar() {
        carregar();
    }

    @FXML
    private void aoLimpar() {
        tipoCombo.setValue(null);
        carregar();
    }

    private void carregar() {
        try {
            List<SugestaoQueixa> lista = sugestaoBLL.listar(Sessao.getUtilizador(), tipoCombo.getValue());
            tabela.getItems().setAll(lista);
            mensagemLabel.setText(lista.size() + " registo(s)");
        } catch (RuntimeException e) {
            tabela.getItems().clear();
            mensagemLabel.setText(e.getMessage());
        }
    }
}