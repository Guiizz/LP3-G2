package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import pt.isep.lp3.g2.hotel4youg2.bll.LigacaoBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.Permissoes;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador do menu principal (US34 e US44): mostra as áreas no menu lateral e as opções de cada uma. As áreas Serviços, Estatísticas e Administração só aparecem ao gestor.
 */
public class InicialController {

    private final LigacaoBLL ligacaoBLL = new LigacaoBLL();

    @FXML
    private BorderPane raiz;

    @FXML
    private Label estadoLabel;

    @FXML
    private Label utilizadorLabel;

    @FXML
    private Button clientesButton;

    @FXML
    private Button reservasButton;

    @FXML
    private Button quartosButton;

    @FXML
    private Button stockButton;

    @FXML
    private Button servicosButton;

    @FXML
    private Button administracaoButton;

    @FXML
    private Button estatisticasButton;

    @FXML
    private void initialize() {
        Navegador.iniciar(raiz);
        Utilizador utilizador = Sessao.getUtilizador();
        if (utilizador != null) {
            utilizadorLabel.setText(utilizador.getNomeUtilizador() + " (" + utilizador.getPerfil() + ")");
        }
        boolean gestor = Permissoes.eGestor(utilizador);
        mostrar(servicosButton, gestor);
        mostrar(administracaoButton, gestor);
        mostrar(estatisticasButton, gestor);
        if (utilizador != null && utilizador.getPerfil() == Perfil.RESTAURACAO) {
            mostrarSoOCartao();
        }
    }

    private void mostrarSoOCartao() {
        mostrar(clientesButton, false);
        mostrar(reservasButton, false);
        mostrar(quartosButton, false);
        mostrar(stockButton, false);
        try {
            Navegador.mostrar("acesso-cartao-view.fxml");
        } catch (IOException e) {
            estadoLabel.setText("Não foi possível abrir o seu cartão. Use o botão \"O meu cartão\".");
        }
    }

    @FXML
    private void aoTestarLigacao() {
        estadoLabel.setText(ligacaoBLL.testarLigacao());
    }

    @FXML
    private void aoAbrirClientes() throws IOException {
        List<OpcaoMenu> opcoes = new ArrayList<>();
        opcoes.add(new OpcaoMenu("Pesquisar cliente", "pesquisar-cliente-view.fxml", false));
        opcoes.add(new OpcaoMenu("Registar cliente", "registar-cliente-view.fxml", true));
        opcoes.add(new OpcaoMenu("Conta do cliente", "conta-cliente-view.fxml", true));
        opcoes.add(new OpcaoMenu("Registar sugestão ou queixa", "sugestoes-view.fxml", true));
        Navegador.mostrarArea("Clientes", opcoes);
    }

    @FXML
    private void aoAbrirReservas() throws IOException {
        List<OpcaoMenu> opcoes = new ArrayList<>();
        opcoes.add(new OpcaoMenu("Gerir reservas", "gerir-reservas-view.fxml", false));
        opcoes.add(new OpcaoMenu("Criar reserva", "criar-reserva-view.fxml", true));
        opcoes.add(new OpcaoMenu("Disponibilidade", "disponibilidade-view.fxml", false));
        opcoes.add(new OpcaoMenu("Reservas de um cliente", "consultar-reservas-view.fxml", false));
        opcoes.add(new OpcaoMenu("Pedidos dos clientes", "pedidos-extra-view.fxml", false));
        Navegador.mostrarArea("Reservas", opcoes);
    }

    @FXML
    private void aoAbrirOMeuCartao() throws IOException {
        Navegador.mostrar("acesso-cartao-view.fxml");
    }

    @FXML
    private void aoAbrirQuartos() throws IOException {
        List<OpcaoMenu> opcoes = new ArrayList<>();
        opcoes.add(new OpcaoMenu("Consultar quartos", "quartos-view.fxml", false));
        opcoes.add(new OpcaoMenu("Disponibilidade", "disponibilidade-view.fxml", false));
        opcoes.add(new OpcaoMenu("Materiais dos quartos", "materiais-quartos-view.fxml", false));
        if (Permissoes.eGestor(Sessao.getUtilizador())) {
            opcoes.add(new OpcaoMenu("Gestão de quartos", "gestao-quartos-view.fxml", false));
            opcoes.add(new OpcaoMenu("Tarifas", "tarifas-view.fxml", false));
        }
        Navegador.mostrarArea("Quartos", opcoes);
    }

    @FXML
    private void aoAbrirServicos() throws IOException {
        Permissoes.exigirGestor(Sessao.getUtilizador());
        List<OpcaoMenu> opcoes = new ArrayList<>();
        opcoes.add(new OpcaoMenu("Tabela de preços", "servicos-view.fxml", false));
        Navegador.mostrarArea("Serviços", opcoes);
    }

    @FXML
    private void aoAbrirStock() throws IOException {
        List<OpcaoMenu> opcoes = new ArrayList<>();
        opcoes.add(new OpcaoMenu("Janela de stock", "stock-view.fxml", false));
        opcoes.add(new OpcaoMenu("Decidir entregas", "decidir-entregas-view.fxml", false));
        opcoes.add(new OpcaoMenu("Saída de materiais", "saida-stock-view.fxml", false));
        Navegador.mostrarArea("Stock", opcoes);
    }

    @FXML
    private void aoAbrirAdministracao() throws IOException {
        Permissoes.exigirGestor(Sessao.getUtilizador());
        List<OpcaoMenu> opcoes = new ArrayList<>();
        opcoes.add(new OpcaoMenu("Contas", "utilizadores-view.fxml", false));
        opcoes.add(new OpcaoMenu("Criar conta", "criar-conta-view.fxml", true));
        opcoes.add(new OpcaoMenu("Criar fornecedor", "criar-fornecedor-view.fxml", true));
        opcoes.add(new OpcaoMenu("Sugestões e queixas", "consultar-sugestoes-view.fxml", false));
        opcoes.add(new OpcaoMenu("Registos de acesso", "consultar-acessos-view.fxml", false));
        opcoes.add(new OpcaoMenu("Horas e taxas", "horario-reserva-view.fxml", false));
        Navegador.mostrarArea("Administração", opcoes);
    }

    @FXML
    private void aoAbrirEstatisticas() throws IOException {
        Permissoes.exigirGestor(Sessao.getUtilizador());
        Navegador.mostrar("estatisticas-view.fxml");
    }

    @FXML
    private void aoTerminarSessao() throws IOException {
        Navegador.terminarSessao();
    }

    private void mostrar(Button botao, boolean visivel) {
        botao.setVisible(visivel);
        botao.setManaged(visivel);
    }
}