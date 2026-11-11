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
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "Acesso com cartão" (US39): regista a passagem de um cartão num local do hotel e mostra as passagens desse cartão.
 */
public class AcessoCartaoController {

    private final AcessoCartaoBLL acessoCartaoBLL = new AcessoCartaoBLL();

    @FXML
    private TextField cartaoField;
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
        localCombo.getItems().setAll(LocalHotel.values());
        mostrarQuarto(false);
    }

    @FXML
    private void aoEscolherLocal() {
        mostrarQuarto(localCombo.getValue() == LocalHotel.QUARTO);
    }

    @FXML
    private void aoRegistarPassagem() {
        try {
            int quarto = lerQuarto();
            AcessoCartao acesso = acessoCartaoBLL.registarPassagem(Sessao.getUtilizador(), cartaoField.getText(),
                    localCombo.getValue(), quarto);
            Mensagens.sucesso(mensagemLabel, "Acesso permitido: cartão " + acesso.getCartao() + " em "
                    + acesso.getLocalTexto() + " às " + acesso.getRegistadoEmTexto() + ".");
            atualizarTabela();
        } catch (ValidacaoException e) {
            mostrarErro(e.getMessage());
            atualizarTabela();
        } catch (IllegalArgumentException | IllegalStateException e) {
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
            tabela.getItems().setAll(acessoCartaoBLL.listarPorCartao(cartaoField.getText()));
        } catch (DataAccessException e) {
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
