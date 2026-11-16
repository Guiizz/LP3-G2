package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.SugestaoQueixaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSugestao;

/**
 * Controlador do ecrã onde o cliente envia uma sugestão ou queixa identificando-se com o NIF (US61). Abre a partir do login.
 */
public class SugestaoClienteController {

    private final SugestaoQueixaBLL sugestaoQueixaBLL = new SugestaoQueixaBLL();

    @FXML
    private TextField nifField;
    @FXML
    private ComboBox<TipoSugestao> tipoCombo;
    @FXML
    private TextArea descricaoArea;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        tipoCombo.getItems().setAll(TipoSugestao.values());
    }

    @FXML
    private void aoEnviar() {
        try {
            SugestaoQueixa registada = sugestaoQueixaBLL.registarPeloNif(nifField.getText(), tipoCombo.getValue(),
                    descricaoArea.getText());
            Mensagens.sucesso(mensagemLabel, "Obrigado! A sua " + (registada.getTipo() == TipoSugestao.QUEIXA ? "queixa" : "sugestão")
                    + " foi enviada ao gestor.");
            nifField.clear();
            tipoCombo.setValue(null);
            descricaoArea.clear();
        } catch (IllegalArgumentException | ValidacaoException e) {
            mostrarErro(e.getMessage());
        } catch (DataAccessException e) {
            mostrarErro("Não foi possível enviar. Tente novamente.");
        }
    }

    private void mostrarErro(String texto) {
        Mensagens.erro(mensagemLabel, texto);
    }
}
