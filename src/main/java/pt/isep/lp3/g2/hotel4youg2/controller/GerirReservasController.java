package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.Permissoes;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaService;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

public class GerirReservasController {

    private final ReservaService reservaService = new ReservaService();

    @FXML
    private TableView<Reserva> tabela;

    @FXML
    private Button cancelarButton;

    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        boolean gestor = Permissoes.eGestor(Sessao.getUtilizador());
        cancelarButton.setVisible(gestor);
        cancelarButton.setManaged(gestor);
        carregar();
    }

    @FXML
    private void aoCancelar() {
        Reserva escolhida = tabela.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            mensagemLabel.setText("Escolha uma reserva na tabela.");
            return;
        }
        try {
            reservaService.cancelar(Sessao.getUtilizador().getPerfil(), escolhida);
            mensagemLabel.setText("Reserva " + escolhida.getId() + " cancelada.");
        } catch (RuntimeException e) {
            mensagemLabel.setText(e.getMessage());
        }
        carregar();
    }

    private void carregar() {
        try {
            tabela.getItems().setAll(reservaService.listar());
        } catch (RuntimeException e) {
            tabela.getItems().clear();
            mensagemLabel.setText("Não foi possível carregar as reservas: " + e.getMessage());
        }
    }
}