package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import pt.isep.lp3.g2.hotel4youg2.bll.Permissoes;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;

public class QuartosMenuController {

    @FXML
    private Button gestaoQuartosButton;

    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        boolean gestor = Permissoes.eGestor(Sessao.getUtilizador());
        gestaoQuartosButton.setVisible(gestor);
        gestaoQuartosButton.setManaged(gestor);
    }

    @FXML
    private void aoConsultarQuartos() throws IOException {
        abrir("quartos-view.fxml", "Quartos", 700, 500);
    }

    @FXML
    private void aoPesquisarDisponibilidade() throws IOException {
        abrir("disponibilidade-view.fxml", "Disponibilidade", 800, 500);
    }

    @FXML
    private void aoGerirQuartos() throws IOException {
        try {
            Permissoes.exigirGestor(Sessao.getUtilizador());
        } catch (IllegalStateException e) {
            mensagemLabel.setText(e.getMessage());
            return;
        }
        abrir("gestao-quartos-view.fxml", "Gestão de quartos", 560, 460);
    }

    private void abrir(String ficheiro, String titulo, double largura, double altura) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                QuartosMenuController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/" + ficheiro));
        Stage stage = new Stage();
        stage.setTitle("Hotel4You - " + titulo);
        stage.setScene(new Scene(loader.load(), largura, altura));
        stage.show();
    }
}