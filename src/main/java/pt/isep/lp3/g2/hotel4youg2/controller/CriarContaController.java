package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.FuncionarioBLL;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

public class CriarContaController {

    private final FuncionarioBLL funcionarioBLL = new FuncionarioBLL();

    @FXML
    private TextField campoNome;
    @FXML
    private TextField campoNif;
    @FXML
    private DatePicker campoDataNascimento;
    @FXML
    private TextField campoTelemovel;
    @FXML
    private TextField campoEmail;
    @FXML
    private TextField campoCc;
    @FXML
    private TextField campoMorada;
    @FXML
    private TextField campoNomeUtilizador;
    @FXML
    private PasswordField campoPassword;
    @FXML
    private ComboBox<Perfil> campoPerfil;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        campoPerfil.getItems().setAll(Perfil.values());
    }

    @FXML
    private void aoCriar() {
        Funcionario dados = new Funcionario(0, campoNome.getText(), campoCc.getText(), campoNif.getText(),
                campoMorada.getText(), campoTelemovel.getText(), campoDataNascimento.getValue(),
                campoEmail.getText(), 0, null);
        try {
            funcionarioBLL.criarConta(Sessao.getUtilizador(), dados, campoNomeUtilizador.getText(),
                    campoPassword.getText(), campoPerfil.getValue());
            limpar();
            mensagemLabel.setText("Conta criada.");
        } catch (RuntimeException e) {
            mensagemLabel.setText(e.getMessage());
        }
    }

    private void limpar() {
        campoNome.clear();
        campoNif.clear();
        campoDataNascimento.setValue(null);
        campoTelemovel.clear();
        campoEmail.clear();
        campoCc.clear();
        campoMorada.clear();
        campoNomeUtilizador.clear();
        campoPassword.clear();
        campoPerfil.setValue(null);
    }
}