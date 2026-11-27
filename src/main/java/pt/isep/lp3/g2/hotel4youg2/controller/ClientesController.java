package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.enums.CampoCliente;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClientesController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClienteBLL clienteBLL = new ClienteBLL();
    private List<Cliente> todos = new ArrayList<>();

    @FXML
    private VBox ecra;
    @FXML
    private TextField filtroField;
    @FXML
    private ComboBox<CampoCliente> campoCombo;
    @FXML
    private TableView<Cliente> clientesTabela;
    @FXML
    private Label detalhesLabel;
    @FXML
    private Button registarButton;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        campoCombo.getItems().setAll(CampoCliente.values());
        carregar();
    }

    private void carregar() {
        try {
            todos = clienteBLL.listar();
            aoFiltrar();
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível carregar os clientes. Tente novamente.");
        }
    }

    @FXML
    private void aoFiltrar() {
        List<Cliente> visiveis = clienteBLL.filtrar(todos, filtroField.getText(), campoCombo.getValue());
        clientesTabela.getItems().setAll(visiveis);
        detalhesLabel.setText("");
        boolean ninguem = visiveis.isEmpty() && !filtroField.getText().isBlank();
        registarButton.setVisible(ninguem);
        registarButton.setManaged(ninguem);
        if (ninguem) {
            Mensagens.info(mensagemLabel, "Cliente não encontrado.");
        } else {
            Mensagens.info(mensagemLabel, visiveis.size() + " de " + todos.size() + " cliente(s)");
        }
    }

    @FXML
    private void aoLimparPesquisa() {
        filtroField.clear();
        campoCombo.setValue(null);
        aoFiltrar();
    }

    @FXML
    private void aoClicarNaTabela(MouseEvent evento) {
        Cliente cliente = clientesTabela.getSelectionModel().getSelectedItem();
        if (cliente == null) {
            return;
        }
        mostrarDetalhes(cliente);
        if (evento.getClickCount() == 2) {
            aoVerReservas();
        }
    }

    private void mostrarDetalhes(Cliente cliente) {
        String nascimento = "-";
        if (cliente.getDataNascimento() != null) {
            nascimento = cliente.getDataNascimento().format(FORMATO_DATA);
        }
        detalhesLabel.setText(cliente.getNome() + "  ·  Data de nascimento: " + nascimento
                + "  ·  CC: " + textoOuTraco(cliente.getCc()) + "  ·  Morada: " + textoOuTraco(cliente.getMorada()));
    }

    @FXML
    private void aoVerReservas() {
        Cliente cliente = clienteEscolhido();
        if (cliente == null) {
            return;
        }
        try {
            FXMLLoader loader = Navegador.carregar("consultar-reservas-view.fxml");
            ConsultarReservasController reservas = loader.getController();
            reservas.mostrarDoCliente(cliente.getNif());
            Navegador.mostrarDetalhe(ecra, (Parent) loader.getRoot());
        } catch (IOException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível abrir as reservas do cliente.");
        }
    }

    @FXML
    private void aoAbrirConta() {
        Cliente cliente = clienteEscolhido();
        if (cliente == null) {
            return;
        }
        try {
            FXMLLoader loader = Navegador.carregar("conta-cliente-view.fxml");
            ContaClienteController conta = loader.getController();
            conta.preencherNif(cliente.getNif());
            Navegador.abrirFormulario((Parent) loader.getRoot(), "Conta do cliente");
        } catch (IOException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível abrir a conta do cliente.");
        }
    }

    @FXML
    private void aoRegistar() {
        try {
            FXMLLoader loader = Navegador.carregar("registar-cliente-view.fxml");
            String texto = filtroField.getText().trim();
            if (texto.matches("\\d{9}")) {
                RegistarClienteController registo = loader.getController();
                registo.preencherNif(texto);
            }
            Navegador.abrirFormulario((Parent) loader.getRoot(), "Registar cliente");
            carregar();
        } catch (IOException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível abrir o registo de cliente.");
        }
    }

    private Cliente clienteEscolhido() {
        Cliente cliente = clientesTabela.getSelectionModel().getSelectedItem();
        if (cliente == null) {
            Mensagens.erro(mensagemLabel, "Escolha um cliente na tabela.");
        }
        return cliente;
    }

    private String textoOuTraco(String texto) {
        if (texto == null) {
            return "-";
        }
        return texto;
    }
}
