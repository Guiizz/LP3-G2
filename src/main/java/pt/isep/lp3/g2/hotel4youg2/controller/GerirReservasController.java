package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.Permissoes;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Controlador do ecrã "Gerir reservas": lista as reservas, deixa alterar as datas (US30)
 * e deixa o gestor cancelá-las (US29).
 */
public class GerirReservasController {

    private static final String ERRO_BD = "Não foi possível comunicar com a base de dados. Tente novamente.";

    private final ReservaBLL reservaService = new ReservaBLL();

    @FXML
    private TableView<Reserva> tabela;

    @FXML
    private Button cancelarButton;

    @FXML
    private Label mensagemLabel;

    @FXML
    private DatePicker novaEntradaPicker;

    @FXML
    private DatePicker novaSaidaPicker;

    @FXML
    private void initialize() {
        boolean gestor = Permissoes.eGestor(Sessao.getUtilizador());
        cancelarButton.setVisible(gestor);
        cancelarButton.setManaged(gestor);
        tabela.getSelectionModel().selectedItemProperty().addListener(new AoEscolherReserva());
        carregar();
    }

    @FXML
    private void aoCancelar() {
        Reserva escolhida = tabela.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            mostrarErro("Escolha uma reserva na tabela.");
            return;
        }
        if (!confirmarCancelar(escolhida)) {
            return;
        }
        try {
            reservaService.cancelar(Sessao.getUtilizador(), escolhida);
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
        Reserva escolhida = tabela.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            mostrarErro("Escolha uma reserva na tabela.");
            return;
        }
        if (!confirmarCheckin(escolhida)) {
            return;
        }
        try {
            String cartao = reservaService.fazerCheckin(Sessao.getUtilizador(), escolhida);
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
        Reserva escolhida = tabela.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            mostrarErro("Escolha uma reserva na tabela.");
            return;
        }
        try {
            BigDecimal total = reservaService.totalAPagar(escolhida);
            if (!confirmarCheckout(escolhida, total)) {
                return;
            }
            reservaService.fazerCheckout(Sessao.getUtilizador(), escolhida);
            mostrarSucesso("Check-out da reserva " + escolhida.getId() + " feito. Total pago: " + total
                    + " €. Recolha o cartão C" + escolhida.getQuartoNumero() + ".");
        } catch (DataAccessException e) {
            mostrarErro(ERRO_BD);
        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }
        carregar();
    }

    private void mostrarErro(String texto) {
        mensagemLabel.setStyle("-fx-text-fill: red;");
        mensagemLabel.setText(texto);
    }

    private void mostrarSucesso(String texto) {
        mensagemLabel.setStyle("-fx-text-fill: green;");
        mensagemLabel.setText(texto);
    }

    @FXML
    private void aoAlterarDatas() {
        Reserva escolhida = tabela.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            mostrarErro("Escolha uma reserva na tabela.");
            return;
        }
        try {
            reservaService.alterarDatas(Sessao.getUtilizador(), escolhida,
                    novaEntradaPicker.getValue(), novaSaidaPicker.getValue());
            mostrarSucesso("Datas da reserva " + escolhida.getId() + " alteradas. Novo preço: "
                    + escolhida.getPrecoTotal() + " €");
        } catch (DataAccessException e) {
            mostrarErro(ERRO_BD);
        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }
        carregar();
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

    private void carregar() {
        try {
            tabela.getItems().setAll(reservaService.listar());
        } catch (DataAccessException e) {
            tabela.getItems().clear();
            mostrarErro(ERRO_BD);
        }
    }

    /** Preenche as datas com as da reserva escolhida, seja com o rato ou com as setas do teclado. */
    private class AoEscolherReserva implements ChangeListener<Reserva> {
        @Override
        public void changed(ObservableValue<? extends Reserva> observavel, Reserva antiga, Reserva nova) {
            if (nova != null) {
                novaEntradaPicker.setValue(nova.getDataEntrada());
                novaSaidaPicker.setValue(nova.getDataSaida());
            }
        }
    }
}