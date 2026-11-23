package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pt.isep.lp3.g2.hotel4youg2.bll.AvaliacaoBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Avaliacao;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Controlador do ecrã "Avaliar estadia": o cliente dá de 1 a 5 estrelas e um comentário.
 */
public class AvaliarEstadiaController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String ERRO_BD = "Não foi possível comunicar com a base de dados. Tente novamente.";

    private final AvaliacaoBLL avaliacaoBLL = new AvaliacaoBLL();
    private Reserva reserva;
    private int estrelas;

    @FXML
    private Label reservaLabel;
    @FXML
    private VBox formularioBox;
    @FXML
    private HBox estrelasBox;
    @FXML
    private Label escolhaLabel;
    @FXML
    private TextArea comentarioArea;
    @FXML
    private VBox avaliadaBox;
    @FXML
    private Label avaliadaEmLabel;
    @FXML
    private Label avaliadaEstrelasLabel;
    @FXML
    private Label avaliadaComentarioLabel;
    @FXML
    private Label mensagemLabel;

    /**
     * Abre a avaliação de uma estadia.
     *
     * @param reserva     a reserva concluída
     * @param ecraDaLista o ecrã da lista, para onde se volta
     * @throws IOException se não for possível ler o FXML
     */
    public static void abrir(Reserva reserva, Node ecraDaLista) throws IOException {
        FXMLLoader loader = Navegador.carregar("avaliar-estadia-view.fxml");
        AvaliarEstadiaController controller = loader.getController();
        controller.reserva = reserva;
        controller.mostrar();
        Navegador.mostrarDetalhe(ecraDaLista, (Parent) loader.getRoot());
    }

    @FXML
    private void initialize() {
        for (int i = Avaliacao.MINIMO_ESTRELAS; i <= Avaliacao.MAXIMO_ESTRELAS; i++) {
            Button estrela = new Button("☆");
            estrela.getStyleClass().add("estrela");
            estrela.setOnAction(new AoEscolherEstrelas(i));
            estrelasBox.getChildren().add(estrela);
        }
    }

    private void mostrar() {
        reservaLabel.setText("Reserva n.º " + reserva.getId() + " · Quarto " + reserva.getQuartoNumero() + " · "
                + reserva.getDataEntrada().format(DATA) + " a " + reserva.getDataSaida().format(DATA));
        try {
            Avaliacao existente = avaliacaoBLL.procurar(Sessao.getContaCliente(), reserva);
            if (existente != null) {
                mostrarAvaliada(existente);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
            mostrarSe(formularioBox, false);
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, ERRO_BD);
        }
    }

    private void escolher(int quantas) {
        estrelas = quantas;
        for (int i = 0; i < estrelasBox.getChildren().size(); i++) {
            ((Button) estrelasBox.getChildren().get(i)).setText(i < quantas ? "★" : "☆");
        }
        escolhaLabel.setText(quantas + " de " + Avaliacao.MAXIMO_ESTRELAS + " estrelas.");
    }

    @FXML
    private void aoEnviar() {
        try {
            Avaliacao avaliacao = avaliacaoBLL.avaliar(Sessao.getContaCliente(), reserva, estrelas,
                    comentarioArea.getText());
            mostrarAvaliada(avaliacao);
            Mensagens.sucesso(mensagemLabel, "Obrigado! A sua avaliação foi registada.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, ERRO_BD);
        }
    }

    private void mostrarAvaliada(Avaliacao avaliacao) {
        String quando = avaliacao.getAvaliadaEm() == null ? "hoje" : "em " + avaliacao.getDataTexto();
        avaliadaEmLabel.setText("Avaliou esta estadia " + quando + ":");
        avaliadaEstrelasLabel.setText(avaliacao.getEstrelasTexto());
        avaliadaComentarioLabel.setText(avaliacao.getComentario() == null ? "Sem comentário."
                : "“" + avaliacao.getComentario() + "”");
        mostrarSe(formularioBox, false);
        mostrarSe(avaliadaBox, true);
    }

    @FXML
    private void aoVoltar() {
        Navegador.voltar();
    }

    private static void mostrarSe(Node no, boolean visivel) {
        no.setVisible(visivel);
        no.setManaged(visivel);
    }

    private class AoEscolherEstrelas implements EventHandler<ActionEvent> {
        private final int quantas;

        AoEscolherEstrelas(int quantas) {
            this.quantas = quantas;
        }

        @Override
        public void handle(ActionEvent evento) {
            escolher(quantas);
        }
    }
}
