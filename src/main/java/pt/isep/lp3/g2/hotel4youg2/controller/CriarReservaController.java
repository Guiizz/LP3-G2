package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ServicoReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador do ecrã para criar uma reserva (US27): escolher o cliente pelo NIF,
 * pesquisar quartos livres, juntar logo os serviços extra (US60), ver o preço e confirmar.
 * Depois de criada, pode juntar-lhe mais serviços (US37).
 */
public class CriarReservaController {

    private static final String SEM_QUARTO = "Escolha o cliente, as datas e o tipo de quarto.";
    private final ClienteBLL clienteBLL = new ClienteBLL();
    private final ReservaBLL reservaBLL = new ReservaBLL();
    private final ServicoReservaBLL servicoReservaBLL = new ServicoReservaBLL();
    private final List<ServicoReserva> servicosEscolhidos = new ArrayList<>();

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
    private ComboBox<Servico> servicoCombo;
    @FXML
    private Spinner<Integer> quantidadeSpinner;
    @FXML
    private TableView<ServicoReserva> servicosTabela;
    @FXML
    private Label precoLabel;
    @FXML
    private Button confirmarButton;
    @FXML
    private Label totalLabel;
    @FXML
    private Label mensagemLabel;

    private Cliente cliente;
    private Quarto quartoEscolhido;
    private LocalDate entradaPesquisada;
    private LocalDate saidaPesquisada;
    private int hospedesPesquisados;
    private boolean earlyPesquisado;
    private boolean latePesquisado;
    private HorarioReserva horarioPesquisado;

    @FXML
    private void initialize() {
        try {
            servicoCombo.getItems().setAll(servicoReservaBLL.listarServicosAtivos());
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível carregar os serviços extra. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoEscolherCliente() {
        try {
            Cliente escolhido = EscolherPessoaController.escolher("Escolher cliente", clienteBLL.pesquisar(""));
            if (escolhido != null) {
                nifField.setText(escolhido.getNif());
                aoProcurarCliente();
            }
        } catch (IOException | DataAccessException e) {
            mostrarMensagem("Não foi possível abrir a lista de clientes. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoProcurarCliente() {
        cliente = null;
        clienteLabel.setText("");
        try {
            cliente = clienteBLL.pesquisarPorNif(nifField.getText());
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
            List<Quarto> opcoes = reservaBLL.opcoesDisponiveis(entrada, saida, hospedes, early, late);
            entradaPesquisada = entrada;
            saidaPesquisada = saida;
            hospedesPesquisados = hospedes;
            earlyPesquisado = early;
            latePesquisado = late;
            horarioPesquisado = reservaBLL.horario();
            quartosTabela.getItems().setAll(opcoes);
            if (opcoes.isEmpty()) {
                mostrarMensagem(reservaBLL.avisoSemQuartos(entrada, saida, hospedes, early, late), "red");
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
            quartoEscolhido = quarto;
            mostrarTotal();
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
    private void aoJuntarServico() {
        try {
            servicosEscolhidos.add(ServicoReservaBLL.novoServico(0, servicoCombo.getValue(),
                    quantidadeSpinner.getValue()));
            servicosTabela.getItems().setAll(servicosEscolhidos);
            mostrarTotal();
            mostrarMensagem("", "black");
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível ler as taxas de horário. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoRetirarServico() {
        ServicoReserva escolhido = servicosTabela.getSelectionModel().getSelectedItem();
        if (escolhido == null) {
            mostrarMensagem("Escolha o serviço a retirar na lista.", "red");
            return;
        }
        servicosEscolhidos.remove(escolhido);
        servicosTabela.getItems().setAll(servicosEscolhidos);
        try {
            mostrarTotal();
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível ler as taxas de horário. Tente novamente.", "red");
        }
    }

    private void mostrarTotal() {
        if (quartoEscolhido == null) {
            precoLabel.setText(SEM_QUARTO);
            totalLabel.setText("");
            return;
        }
        String resumo = reservaBLL.resumoDoTotal(quartoEscolhido, entradaPesquisada, saidaPesquisada,
                earlyPesquisado, latePesquisado, servicosEscolhidos, horarioPesquisado);
        int ultimaLinha = resumo.lastIndexOf('\n');
        precoLabel.setText(resumo.substring(0, ultimaLinha));
        totalLabel.setText(resumo.substring(ultimaLinha + 1));

    }

    @FXML
    private void aoConfirmar() {
        try {
            Reserva reserva = reservaBLL.criar(cliente, quartoEscolhido.getTipo(), quartoEscolhido.getVista(),
                    entradaPesquisada, saidaPesquisada, hospedesPesquisados, earlyPesquisado, latePesquisado,
                    servicosEscolhidos);
            mostrarMensagem("Reserva n.º " + reserva.getId() + " criada para " + cliente.getNome()
                    + ": foi atribuído o quarto " + reserva.getQuartoNumero() + " (" + reserva.getTotal() + " €)",
                    "green");
            servicosEscolhidos.clear();
            servicosTabela.getItems().clear();
            limparQuartoEscolhido();
            quartosTabela.getItems().clear();
        } catch (ValidacaoException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível gravar a reserva. Tente novamente.", "red");
        }
    }

    private void limparQuartoEscolhido() {
        quartoEscolhido = null;
        precoLabel.setText(SEM_QUARTO);
        totalLabel.setText("");
        atualizarConfirmar();
    }

    private void atualizarConfirmar() {
        confirmarButton.setDisable(cliente == null || quartoEscolhido == null);
    }

    private void mostrarMensagem(String texto, String cor) {
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }
}