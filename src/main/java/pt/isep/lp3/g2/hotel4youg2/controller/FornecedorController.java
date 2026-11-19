package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import pt.isep.lp3.g2.hotel4youg2.bll.EntregaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ErrosFicheiroException;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoEntrega;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Controlador do ecrã do fornecedor: importa o XSD, carrega o ficheiro XML de uma entrega e mostra
 * o histórico das suas entregas com o estado de cada uma, com filtro por estado, e abre cada uma
 * como uma fatura (US38, US53 e US54).
 */
public class FornecedorController {

    private final EntregaBLL entregaBLL = new EntregaBLL();
    private Entrega entregaLida;

    @FXML
    private BorderPane raiz;
    @FXML
    private VBox ecra;
    @FXML
    private ComboBox<String> filtroCombo;
    @FXML
    private Label fornecedorLabel;
    @FXML
    private Label xsdLabel;
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
        mostrarXsdEmUso();
        filtroCombo.getItems().setAll(EntregaBLL.TODAS, EstadoEntrega.PENDENTE.name(), EstadoEntrega.ACEITE.name(),
                EstadoEntrega.RECUSADA.name());
        // escolher o valor do filtro dispara o aoFiltrar, que já preenche a tabela
        filtroCombo.setValue(EntregaBLL.TODAS);
    }

    @FXML
    private void aoFiltrar() {
        atualizarHistorico();
    }

    @FXML
    private void aoVerFatura() {
        ResumoEntrega escolhida = tabela.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            mostrarMensagem("Escolha primeiro uma entrega na tabela.", "red");
            return;
        }
        try {
            Entrega entrega = entregaBLL.faturaParaFornecedor(Sessao.getFornecedor(), escolhida.getEntregaId());
            FaturaEntregaController.abrir(entrega, ecra);
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível abrir a entrega. Tente novamente.", "red");
        } catch (IOException e) {
            mostrarMensagem("Não foi possível abrir a fatura da entrega.", "red");
        }
    }

    @FXML
    private void aoImportarXsd() {
        FileChooser escolha = new FileChooser();
        escolha.setTitle("Escolher o XSD das entregas");
        escolha.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ficheiros XSD", "*.xsd"));
        File ficheiro = escolha.showOpenDialog(tabela.getScene().getWindow());
        if (ficheiro == null) {
            return;
        }
        try {
            entregaBLL.importarXsd(ficheiro);
            limpar();
            ficheiroLabel.setText("Nenhum ficheiro escolhido");
            mostrarMensagem("XSD " + ficheiro.getName() + " importado. Os próximos ficheiros XML são validados com ele.",
                    "green");
        } catch (ErrosFicheiroException e) {
            mostrarMensagem("XSD recusado. Continua o XSD anterior." + emLista(e.getErros()), "red");
        } catch (IllegalArgumentException e) {
            mostrarMensagem(e.getMessage(), "red");
        }
        mostrarXsdEmUso();
    }

    @FXML
    private void aoEscolherFicheiro() {
        FileChooser escolha = new FileChooser();
        escolha.setTitle("Escolher o XML ou o JSON da entrega");
        escolha.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ficheiros XML ou JSON", "*.xml", "*.json"));
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
            List<String> avisos = entregaBLL.verificarContas(entregaLida);
            if (avisos.isEmpty()) {
                mostrarMensagem("Ficheiro válido. Carregue em \"Carregar entrega\" para o enviar ao hotel.", "green");
            } else {
                mostrarMensagem("Ficheiro válido, com diferenças de cêntimos (arredondamentos):" + emLista(avisos)
                        + "\nPode carregar a entrega na mesma.", "darkorange");
            }
        } catch (ErrosFicheiroException e) {
            mostrarMensagem("Ficheiro recusado. Nada foi guardado. Corrija:" + emLista(e.getErros()), "red");
        } catch (IllegalArgumentException e) {
            mostrarMensagem(e.getMessage(), "red");
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
            EstadoEntrega estado = EntregaBLL.estadoDoFiltro(filtroCombo.getValue());
            tabela.getItems().setAll(entregaBLL.historico(Sessao.getFornecedor(), estado));
        } catch (IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível obter o histórico.", "red");
        }
    }

    private void mostrarXsdEmUso() {
        String nome = entregaBLL.nomeXsdEmUso();
        if (nome == null) {
            xsdLabel.setText("XSD em uso: o do projeto (Hotel4You.xsd)");
        } else {
            xsdLabel.setText("XSD em uso: " + nome);
        }
    }

    // um erro por linha, com um ponto à frente
    private static String emLista(List<String> erros) {
        StringBuilder texto = new StringBuilder();
        for (String erro : erros) {
            texto.append("\n• ").append(erro);
        }
        return texto.toString();
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
