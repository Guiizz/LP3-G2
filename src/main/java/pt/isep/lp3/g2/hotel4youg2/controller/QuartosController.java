package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.QuartoBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import java.util.List;

/**
 * Controlador do ecrã "Consultar quartos": mostra os quartos e os preços, com filtros por tipo e piso.
 */
public class QuartosController {
    private final QuartoBLL quartoBLL = new QuartoBLL();

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
            List<Quarto> quartos = quartoBLL.listar(tipoCombo.getValue(), pisoCombo.getValue());
            quartosTabela.getItems().setAll(quartos);
            Mensagens.info(mensagemLabel, quartos.size() + " quarto(s)");
        } catch (DataAccessException e) {
            quartosTabela.getItems().clear();
            Mensagens.erro(mensagemLabel, "Não foi possível carregar os quartos: " + e.getMessage());
        }
    }
}
