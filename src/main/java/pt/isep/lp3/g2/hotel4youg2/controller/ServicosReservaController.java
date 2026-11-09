package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.ServicoReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class ServicosReservaController {

    private final ServicoReservaBLL servicoReservaBLL = new ServicoReservaBLL();
    private Reserva reserva;

    @FXML
    private Label tituloLabel;
    @FXML
    private Label reservaLabel;
    @FXML
    private TableView<ServicoReserva> tabela;
    @FXML
    private ComboBox<Servico> servicoCombo;
    @FXML
    private Spinner<Integer> quantidadeSpinner;
    @FXML
    private Button adicionarButton;
    @FXML
    private Button retirarButton;
    @FXML
    private Label quartoLabel;
    @FXML
    private Label servicosLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label mensagemLabel;

    public static void abrir(Reserva reserva) throws IOException {
        FXMLLoader loader = Navegador.carregar("servicos-reserva-view.fxml");
        ServicosReservaController controller = loader.getController();
        controller.mostrar(reserva);
        Navegador.abrirFormulario((Parent) loader.getRoot(), "Serviços da reserva " + reserva.getId());
    }

    @FXML
    private void initialize() {
        try {
            servicoCombo.getItems().setAll(servicoReservaBLL.listarServicosAtivos());
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível carregar os serviços. Tente novamente.", "red");
        }
    }

    public void mostrar(Reserva reserva) {
        this.reserva = reserva;
        tituloLabel.setText("Serviços da reserva n.º " + reserva.getId());
        reservaLabel.setText("Quarto " + reserva.getQuartoNumero() + ", de " + reserva.getDataEntrada() + " a "
                + reserva.getDataSaida() + " · " + reserva.getEstado().getDescricao());
        boolean aberta = reserva.aceitaServicos();
        servicoCombo.setDisable(!aberta);
        quantidadeSpinner.setDisable(!aberta);
        adicionarButton.setDisable(!aberta);
        retirarButton.setDisable(!aberta);
        if (!aberta) {
            mostrarMensagem("Esta reserva já não aceita serviços.", "darkorange");
        }
        carregar();
    }

    @FXML
    private void aoAdicionar() {
        try {
            int quantidade = quantidadeSpinner.getValue();
            ServicoReserva novo = servicoReservaBLL.adicionar(Sessao.getUtilizador(), reserva, servicoCombo.getValue(),
                    quantidade);
            carregar();
            mostrarMensagem(novo.getQuantidade() + " × " + novo.getServico().getDesignacao() + " junto à reserva ("
                    + euros(novo.getTotal()) + ").", "green");
            servicoCombo.setValue(null);
            quantidadeSpinner.getValueFactory().setValue(1);
        } catch (ValidacaoException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível juntar o serviço. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoRetirar() {
        ServicoReserva escolhido = tabela.getSelectionModel().getSelectedItem();
        if (escolhido == null) {
            mostrarMensagem("Escolha na tabela o serviço a retirar.", "red");
            return;
        }
        if (!confirmarRetirar(escolhido)) {
            return;
        }
        try {
            servicoReservaBLL.retirar(Sessao.getUtilizador(), reserva, escolhido);
            carregar();
            mostrarMensagem(escolhido.getServico().getDesignacao() + " retirado da reserva.", "green");
        } catch (ValidacaoException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível retirar o serviço. Tente novamente.", "red");
        }
    }

    @FXML
    private void aoFechar() {
        Navegador.fecharFormulario(tabela);
    }

    private boolean confirmarRetirar(ServicoReserva servico) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, "Retirar " + servico.getQuantidade() + " × "
                + servico.getServico().getDesignacao() + " (" + euros(servico.getTotal()) + ") da reserva "
                + reserva.getId() + "?", ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Retirar serviço");
        alerta.setHeaderText(null);
        Optional<ButtonType> resposta = alerta.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.YES;
    }

    private void carregar() {
        try {
            List<ServicoReserva> servicos = servicoReservaBLL.listarDaReserva(reserva);
            tabela.getItems().setAll(servicos);
            reserva.setTotalServicos(ServicoReservaBLL.somar(servicos));
            quartoLabel.setText(euros(reserva.getPrecoTotal()));
            servicosLabel.setText(euros(reserva.getTotalServicos()));
            totalLabel.setText(euros(reserva.getTotal()));
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível carregar os serviços da reserva. Tente novamente.", "red");
        }
    }

    private void mostrarMensagem(String texto, String cor) {
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }

    private static String euros(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " €";
    }
}
