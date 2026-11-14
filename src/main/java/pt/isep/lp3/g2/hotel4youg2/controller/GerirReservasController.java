package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import pt.isep.lp3.g2.hotel4youg2.model.enums.SeparadorReserva;
import java.util.ArrayList;
import java.util.List;
import pt.isep.lp3.g2.hotel4youg2.bll.Permissoes;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Controlador do ecrã "Gerir reservas": lista as reservas, deixa alterar as datas (US30),
 * deixa o gestor cancelá-las (US29) e abre os serviços de cada reserva (US37).
 */
public class GerirReservasController {

    private static final String ERRO_BD = "Não foi possível comunicar com a base de dados. Tente novamente.";

    private final ReservaBLL reservaBLL = new ReservaBLL();

    private List<Reserva> todas = new ArrayList<>();

    @FXML
    private VBox ecra;

    @FXML
    private TextField pesquisaField;

    @FXML
    private TabPane separadores;

    @FXML
    private TableView<Reserva> tabelaPorFazer;

    @FXML
    private TableView<Reserva> tabelaEmCurso;

    @FXML
    private TableView<Reserva> tabelaHistorico;

    @FXML
    private Button cancelarButton;

    @FXML
    private Button checkinButton;

    @FXML
    private Button checkoutButton;

    @FXML
    private Label historicoLabel;

    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        AoClicarNaTabela duploClique = new AoClicarNaTabela();
        tabelaPorFazer.setOnMouseClicked(duploClique);
        tabelaEmCurso.setOnMouseClicked(duploClique);
        tabelaHistorico.setOnMouseClicked(duploClique);
        separadores.getSelectionModel().selectedIndexProperty().addListener(new AoMudarDeSeparador());
        pesquisaField.textProperty().addListener(new AoPesquisar());
        mostrarAcoes();
        carregar();
    }

    @FXML
    private void aoCancelar() {
        Reserva escolhida = escolhida();
        if (escolhida == null) {
            mostrarErro("Escolha uma reserva na tabela.");
            return;
        }
        if (!confirmarCancelar(escolhida)) {
            return;
        }
        try {
            reservaBLL.cancelar(Sessao.getUtilizador(), escolhida);
            mostrarSucesso("Reserva " + escolhida.getId() + " cancelada.");
        } catch (DataAccessException e) {
            mostrarErro(ERRO_BD);
        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }
        carregar();
    }

    @FXML
    private void aoFazerCheckin() {
        Reserva escolhida = escolhida();
        if (escolhida == null) {
            mostrarErro("Escolha uma reserva na tabela.");
            return;
        }
        if (!confirmarCheckin(escolhida)) {
            return;
        }
        try {
            String cartao = reservaBLL.fazerCheckin(Sessao.getUtilizador(), escolhida);
            mostrarSucesso("Check-in da reserva " + escolhida.getId() + " feito. Entregue o cartão "
                    + cartao + " (quarto " + escolhida.getQuartoNumero() + ").");
        } catch (DataAccessException e) {
            mostrarErro(ERRO_BD);
        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }
        carregar();
    }

    @FXML
    private void aoFazerCheckout() {
        Reserva escolhida = escolhida();
        if (escolhida == null) {
            mostrarErro("Escolha uma reserva na tabela.");
            return;
        }
        try {
            BigDecimal total = reservaBLL.totalAPagar(Sessao.getUtilizador(), escolhida);
            if (!confirmarCheckout(escolhida, total)) {
                return;
            }
            reservaBLL.fazerCheckout(Sessao.getUtilizador(), escolhida);
            mostrarSucesso("Check-out da reserva " + escolhida.getId() + " feito. Total pago: " + total
                    + " €. Recolha o cartão " + escolhida.getCartao() + ".");
        } catch (DataAccessException e) {
            mostrarErro(ERRO_BD);
        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }
        carregar();
    }

    private void mostrarErro(String texto) {
        Mensagens.erro(mensagemLabel, texto);
    }

    private void mostrarSucesso(String texto) {
        Mensagens.sucesso(mensagemLabel, texto);
    }

    private boolean confirmarCancelar(Reserva reserva) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Tem a certeza que quer cancelar a reserva " + reserva.getId() + " (quarto "
                        + reserva.getQuartoNumero() + ", " + reserva.getDataEntrada() + " a "
                        + reserva.getDataSaida() + ")?\n"
                        + "A reserva fica no histórico e o quarto volta a ficar livre.",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Cancelar reserva");
        alerta.setHeaderText(null);
        Optional<ButtonType> resposta = alerta.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.YES;
    }

    private boolean confirmarCheckin(Reserva reserva) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirma o check-in da reserva " + reserva.getId() + " (quarto "
                        + reserva.getQuartoNumero() + ", " + reserva.getDataEntrada() + " a "
                        + reserva.getDataSaida() + ")?\n"
                        + "O cliente recebe o cartão C" + reserva.getQuartoNumero() + ".",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Fazer check-in");
        alerta.setHeaderText(null);
        Optional<ButtonType> resposta = alerta.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.YES;
    }

    private boolean confirmarCheckout(Reserva reserva, BigDecimal total) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Check-out da reserva " + reserva.getId() + " (quarto " + reserva.getQuartoNumero() + ", "
                        + reserva.getDataEntrada() + " a " + reserva.getDataSaida() + ", "
                        + reserva.getNoites() + " noite(s)).\n"
                        + "Total a pagar: " + total + " €\n"
                        + "Confirma o pagamento e o check-out?",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Fazer check-out");
        alerta.setHeaderText(null);
        Optional<ButtonType> resposta = alerta.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.YES;
    }

    @FXML
    private void aoVerDetalhes() {
        Reserva escolhida = escolhida();
        if (escolhida == null) {
            mostrarErro("Escolha uma reserva na tabela.");
            return;
        }
        try {
            DetalhesReservaController.abrir(escolhida, ecra, this);
        } catch (IOException e) {
            mostrarErro("Não foi possível abrir os detalhes da reserva.");
        }
    }

    public void atualizar() {
        mensagemLabel.setText("");
        carregar();
    }

    private void carregar() {
        try {
            todas = reservaBLL.listar();
        } catch (DataAccessException e) {
            todas = new ArrayList<>();
            mostrarErro(ERRO_BD);
        }
        mostrarReservas();
    }

    private void mostrarReservas() {
        String pesquisa = pesquisaField.getText();
        tabelaPorFazer.getItems().setAll(ReservaBLL.filtrar(todas, SeparadorReserva.POR_FAZER, pesquisa));
        tabelaEmCurso.getItems().setAll(ReservaBLL.filtrar(todas, SeparadorReserva.EM_CURSO, pesquisa));
        tabelaHistorico.getItems().setAll(ReservaBLL.filtrar(todas, SeparadorReserva.HISTORICO, pesquisa));
    }

    private SeparadorReserva separadorAberto() {
        int indice = separadores.getSelectionModel().getSelectedIndex();
        if (indice == 1) {
            return SeparadorReserva.EM_CURSO;
        }
        if (indice == 2) {
            return SeparadorReserva.HISTORICO;
        }
        return SeparadorReserva.POR_FAZER;
    }

    private Reserva escolhida() {
        SeparadorReserva separador = separadorAberto();
        if (separador == SeparadorReserva.EM_CURSO) {
            return tabelaEmCurso.getSelectionModel().getSelectedItem();
        }
        if (separador == SeparadorReserva.HISTORICO) {
            return tabelaHistorico.getSelectionModel().getSelectedItem();
        }
        return tabelaPorFazer.getSelectionModel().getSelectedItem();
    }

    private void mostrarAcoes() {
        SeparadorReserva separador = separadorAberto();
        boolean porFazer = separador == SeparadorReserva.POR_FAZER;
        boolean emCurso = separador == SeparadorReserva.EM_CURSO;
        boolean gestor = Permissoes.eGestor(Sessao.getUtilizador());
        mostrarSe(cancelarButton, porFazer && gestor);
        mostrarSe(checkinButton, porFazer);
        mostrarSe(checkoutButton, emCurso);
        mostrarSe(historicoLabel, separador == SeparadorReserva.HISTORICO);
    }

    private void mostrarSe(Node no, boolean visivel) {
        no.setVisible(visivel);
        no.setManaged(visivel);
    }

    private class AoMudarDeSeparador implements ChangeListener<Number> {
        @Override
        public void changed(ObservableValue<? extends Number> observavel, Number antigo, Number novo) {
            mostrarAcoes();
            mensagemLabel.setText("");
        }
    }

    private class AoPesquisar implements ChangeListener<String> {
        @Override
        public void changed(ObservableValue<? extends String> observavel, String antigo, String novo) {
            mostrarReservas();
        }
    }

    private class AoClicarNaTabela implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent evento) {
            if (evento.getClickCount() == 2 && escolhida() != null) {
                aoVerDetalhes();
            }
        }
    }
}