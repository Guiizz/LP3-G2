package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import pt.isep.lp3.g2.hotel4youg2.bll.ReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.SeparadorReserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.util.List;

public class MinhasReservasController {

    private final ReservaBLL reservaBLL = new ReservaBLL();

    @FXML
    private VBox ecra;
    @FXML
    private TabPane separadores;
    @FXML
    private TableView<Reserva> tabelaPorFazer;
    @FXML
    private TableView<Reserva> tabelaEmCurso;
    @FXML
    private TableView<Reserva> tabelaHistorico;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        AoClicarDuasVezes duploClique = new AoClicarDuasVezes();
        tabelaPorFazer.setOnMouseClicked(duploClique);
        tabelaEmCurso.setOnMouseClicked(duploClique);
        tabelaHistorico.setOnMouseClicked(duploClique);
        carregar();
    }

    private void carregar() {
        ContaCliente conta = Sessao.getContaCliente();
        if (conta == null) {
            Mensagens.erro(mensagemLabel, "Entre na sua conta para ver as suas reservas.");
            return;
        }
        try {
            Cliente cliente = new Cliente(conta.getClienteId(), conta.getNome(), null, conta.getNif(), null, null,
                    null, null);
            List<Reserva> reservas = reservaBLL.consultarDoCliente(cliente);
            for (Reserva reserva : reservas) {
                reserva.setNomeCliente(conta.getNome());
                reserva.setNifCliente(conta.getNif());
            }
            tabelaPorFazer.getItems().setAll(ReservaBLL.filtrar(reservas, SeparadorReserva.POR_FAZER, ""));
            tabelaEmCurso.getItems().setAll(ReservaBLL.filtrar(reservas, SeparadorReserva.EM_CURSO, ""));
            tabelaHistorico.getItems().setAll(ReservaBLL.filtrar(reservas, SeparadorReserva.HISTORICO, ""));
            if (!tabelaEmCurso.getItems().isEmpty()) {
                separadores.getSelectionModel().select(1);
            }
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível carregar as suas reservas. Tente novamente.");
        }
    }

    @FXML
    private void aoVerDetalhes() {
        Reserva escolhida = escolhida();
        if (escolhida == null) {
            Mensagens.erro(mensagemLabel, "Escolha uma reserva na tabela.");
            return;
        }
        try {
            mensagemLabel.setText("");
            DetalhesReservaController.abrirSoParaVer(escolhida, ecra);
        } catch (IOException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível abrir os detalhes da reserva.");
        }
    }

    private Reserva escolhida() {
        int separador = separadores.getSelectionModel().getSelectedIndex();
        TableView<Reserva> tabela = separador == 0 ? tabelaPorFazer : separador == 1 ? tabelaEmCurso : tabelaHistorico;
        return tabela.getSelectionModel().getSelectedItem();
    }

    private class AoClicarDuasVezes implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent evento) {
            if (evento.getClickCount() == 2 && escolhida() != null) {
                aoVerDetalhes();
            }
        }
    }
}
