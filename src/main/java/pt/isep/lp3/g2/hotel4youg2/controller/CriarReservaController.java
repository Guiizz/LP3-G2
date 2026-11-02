package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteService;
import pt.isep.lp3.g2.hotel4youg2.bll.QuartoService;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaService;
import pt.isep.lp3.g2.hotel4youg2.bll.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador do ecrã para criar uma reserva (US27): escolher o cliente pelo NIF,
 * pesquisar quartos livres, ver o preço e confirmar.
 */
public class CriarReservaController {

    private final ClienteService clienteService = new ClienteService();
    private final QuartoService quartoService = new QuartoService();
    private final ReservaService reservaService = new ReservaService();

    @FXML
    private TextField nifField;
    @FXML
    private Label clienteLabel;
    @FXML
    private DatePicker entradaPicker;
    @FXML
    private DatePicker saidaPicker;
    @FXML
    private Spinner<Integer> hospedesSpinner;
    @FXML
    private TableView<Quarto> quartosTabela;
    @FXML
    private Label precoLabel;
    @FXML
    private Button confirmarButton;
    @FXML
    private Label mensagemLabel;

    private Cliente cliente;
    private Quarto quartoEscolhido;
    private LocalDate entradaPesquisada;
    private LocalDate saidaPesquisada;
    private int hospedesPesquisados;

    @FXML
    private void aoProcurarCliente() {
        cliente = null;
        clienteLabel.setText("");
        try {
            cliente = clienteService.pesquisarPorNif(nifField.getText());
            if (cliente == null) {
                mostrarMensagem("Cliente não encontrado. Registe-o primeiro em Clientes.", "red");
            } else {
                clienteLabel.setText(cliente.getNome());
                mostrarMensagem("", "black");
            }
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível procurar o cliente. Tente novamente.", "red");
        }
        atualizarConfirmar();
    }

    @FXML
    private void aoPesquisarQuartos() {
        limparQuartoEscolhido();
        quartosTabela.getItems().clear();
        try {
            LocalDate entrada = entradaPicker.getValue();
            LocalDate saida = saidaPicker.getValue();
            int hospedes = hospedesSpinner.getValue();
            List<Quarto> quartos = quartoService.pesquisarDisponiveis(entrada, saida, hospedes, null, null);

            entradaPesquisada = entrada;
            saidaPesquisada = saida;
            hospedesPesquisados = hospedes;
            quartosTabela.getItems().setAll(quartos);
            if (quartos.isEmpty()) {
                mostrarMensagem("Não há quartos livres para estas datas e hóspedes", "black");
            } else {
                mostrarMensagem("Escolha um quarto na tabela", "black");
            }
        } catch (IllegalArgumentException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível pesquisar os quartos. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoEscolherQuarto() {
        Quarto quarto = quartosTabela.getSelectionModel().getSelectedItem();
        if (quarto == null) {
            return;
        }
        try {
            BigDecimal preco = reservaService.calcularPreco(quarto, entradaPesquisada, saidaPesquisada);
            quartoEscolhido = quarto;
            precoLabel.setText("Quarto " + quarto.getNumero() + ": " + preco + " € ("
                    + quarto.getValorNoite() + " € por noite)");
            mostrarMensagem("", "black");
        } catch (ValidacaoException e) {
            limparQuartoEscolhido();
            mostrarMensagem(e.getMessage(), "red");
        }
        atualizarConfirmar();
    }

    @FXML
    private void aoConfirmar() {
        try {
            Reserva reserva = reservaService.criar(cliente, quartoEscolhido, entradaPesquisada,
                    saidaPesquisada, hospedesPesquisados);
            mostrarMensagem("Reserva n.º " + reserva.getId() + " criada para " + cliente.getNome()
                    + " (quarto " + reserva.getQuartoNumero() + ", " + reserva.getPrecoTotal() + " €)", "green");
            limparQuartoEscolhido();
            quartosTabela.getItems().clear();
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível gravar a reserva. Tente novamente.", "red");
        }
    }

    private void limparQuartoEscolhido() {
        quartoEscolhido = null;
        precoLabel.setText("");
        atualizarConfirmar();
    }

    private void atualizarConfirmar() {
        confirmarButton.setDisable(cliente == null || quartoEscolhido == null);
    }

    private void mostrarMensagem(String texto, String cor) {
        mensagemLabel.setStyle("-fx-text-fill: " + cor + ";");
        mensagemLabel.setText(texto);
    }
}