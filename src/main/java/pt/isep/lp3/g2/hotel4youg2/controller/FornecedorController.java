package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import pt.isep.lp3.g2.hotel4youg2.bll.EntregaBLL;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoEntrega;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.File;
import java.io.IOException;

/**
 * Controlador do ecrã do fornecedor: carrega o ficheiro XML de uma entrega e mostra o histórico
 * das suas entregas com o estado de cada uma (US38).
 */
public class FornecedorController {

    private final EntregaBLL entregaBLL = new EntregaBLL();
    private Entrega entregaLida;

    @FXML
    private BorderPane raiz;
    @FXML
    private Label fornecedorLabel;
    @FXML
    private Label ficheiroLabel;
    @FXML
    private Label resumoLabel;
    @FXML
    private Label mensagemLabel;
    @FXML
    private Button carregarButton;
    @FXML
    private TableView<ResumoEntrega> tabela;

    @FXML
    private void initialize() {
        Navegador.iniciar(raiz);
        Fornecedor fornecedor = Sessao.getFornecedor();
        if (fornecedor != null) {
            fornecedorLabel.setText(fornecedor.toString());
        }
        atualizarHistorico();
    }

    @FXML
    private void aoEscolherFicheiro() {
        FileChooser escolha = new FileChooser();
        escolha.setTitle("Escolher o XML da entrega");
        escolha.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ficheiros XML", "*.xml"));
        File ficheiro = escolha.showOpenDialog(tabela.getScene().getWindow());
        if (ficheiro == null) {
            return;
        }
        ficheiroLabel.setText(ficheiro.getName());
        limpar();
        try {
            entregaLida = entregaBLL.lerFicheiro(ficheiro);
            resumoLabel.setText("Encomenda " + entregaLida.getNumeroEncomenda() + " com "
                    + entregaLida.getLinhas().size() + " linhas, total " + entregaLida.getTotal() + " €.");
            carregarButton.setDisable(false);
            mostrarMensagem("Ficheiro válido. Carregue em \"Carregar entrega\" para o enviar ao hotel.", "green");
        } catch (IllegalArgumentException e) {
            mostrarMensagem("Ficheiro recusado. " + e.getMessage(), "red");
        }
    }

    @FXML
    private void aoCarregar() {
        try {
            entregaBLL.carregar(Sessao.getFornecedor(), entregaLida);
            mostrarMensagem("Entrega " + entregaLida.getNumeroEncomenda() + " enviada. Fica pendente até o hotel decidir.",
                    "green");
            limpar();
            atualizarHistorico();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível carregar a entrega. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoTerminarSessao() throws IOException {
        Navegador.terminarSessao();
    }

    private void atualizarHistorico() {
        try {
            tabela.getItems().setAll(entregaBLL.historico(Sessao.getFornecedor()));
        } catch (IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível obter o histórico.", "red");
        }
    }

    private void limpar() {
        entregaLida = null;
        resumoLabel.setText("");
        carregarButton.setDisable(true);
    }

    private void mostrarMensagem(String texto, String cor) {
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }
}
