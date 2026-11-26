package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import java.time.LocalTime;

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
    private Utilizador utilizadorPorMudar;
    private boolean modoFornecedor;

    private static final String TEXTO_PRIMEIRO_ACESSO =
            "Primeiro acesso: escolha a sua palavra-passe, com pelo menos 6 caracteres.";
    private static final String TEXTO_PASSWORD_REPOSTA =
            "A sua palavra-passe foi reposta pelo gestor: escolha uma nova, com pelo menos 8 caracteres.";

    @FXML
    private Label saudacaoLabel;

    @FXML
    private Label tituloLabel;

    @FXML
    private Label subtituloLabel;

    @FXML
    private Label utilizadorLabel;

    @FXML
    private TextField nomeUtilizadorField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisivelField;

    @FXML
    private Button mostrarPasswordButton;

    @FXML
    private Button modoFornecedorButton;

    @FXML
    private VBox primeiroAcessoBox;

    @FXML
    private Label primeiroAcessoLabel;

    @FXML
    private PasswordField novaPasswordField;

    @FXML
    private PasswordField confirmarPasswordField;

    @FXML
    private Label erroLabel;

    @FXML
    private void initialize() {
        ligacaoBLL.prepararLigacao();
        passwordVisivelField.textProperty().bindBidirectional(passwordField.textProperty());
        saudacaoLabel.setText(saudacao(LocalTime.now()));
        mostrarErro("");
    }

    private String saudacao(LocalTime agora) {
        int hora = agora.getHour();
        if (hora >= 6 && hora < 12) {
            return "BOM DIA";
        }
        if (hora >= 12 && hora < 20) {
            return "BOA TARDE";
        }
        return "BOA NOITE";
    }

    @FXML
    private void aoEntrar() {
        mostrarErro("");
        esconderPrimeiroAcesso();
        String identificacao = nomeUtilizadorField.getText() == null ? "" : nomeUtilizadorField.getText().trim();
        if (modoFornecedor) {
            autenticarFornecedor();
        } else if (identificacao.matches("\\d{9}")) {
            autenticarCliente();
        } else {
            autenticarFuncionario();
        }
    }

    @FXML
    private void aoMudarModo() {
        modoFornecedor = !modoFornecedor;
        mostrarErro("");
        esconderPrimeiroAcesso();
        nomeUtilizadorField.clear();
        passwordField.clear();
        if (modoFornecedor) {
            tituloLabel.setText("Área do fornecedor");
            subtituloLabel.setText("Entre com o código de fornecedor que o hotel lhe deu.");
            utilizadorLabel.setText("Código de fornecedor");
            nomeUtilizadorField.setPromptText("o seu código");
            modoFornecedorButton.setText("Voltar ao login do hotel");
        } else {
            tituloLabel.setText("Iniciar sessão");
            subtituloLabel.setText("Funcionários entram com o nome de utilizador; clientes com o NIF.");
            utilizadorLabel.setText("Nome de utilizador ou NIF");
            nomeUtilizadorField.setPromptText("ex.: gestor ou 123456789");
            modoFornecedorButton.setText("É fornecedor? Entrar com o código");
        }
        nomeUtilizadorField.requestFocus();
    }

    @FXML
    private void aoMostrarPassword() {
        boolean mostrar = !passwordVisivelField.isVisible();
        passwordVisivelField.setVisible(mostrar);
        passwordField.setVisible(!mostrar);
        mostrarPasswordButton.setText(mostrar ? "Ocultar" : "Mostrar");
        if (mostrar) {
            passwordVisivelField.requestFocus();
            passwordVisivelField.end();
        } else {
            passwordField.requestFocus();
            passwordField.end();
        }
    }

    @FXML
    private void aoPassarParaConfirmar() {
        confirmarPasswordField.requestFocus();
    }

    private void autenticarFuncionario() {
        try {
            Utilizador utilizador = autenticacaoBLL.autenticar(nomeUtilizadorField.getText(), passwordField.getText());
            passwordField.clear();
            if (utilizador.isMudarPassword()) {
                utilizadorPorMudar = utilizador;
                primeiroAcessoLabel.setText(TEXTO_PASSWORD_REPOSTA);
                mostrarPrimeiroAcesso(true);
                novaPasswordField.requestFocus();
                return;
            }
            entrarComoFuncionario(utilizador);
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
            passwordField.clear();
        } catch (DataAccessException e) {
            mostrarErro(e.getMessage());
        } catch (IOException e) {
            mostrarErro("Não foi possível abrir a janela principal.");
        }
    }

    private void autenticarCliente() {
        try {
            ContaCliente conta = contaClienteBLL.autenticar(nomeUtilizadorField.getText(), passwordField.getText());
            passwordField.clear();
            if (conta.isPrimeiroAcesso()) {
                contaPorAtivar = conta;
                primeiroAcessoLabel.setText(TEXTO_PRIMEIRO_ACESSO);
                mostrarPrimeiroAcesso(true);
                novaPasswordField.requestFocus();
                return;
            }
            entrarComoCliente(conta);
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
            passwordField.clear();
        } catch (DataAccessException e) {
            mostrarErro(e.getMessage());
        } catch (IOException e) {
            mostrarErro("Não foi possível abrir a sua área.");
        }
    }

    private void autenticarFornecedor() {
        try {
            Fornecedor fornecedor = autenticacaoFornecedorBLL.autenticar(nomeUtilizadorField.getText(),
                    passwordField.getText());
            Sessao.iniciarFornecedor(fornecedor);
            abrirJanela("fornecedor-view.fxml", "Hotel4You · Fornecedor " + fornecedor.getNome());
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
            passwordField.clear();
        } catch (DataAccessException e) {
            mostrarErro(e.getMessage());
        } catch (IOException e) {
            mostrarErro("Não foi possível abrir o ecrã do fornecedor.");
        }
    }

    @FXML
    private void aoGuardarPalavraPasse() {
        mostrarErro("");
        try {
            if (utilizadorPorMudar != null) {
                autenticacaoBLL.mudarPassword(utilizadorPorMudar, novaPasswordField.getText(),
                        confirmarPasswordField.getText());
                Utilizador utilizador = utilizadorPorMudar;
                esconderPrimeiroAcesso();
                entrarComoFuncionario(utilizador);
                return;
            }
            contaClienteBLL.definirPalavraPasse(contaPorAtivar, novaPasswordField.getText(),
                    confirmarPasswordField.getText());
            ContaCliente conta = contaPorAtivar;
            esconderPrimeiroAcesso();
            entrarComoCliente(conta);
        } catch (ValidacaoException | IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
            novaPasswordField.clear();
            confirmarPasswordField.clear();
        } catch (DataAccessException e) {
            mostrarErro(e.getMessage());
        } catch (IOException e) {
            mostrarErro("Não foi possível abrir a sua área.");
        }
    }

    private void entrarComoFuncionario(Utilizador utilizador) throws IOException {
        Sessao.iniciar(utilizador);
        cancelarReservasSemCheckin();
        abrirJanela("inicial-view.fxml", "Hotel4You · Bem-vindo, " + utilizador.getNomeUtilizador()
                + " (" + utilizador.getPerfil() + ")");
    }

    private void entrarComoCliente(ContaCliente conta) throws IOException {
        Sessao.iniciarCliente(conta);
        cancelarReservasSemCheckin();
        abrirJanela("area-cliente-view.fxml", "Hotel4You · " + conta.getNome());
    }

    private void cancelarReservasSemCheckin() {
        try {
            reservaBLL.cancelarSemCheckin();
        } catch (DataAccessException ignorada) {
        }
    }

    private void abrirJanela(String ficheiro, String titulo) throws IOException {
        Stage stage = (Stage) nomeUtilizadorField.getScene().getWindow();
        Scene cena = new Scene(Navegador.carregar(ficheiro).getRoot(), 1100, 700);
        Navegador.aplicarEstilo(cena);
        stage.setScene(cena);
        stage.setTitle(titulo);
        stage.centerOnScreen();
    }

    private void mostrarErro(String mensagem) {
        String texto = mensagem == null ? "" : mensagem;
        erroLabel.setText(texto);
        erroLabel.setVisible(!texto.isEmpty());
        erroLabel.setManaged(!texto.isEmpty());
    }

    private void esconderPrimeiroAcesso() {
        contaPorAtivar = null;
        utilizadorPorMudar = null;
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
            mostrarErro("Não foi possível abrir o formulário de sugestões.");
        }
    }
}