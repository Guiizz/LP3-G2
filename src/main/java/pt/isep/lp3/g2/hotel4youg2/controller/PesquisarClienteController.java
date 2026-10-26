package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteService;
import pt.isep.lp3.g2.hotel4youg2.bll.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class PesquisarClienteController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClienteService clienteService = new ClienteService();

    @FXML
    private TextField nifField;
    @FXML
    private Label mensagemLabel;
    @FXML
    private GridPane dadosGrid;
    @FXML
    private Label nomeLabel;
    @FXML
    private Label nifLabel;
    @FXML
    private Label dataNascimentoLabel;
    @FXML
    private Label telemovelLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label ccLabel;
    @FXML
    private Label moradaLabel;
    @FXML
    private Button registarButton;

    private String nifNaoEncontrado;

    @FXML
    private void aoPesquisar() {
        esconderResultado();
        try {
            Cliente cliente = clienteService.pesquisarPorNif(nifField.getText());
            if (cliente == null) {
                mostrarMensagem("Cliente não encontrado", "black");
                nifNaoEncontrado = nifField.getText().trim();
                registarButton.setVisible(true);
                registarButton.setManaged(true);
            } else {
                mostrarCliente(cliente);
            }
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível pesquisar. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoRegistarNovo() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                PesquisarClienteController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/registar-cliente-view.fxml"));
        Scene cena = new Scene(loader.load());
        RegistarClienteController registo = loader.getController();
        registo.preencherNif(nifNaoEncontrado);
        Stage stage = new Stage();
        stage.setTitle("Hotel4You - Registar cliente");
        stage.setScene(cena);
        stage.show();
    }

    private void mostrarCliente(Cliente cliente) {
        nomeLabel.setText(cliente.getNome());
        nifLabel.setText(cliente.getNif());
        if (cliente.getDataNascimento() == null) {
            dataNascimentoLabel.setText("-");
        } else {
            dataNascimentoLabel.setText(cliente.getDataNascimento().format(FORMATO_DATA));
        }
        telemovelLabel.setText(textoOuTraco(cliente.getTelemovel()));
        emailLabel.setText(textoOuTraco(cliente.getEmail()));
        ccLabel.setText(textoOuTraco(cliente.getCc()));
        moradaLabel.setText(textoOuTraco(cliente.getMorada()));
        dadosGrid.setVisible(true);
        dadosGrid.setManaged(true);
    }

    private void esconderResultado() {
        mensagemLabel.setText("");
        dadosGrid.setVisible(false);
        dadosGrid.setManaged(false);
        registarButton.setVisible(false);
        registarButton.setManaged(false);
    }

    private void mostrarMensagem(String texto, String cor) {
        mensagemLabel.setStyle("-fx-text-fill: " + cor + ";");
        mensagemLabel.setText(texto);
    }

    private String textoOuTraco(String texto) {
        if (texto == null) {
            return "-";
        }
        return texto;
    }
}