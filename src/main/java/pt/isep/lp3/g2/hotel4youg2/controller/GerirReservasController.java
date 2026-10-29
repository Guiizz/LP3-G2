package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.Permissoes;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaService;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.util.Optional;

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
        if (!confirmarCancelar(escolhida)) {
            return;
        }
        try {
            reservaService.cancelar(Sessao.getUtilizador(), escolhida);
            mensagemLabel.setText("Reserva " + escolhida.getId() + " cancelada.");
        } catch (RuntimeException e) {
            mensagemLabel.setText(e.getMessage());
        }
        carregar();
    }

    private boolean confirmarCancelar(Reserva reserva) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Tem a certeza que quer cancelar a reserva " + reserva.getId() + " (quarto "
                        + reserva.getQuartoNumero() + ", " + reserva.getDataEntrada() + " a "
                        + reserva.getDataSaida() + ")?\n"
                        + "A reserva fica no histórico e o quarto volta a ficar livre.",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Cancelar reserva");
        alerta.setHeaderText(null);
        Optional<ButtonType> resposta = alerta.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.YES;
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