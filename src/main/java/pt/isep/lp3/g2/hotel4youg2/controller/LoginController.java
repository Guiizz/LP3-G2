package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pt.isep.lp3.g2.hotel4youg2.bll.AutenticacaoBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.AutenticacaoFornecedorBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.LigacaoBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;

/**
 * Controlador do ecrã de login: autentica o funcionário e abre a janela principal.
 */
public class LoginController {

    private final AutenticacaoBLL autenticacaoBLL = new AutenticacaoBLL();
    private final AutenticacaoFornecedorBLL autenticacaoFornecedorBLL = new AutenticacaoFornecedorBLL();
    private final LigacaoBLL ligacaoBLL = new LigacaoBLL();

    @FXML
    private TextField nomeUtilizadorField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label erroLabel;

    @FXML
    private void initialize() {
        ligacaoBLL.prepararLigacao();
    }

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
            erroLabel.setText(e.getMessage());
        } catch (IOException e) {
            erroLabel.setText("Não foi possível abrir a janela principal.");
        }
    }

    // o fornecedor escreve o código dele no campo do nome de utilizador
    @FXML
    private void aoEntrarComoFornecedor() {
        erroLabel.setText("");
        try {
            Fornecedor fornecedor = autenticacaoFornecedorBLL.autenticar(nomeUtilizadorField.getText(),
                    passwordField.getText());
            Sessao.iniciarFornecedor(fornecedor);
            Stage stage = (Stage) nomeUtilizadorField.getScene().getWindow();
            Scene cena = new Scene(Navegador.carregar("fornecedor-view.fxml").getRoot(), 1100, 700);
            Navegador.aplicarEstilo(cena);
            stage.setScene(cena);
            stage.setTitle("Hotel4You · Fornecedor " + fornecedor.getNome());
        } catch (IllegalArgumentException e) {
            erroLabel.setText(e.getMessage());
            passwordField.clear();
        } catch (DataAccessException e) {
            erroLabel.setText(e.getMessage());
        } catch (IOException e) {
            erroLabel.setText("Não foi possível abrir o ecrã do fornecedor.");
        }
    }

    private void abrirJanelaPrincipal(Utilizador utilizador) throws IOException {
        Stage stage = (Stage) nomeUtilizadorField.getScene().getWindow();
        Scene cena = new Scene(Navegador.carregar("inicial-view.fxml").getRoot(), 1100, 700);
        Navegador.aplicarEstilo(cena);
        stage.setScene(cena);
        stage.setTitle("Hotel4You · Bem-vindo, " + utilizador.getNomeUtilizador() + " (" + utilizador.getPerfil() + ")");
    }

    @FXML
    private void aoSugestaoCliente() {
        try {
            Stage janela = new Stage();
            janela.setTitle("Hotel4You · Sugestão ou queixa");
            Scene cena = new Scene(Navegador.carregar("sugestao-cartao-view.fxml").getRoot());
            Navegador.aplicarEstilo(cena);
            janela.setScene(cena);
            janela.initOwner(nomeUtilizadorField.getScene().getWindow());
            janela.initModality(Modality.WINDOW_MODAL);
            janela.showAndWait();
        } catch (IOException e) {
            erroLabel.setText("Não foi possível abrir o formulário de sugestões.");
        }
    }
}
