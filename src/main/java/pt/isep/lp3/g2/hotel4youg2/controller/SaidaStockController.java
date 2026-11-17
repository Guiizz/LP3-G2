package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.StockBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.SaidaStock;
import pt.isep.lp3.g2.hotel4youg2.model.enums.MotivoSaida;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "Saída de materiais" (US42): mostra o stock de cada material e regista as saídas,
 * com o tipo (temporária ou definitiva) e o motivo, e dá como devolvidas as saídas temporárias (US77).
 */
public class SaidaStockController {

    private final StockBLL stockBLL = new StockBLL();

    @FXML
    private TableView<MaterialStock> tabela;
    @FXML
    private TextField quantidadeField;
    @FXML
    private ComboBox<TipoSaida> tipoCombo;
    @FXML
    private ComboBox<MotivoSaida> motivoCombo;
    @FXML
    private TextField motivoField;
    @FXML
    private TableView<SaidaStock> porDevolverTabela;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        tipoCombo.getItems().setAll(TipoSaida.values());
        motivoCombo.getItems().setAll(MotivoSaida.escolhiveis());
        aoEscolherTipo();
        atualizar();
    }

    @FXML
    private void aoEscolherTipo() {
        boolean definitiva = tipoCombo.getValue() == TipoSaida.DEFINITIVA;
        motivoCombo.setDisable(!definitiva);
        if (!definitiva) {
            motivoCombo.setValue(null);
            motivoField.setPromptText("Motivo (opcional), ex.: Reparação");
        } else {
            motivoField.setPromptText("Escreva o motivo (obrigatório no \"Outro\")");
        }
    }

    @FXML
    private void aoRegistarSaida() {
        MaterialStock material = tabela.getSelectionModel().getSelectedItem();
        try {
            stockBLL.registarSaida(Sessao.getUtilizador(), material, quantidadeField.getText(), tipoCombo.getValue(),
                    motivoCombo.getValue(), motivoField.getText());
            mostrarMensagem("Saída " + tipoCombo.getValue().getDescricao().toLowerCase() + " registada: "
                    + material.getDescricao() + ".", "green");
            limparFormulario();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível registar a saída. Tente novamente.", "red");
        }
        atualizar();
    }

    @FXML
    private void aoDevolver() {
        SaidaStock saida = porDevolverTabela.getSelectionModel().getSelectedItem();
        try {
            stockBLL.devolver(Sessao.getUtilizador(), saida);
            mostrarMensagem(saida.getQuantidade() + " × " + saida.getMaterial() + " voltou ao stock.", "green");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível devolver a saída. Tente novamente.", "red");
        }
        atualizar();
    }

    private void limparFormulario() {
        quantidadeField.clear();
        tipoCombo.setValue(null);
        motivoCombo.setValue(null);
        motivoField.clear();
    }

    private void atualizar() {
        try {
            tabela.getItems().setAll(stockBLL.listar(Sessao.getUtilizador()));
            porDevolverTabela.getItems().setAll(stockBLL.listarPorDevolver(Sessao.getUtilizador()));
        } catch (IllegalStateException e) {
            tabela.getItems().clear();
            porDevolverTabela.getItems().clear();
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            tabela.getItems().clear();
            porDevolverTabela.getItems().clear();
            mostrarMensagem("Não foi possível carregar o stock. Tente novamente.", "red");
        }
    }

    private void mostrarMensagem(String texto, String cor) {
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }
}
