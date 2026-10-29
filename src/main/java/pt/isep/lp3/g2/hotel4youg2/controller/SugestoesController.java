package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteService;
import pt.isep.lp3.g2.hotel4youg2.bll.SugestaoQueixaBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoSugestao;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.time.format.DateTimeFormatter;

public class SugestoesController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final ClienteService clienteService = new ClienteService();
    private final SugestaoQueixaBLL sugestaoBLL = new SugestaoQueixaBLL();
    private Cliente cliente;

    @FXML
    private TextField nifField;
    @FXML
    private Label clienteLabel;
    @FXML
    private ComboBox<TipoSugestao> tipoCombo;
    @FXML
    private TextArea descricaoArea;
    @FXML
    private Label contadorLabel;
    @FXML
    private Button registarButton;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        tipoCombo.getItems().setAll(TipoSugestao.values());
        // não deixa escrever mais do que o máximo e vai mostrando quantos caracteres já tem
        descricaoArea.setTextFormatter(new TextFormatter<String>(mudanca ->
                mudanca.getControlNewText().length() <= SugestaoQueixaBLL.MAXIMO_DESCRICAO ? mudanca : null));
        descricaoArea.textProperty().addListener((obs, antes, agora) ->
                contadorLabel.setText(agora.length() + "/" + SugestaoQueixaBLL.MAXIMO_DESCRICAO));
    }

    @FXML
    private void aoProcurarCliente() {
        cliente = null;
        clienteLabel.setText("");
        try {
            cliente = clienteService.pesquisarPorNif(nifField.getText());
            if (cliente == null) {
                mostrarMensagem("Cliente não encontrado. Registe-o primeiro em Registar cliente.", "red");
            } else {
                clienteLabel.setText(cliente.getNome());
                mostrarMensagem("", "black");
            }
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível procurar o cliente. Tente novamente.", "red");
        }
        registarButton.setDisable(cliente == null);
    }

    @FXML
    private void aoRegistar() {
        try {
            SugestaoQueixa registada = sugestaoBLL.registar(Sessao.getUtilizador(), cliente, tipoCombo.getValue(),
                    descricaoArea.getText());
            mostrarMensagem(registada.getTipo() + " de " + cliente.getNome() + " registada a "
                    + registada.getRegistadaEm().format(FORMATO_DATA) + ".", "green");
            limpar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível registar. Tente novamente.", "red");
        }
    }

    private void limpar() {
        cliente = null;
        nifField.clear();
        clienteLabel.setText("");
        tipoCombo.setValue(null);
        descricaoArea.clear();
        registarButton.setDisable(true);
    }

    private void mostrarMensagem(String texto, String cor) {
        mensagemLabel.setStyle("-fx-text-fill: " + cor + ";");
        mensagemLabel.setText(texto);
    }
}
