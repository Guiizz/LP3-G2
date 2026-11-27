package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.StockBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.MovimentoStock;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoMovimento;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.util.List;

/**
 * Controlador da "Janela de stock" (US62): mostra cada material com o que entrou, o que saiu e o stock efetivo,
 * permite pesquisar e mostra o histórico de movimentos do material escolhido, com o motivo de cada saída e com
 * filtro por entradas, saídas temporárias, saídas definitivas e devoluções (US77).
 */
public class StockController implements AtualizadorDeTabelas.Ecra {

    private static final AtualizadorDeTabelas.Chave<MaterialStock> PELO_MATERIAL = new IdDoMaterial();

    private final StockBLL stockBLL = new StockBLL();

    @FXML
    private TextField pesquisaField;
    @FXML
    private TableView<MaterialStock> materiaisTabela;
    @FXML
    private Label materialLabel;
    @FXML
    private ComboBox<TipoMovimento> tipoCombo;
    @FXML
    private TableView<MovimentoStock> movimentosTabela;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        tipoCombo.getItems().setAll(TipoMovimento.values());
        aoPesquisar();
        AtualizadorDeTabelas.ligar(materiaisTabela, this);
    }

    @Override
    public void recarregar() {
        aoPesquisar();
    }

    @FXML
    private void aoPesquisar() {
        try {
            AtualizadorDeTabelas.mostrar(materiaisTabela,
                    stockBLL.pesquisar(Sessao.getUtilizador(), pesquisaField.getText()), PELO_MATERIAL);
            Mensagens.info(mensagemLabel, "");
        } catch (IllegalStateException e) {
            materiaisTabela.getItems().clear();
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            materiaisTabela.getItems().clear();
            Mensagens.erro(mensagemLabel, "Não foi possível carregar o stock. Tente novamente.");
        }
        mostrarHistorico();
    }

    @FXML
    private void aoEscolherMaterial() {
        mostrarHistorico();
    }

    @FXML
    private void aoFiltrar() {
        mostrarHistorico();
    }

    @FXML
    private void aoLimparFiltro() {
        tipoCombo.setValue(null);
    }

    private void mostrarHistorico() {
        MaterialStock material = materiaisTabela.getSelectionModel().getSelectedItem();
        if (material == null) {
            materialLabel.setText("Histórico: escolha um material na tabela");
            movimentosTabela.getItems().clear();
            return;
        }
        materialLabel.setText("Histórico de " + material.getDescricao() + " (" + material.getFornecedor() + ")");
        try {
            List<MovimentoStock> movimentos = stockBLL.historico(Sessao.getUtilizador(), material, tipoCombo.getValue());
            movimentosTabela.getItems().setAll(movimentos);
            Mensagens.info(mensagemLabel, movimentos.size() + " movimento(s)");
        } catch (IllegalArgumentException | IllegalStateException e) {
            movimentosTabela.getItems().clear();
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            movimentosTabela.getItems().clear();
            Mensagens.erro(mensagemLabel, "Não foi possível carregar o histórico. Tente novamente.");
        }
    }

    private static class IdDoMaterial implements AtualizadorDeTabelas.Chave<MaterialStock> {
        @Override
        public Object de(MaterialStock material) {
            return material.getMaterialId();
        }
    }
}
