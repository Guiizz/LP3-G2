package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.UtilizadorBLL;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "Contas": lista as contas e deixa o gestor desativá-las ou reativá-las (US22).
 */
public class UtilizadoresController {

    private final UtilizadorBLL utilizadorBLL = new UtilizadorBLL();

    @FXML
    private TableView<Utilizador> tabela;
    @FXML
    private TableColumn<Utilizador, String> colunaNome;
    @FXML
    private TableColumn<Utilizador, String> colunaPerfil;
    @FXML
    private TableColumn<Utilizador, String> colunaEstado;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        colunaNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomeUtilizador()));
        colunaPerfil.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPerfil().name()));
        colunaEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isAtivo() ? "Ativa" : "Desativada"));
        carregar();
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
        Utilizador escolhida = tabela.getSelectionModel().getSelectedItem();
        try {
            if (ativar) {
                utilizadorBLL.reativar(Sessao.getUtilizador(), escolhida);
            } else {
                utilizadorBLL.desativar(Sessao.getUtilizador(), escolhida);
            }
            mensagemLabel.setText(ativar ? "Conta reativada." : "Conta desativada.");
            carregar();
        } catch (RuntimeException e) {
            mensagemLabel.setText(e.getMessage());
        }
    }

    private void carregar() {
        tabela.getItems().setAll(utilizadorBLL.listar());
    }
}
