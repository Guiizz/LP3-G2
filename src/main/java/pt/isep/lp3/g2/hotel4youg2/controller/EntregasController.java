package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import pt.isep.lp3.g2.hotel4youg2.bll.EntregaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador do ecrã das entregas de fornecedores: escolhe o ficheiro XML, mostra os dados,
 * avisa das contas erradas e regista a entrega (US26).
 */
public class EntregasController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EntregaBLL entregaBLL = new EntregaBLL();
    private Entrega entregaLida;

    @FXML
    private Label ficheiroLabel;
    @FXML
    private Label mensagemLabel;
    @FXML
    private GridPane dadosGrid;
    @FXML
    private Label encomendaLabel;
    @FXML
    private Label dataLabel;
    @FXML
    private Label fornecedorLabel;
    @FXML
    private Label moradaLabel;
    @FXML
    private TableView<LinhaEntrega> tabela;
    @FXML
    private Label totalLabel;
    @FXML
    private Button registarButton;

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
            mostrar(entregaLida);
            List<String> avisos = entregaBLL.verificarContas(entregaLida);
            if (avisos.isEmpty()) {
                mostrarMensagem("Ficheiro válido. Confira os dados e carregue em \"Registar entrega\".", "green");
            } else {
                mostrarMensagem("Ficheiro válido, mas com contas que não batem certo:\n• " + String.join("\n• ", avisos)
                        + "\nConfira com o fornecedor. Pode registar a entrega na mesma.", "darkorange");
            }
        } catch (IllegalArgumentException e) {
            mostrarMensagem("Ficheiro recusado. " + e.getMessage(), "red");
        }
    }

    @FXML
    private void aoRegistar() {
        try {
            entregaBLL.registar(Sessao.getUtilizador(), entregaLida);
            mostrarMensagem("Entrega " + entregaLida.getNumeroEncomenda() + " registada.", "green");
            registarButton.setDisable(true);
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível registar a entrega. Tente novamente.", "red");
        }
    }

    private void mostrar(Entrega entrega) {
        encomendaLabel.setText(entrega.getNumeroEncomenda());
        dataLabel.setText(entrega.getDataEncomenda().format(FORMATO_DATA));
        fornecedorLabel.setText(entrega.getFornecedor().toString());
        moradaLabel.setText(entrega.getFornecedor().getMoradaCompleta());
        dadosGrid.setVisible(true);
        dadosGrid.setManaged(true);
        tabela.getItems().setAll(entrega.getLinhas());
        totalLabel.setText(euros(entrega.getTotal()));
        registarButton.setDisable(false);
    }

    private void limpar() {
        entregaLida = null;
        dadosGrid.setVisible(false);
        dadosGrid.setManaged(false);
        tabela.getItems().clear();
        totalLabel.setText("");
        registarButton.setDisable(true);
    }

    private void mostrarMensagem(String texto, String cor) {
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }

    private static String euros(BigDecimal valor) {
        return valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " €";
    }
}
