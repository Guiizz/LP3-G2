package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pt.isep.lp3.g2.hotel4youg2.bll.AutenticacaoBLL;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;

/**
 * Controlador do ecrã de login: autentica o funcionário e abre a janela principal.
 */
public class LoginController {

    private final AutenticacaoBLL autenticacaoBLL = new AutenticacaoBLL();

    @FXML
    private TextField nomeUtilizadorField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label erroLabel;

    @FXML
    private void aoEntrar() {
        erroLabel.setText("");
        try {
            Utilizador utilizador = autenticacaoBLL.autenticar(nomeUtilizadorField.getText(), passwordField.getText());
            Sessao.iniciar(utilizador);
            abrirJanelaPrincipal(utilizador);
        } catch (IllegalArgumentException e) {
            erroLabel.setText(e.getMessage());
            passwordField.clear();
        } catch (DataAccessException e) {
            erroLabel.setText("Não foi possível ligar à base de dados: " + e.getMessage());
        } catch (IOException e) {
            erroLabel.setText("Não foi possível abrir a janela principal.");
        }
    }

    private void abrirJanelaPrincipal(Utilizador utilizador) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                LoginController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/inicial-view.fxml"));
        Stage stage = (Stage) nomeUtilizadorField.getScene().getWindow();
        stage.setScene(new Scene(loader.load(), 400, 300));
        stage.setTitle("Hotel4You · Bem-vindo, " + utilizador.getNomeUtilizador() + " (" + utilizador.getPerfil() + ")");
    }
}
