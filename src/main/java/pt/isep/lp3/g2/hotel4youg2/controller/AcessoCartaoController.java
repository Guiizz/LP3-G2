package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import pt.isep.lp3.g2.hotel4youg2.bll.AcessoCartaoBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.LocalPicagem;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "Acesso com cartão" (US39, US52 e US73): o funcionário pica só com o seu cartão
 * e o gestor só com o cartão mestre, e vê as suas picagens.
 */
public class AcessoCartaoController {

    private final AcessoCartaoBLL acessoCartaoBLL = new AcessoCartaoBLL();
    private String cartao;

    @FXML
    private Label cartaoLabel;
    @FXML
    private HBox picarBox;
    @FXML
    private ComboBox<LocalPicagem> localCombo;
    @FXML
    private Label mensagemLabel;
    @FXML
    private TableView<AcessoCartao> tabela;

    @FXML
    private void initialize() {
        try {
            cartao = acessoCartaoBLL.cartaoDe(Sessao.getUtilizador());
            cartaoLabel.setText(cartao.equals(AcessoCartao.CARTAO_MESTRE) ? cartao + " (cartão mestre)" : cartao);
            localCombo.getItems().setAll(acessoCartaoBLL.listarLocais());
            atualizarTabela();
        } catch (IllegalStateException e) {
            semCartao(e.getMessage());
        } catch (DataAccessException e) {
            semCartao("Não foi possível encontrar o seu cartão. Tente novamente.");
        }
    }

    @FXML
    private void aoPicar() {
        try {
            AcessoCartao acesso = acessoCartaoBLL.picar(Sessao.getUtilizador(), localCombo.getValue());
            Mensagens.sucesso(mensagemLabel, "Acesso permitido: cartão " + acesso.getCartao() + " em "
                    + acesso.getLocalTexto() + " às " + acesso.getRegistadoEmTexto() + ".");
            atualizarTabela();
        } catch (ValidacaoException | IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível registar a picagem. Tente novamente.");
        }
    }

    private void atualizarTabela() {
        try {
            tabela.getItems().setAll(acessoCartaoBLL.listarPorCartao(cartao));
        } catch (DataAccessException e) {
            tabela.getItems().clear();
        }
    }

    private void semCartao(String motivo) {
        cartaoLabel.setText("Sem cartão");
        picarBox.setVisible(false);
        picarBox.setManaged(false);
        Mensagens.erro(mensagemLabel, motivo);
    }
}
