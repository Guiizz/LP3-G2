package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import pt.isep.lp3.g2.hotel4youg2.bll.AcessoCartaoBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.LocalPicagem;
import pt.isep.lp3.g2.hotel4youg2.model.PlantaHotel;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador do ecrã "Planta do hotel": mostra os locais de cada piso e pica o cartão com um clique.
 */
public class PlantaController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM");

    private final AcessoCartaoBLL acessoCartaoBLL = new AcessoCartaoBLL();
    private PlantaHotel planta;
    private List<LocalPicagem> abre = new ArrayList<>();
    private boolean cliente;
    private Button ultimoBotao;

    @FXML
    private Label instrucaoLabel;
    @FXML
    private HBox escolherCartaoBox;
    @FXML
    private ComboBox<Reserva> cartaoCombo;
    @FXML
    private Label cartaoLabel;
    @FXML
    private Label atalhosTitulo;
    @FXML
    private FlowPane atalhosBox;
    @FXML
    private VBox pisosBox;
    @FXML
    private Label pisoLabel;
    @FXML
    private StackPane plantaBox;
    @FXML
    private Label legendaLabel;
    @FXML
    private Label detalhesLabel;
    @FXML
    private VBox passagensBox;
    @FXML
    private TableView<AcessoCartao> tabela;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        cliente = Sessao.getContaCliente() != null;
        cartaoCombo.setConverter(new CartaoDaReserva());
        mostrar(atalhosTitulo, cliente);
        mostrar(atalhosBox, cliente);
        mostrar(detalhesLabel, !cliente);
        instrucaoLabel.setText("Os locais a verde são os que o seu cartão abre. Clique num local para picar o cartão.");
        legendaLabel.setText("Verde = o seu cartão abre · cinzento = não abre");
        try {
            planta = acessoCartaoBLL.planta();
            if (cliente) {
                carregarCartoes();
            } else {
                carregarCartaoDoFuncionario();
            }
            atualizarPassagens();
            criarBotoesDosPisos();
            if (!planta.getPisos().isEmpty()) {
                mostrarPiso(pisoInicial());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível carregar a planta do hotel. Tente novamente.");
        }
    }

    private void carregarCartoes() {
        List<Reserva> reservas = acessoCartaoBLL.reservasComCartao(Sessao.getContaCliente());
        cartaoCombo.getItems().setAll(reservas);
        mostrar(escolherCartaoBox, reservas.size() > 1);
        if (reservas.isEmpty()) {
            cartaoLabel.setText("Sem cartão");
            Mensagens.aviso(mensagemLabel, AcessoCartaoBLL.SEM_RESERVA_EM_CURSO);
            return;
        }
        cartaoCombo.setValue(reservas.get(0));
        carregarAtalhos();
    }

    private void carregarCartaoDoFuncionario() {
        try {
            String cartao = acessoCartaoBLL.cartaoDoUtilizador(Sessao.getUtilizador());
            cartaoLabel.setText(AcessoCartao.CARTAO_MESTRE.equals(cartao)
                    ? "Cartão " + cartao + " · cartão mestre" : "Cartão " + cartao);
            abre = acessoCartaoBLL.locaisQueOCartaoAbre(Sessao.getUtilizador());
        } catch (IllegalStateException e) {
            cartaoLabel.setText("Sem cartão");
            Mensagens.aviso(mensagemLabel, e.getMessage());
        }
    }

    @FXML
    private void aoEscolherCartao() {
        if (cartaoCombo.getValue() == null || planta == null) {
            return;
        }
        mensagemLabel.setText("");
        carregarAtalhos();
        criarBotoesDosPisos();
        mostrarPiso(pisoInicial());
    }

    private void carregarAtalhos() {
        Reserva reserva = cartaoCombo.getValue();
        cartaoLabel.setText("Cartão " + reserva.getCartao() + " · Quarto " + reserva.getQuartoNumero()
                + " · até " + reserva.getDataSaida().format(DATA));
        abre = acessoCartaoBLL.locaisQueAbre(Sessao.getContaCliente(), reserva);
        atalhosBox.getChildren().clear();
        for (LocalPicagem local : abre) {
            Button atalho = new Button("✓  " + local + "\nPiso " + planta.pisoDe(local));
            atalho.getStyleClass().add("atalho-planta");
            atalho.setOnAction(new AoPicar(local, atalho));
            atalhosBox.getChildren().add(atalho);
        }
    }

    private int pisoInicial() {
        if (!abre.isEmpty()) {
            return planta.pisoDe(abre.get(0));
        }
        for (Integer piso : planta.getPisos()) {
            if (planta.temQuartos(piso)) {
                return piso;
            }
        }
        return planta.getPisos().get(0);
    }

    private void criarBotoesDosPisos() {
        pisosBox.getChildren().clear();
        pisosBox.getChildren().add(new Label("Piso"));
        List<Integer> pisos = planta.getPisos();
        for (int i = pisos.size() - 1; i >= 0; i--) {
            int piso = pisos.get(i);
            Button botao = new Button(String.valueOf(piso));
            botao.getStyleClass().add("piso-planta");
            if (pisoTemAcesso(piso)) {
                botao.getStyleClass().add("piso-com-acesso");
            }
            botao.setOnAction(new AoEscolherPiso(piso));
            pisosBox.getChildren().add(botao);
        }
    }

    private boolean pisoTemAcesso(int piso) {
        for (LocalPicagem local : abre) {
            if (planta.pisoDe(local) == piso) {
                return true;
            }
        }
        return false;
    }

    private void mostrarPiso(int piso) {
        for (Node no : pisosBox.getChildren()) {
            no.getStyleClass().remove("piso-ativo");
            if (no instanceof Button && ((Button) no).getText().equals(String.valueOf(piso))) {
                no.getStyleClass().add("piso-ativo");
            }
        }
        pisoLabel.setText("PISO " + piso);
        plantaBox.getChildren().setAll(criarPlanta(piso));
        ultimoBotao = null;
    }

    private GridPane criarPlanta(int piso) {
        GridPane grelha = new GridPane();
        grelha.getStyleClass().add("planta");
        List<LocalPicagem> cima = planta.filaDeCima(piso);
        List<LocalPicagem> baixo = planta.filaDeBaixo(piso);
        int colunas = Math.max(1, Math.max(cima.size(), baixo.size()));

        for (int i = 0; i < cima.size(); i++) {
            grelha.add(criarQuadrado(cima.get(i)), i, 0);
        }
        Label corredor = new Label("Corredor");
        corredor.getStyleClass().add("planta-corredor");
        corredor.setMaxWidth(Double.MAX_VALUE);
        corredor.setAlignment(Pos.CENTER);
        grelha.add(corredor, 0, 1, colunas, 1);
        for (int i = 0; i < baixo.size(); i++) {
            grelha.add(criarQuadrado(baixo.get(i)), i, 2);
        }
        for (int i = baixo.size(); i < colunas; i++) {
            Label vazio = new Label();
            vazio.getStyleClass().add("planta-vazio");
            vazio.setMaxWidth(Double.MAX_VALUE);
            vazio.setMaxHeight(Double.MAX_VALUE);
            grelha.add(vazio, i, 2);
        }
        Label escadas = new Label("Escadas ·\nElevador");
        escadas.getStyleClass().add("planta-escadas");
        escadas.setMaxHeight(Double.MAX_VALUE);
        escadas.setAlignment(Pos.CENTER);
        grelha.add(escadas, colunas, 0, 1, 3);
        return grelha;
    }

    private Button criarQuadrado(LocalPicagem local) {
        Button quadrado = new Button(textoDoQuadrado(local));
        quadrado.getStyleClass().add("quadrado-planta");
        quadrado.setMaxWidth(Double.MAX_VALUE);
        quadrado.setMaxHeight(Double.MAX_VALUE);
        if (abre.contains(local)) {
            quadrado.getStyleClass().add("quadrado-abre");
        }
        quadrado.setOnAction(new AoPicar(local, quadrado));
        return quadrado;
    }

    private String textoDoQuadrado(LocalPicagem local) {
        if (local.getLocal() == LocalHotel.QUARTO) {
            Quarto quarto = planta.procurarQuarto(local.getQuartoNumero());
            if (quarto != null) {
                return quarto.getNumero() + "\n" + quarto.getResumo();
            }
        }
        return local.toString();
    }

    private void picar(LocalPicagem local, Button botao) {
        limparUltimoBotao();
        ultimoBotao = botao;
        if (!cliente && local.getLocal() == LocalHotel.QUARTO) {
            verQuarto(local.getQuartoNumero());
        }
        try {
            AcessoCartao acesso = cliente
                    ? acessoCartaoBLL.picarComoCliente(Sessao.getContaCliente(), cartaoCombo.getValue(), local)
                    : acessoCartaoBLL.picar(Sessao.getUtilizador(), local);
            botao.getStyleClass().add("picagem-aceite");
            Mensagens.sucesso(mensagemLabel, "Acesso permitido: cartão " + acesso.getCartao() + " em "
                    + acesso.getLocalTexto() + " às " + acesso.getRegistadoEmTexto() + ".");
            atualizarPassagens();
        } catch (ValidacaoException | IllegalArgumentException | IllegalStateException e) {
            botao.getStyleClass().add("picagem-recusada");
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível registar a picagem. Tente novamente.");
        }
    }

    private void atualizarPassagens() {
        try {
            tabela.getItems().setAll(cliente
                    ? acessoCartaoBLL.listarPicagensDoCliente(Sessao.getContaCliente())
                    : acessoCartaoBLL.listarAsMinhasPassagens(Sessao.getUtilizador()));
        } catch (DataAccessException | IllegalStateException e) {
            tabela.getItems().clear();
        }
    }

    private void verQuarto(int numero) {
        Quarto quarto = planta.procurarQuarto(numero);
        if (quarto != null) {
            detalhesLabel.setText(quarto.getDetalhes());
        }
    }

    private void limparUltimoBotao() {
        if (ultimoBotao != null) {
            ultimoBotao.getStyleClass().removeAll("picagem-aceite", "picagem-recusada");
        }
    }

    private void mostrar(Node no, boolean visivel) {
        no.setVisible(visivel);
        no.setManaged(visivel);
    }

    private class AoPicar implements EventHandler<ActionEvent> {
        private final LocalPicagem local;
        private final Button botao;

        AoPicar(LocalPicagem local, Button botao) {
            this.local = local;
            this.botao = botao;
        }

        @Override
        public void handle(ActionEvent evento) {
            picar(local, botao);
        }
    }

    private class AoEscolherPiso implements EventHandler<ActionEvent> {
        private final int piso;

        AoEscolherPiso(int piso) {
            this.piso = piso;
        }

        @Override
        public void handle(ActionEvent evento) {
            mostrarPiso(piso);
        }
    }
}