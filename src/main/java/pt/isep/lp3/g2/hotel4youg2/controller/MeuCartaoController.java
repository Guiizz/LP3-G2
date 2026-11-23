package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import pt.isep.lp3.g2.hotel4youg2.bll.AcessoCartaoBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.LocalPicagem;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MeuCartaoController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AcessoCartaoBLL acessoCartaoBLL = new AcessoCartaoBLL();

    @FXML
    private HBox escolherCartaoBox;
    @FXML
    private ComboBox<Reserva> cartaoCombo;
    @FXML
    private Label cartaoLabel;
    @FXML
    private HBox picarBox;
    @FXML
    private ComboBox<LocalPicagem> localCombo;
    @FXML
    private Label mensagemLabel;
    @FXML
    private TableView<AcessoCartao> tabela;

    @FXML
    private void initialize() {
        cartaoCombo.setConverter(new CartaoDaReserva());
        try {
            mostrarCartoes();
            atualizarTabela();
        } catch (IllegalStateException e) {
            mostrarSemCartao(e.getMessage());
        } catch (DataAccessException e) {
            mostrarSemCartao("Não foi possível carregar o seu cartão. Tente novamente.");
        }
    }

    private void mostrarCartoes() {
        List<Reserva> reservas = acessoCartaoBLL.reservasComCartao(Sessao.getContaCliente());
        if (reservas.isEmpty()) {
            mostrarSemCartao(AcessoCartaoBLL.SEM_RESERVA_EM_CURSO);
            return;
        }
        localCombo.getItems().setAll(acessoCartaoBLL.listarLocais());
        cartaoCombo.getItems().setAll(reservas);
        escolherCartaoBox.setVisible(reservas.size() > 1);
        escolherCartaoBox.setManaged(reservas.size() > 1);
        cartaoCombo.setValue(reservas.get(0));
        mostrarCartao(reservas.get(0));
    }

    @FXML
    private void aoEscolherCartao() {
        Reserva escolhida = cartaoCombo.getValue();
        if (escolhida != null) {
            mensagemLabel.setText("");
            mostrarCartao(escolhida);
        }
    }

    private void mostrarCartao(Reserva reserva) {
        cartaoLabel.setText(reserva.getCartao() + " · Quarto " + reserva.getQuartoNumero() + " · "
                + reserva.getDataEntrada().format(DATA) + " a " + reserva.getDataSaida().format(DATA));
        localCombo.setValue(LocalPicagem.quarto(reserva.getQuartoNumero()));
    }

    private void mostrarSemCartao(String motivo) {
        cartaoLabel.setText("Sem cartão");
        picarBox.setVisible(false);
        picarBox.setManaged(false);
        Mensagens.aviso(mensagemLabel, motivo);
    }

    @FXML
    private void aoPicar() {
        try {
            AcessoCartao acesso = acessoCartaoBLL.picarComoCliente(Sessao.getContaCliente(), cartaoCombo.getValue(),
                    localCombo.getValue());
            Mensagens.sucesso(mensagemLabel, "Acesso permitido: cartão " + acesso.getCartao() + " em "
                    + acesso.getLocalTexto() + " às " + acesso.getRegistadoEmTexto() + ".");
            atualizarTabela();
        } catch (ValidacaoException | IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível registar a picagem. Tente novamente.");
        }
    }

    private void atualizarTabela() {
        try {
            tabela.getItems().setAll(acessoCartaoBLL.listarPicagensDoCliente(Sessao.getContaCliente()));
        } catch (DataAccessException | IllegalStateException e) {
            tabela.getItems().clear();
        }
    }
}
