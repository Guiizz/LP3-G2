package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pt.isep.lp3.g2.hotel4youg2.bll.LigacaoService;

public class InicialController {

    private final LigacaoService ligacaoService = new  LigacaoService();

    @FXML
    private Label estadoLabel;

    @FXML
    private void aoTestarLigacao() {
        estadoLabel.setText(ligacaoService.testarLigacao());
    }
}
