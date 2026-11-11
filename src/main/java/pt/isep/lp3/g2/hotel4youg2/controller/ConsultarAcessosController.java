package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.AcessoCartaoBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.util.List;

/**
 * Controlador do ecrã "Registos de acesso" (US41): o gestor vê onde e quando um cartão foi usado, ou que cartões entraram num quarto.
 */
public class ConsultarAcessosController {

    private final AcessoCartaoBLL acessoCartaoBLL = new AcessoCartaoBLL();

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
            mostrar(acessoCartaoBLL.consultarPorCartao(Sessao.getUtilizador(), cartaoField.getText()));
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
            mostrar(acessoCartaoBLL.consultarPorQuarto(Sessao.getUtilizador(), quartoField.getText()));
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
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }
}
