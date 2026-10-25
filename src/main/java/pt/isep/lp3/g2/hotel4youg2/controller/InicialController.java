package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import pt.isep.lp3.g2.hotel4youg2.bll.LigacaoService;

import java.io.IOException;

public class InicialController {

    private final LigacaoService ligacaoService = new  LigacaoService();

    @FXML
    private Label estadoLabel;

    @FXML
    private void aoTestarLigacao() {
        estadoLabel.setText(ligacaoService.testarLigacao());
    }

    @FXML
    private void aoConsultarQuartos() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                InicialController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/quartos-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hotel4You - Quartos");
        stage.setScene(new Scene(loader.load(), 700, 500));
        stage.show();
    }

    @FXML
    private void aoPesquisarDisponibilidade() throws IOException {
       FXMLLoader loader = new FXMLLoader(
               InicialController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/disponibilidade-view.fxml"));
       Stage stage = new Stage();
       stage.setTitle("Hotel4You - Disponibilidade");
       stage.setScene(new Scene(loader.load(), 800, 500));
       stage.show();
    }
}
