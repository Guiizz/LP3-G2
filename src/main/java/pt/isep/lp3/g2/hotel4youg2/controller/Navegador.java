package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class Navegador {
    private static final String PASTA_VIEWS = "/pt/isep/lp3/g2/hotel4youg2/view/";
    private static final String ESTILO = "/pt/isep/lp3/g2/hotel4youg2/css/hotel4you.css";
    private static BorderPane janelaPrincipal;
    private static final Deque<Node> anteriores = new ArrayDeque<>();

    private Navegador(){

    }

    public static void iniciar(BorderPane principal) {
        janelaPrincipal = principal;
        anteriores.clear();
    }

    public static URL localizar(String ficheiro) {
        URL url = Navegador.class.getResource(PASTA_VIEWS + ficheiro);
        if (url == null) {
            throw new IllegalArgumentException("Não existe o ecrã " + ficheiro);
        }
        return url;
    }

    public static FXMLLoader carregar(String ficheiro) throws IOException {
        FXMLLoader loader = new FXMLLoader(localizar(ficheiro));
        loader.load();
        return loader;
    }

    public static void mostrar(String ficheiro) throws IOException {
        anteriores.clear();
        janelaPrincipal.setCenter(carregar(ficheiro).getRoot());
    }

    public static void mostrarDetalhe(Parent ecra) {
        anteriores.push(janelaPrincipal.getCenter());
        janelaPrincipal.setCenter(ecra);
    }

    public static void voltar() {
        if (!anteriores.isEmpty()) {
            janelaPrincipal.setCenter(anteriores.pop());
        }
    }

    public static void abrirFormulario(String ficheiro, String titulo) throws IOException {
        Parent formulario = carregar(ficheiro).getRoot();
        abrirFormulario(formulario, titulo);
    }

    public static void abrirFormulario(Parent formulario, String titulo) {
        Stage janela = new Stage();
        janela.setTitle("Hotel4You - " + titulo);
        Scene cena = new Scene(formulario);
        aplicarEstilo(cena);
        janela.setScene(cena);
        janela.initOwner(janelaPrincipal.getScene().getWindow());
        janela.initModality(Modality.WINDOW_MODAL);
        janela.showAndWait();
    }

    public static void fecharFormulario(Node noDoFormulario) {
        ((Stage) noDoFormulario.getScene().getWindow()).close();
    }

    public static void terminarSessao() throws IOException {
        Sessao.terminar();
        Stage janela = (Stage) janelaPrincipal.getScene().getWindow();
        for (Window outra : new ArrayList<>(Window.getWindows())) {
            if (outra != janela && outra instanceof Stage outraJanela) {
                outraJanela.close();
            }
        }
        anteriores.clear();
        janela.getScene().setRoot(carregar("login-view.fxml").getRoot());
        janela.setTitle("Hotel4You · Entrar");
    }

    public static void mostrarArea(String titulo, List<OpcaoMenu> opcoes) throws IOException {
        FXMLLoader loader = carregar("area-view.fxml");
        AreaController controlador = loader.getController();
        controlador.configurar(titulo, opcoes);
        anteriores.clear();
        janelaPrincipal.setCenter(loader.getRoot());
    }
    public static void aplicarEstilo(Scene cena) {
        cena.getStylesheets().add(Navegador.class.getResource(ESTILO).toExternalForm());
    }
}
