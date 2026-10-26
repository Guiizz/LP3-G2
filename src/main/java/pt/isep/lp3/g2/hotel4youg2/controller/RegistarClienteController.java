package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteService;
import pt.isep.lp3.g2.hotel4youg2.bll.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.util.List;

public class RegistarClienteController {

    private final ClienteService clienteService = new ClienteService();

    @FXML
    private TextField nomeField;
    @FXML
    private TextField nifField;
    @FXML
    private DatePicker dataNascimentoPicker;
    @FXML
    private TextField telemovelField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField ccField;
    @FXML
    private TextField moradaField;
    @FXML
    private Label mensagemLabel;
    @FXML
    private TextField pesquisaField;
    @FXML
    private ListView<Cliente> resultadosList;

    @FXML
    private void aoRegistar() {
        try {
            Cliente cliente = clienteService.registar(
                    nomeField.getText(),
                    nifField.getText(),
                    dataNascimentoPicker.getValue(),
                    telemovelField.getText(),
                    emailField.getText(),
                    ccField.getText(),
                    moradaField.getText());
            mostrarMensagem("Cliente " + cliente.getNome() + " registado com sucesso", "green");
            limparCampos();
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível guardar o cliente. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoPesquisar() {
        try {
            List<Cliente> clientes = clienteService.pesquisar(pesquisaField.getText());
            resultadosList.getItems().setAll(clientes);
            if (clientes.isEmpty()) {
                mostrarMensagem("Nenhum cliente encontrado", "black");
            }
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível pesquisar. Tente novamente.", "red");
        }
    }

    private void mostrarMensagem(String texto, String cor) {
        mensagemLabel.setStyle("-fx-text-fill: " + cor + ";");
        mensagemLabel.setText(texto);
    }

    private void limparCampos() {
        nomeField.clear();
        nifField.clear();
        dataNascimentoPicker.setValue(null);
        telemovelField.clear();
        emailField.clear();
        ccField.clear();
        moradaField.clear();
    }
}