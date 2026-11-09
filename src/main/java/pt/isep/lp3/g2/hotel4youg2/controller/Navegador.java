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

/**
 * Navegação entre ecrãs (US34). A janela principal é um {@code BorderPane}: o menu fica à esquerda
 * e o ecrã escolhido aparece no centro. Os formulários abrem em pop-up por cima da janela principal.
 * Todas as janelas usam a folha de estilos {@code hotel4you.css} (US44).
 */
public final class Navegador {
    private static final String PASTA_VIEWS = "/pt/isep/lp3/g2/hotel4youg2/view/";
    private static final String ESTILO = "/pt/isep/lp3/g2/hotel4youg2/css/hotel4you.css";
    private static BorderPane janelaPrincipal;
    private static final Deque<Node> anteriores = new ArrayDeque<>();

    private Navegador(){

    }

    /**
     * Guarda a janela principal onde os ecrãs vão aparecer. É chamado quando o menu principal abre.
     *
     * @param principal o {@code BorderPane} do menu principal
     */
    public static void iniciar(BorderPane principal) {
        janelaPrincipal = principal;
        anteriores.clear();
    }

    /**
     * Procura um ecrã na pasta das views.
     *
     * @param ficheiro o nome do ficheiro FXML, por exemplo {@code "gerir-reservas-view.fxml"}
     * @return o endereço do ficheiro
     * @throws IllegalArgumentException se o ecrã não existir
     */
    public static URL localizar(String ficheiro) {
        URL url = Navegador.class.getResource(PASTA_VIEWS + ficheiro);
        if (url == null) {
            throw new IllegalArgumentException("Não existe o ecrã " + ficheiro);
        }
        return url;
    }

    /**
     * Carrega um ecrã. O loader devolvido dá acesso ao ecrã ({@code getRoot()}) e ao seu controlador
     * ({@code getController()}).
     *
     * @param ficheiro o nome do ficheiro FXML
     * @return o loader, já carregado
     * @throws IOException se não for possível ler o FXML
     */
    public static FXMLLoader carregar(String ficheiro) throws IOException {
        FXMLLoader loader = new FXMLLoader(localizar(ficheiro));
        loader.load();
        return loader;
    }

    /**
     * Mostra um ecrã no centro da janela principal e esquece os ecrãs anteriores.
     *
     * @param ficheiro o nome do ficheiro FXML
     * @throws IOException se não for possível ler o FXML
     */
    public static void mostrar(String ficheiro) throws IOException {
        anteriores.clear();
        janelaPrincipal.setCenter(carregar(ficheiro).getRoot());
    }

    /**
     * Mostra um ecrã de detalhe no centro da janela principal, guardando o atual para se poder
     * voltar a ele com {@link #voltar()}.
     *
     * @param ecra o ecrã de detalhe, já carregado
     */
    public static void mostrarDetalhe(Parent ecra) {
        anteriores.push(janelaPrincipal.getCenter());
        janelaPrincipal.setCenter(ecra);
    }

    /**
     * Volta ao ecrã que estava antes do último {@link #mostrarDetalhe(Parent)}. Se não houver nenhum, não faz nada.
     */
    public static void voltar() {
        if (!anteriores.isEmpty()) {
            janelaPrincipal.setCenter(anteriores.pop());
        }
    }

    /**
     * Abre um formulário em pop-up e espera que seja fechado.
     *
     * @param ficheiro o nome do ficheiro FXML do formulário
     * @param titulo   o título da janela
     * @throws IOException se não for possível ler o FXML
     */
    public static void abrirFormulario(String ficheiro, String titulo) throws IOException {
        Parent formulario = carregar(ficheiro).getRoot();
        abrirFormulario(formulario, titulo);
    }

    /**
     * Abre um formulário já carregado em pop-up, por cima da janela principal, com a folha de estilos,
     * e só volta quando for fechado. Assim quem o abriu pode atualizar a sua lista a seguir.
     *
     * @param formulario o formulário, já carregado
     * @param titulo     o título da janela
     */
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

    /**
     * Fecha o pop-up onde está um elemento do formulário.
     *
     * @param noDoFormulario qualquer elemento do formulário, por exemplo o botão "Cancelar"
     */
    public static void fecharFormulario(Node noDoFormulario) {
        ((Stage) noDoFormulario.getScene().getWindow()).close();
    }

    /**
     * Termina a sessão: esquece o utilizador, fecha os pop-ups abertos e volta ao ecrã de login.
     *
     * @throws IOException se não for possível ler o FXML do login
     */
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

    /**
     * Mostra uma área do menu (US44): o título e o submenu com as opções no topo, e a primeira
     * opção já aberta por baixo (se não for um formulário).
     *
     * @param titulo o nome da área, por exemplo {@code "Reservas"}
     * @param opcoes as opções do submenu, pela ordem em que aparecem
     * @throws IOException se não for possível ler o FXML da área
     */
    public static void mostrarArea(String titulo, List<OpcaoMenu> opcoes) throws IOException {
        FXMLLoader loader = carregar("area-view.fxml");
        AreaController controlador = loader.getController();
        controlador.configurar(titulo, opcoes);
        anteriores.clear();
        janelaPrincipal.setCenter(loader.getRoot());
    }

    /**
     * Junta a folha de estilos do Hotel4You a uma janela (US44).
     *
     * @param cena a cena da janela
     */
    public static void aplicarEstilo(Scene cena) {
        cena.getStylesheets().add(Navegador.class.getResource(ESTILO).toExternalForm());
    }
}
