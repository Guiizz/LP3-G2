package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

import java.util.List;

public class ConsultarReservasController {

    private final ClienteBLL clienteService = new ClienteBLL();
    private final ReservaBLL reservaService = new ReservaBLL();

    @FXML
    private TextField nifField;
    @FXML
    private Label clienteLabel;
    @FXML
    private TableView<Reserva> reservasTabela;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void aoProcurar() {
        clienteLabel.setText("");
        reservasTabela.getItems().clear();
        try {
            Cliente cliente = clienteService.pesquisarPorNif(nifField.getText());
            if (cliente == null) {
                mostrarMensagem("Não existe nenhum cliente com o NIF " + nifField.getText().trim(), "red");
                return;
            }
            clienteLabel.setText(cliente.getNome());

            List<Reserva> reservas = reservaService.consultarDoCliente(cliente);
            if (reservas.isEmpty()) {
                mostrarMensagem(cliente.getNome() + " ainda não tem reservas", "black");
            } else {
                reservasTabela.getItems().setAll(reservas);
                mostrarMensagem(reservas.size() + " reserva(s), das mais recentes para as mais antigas", "black");
            }
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível consultar as reservas. Tente novamente.", "red");
        }
    }

    private void mostrarMensagem(String texto, String cor) {
        mensagemLabel.setStyle("-fx-text-fill: " + cor + ";");
        mensagemLabel.setText(texto);
    }
}
