package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class InicialController {

    @FXML
    private Label estadoLabel;

    @FXML
    private void aoTestarLigacao() {
        estadoLabel.setText("Ligação ainda por configurar");
    }
}
