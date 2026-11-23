package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.AcessoCartaoBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "O meu cartão" (US39, US52 e US74): quem tem a sessão iniciada pica o seu próprio cartão
 * num local do hotel e vê as suas passagens. O gestor usa o cartão mestre e cada funcionário o seu cartão.
 */
public class AcessoCartaoController {

    private final AcessoCartaoBLL acessoCartaoBLL = new AcessoCartaoBLL();

    @FXML
    private Label cartaoLabel;
    @FXML
    private ComboBox<LocalHotel> localCombo;
    @FXML
    private Label quartoLabel;
    @FXML
    private TextField quartoField;
    @FXML
    private Label mensagemLabel;
    @FXML
    private TableView<AcessoCartao> tabela;

    @FXML
    private void initialize() {
        mostrarQuarto(false);
        Utilizador utilizador = Sessao.getUtilizador();
        try {
            String cartao = acessoCartaoBLL.cartaoDoUtilizador(utilizador);
            if (AcessoCartao.CARTAO_MESTRE.equals(cartao)) {
                cartaoLabel.setText("O seu cartão: " + cartao + " (cartão mestre)");
            } else {
                cartaoLabel.setText("O seu cartão: " + cartao + " (" + utilizador.getNomeUtilizador() + ")");
            }
            localCombo.getItems().setAll(acessoCartaoBLL.locaisDoCartao(utilizador));
            atualizarTabela();
        } catch (IllegalStateException e) {
            cartaoLabel.setText("Sem cartão");
            localCombo.setDisable(true);
            mostrarErro(e.getMessage());
        } catch (DataAccessException e) {
            cartaoLabel.setText("Sem cartão");
            localCombo.setDisable(true);
            mostrarErro("Não foi possível encontrar o seu cartão. Tente novamente.");
        }
    }

    @FXML
    private void aoEscolherLocal() {
        mostrarQuarto(localCombo.getValue() == LocalHotel.QUARTO);
    }

    @FXML
    private void aoPicar() {
        try {
            AcessoCartao acesso = acessoCartaoBLL.picar(Sessao.getUtilizador(), localCombo.getValue(), lerQuarto());
            Mensagens.sucesso(mensagemLabel, "Acesso permitido: " + acesso.getLocalTexto() + " às "
                    + acesso.getHoraTexto() + ".");
            atualizarTabela();
        } catch (ValidacaoException | IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (DataAccessException e) {
            mostrarErro("Não foi possível registar a passagem. Tente novamente.");
        }
    }

    private int lerQuarto() {
        if (localCombo.getValue() != LocalHotel.QUARTO) {
            return 0;
        }
        String texto = quartoField.getText() == null ? "" : quartoField.getText().trim();
        if (texto.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O número do quarto tem de ser um número inteiro.");
        }
    }

    private void atualizarTabela() {
        try {
            tabela.getItems().setAll(acessoCartaoBLL.listarAsMinhasPassagens(Sessao.getUtilizador()));
        } catch (IllegalStateException | DataAccessException e) {
            tabela.getItems().clear();
        }
    }

    private void mostrarQuarto(boolean visivel) {
        quartoLabel.setVisible(visivel);
        quartoLabel.setManaged(visivel);
        quartoField.setVisible(visivel);
        quartoField.setManaged(visivel);
        if (!visivel) {
            quartoField.clear();
        }
    }

    private void mostrarErro(String texto) {
        Mensagens.erro(mensagemLabel, texto);
    }
}
