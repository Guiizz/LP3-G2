package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador do submenu Reservas: junta num só sítio os ecrãs ligados às reservas.
 */
public class ReservasController {

    @FXML
    private void aoCriarReserva() throws IOException {
        abrir("criar-reserva-view.fxml", "Hotel4You - Criar reserva", 760, 560);
    }

    @FXML
    private void aoPesquisarDisponibilidade() throws IOException {
        abrir("disponibilidade-view.fxml", "Hotel4You - Disponibilidade", 800, 500);
    }

    private void abrir(String ficheiro, String titulo, double largura, double altura) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                ReservasController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/" + ficheiro));
        Stage stage = new Stage();
        stage.setTitle(titulo);
        stage.setScene(new Scene(loader.load(), largura, altura));
        stage.show();
    }
}