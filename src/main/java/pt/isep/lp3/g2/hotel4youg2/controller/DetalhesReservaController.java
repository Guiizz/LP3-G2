package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ServicoReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class DetalhesReservaController {

    private static final String ERRO_BD = "Não foi possível comunicar com a base de dados. Tente novamente.";
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ReservaBLL reservaBLL = new ReservaBLL();
    private final ServicoReservaBLL servicoReservaBLL = new ServicoReservaBLL();
    private Reserva reserva;
    private GerirReservasController lista;

    @FXML
    private Label tituloLabel;
    @FXML
    private Label estadoLabel;
    @FXML
    private Label clienteLabel;
    @FXML
    private Label nifLabel;
    @FXML
    private Label quartoLabel;
    @FXML
    private Label hospedesLabel;
    @FXML
    private Label entradaLabel;
    @FXML
    private Label saidaLabel;
    @FXML
    private Label earlyLabel;
    @FXML
    private Label lateLabel;
    @FXML
    private Label cartaoLabel;
    @FXML
    private Label checkinLabel;
    @FXML
    private Label checkoutLabel;
    @FXML
    private TableView<ServicoReserva> servicosTabela;
    @FXML
    private Label precoQuartoLabel;
    @FXML
    private Label totalServicosLabel;
    @FXML
    private Label taxasLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private HBox datasBox;
    @FXML
    private DatePicker novaEntradaPicker;
    @FXML
    private DatePicker novaSaidaPicker;
    @FXML
    private Button servicosButton;
    @FXML
    private Label avisoLabel;
    @FXML
    private Label mensagemLabel;

    public static void abrir(Reserva reserva, Node ecraDaLista, GerirReservasController lista) throws IOException {
        FXMLLoader loader = Navegador.carregar("detalhes-reserva-view.fxml");
        DetalhesReservaController controller = loader.getController();
        controller.reserva = reserva;
        controller.lista = lista;
        controller.mostrar();
        Navegador.mostrarDetalhe(ecraDaLista, (Parent) loader.getRoot());
    }

    private void mostrar() {
        tituloLabel.setText("Reserva n.º " + reserva.getId());
        estadoLabel.setText(reserva.getEstadoTexto());
        clienteLabel.setText(textoOuTraco(reserva.getNomeCliente()));
        nifLabel.setText(textoOuTraco(reserva.getNifCliente()));
        quartoLabel.setText(String.valueOf(reserva.getQuartoNumero()));
        hospedesLabel.setText(String.valueOf(reserva.getNumHospedes()));
        entradaLabel.setText(reserva.getDataEntrada().format(DATA));
        saidaLabel.setText(reserva.getDataSaida().format(DATA) + " (" + reserva.getNoites() + " noite(s))");
        earlyLabel.setText(reserva.isEarlyCheckin() ? "Sim" : "Não");
        lateLabel.setText(reserva.isLateCheckout() ? "Sim" : "Não");
        cartaoLabel.setText(textoOuTraco(reserva.getCartao()));
        checkinLabel.setText(textoOuTraco(reserva.getCheckinEmTexto()));
        checkoutLabel.setText(textoOuTraco(reserva.getCheckoutEmTexto()));
        novaEntradaPicker.setValue(reserva.getDataEntrada());
        novaSaidaPicker.setValue(reserva.getDataSaida());
        mostrarServicos();
        mostrarAcoes();
    }

    private void mostrarServicos() {
        try {
            List<ServicoReserva> servicos = servicoReservaBLL.listarDaReserva(reserva);
            servicosTabela.getItems().setAll(servicos);
            reserva.setTotalServicos(ServicoReservaBLL.somar(servicos));
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, ERRO_BD);
        }
        precoQuartoLabel.setText("Quarto: " + reserva.getPrecoTotal() + " €");
        totalServicosLabel.setText("Serviços: " + reserva.getTotalServicos() + " €");
        taxasLabel.setText("Taxas: " + reserva.getTaxaHorario() + " €");
        totalLabel.setText("Total: " + reserva.getTotal() + " €");
    }

    private void mostrarAcoes() {
        mostrarSe(datasBox, reserva.podeSerAlterada());
        mostrarSe(servicosButton, reserva.aceitaServicos());
        String aviso = reserva.getAvisoAlteracoes();
        avisoLabel.setText(aviso);
        mostrarSe(avisoLabel, !aviso.isEmpty());
    }

    private void mostrarSe(Node no, boolean visivel) {
        no.setVisible(visivel);
        no.setManaged(visivel);
    }

    private static String textoOuTraco(String texto) {
        return texto == null || texto.isEmpty() ? "-" : texto;
    }

    @FXML
    private void aoVoltar() {
        Navegador.voltar();
        lista.atualizar();
    }

    @FXML
    private void aoAlterarDatas() {
        try {
            reservaBLL.alterarDatas(Sessao.getUtilizador(), reserva, novaEntradaPicker.getValue(), novaSaidaPicker.getValue());
            mostrar();
            Mensagens.sucesso(mensagemLabel, "Datas alteradas. Novo preço do quarto: " + reserva.getPrecoTotal() + " €");
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, ERRO_BD);
        } catch (RuntimeException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        }
    }

    @FXML
    private void aoServicos() {
        try {
            ServicosReservaController.abrir(reserva);
        } catch (IOException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível abrir os serviços da reserva.");
        }
        mostrarServicos();
    }
}
