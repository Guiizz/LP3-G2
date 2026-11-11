package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

import java.io.IOException;
import java.util.List;

/**
 * Controlador do ecrã "Reservas de um cliente" (US33): procura o cliente pelo NIF, mostra as suas reservas e abre os serviços de cada uma.
 */
public class ConsultarReservasController {

    private final ClienteBLL clienteBLL = new ClienteBLL();
    private final ReservaBLL reservaBLL = new ReservaBLL();

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
            Cliente cliente = clienteBLL.pesquisarPorNif(nifField.getText());
            if (cliente == null) {
                mostrarMensagem("Não existe nenhum cliente com o NIF " + nifField.getText().trim(), "red");
                return;
            }
            clienteLabel.setText(cliente.getNome());

            List<Reserva> reservas = reservaBLL.consultarDoCliente(cliente);
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

    @FXML
    private void aoVerServicos() {
        Reserva escolhida = reservasTabela.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            mostrarMensagem("Escolha uma reserva na tabela.", "red");
            return;
        }
        try {
            ServicosReservaController.abrir(escolhida);
            aoProcurar();
        } catch (IOException e) {
            mostrarMensagem("Não foi possível abrir os serviços da reserva.", "red");
        }
    }

    private void mostrarMensagem(String texto, String cor) {
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }
}
