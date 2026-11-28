package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import pt.isep.lp3.g2.hotel4youg2.bll.InicioBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.LigacaoBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.Permissoes;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoDoDia;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.SeparadorReserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InicioController {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-PT"));
    private static final String PASTA_ICONES = "/pt/isep/lp3/g2/hotel4youg2/images/icons/";

    private final InicioBLL inicioBLL = new InicioBLL();
    private final LigacaoBLL ligacaoBLL = new LigacaoBLL();

    @FXML private Label saudacaoLabel;
    @FXML private Label dataLabel;
    @FXML private Label ligacaoLabel;
    @FXML private Label checkinsLabel;
    @FXML private Label checkoutsLabel;
    @FXML private Label pedidosLabel;
    @FXML private Label entregasLabel;
    @FXML private Label chegadasLabel;
    @FXML private VBox listaHoje;
    @FXML private GridPane atalhosGrid;
    @FXML private Label mensagemLabel;

    @FXML
    private void initialize() {
        Utilizador utilizador = Sessao.getUtilizador();
        String nome = utilizador == null ? "" : ", " + utilizador.getNomeUtilizador();
        saudacaoLabel.setText(InicioBLL.saudacao(LocalTime.now()) + nome);
        String data = LocalDate.now().format(FORMATO_DATA);
        dataLabel.setText(data.substring(0, 1).toUpperCase() + data.substring(1));
        mostrarLigacao();
        atualizar();
        mostrarAtalhos(utilizador);
    }

    private void atualizar() {
        mensagemLabel.setText("");
        mostrarNumeros();
        mostrarHojeNaRececao();
    }

    private void mostrarLigacao() {
        boolean ligada = ligacaoBLL.estaLigada();
        ligacaoLabel.setText(ligada ? "●  Base de dados ligada" : "●  Sem ligação à base de dados");
        ligacaoLabel.getStyleClass().add(ligada ? "estado-ligada" : "estado-desligada");
    }

    private void mostrarNumeros() {
        try {
            ResumoDoDia resumo = inicioBLL.resumoDoDia(Sessao.getUtilizador());
            mostrarNumero(checkinsLabel, resumo.getCheckins());
            mostrarNumero(checkoutsLabel, resumo.getCheckouts());
            mostrarNumero(pedidosLabel, resumo.getPedidos());
            mostrarNumero(entregasLabel, resumo.getEntregas());
            chegadasLabel.setText(resumo.getChegadas() == 1 ? "1 cliente já fez check-in hoje"
                    : resumo.getChegadas() + " clientes já fizeram check-in hoje");
        } catch (DataAccessException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
    }

    private void mostrarNumero(Label label, int valor) {
        label.setText(String.valueOf(valor));
        label.getStyleClass().remove("numero-zero");
        if (valor == 0) {
            label.getStyleClass().add("numero-zero");
        }
    }

    private void mostrarHojeNaRececao() {
        listaHoje.getChildren().clear();
        try {
            LocalDate hoje = LocalDate.now();
            List<Reserva> reservas = inicioBLL.hojeNaRececao(Sessao.getUtilizador(), hoje);
            if (reservas.isEmpty()) {
                Label vazio = new Label("Não há check-ins nem check-outs por fazer hoje.");
                vazio.getStyleClass().add("inicio-vazio");
                listaHoje.getChildren().add(vazio);
            }
            for (Reserva reserva : reservas) {
                listaHoje.getChildren().add(criarLinha(reserva, hoje));
            }
        } catch (DataAccessException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
    }

    private HBox criarLinha(Reserva reserva, LocalDate hoje) {
        Label quarto = new Label(String.valueOf(reserva.getQuartoNumero()));
        quarto.getStyleClass().add("inicio-quarto");
        Label nome = new Label(reserva.getNomeCliente());
        nome.getStyleClass().add("inicio-nome");
        Label detalhe = new Label(ReservaBLL.descricaoDeHoje(reserva, hoje));
        detalhe.getStyleClass().add("inicio-detalhe");
        VBox textos = new VBox(2, nome, detalhe);
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        boolean checkin = ReservaBLL.eCheckinDeHoje(reserva, hoje);
        Button acao = new Button(checkin ? "Check-in" : "Check-out");
        acao.getStyleClass().add("botao-contorno");
        SeparadorReserva separador = checkin ? SeparadorReserva.POR_FAZER : SeparadorReserva.EM_CURSO;
        acao.setOnAction(new AoAbrirReserva(reserva, separador));
        HBox linha = new HBox(14, quarto, textos, espaco, acao);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.getStyleClass().add("inicio-linha");
        return linha;
    }

    private void mostrarAtalhos(Utilizador utilizador) {
        atalhosGrid.getChildren().clear();
        List<OpcaoMenu> atalhos = atalhosDoPerfil(utilizador);
        for (int i = 0; i < atalhos.size(); i++) {
            OpcaoMenu atalho = atalhos.get(i);
            Button botao = new Button(atalho.getTexto());
            botao.getStyleClass().add("inicio-atalho");
            botao.setGraphic(icone(atalho.getIcone()));
            botao.setContentDisplay(ContentDisplay.TOP);
            botao.setAlignment(Pos.CENTER);
            botao.setTextAlignment(TextAlignment.CENTER);
            botao.setMaxWidth(Double.MAX_VALUE);
            botao.setOnAction(new AoEscolherAtalho(atalho));
            atalhosGrid.add(botao, i % 2, i / 2);
        }
    }

    private List<OpcaoMenu> atalhosDoPerfil(Utilizador utilizador) {
        List<OpcaoMenu> atalhos = new ArrayList<>();
        if (Permissoes.eGestor(utilizador)) {
            atalhos.add(new OpcaoMenu("Estatísticas", "estatisticas-view.fxml", false, "estatisticas.png"));
            atalhos.add(new OpcaoMenu("Registos de acesso", "consultar-acessos-view.fxml", false, "cadeado.png"));
            atalhos.add(new OpcaoMenu("Sugestões e queixas", "consultar-sugestoes-view.fxml", false, "mensagens.png"));
            atalhos.add(new OpcaoMenu("Criar conta", "criar-conta-view.fxml", true, "gerir_utilizador.png"));
        } else {
            atalhos.add(new OpcaoMenu("Pesquisar cliente", "pesquisar-cliente-view.fxml", false, "pesquisar.png"));
            atalhos.add(new OpcaoMenu("Registar cliente", "registar-cliente-view.fxml", true, "adicionar_utilizador.png"));
            atalhos.add(new OpcaoMenu("Disponibilidade", "disponibilidade-view.fxml", false, "quartos.png"));
            atalhos.add(new OpcaoMenu("O meu cartão", "planta-view.fxml", false, "cartao_quarto.png"));
        }
        return atalhos;
    }

    private ImageView icone(String ficheiro) {
        if (ficheiro == null) {
            return null;
        }
        ImageView vista = new ImageView(new Image(getClass().getResource(PASTA_ICONES + ficheiro).toExternalForm()));
        vista.setFitWidth(22);
        vista.setFitHeight(22);
        vista.setPreserveRatio(true);
        vista.setSmooth(true);
        return vista;
    }

    private void abrirAtalho(OpcaoMenu atalho) {
        try {
            if (atalho.isFormulario()) {
                Navegador.abrirFormulario(atalho.getFicheiro(), atalho.getTexto());
                atualizar();
            } else {
                Navegador.mostrar(atalho.getFicheiro());
            }
        } catch (IOException e) {
            mostrarErro("Não foi possível abrir " + atalho.getTexto() + ".");
        }
    }

    private void abrirReservas(SeparadorReserva separador, Reserva reserva) {
        try {
            FXMLLoader loader = Navegador.carregar("gerir-reservas-view.fxml");
            GerirReservasController controlador = loader.getController();
            if (reserva == null) {
                controlador.abrirSeparador(separador);
            } else {
                controlador.abrirReserva(reserva, separador);
            }
            Parent ecra = loader.getRoot();
            Navegador.mostrar(ecra);
        } catch (IOException e) {
            mostrarErro("Não foi possível abrir as reservas.");
        }
    }

    private void mostrarErro(String texto) {
        mensagemLabel.setText(texto);
        if (!mensagemLabel.getStyleClass().contains("mensagem-erro")) {
            mensagemLabel.getStyleClass().add("mensagem-erro");
        }
    }

    @FXML
    private void aoCriarReserva() throws IOException {
        Navegador.abrirFormulario("criar-reserva-view.fxml", "Criar reserva");
        atualizar();
    }

    @FXML
    private void aoAbrirCheckins() {
        abrirReservas(SeparadorReserva.POR_FAZER, null);
    }

    @FXML
    private void aoAbrirCheckouts() {
        abrirReservas(SeparadorReserva.EM_CURSO, null);
    }

    @FXML
    private void aoAbrirPedidos() throws IOException {
        Navegador.mostrar("pedidos-extra-view.fxml");
    }

    @FXML
    private void aoAbrirEntregas() throws IOException {
        Navegador.mostrar("decidir-entregas-view.fxml");
    }

    private class AoEscolherAtalho implements EventHandler<ActionEvent> {
        private final OpcaoMenu atalho;

        AoEscolherAtalho(OpcaoMenu atalho) {
            this.atalho = atalho;
        }

        @Override
        public void handle(ActionEvent evento) {
            abrirAtalho(atalho);
        }
    }

    private class AoAbrirReserva implements EventHandler<ActionEvent> {
        private final Reserva reserva;
        private final SeparadorReserva separador;

        AoAbrirReserva(Reserva reserva, SeparadorReserva separador) {
            this.reserva = reserva;
            this.separador = separador;
        }

        @Override
        public void handle(ActionEvent evento) {
            abrirReservas(separador, reserva);
        }
    }
}