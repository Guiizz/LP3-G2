package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Controlador do ecrã "Pesquisar cliente": procura um cliente pelo NIF e,
 * se não existir, deixa registá-lo logo.
 */
public class PesquisarClienteController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClienteBLL clienteBLL = new ClienteBLL();

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
            Cliente cliente = clienteBLL.pesquisarPorNif(nifField.getText());
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
        FXMLLoader loader = Navegador.carregar("registar-cliente-view.fxml");
        RegistarClienteController registo = loader.getController();
        registo.preencherNif(nifNaoEncontrado);
        Parent formulario = loader.getRoot();
        Navegador.abrirFormulario(formulario, "Registar cliente");
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
        Mensagens.info(mensagemLabel, "");
        dadosGrid.setVisible(false);
        dadosGrid.setManaged(false);
        registarButton.setVisible(false);
        registarButton.setManaged(false);
    }

    private void mostrarMensagem(String texto, String cor) {
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }

    private String textoOuTraco(String texto) {
        if (texto == null) {
            return "-";
        }
        return texto;
    }
}