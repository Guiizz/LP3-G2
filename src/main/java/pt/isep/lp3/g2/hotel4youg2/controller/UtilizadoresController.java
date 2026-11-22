package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.UtilizadorBLL;
import pt.isep.lp3.g2.hotel4youg2.model.Conta;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.util.Optional;

/**
 * Controlador do ecrã "Contas": lista as contas dos funcionários e dos fornecedores e deixa o gestor desativá-las ou reativá-las (US22).
 */
public class UtilizadoresController {

    private final UtilizadorBLL utilizadorBLL = new UtilizadorBLL();

    @FXML
    private TableView<Conta> tabela;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        carregar();
    }
    @FXML
    private void aoEditar() {
        Conta escolhida = tabela.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            Mensagens.erro(mensagemLabel, "Escolha uma conta na tabela.");
            return;
        }
        if (escolhida.isDeFornecedor()) {
            Mensagens.erro(mensagemLabel, "Só se editam as contas dos funcionários.");
            return;
        }
        try {
            mensagemLabel.setText("");
            EditarFuncionarioController.abrir(escolhida.getUtilizador());
            carregar();
        } catch (IOException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível abrir a edição do funcionário.");
        }
    }

    @FXML
    private void aoReporPassword() {
        Conta escolhida = tabela.getSelectionModel().getSelectedItem();
        if (escolhida != null && !escolhida.isDeFornecedor() && !confirmarReposicao(escolhida)) {
            return;
        }
        try {
            String temporaria = utilizadorBLL.reporPassword(Sessao.getUtilizador(), escolhida);
            Mensagens.sucesso(mensagemLabel, "Palavra-passe reposta. Dê esta palavra-passe temporária a "
                    + escolhida.getNome() + ":\n" + temporaria + "\nNo próximo login, tem de escolher uma nova.");
            carregar();
        } catch (RuntimeException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        }
    }

    private boolean confirmarReposicao(Conta conta) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Repor a palavra-passe da conta " + conta.getNome() + "?\n"
                        + "A palavra-passe atual deixa de funcionar e o funcionário escolhe uma nova no próximo login.",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Repor palavra-passe");
        alerta.setHeaderText(null);
        Optional<ButtonType> resposta = alerta.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.YES;
    }

    @FXML
    private void aoDesativar() {
        executar(false);
    }

    @FXML
    private void aoReativar() {
        executar(true);
    }

    private void executar(boolean ativar) {
        Conta escolhida = tabela.getSelectionModel().getSelectedItem();
        try {
            if (ativar) {
                utilizadorBLL.reativarConta(Sessao.getUtilizador(), escolhida);
            } else {
                utilizadorBLL.desativarConta(Sessao.getUtilizador(), escolhida);
            }
            Mensagens.sucesso(mensagemLabel, ativar ? "Conta reativada." : "Conta desativada.");
            carregar();
        } catch (RuntimeException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        }
    }

    private void carregar() {
        tabela.getItems().setAll(utilizadorBLL.listarContas(Sessao.getUtilizador()));
    }
}
