package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;
import pt.isep.lp3.g2.hotel4youg2.bll.LigacaoService;
import pt.isep.lp3.g2.hotel4youg2.bll.Permissoes;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.util.ArrayList;

public class InicialController {

    private final LigacaoService ligacaoService = new  LigacaoService();

    @FXML
    private Label estadoLabel;

    @FXML
    private Button terminarSessaoButton;

    @FXML
    private Label utilizadorLabel;

    @FXML
    private Button tarifasButton;

    @FXML
    private Button contasButton;

    @FXML
    private Button criarContaButton;

    @FXML
    private void aoTestarLigacao() {
        estadoLabel.setText(ligacaoService.testarLigacao());
    }

    @FXML
    private void aoConsultarQuartos() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                InicialController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/quartos-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hotel4You - Quartos");
        stage.setScene(new Scene(loader.load(), 700, 500));
        stage.show();
    }

    @FXML
    private void aoAbrirTarifas() throws IOException {
        if (!podeAbrirEcraDeGestor()) {
            return;
        }
        FXMLLoader loader = new FXMLLoader(
                InicialController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/tarifas-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hotel4You - Tarifas");
        stage.setScene(new Scene(loader.load(), 520, 460));
        stage.show();
    }

    @FXML
    private void aoAbrirContas() throws IOException {
        if (!podeAbrirEcraDeGestor()) {
            return;
        }
        FXMLLoader loader = new FXMLLoader(
                InicialController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/utilizadores-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hotel4You - Contas");
        stage.setScene(new Scene(loader.load(), 460, 400));
        stage.show();
    }

    @FXML
    private void aoCriarConta() throws IOException {
        if (!podeAbrirEcraDeGestor()) {
            return;
        }
        FXMLLoader loader = new FXMLLoader(
                InicialController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/criar-conta-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hotel4You - Criar conta");
        stage.setScene(new Scene(loader.load(), 480, 560));
        stage.show();
    }
  
    @FXML
    private void aoPesquisarDisponibilidade() throws IOException {
       FXMLLoader loader = new FXMLLoader(
               InicialController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/disponibilidade-view.fxml"));
       Stage stage = new Stage();
       stage.setTitle("Hotel4You - Disponibilidade");
       stage.setScene(new Scene(loader.load(), 800, 500));
       stage.show();
    }

    @FXML
    private void aoRegistarCliente() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                InicialController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/registar-cliente-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hotel4You - Registar cliente");
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    @FXML
    private void aoPesquisarCliente() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                InicialController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/pesquisar-cliente-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hotel4You - Pesquisar cliente");
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    /**
     * Termina a sessão do utilizador: fecha as outras janelas abertas
     * e volta ao ecrã de login, onde é preciso entrar outra vez.
     */
    @FXML
    private void aoTerminarSessao() throws IOException {
        Sessao.terminar();

        Stage janelaPrincipal = (Stage) terminarSessaoButton.getScene().getWindow();
        for (Window janela : new ArrayList<>(Window.getWindows())) {
            if (janela != janelaPrincipal && janela instanceof Stage outraJanela) {
                outraJanela.close();
            }
        }

        FXMLLoader loader = new FXMLLoader(
                InicialController.class.getResource("/pt/isep/lp3/g2/hotel4youg2/view/login-view.fxml"));
        janelaPrincipal.setScene(new Scene(loader.load(), 360, 320));
        janelaPrincipal.setTitle("Hotel4You · Entrar");
    }

    /**
     * Prepara o menu para quem entrou: mostra o nome e o perfil
     * e esconde as opções do gestor ao rececionista (US12).
     */
    @FXML
    private void initialize() {
        Utilizador utilizador = Sessao.getUtilizador();
        if (utilizador != null) {
            utilizadorLabel.setText(utilizador.getNomeUtilizador() + " (" + utilizador.getPerfil() + ")");
        }

        boolean gestor = Permissoes.eGestor(utilizador);
        mostrar(tarifasButton, gestor);
        mostrar(contasButton, gestor);
        mostrar(criarContaButton, gestor);
    }

    /**
     * Confirma que quem está na sessão é gestor antes de abrir um ecrã de gestor.
     *
     * @return {@code true} se pode abrir; senão mostra o motivo e devolve {@code false}
     */
    private boolean podeAbrirEcraDeGestor() {
        try {
            Permissoes.exigirGestor(Sessao.getUtilizador());
            return true;
        } catch (IllegalStateException e) {
            estadoLabel.setText(e.getMessage());
            return false;
        }
    }

    private void mostrar(Button botao, boolean visivel) {
        botao.setVisible(visivel);
        botao.setManaged(visivel);
    }
}
