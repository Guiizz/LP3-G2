package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador do ecrã para criar uma reserva (US27): escolher o cliente pelo NIF,
 * pesquisar quartos livres, ver o preço e confirmar. Depois de criada, pode juntar-lhe serviços (US37).
 */
public class CriarReservaController {

    private final ClienteBLL clienteService = new ClienteBLL();
    private final ReservaBLL reservaService = new ReservaBLL();

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
    private CheckBox earlyCheckBox;
    @FXML
    private CheckBox lateCheckBox;
    @FXML
    private TableView<Quarto> quartosTabela;
    @FXML
    private Label precoLabel;
    @FXML
    private Button confirmarButton;
    @FXML
    private Button servicosButton;
    @FXML
    private Label mensagemLabel;

    private Cliente cliente;
    private Reserva reservaCriada;
    private Quarto quartoEscolhido;
    private LocalDate entradaPesquisada;
    private LocalDate saidaPesquisada;
    private int hospedesPesquisados;
    private boolean earlyPesquisado;
    private boolean latePesquisado;

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
            boolean early = earlyCheckBox.isSelected();
            boolean late = lateCheckBox.isSelected();
            List<Quarto> opcoes = reservaService.opcoesDisponiveis(entrada, saida, hospedes, early, late);
            String aviso = null;
            if (opcoes.isEmpty() && (early || late)) {
                List<Quarto> standard = reservaService.opcoesDisponiveis(entrada, saida, hospedes, false, false);
                if (!standard.isEmpty()) {
                    HorarioReserva horario = reservaService.horario();
                    aviso = "Nenhum quarto permite o early check-in ou o late check-out nestas datas. "
                            + "Pode fazer a reserva nas horas standard (check-in às " + horario.getHoraCheckin()
                            + " e check-out às " + horario.getHoraCheckout() + ").";
                    opcoes = standard;
                    early = false;
                    late = false;
                    earlyCheckBox.setSelected(false);
                    lateCheckBox.setSelected(false);
                }
            }

            entradaPesquisada = entrada;
            saidaPesquisada = saida;
            hospedesPesquisados = hospedes;
            earlyPesquisado = early;
            latePesquisado = late;
            quartosTabela.getItems().setAll(opcoes);
            if (aviso != null) {
                mostrarMensagem(aviso, "red");
            } else if (opcoes.isEmpty()) {
                mostrarMensagem("Não há quartos livres para estas datas e hóspedes", "black");
            } else {
                mostrarMensagem("Escolha o tipo de quarto na tabela. O número do quarto é atribuído ao confirmar.",
                        "black");
            }
        } catch (ValidacaoException e) {
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
            BigDecimal taxa = reservaService.calcularTaxa(earlyPesquisado, latePesquisado);
            quartoEscolhido = quarto;
            precoLabel.setText(quarto.getTipo() + " com vista " + quarto.getVista() + ": " + preco + " € ("
                    + quarto.getValorNoite() + " € por noite) + taxa de horário " + taxa + " € = "
                    + preco.add(taxa) + " €");
            mostrarMensagem("", "black");
        } catch (ValidacaoException e) {
            limparQuartoEscolhido();
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            limparQuartoEscolhido();
            mostrarMensagem("Não foi possível ler as taxas de horário. Tente novamente.", "red");
        }
        atualizarConfirmar();
    }

    @FXML
    private void aoConfirmar() {
        try {
            Reserva reserva = reservaService.criar(cliente, quartoEscolhido.getTipo(), quartoEscolhido.getVista(),
                    entradaPesquisada, saidaPesquisada, hospedesPesquisados, earlyPesquisado, latePesquisado);
            mostrarMensagem("Reserva n.º " + reserva.getId() + " criada para " + cliente.getNome()
                    + ": foi atribuído o quarto " + reserva.getQuartoNumero() + " (" + reserva.getTotal() + " €)",
                    "green");
            reservaCriada = reserva;
            servicosButton.setDisable(false);
            limparQuartoEscolhido();
            quartosTabela.getItems().clear();
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível gravar a reserva. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoJuntarServicos() {
        if (reservaCriada == null) {
            return;
        }
        try {
            ServicosReservaController.abrir(reservaCriada);
            mostrarMensagem("Reserva n.º " + reservaCriada.getId() + ": quarto " + reservaCriada.getPrecoTotal()
                    + " € + taxa de horário " + reservaCriada.getTaxaHorario() + " € + serviços "
                    + reservaCriada.getTotalServicos() + " € = " + reservaCriada.getTotal() + " €", "green");
        } catch (IOException e) {
            mostrarMensagem("Não foi possível abrir os serviços da reserva.", "red");
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
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }
}