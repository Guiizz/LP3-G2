package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EscolherPessoaController {

    @FXML
    private Label tituloLabel;
    @FXML
    private TextField pesquisaField;
    @FXML
    private ListView<Object> lista;

    private List<?> pessoas = new ArrayList<>();
    private Object escolhida;

    @SuppressWarnings("unchecked")
    public static <T> T escolher(String titulo, List<T> pessoas) throws IOException {
        FXMLLoader loader = Navegador.carregar("escolher-pessoa-view.fxml");
        EscolherPessoaController controlador = loader.getController();
        controlador.configurar(titulo, pessoas);
        Navegador.abrirFormulario((Parent) loader.getRoot(), titulo);
        return (T) controlador.escolhida;
    }

    static List<Object> filtrar(List<?> pessoas, String termo) {
        String procurado = termo == null ? "" : termo.trim().toLowerCase();
        List<Object> encontradas = new ArrayList<>();
        for (Object pessoa : pessoas) {
            if (pessoa.toString().toLowerCase().contains(procurado)) {
                encontradas.add(pessoa);
            }
        }
        return encontradas;
    }

    private void configurar(String titulo, List<?> pessoas) {
        this.pessoas = pessoas;
        tituloLabel.setText(titulo);
        lista.getItems().setAll(pessoas);
    }

    @FXML
    private void aoPesquisar() {
        lista.getItems().setAll(filtrar(pessoas, pesquisaField.getText()));
    }

    @FXML
    private void aoClicar(MouseEvent evento) {
        if (evento.getClickCount() == 2) {
            aoEscolher();
        }
    }

    @FXML
    private void aoEscolher() {
        Object selecionada = lista.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            escolhida = selecionada;
            Navegador.fecharFormulario(lista);
        }
    }

    @FXML
    private void aoCancelar() {
        Navegador.fecharFormulario(lista);
    }
}
