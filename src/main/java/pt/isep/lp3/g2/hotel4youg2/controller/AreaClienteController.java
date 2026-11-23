package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;

public class AreaClienteController {

    @FXML
    private BorderPane raiz;

    @FXML
    private Label clienteLabel;

    @FXML
    private Label boasVindasLabel;

    @FXML
    private void initialize() {
        Navegador.iniciar(raiz);
        ContaCliente conta = Sessao.getContaCliente();
        if (conta != null) {
            clienteLabel.setText(conta.getNome() + " (cliente)");
            boasVindasLabel.setText("Bem-vindo, " + conta.getNome());
        }
    }

    @FXML
    private void aoAbrirPlanta() throws IOException {
        Navegador.mostrar("planta-view.fxml");
    }

    @FXML
    private void aoAbrirReservas() throws IOException {
        Navegador.mostrar("minhas-reservas-view.fxml");
    }

    @FXML
    private void aoAbrirCartao() throws IOException {
        Navegador.mostrar("meu-cartao-view.fxml");
    }

    @FXML
    private void aoAbrirSugestao() throws IOException {
        Navegador.mostrar("sugestao-cliente-view.fxml");
    }

    @FXML
    private void aoTerminarSessao() throws IOException {
        Navegador.terminarSessao();
    }
}
