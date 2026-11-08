package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.AcessoCartaoBLL;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.util.List;

public class ConsultarAcessosController {

    private final AcessoCartaoBLL acessoBLL = new AcessoCartaoBLL();

    @FXML
    private TextField cartaoField;
    @FXML
    private TextField quartoField;
    @FXML
    private TableView<AcessoCartao> tabela;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void aoProcurarCartao() {
        tabela.getItems().clear();
        try {
            mostrar(acessoBLL.consultarPorCartao(Sessao.getUtilizador(), cartaoField.getText()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível consultar os registos. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoProcurarQuarto() {
        tabela.getItems().clear();
        try {
            mostrar(acessoBLL.consultarPorQuarto(Sessao.getUtilizador(), quartoField.getText()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível consultar os registos. Tente novamente.", "red");
        }
    }

    private void mostrar(List<AcessoCartao> acessos) {
        tabela.getItems().setAll(acessos);
        mostrarMensagem(acessos.isEmpty() ? "Não há registos de acesso." : acessos.size()
                + " registo(s), do mais recente para o mais antigo.", "black");
    }

    private void mostrarMensagem(String texto, String cor) {
        mensagemLabel.setStyle("-fx-text-fill: " + cor + ";");
        mensagemLabel.setText(texto);
    }
}
