package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pt.isep.lp3.g2.hotel4youg2.bll.AutenticacaoBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.AutenticacaoFornecedorBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ContaClienteBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.LigacaoBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;
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
    private final ContaClienteBLL contaClienteBLL = new ContaClienteBLL();
    private final ReservaBLL reservaBLL = new ReservaBLL();
    private final LigacaoBLL ligacaoBLL = new LigacaoBLL();
    private ContaCliente contaPorAtivar;

    @FXML
    private TextField nomeUtilizadorField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private VBox primeiroAcessoBox;

    @FXML
    private PasswordField novaPasswordField;

    @FXML
    private PasswordField confirmarPasswordField;

    @FXML
    private Label erroLabel;

    @FXML
    private void initialize() {
        ligacaoBLL.prepararLigacao();
    }

    @FXML
    private void aoEntrar() {
        erroLabel.setText("");
        esconderPrimeiroAcesso();
        try {
            Utilizador utilizador = autenticacaoBLL.autenticar(nomeUtilizadorField.getText(), passwordField.getText());
            Sessao.iniciar(utilizador);
            reservaBLL.cancelarSemCheckin();
            abrirJanela("inicial-view.fxml", "Hotel4You · Bem-vindo, " + utilizador.getNomeUtilizador()
                    + " (" + utilizador.getPerfil() + ")");
        } catch (IllegalArgumentException e) {
            erroLabel.setText(e.getMessage());
            passwordField.clear();
        } catch (DataAccessException e) {
            erroLabel.setText(e.getMessage());
        } catch (IOException e) {
            erroLabel.setText("Não foi possível abrir a janela principal.");
        }
    }

    @FXML
    private void aoEntrarComoCliente() {
        erroLabel.setText("");
        esconderPrimeiroAcesso();
        try {
            ContaCliente conta = contaClienteBLL.autenticar(nomeUtilizadorField.getText(), passwordField.getText());
            passwordField.clear();
            if (conta.isPrimeiroAcesso()) {
                contaPorAtivar = conta;
                mostrarPrimeiroAcesso(true);
                novaPasswordField.requestFocus();
                return;
            }
            entrarComoCliente(conta);
        } catch (IllegalArgumentException e) {
            erroLabel.setText(e.getMessage());
            passwordField.clear();
        } catch (DataAccessException e) {
            erroLabel.setText(e.getMessage());
        } catch (IOException e) {
            erroLabel.setText("Não foi possível abrir a sua área.");
        }
    }

    @FXML
    private void aoGuardarPalavraPasse() {
        erroLabel.setText("");
        try {
            contaClienteBLL.definirPalavraPasse(contaPorAtivar, novaPasswordField.getText(),
                    confirmarPasswordField.getText());
            ContaCliente conta = contaPorAtivar;
            esconderPrimeiroAcesso();
            entrarComoCliente(conta);
        } catch (ValidacaoException | IllegalStateException e) {
            erroLabel.setText(e.getMessage());
            novaPasswordField.clear();
            confirmarPasswordField.clear();
        } catch (DataAccessException e) {
            erroLabel.setText(e.getMessage());
        } catch (IOException e) {
            erroLabel.setText("Não foi possível abrir a sua área.");
        }
    }

    // o fornecedor escreve o código dele no campo do nome de utilizador
    @FXML
    private void aoEntrarComoFornecedor() {
        erroLabel.setText("");
        esconderPrimeiroAcesso();
        try {
            Fornecedor fornecedor = autenticacaoFornecedorBLL.autenticar(nomeUtilizadorField.getText(),
                    passwordField.getText());
            Sessao.iniciarFornecedor(fornecedor);
            abrirJanela("fornecedor-view.fxml", "Hotel4You · Fornecedor " + fornecedor.getNome());
        } catch (IllegalArgumentException e) {
            erroLabel.setText(e.getMessage());
            passwordField.clear();
        } catch (DataAccessException e) {
            erroLabel.setText(e.getMessage());
        } catch (IOException e) {
            erroLabel.setText("Não foi possível abrir o ecrã do fornecedor.");
        }
    }

    private void entrarComoCliente(ContaCliente conta) throws IOException {
        Sessao.iniciarCliente(conta);
        reservaBLL.cancelarSemCheckin();
        abrirJanela("area-cliente-view.fxml", "Hotel4You · " + conta.getNome());
    }

    private void abrirJanela(String ficheiro, String titulo) throws IOException {
        Stage stage = (Stage) nomeUtilizadorField.getScene().getWindow();
        Scene cena = new Scene(Navegador.carregar(ficheiro).getRoot(), 1100, 700);
        Navegador.aplicarEstilo(cena);
        stage.setScene(cena);
        stage.setTitle(titulo);
        stage.centerOnScreen();
    }

    private void esconderPrimeiroAcesso() {
        contaPorAtivar = null;
        mostrarPrimeiroAcesso(false);
        novaPasswordField.clear();
        confirmarPasswordField.clear();
    }

    private void mostrarPrimeiroAcesso(boolean visivel) {
        primeiroAcessoBox.setVisible(visivel);
        primeiroAcessoBox.setManaged(visivel);
    }

    @FXML
    private void aoSugestaoCliente() {
        try {
            Stage janela = new Stage();
            janela.setTitle("Hotel4You · Sugestão ou queixa");
            Scene cena = new Scene(Navegador.carregar("sugestao-cliente-view.fxml").getRoot());
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
