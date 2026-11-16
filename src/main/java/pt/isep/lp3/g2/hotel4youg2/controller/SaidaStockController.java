package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.StockBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "Saída de materiais" (US42): mostra o stock de cada material e regista as saídas.
 */
public class SaidaStockController {

    private final StockBLL stockBLL = new StockBLL();

    @FXML
    private TableView<MaterialStock> tabela;
    @FXML
    private TextField quantidadeField;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        atualizar();
    }

    @FXML
    private void aoRegistarSaida() {
        MaterialStock material = tabela.getSelectionModel().getSelectedItem();
        try {
            stockBLL.registarSaida(Sessao.getUtilizador(), material, quantidadeField.getText());
            quantidadeField.clear();
            mostrarMensagem("Saída registada: " + material.getDescricao() + ".", "green");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível registar a saída. Tente novamente.", "red");
        }
        atualizar();
    }

    private void atualizar() {
        try {
            tabela.getItems().setAll(stockBLL.listar(Sessao.getUtilizador()));
        } catch (IllegalStateException e) {
            tabela.getItems().clear();
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            tabela.getItems().clear();
            mostrarMensagem("Não foi possível carregar o stock. Tente novamente.", "red");
        }
    }

    private void mostrarMensagem(String texto, String cor) {
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }
}
