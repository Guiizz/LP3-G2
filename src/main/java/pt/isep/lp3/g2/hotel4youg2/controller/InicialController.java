package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import pt.isep.lp3.g2.hotel4youg2.bll.LigacaoBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.Permissoes;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;

/**
 * Controlador da janela principal: o menu fica sempre visível à esquerda e cada área
 * abre no painel central (US34); as opções do gestor ficam escondidas ao rececionista (US12).
 */
public class InicialController {

    private final LigacaoBLL ligacaoService = new LigacaoBLL();

    @FXML
    private BorderPane raiz;

    @FXML
    private Label estadoLabel;

    @FXML
    private Label utilizadorLabel;

    @FXML
    private Button tarifasButton;

    @FXML
    private Button contasButton;

    @FXML
    private Button criarContaButton;

    @FXML
    private Button servicosButton;

    @FXML
    private Button consultarSugestoesButton;

    @FXML
    private void initialize() {
        Navegador.iniciar(raiz);
        Utilizador utilizador = Sessao.getUtilizador();
        if (utilizador != null) {
            utilizadorLabel.setText(utilizador.getNomeUtilizador() + " (" + utilizador.getPerfil() + ")");
        }
        boolean gestor = Permissoes.eGestor(utilizador);
        mostrar(tarifasButton, gestor);
        mostrar(contasButton, gestor);
        mostrar(criarContaButton, gestor);
        mostrar(servicosButton, gestor);
        mostrar(consultarSugestoesButton, gestor);
    }

    @FXML
    private void aoTestarLigacao() {
        estadoLabel.setText(ligacaoService.testarLigacao());
    }

    @FXML
    private void aoAbrirQuartos() throws IOException {
        Navegador.mostrar("quartos-menu-view.fxml");
    }

    @FXML
    private void aoAbrirReservas() throws IOException {
        Navegador.mostrar("reservas-view.fxml");
    }

    @FXML
    private void aoPesquisarCliente() throws IOException {
        Navegador.mostrar("pesquisar-cliente-view.fxml");
    }

    @FXML
    private void aoRegistarCliente() throws IOException {
        Navegador.abrirFormulario("registar-cliente-view.fxml", "Registar cliente");
    }

    @FXML
    private void aoAbrirSugestoes() throws IOException {
        Navegador.abrirFormulario("sugestoes-view.fxml", "Registar sugestão ou queixa");
    }

    @FXML
    private void aoAbrirEntregas() throws IOException {
        Navegador.mostrar("entregas-view.fxml");
    }

    @FXML
    private void aoAbrirServicos() throws IOException {
        if (podeAbrirEcraDeGestor()) {
            Navegador.mostrar("servicos-view.fxml");
        }
    }

    @FXML
    private void aoAbrirTarifas() throws IOException {
        if (podeAbrirEcraDeGestor()) {
            Navegador.mostrar("tarifas-view.fxml");
        }
    }

    @FXML
    private void aoConsultarSugestoes() throws IOException {
        if (podeAbrirEcraDeGestor()) {
            Navegador.mostrar("consultar-sugestoes-view.fxml");
        }
    }

    @FXML
    private void aoAbrirContas() throws IOException {
        if (podeAbrirEcraDeGestor()) {
            Navegador.mostrar("utilizadores-view.fxml");
        }
    }

    @FXML
    private void aoCriarConta() throws IOException {
        if (podeAbrirEcraDeGestor()) {
            Navegador.abrirFormulario("criar-conta-view.fxml", "Criar conta");
        }
    }

    @FXML
    private void aoTerminarSessao() throws IOException {
        Navegador.terminarSessao();
    }

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