package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.QuartoService;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;

import java.util.List;

/**
 * Controlador do ecrã "Consultar quartos": mostra os quartos e os preços, com filtros por tipo e piso.
 */
public class QuartosController {
    private final QuartoService quartoService = new QuartoService();

    @FXML
    private ComboBox<TipoQuarto> tipoCombo;

    @FXML
    private ComboBox<Integer> pisoCombo;

    @FXML
    private TableView<Quarto> quartosTabela;

    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        tipoCombo.getItems().setAll(TipoQuarto.values());
        pisoCombo.getItems().setAll(2,3);
        atualizar();
    }

    @FXML
    private void aoFiltrar() {
        atualizar();
    }

    @FXML
    private void aoLimparFiltros() {
        tipoCombo.setValue(null);
        pisoCombo.setValue(null);
        atualizar();
    }

    @FXML
    private void atualizar() {
        try {
            List<Quarto> quartos = quartoService.listar(tipoCombo.getValue(), pisoCombo.getValue());
            quartosTabela.getItems().setAll(quartos);
            mensagemLabel.setText(quartos.size() + " quarto(s)");
        } catch (DataAccessException e) {
            quartosTabela.getItems().clear();
            mensagemLabel.setText("Não foi possível carregar os quartos: " + e.getMessage());
        }
    }
}
