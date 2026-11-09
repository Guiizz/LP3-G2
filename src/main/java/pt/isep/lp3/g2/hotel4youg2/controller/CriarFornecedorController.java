package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.FornecedorBLL;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "Criar fornecedor": o gestor cria a conta de um fornecedor (US38).
 */
public class CriarFornecedorController {

    private final FornecedorBLL fornecedorBLL = new FornecedorBLL();

    @FXML
    private TextField campoCodigo;
    @FXML
    private TextField campoNome;
    @FXML
    private PasswordField campoPassword;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void aoCriar() {
        try {
            Fornecedor criado = fornecedorBLL.criar(Sessao.getUtilizador(), campoCodigo.getText(),
                    campoNome.getText(), campoPassword.getText());
            campoCodigo.clear();
            campoNome.clear();
            campoPassword.clear();
            Mensagens.sucesso(mensagemLabel, "Fornecedor " + criado.getCodigo() + " criado.");
        } catch (RuntimeException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        }
    }
}
