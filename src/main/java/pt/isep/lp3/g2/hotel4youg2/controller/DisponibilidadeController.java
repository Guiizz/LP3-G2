package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.isep.lp3.g2.hotel4youg2.bll.QuartoService;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.util.List;

public class DisponibilidadeController {
   private final QuartoService quartoService = new QuartoService();

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
               mensagemLabel.setText("Sem disponibilidade para estas datas");
           } else {
               quartosTabela.getItems().setAll(quartos);
               mensagemLabel.setText(quartos.size() + " quarto(s) disponível(eis)");
           }
       } catch (IllegalArgumentException e) {
           mensagemLabel.setText(e.getMessage());
       } catch (DataAccessException e) {
           mensagemLabel.setText("Não foi possível pesquisar: " + e.getMessage());
       }
   }

   @FXML
    private void aoLimparFiltros() {
       tipoCombo.setValue(null);
       vistaCombo.setValue(null);
   }
}
