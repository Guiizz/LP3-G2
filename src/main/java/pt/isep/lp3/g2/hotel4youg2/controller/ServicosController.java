package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import pt.isep.lp3.g2.hotel4youg2.bll.ServicoBLL;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.TipoCobranca;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServicosController {

    private static final Categoria TODAS = new Categoria(0, "Todas as categorias");

    private final ServicoBLL servicoBLL = new ServicoBLL();
    private List<Servico> todos = new ArrayList<>();

    @FXML
    private ComboBox<Categoria> filtroCategoria;
    @FXML
    private TableView<Servico> tabela;
    @FXML
    private TableColumn<Servico, String> colunaCategoria;
    @FXML
    private TableColumn<Servico, String> colunaDesignacao;
    @FXML
    private TableColumn<Servico, String> colunaPreco;
    @FXML
    private TableColumn<Servico, String> colunaCobranca;
    @FXML
    private TableColumn<Servico, String> colunaEstado;
    @FXML
    private Button desativarButton;
    @FXML
    private Button reativarButton;
    @FXML
    private Label tituloFormularioLabel;
    @FXML
    private TextField campoDesignacao;
    @FXML
    private ComboBox<Categoria> campoCategoria;
    @FXML
    private TextField campoPreco;
    @FXML
    private ComboBox<TipoCobranca> campoTipoCobranca;
    @FXML
    private Button guardarButton;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        colunaCategoria.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoria().getNome()));
        colunaDesignacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignacao()));
        colunaPreco.setCellValueFactory(c -> new SimpleStringProperty(formatarPreco(c.getValue().getPreco())));
        colunaCobranca.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoCobranca().getDescricao()));
        colunaEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isAtivo() ? "Ativo" : "Desativado"));
        campoTipoCobranca.getItems().setAll(TipoCobranca.values());

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, escolhido) -> mostrarEscolhido(escolhido));
        filtroCategoria.setOnAction(e -> aplicarFiltro());
        carregarCategorias();
        filtroCategoria.setValue(TODAS);
        carregar();
        mostrarEscolhido(null);
    }

    @FXML
    private void aoGuardar() {
        Servico escolhido = tabela.getSelectionModel().getSelectedItem();
        try {
            BigDecimal preco = ServicoBLL.lerPreco(campoPreco.getText());
            if (escolhido == null) {
                Servico novo = servicoBLL.criar(Sessao.getUtilizador(), campoDesignacao.getText(), preco, campoTipoCobranca.getValue(), campoCategoria.getValue());
                carregar();
                limparFormulario();
                mostrarMensagem("Serviço \"" + novo.getDesignacao() + "\" criado.", "green");
            } else {
                Servico alterado = servicoBLL.editar(Sessao.getUtilizador(), escolhido.getId(), campoDesignacao.getText(), preco, campoTipoCobranca.getValue(), campoCategoria.getValue());
                carregar();
                selecionar(alterado.getId());
                mostrarMensagem("Serviço \"" + alterado.getDesignacao() + "\" guardado.", "green");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível guardar o serviço. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoNovaCategoria() {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Nova categoria");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Nome da categoria:");
        Optional<String> nome = dialogo.showAndWait();
        if (nome.isEmpty()) {
            return;
        }
        try {
            Categoria nova = servicoBLL.criarCategoria(Sessao.getUtilizador(), nome.get());
            Categoria filtro = filtroCategoria.getValue();
            carregarCategorias();
            filtroCategoria.setValue(filtro);
            campoCategoria.setValue(procurarCategoria(nova.getId()));
            mostrarMensagem("Categoria \"" + nova.getNome() + "\" criada.", "green");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível criar a categoria. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoNovo() {
        tabela.getSelectionModel().clearSelection();
        mostrarMensagem("", "black");
    }

    @FXML
    private void aoDesativar() {
        Servico escolhido = tabela.getSelectionModel().getSelectedItem();
        if (escolhido == null || !confirmarDesativar(escolhido)) {
            return;
        }
        try {
            servicoBLL.desativar(Sessao.getUtilizador(), escolhido.getId());
            carregar();
            selecionar(escolhido.getId());
            mostrarMensagem("Serviço \"" + escolhido.getDesignacao() + "\" desativado.", "green");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível desativar o serviço. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoReativar() {
        Servico escolhido = tabela.getSelectionModel().getSelectedItem();
        if (escolhido == null) {
            return;
        }
        try {
            servicoBLL.reativar(Sessao.getUtilizador(), escolhido.getId());
            carregar();
            selecionar(escolhido.getId());
            mostrarMensagem("Serviço \"" + escolhido.getDesignacao() + "\" reativado.", "green");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível reativar o serviço. Tente novamente.", "red");
        }
    }

    private boolean confirmarDesativar(Servico servico) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, "Tem a certeza que quer desativar o serviço \"" + servico.getDesignacao() + "\"?\n" + "Deixa de poder ser adicionado às reservas, mas pode ser reativado mais tarde.", ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Desativar serviço");
        alerta.setHeaderText(null);
        Optional<ButtonType> resposta = alerta.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.YES;
    }

    private void mostrarEscolhido(Servico servico) {
        if (servico == null) {
            limparFormulario();
            return;
        }
        tituloFormularioLabel.setText("Editar serviço");
        guardarButton.setText("Guardar alterações");
        campoDesignacao.setText(servico.getDesignacao());
        campoCategoria.setValue(procurarCategoria(servico.getCategoria().getId()));
        campoPreco.setText(servico.getPreco().toPlainString().replace('.', ','));
        campoTipoCobranca.setValue(servico.getTipoCobranca());
        desativarButton.setDisable(!servico.isAtivo());
        reativarButton.setDisable(servico.isAtivo());
    }

    private void limparFormulario() {
        tituloFormularioLabel.setText("Novo serviço");
        guardarButton.setText("Criar serviço");
        campoDesignacao.clear();
        campoCategoria.setValue(null);
        campoPreco.clear();
        campoTipoCobranca.setValue(null);
        desativarButton.setDisable(true);
        reativarButton.setDisable(true);
    }

    private void carregarCategorias() {
        try {
            List<Categoria> categorias = servicoBLL.listarCategorias();
            campoCategoria.getItems().setAll(categorias);
            filtroCategoria.getItems().setAll(TODAS);
            filtroCategoria.getItems().addAll(categorias);
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível carregar as categorias: " + e.getMessage(), "red");
        }
    }

    private void carregar() {
        try {
            todos = servicoBLL.listar();
            aplicarFiltro();
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível carregar os serviços: " + e.getMessage(), "red");
        }
    }

    private void aplicarFiltro() {
        Categoria filtro = filtroCategoria.getValue();
        List<Servico> visiveis = new ArrayList<>();
        for (Servico servico : todos) {
            if (filtro == null || filtro == TODAS || servico.getCategoria().getId() == filtro.getId()) {
                visiveis.add(servico);
            }
        }
        tabela.getItems().setAll(visiveis);
    }

    private Categoria procurarCategoria(int id) {
        for (Categoria categoria : campoCategoria.getItems()) {
            if (categoria.getId() == id) {
                return categoria;
            }
        }
        return null;
    }

    private void selecionar(int id) {
        for (Servico servico : tabela.getItems()) {
            if (servico.getId() == id) {
                tabela.getSelectionModel().select(servico);
                return;
            }
        }
    }

    private void mostrarMensagem(String texto, String cor) {
        mensagemLabel.setStyle("-fx-text-fill: " + cor + ";");
        mensagemLabel.setText(texto);
    }

    private static String formatarPreco(BigDecimal preco) {
        return preco.setScale(2).toPlainString().replace('.', ',') + " €";
    }
}
