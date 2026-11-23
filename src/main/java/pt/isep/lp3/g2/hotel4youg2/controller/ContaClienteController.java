package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.ContaClienteBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "Conta do cliente": a receção cria a conta ou gera um código novo.
 */
public class ContaClienteController {

    private final ContaClienteBLL contaClienteBLL = new ContaClienteBLL();

    @FXML
    private TextField nifField;
    @FXML
    private Label mensagemLabel;
    @FXML
    private Label codigoLabel;

    @FXML
    private void aoCriarConta() {
        codigoLabel.setText("");
        try {
            String codigo = contaClienteBLL.criarConta(Sessao.getUtilizador(), nifField.getText());
            mostrarCodigo("Conta criada. Entregue este código ao cliente para o primeiro acesso:", codigo);
        } catch (ValidacaoException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível criar a conta. Tente novamente.");
        }
    }

    @FXML
    private void aoGerarNovoCodigo() {
        codigoLabel.setText("");
        try {
            String codigo = contaClienteBLL.gerarNovoCodigo(Sessao.getUtilizador(), nifField.getText());
            mostrarCodigo("Código novo gerado. O cliente volta a escolher a palavra-passe com este código:", codigo);
        } catch (ValidacaoException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível gerar o código. Tente novamente.");
        }
    }

    private void mostrarCodigo(String texto, String codigo) {
        Mensagens.sucesso(mensagemLabel, texto);
        codigoLabel.setText(codigo);
    }

    @FXML
    private void aoFechar() {
        Navegador.fecharFormulario(nifField);
    }
}
