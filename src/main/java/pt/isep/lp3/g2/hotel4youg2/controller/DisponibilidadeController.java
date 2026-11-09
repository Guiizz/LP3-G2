package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.isep.lp3.g2.hotel4youg2.bll.QuartoBLL;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.util.List;

/**
 * Controlador do ecrã "Pesquisar disponibilidade": procura quartos livres entre duas datas.
 */
public class DisponibilidadeController {
   private final QuartoBLL quartoService = new QuartoBLL();

   @FXML
    private DatePicker entradaPicker;

   @FXML
    private DatePicker saidaPicker;

   @FXML
    private Spinner<Integer> hospedesSpinner;

   @FXML
    private ComboBox<TipoQuarto> tipoCombo;

   @FXML
    private ComboBox<Vista> vistaCombo;

   @FXML
    private TableView<Quarto> quartosTabela;

   @FXML
    private Label mensagemLabel;

   @FXML
    private void initialize() {
       tipoCombo.getItems().setAll(TipoQuarto.values());
       vistaCombo.getItems().setAll(Vista.values());
   }

    @FXML
    private void aoPesquisar() {
        quartosTabela.getItems().clear();
        try {
            List<Quarto> quartos = quartoService.pesquisarDisponiveis(entradaPicker.getValue(), saidaPicker.getValue(), hospedesSpinner.getValue(), tipoCombo.getValue(), vistaCombo.getValue());
            if (quartos.isEmpty()) {
                Mensagens.info(mensagemLabel, "Sem disponibilidade para estas datas");
            } else {
                quartosTabela.getItems().setAll(quartos);
                Mensagens.info(mensagemLabel, quartos.size() + " quarto(s) disponível(eis)");
            }
        } catch (IllegalArgumentException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível pesquisar: " + e.getMessage());
        }
    }

   @FXML
    private void aoLimparFiltros() {
       tipoCombo.setValue(null);
       vistaCombo.setValue(null);
   }
}
