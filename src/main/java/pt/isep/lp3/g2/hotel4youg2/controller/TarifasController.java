package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.TarifaBLL;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.math.BigDecimal;

/**
 * Controlador do ecrã das tarifas: mostra os preços por noite e deixa o gestor alterá-los.
 */
public class TarifasController {

    private final TarifaBLL tarifaBLL = new TarifaBLL();

    @FXML
    private TableView<Tarifa> tabela;
    @FXML
    private TableColumn<Tarifa, String> colunaTipo;
    @FXML
    private TableColumn<Tarifa, String> colunaVista;
    @FXML
    private TableColumn<Tarifa, String> colunaPreco;
    @FXML
    private TextField campoPreco;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        colunaTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().name()));
        colunaVista.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVista().name()));
        colunaPreco.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getValorNoite().toPlainString()));
        carregar();
    }

    private void carregar() {
        try {
            tabela.setItems(FXCollections.observableArrayList(tarifaBLL.listar()));
        } catch (RuntimeException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível ler as tarifas: " + e.getMessage());
        }
    }

    @FXML
    private void aoGuardar() {
        Tarifa selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            Mensagens.erro(mensagemLabel, "Escolha uma tarifa na tabela");
            return;
        }
        try {
            BigDecimal novoValor = new BigDecimal(campoPreco.getText().trim().replace(',', '.'));
            tarifaBLL.alterarPreco(perfilAtual(), selecionada.getTipo(), selecionada.getVista(), novoValor);
            Mensagens.sucesso(mensagemLabel, "Preço atualizado");
            campoPreco.clear();
            carregar();
        } catch (NumberFormatException e) {
            Mensagens.erro(mensagemLabel, "Escreva um preço válido (por exemplo, 120.50)");
        } catch (RuntimeException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        }
    }

    /**
     * @return o perfil de quem tem a sessão iniciada, ou {@code null} se ninguém tiver
     */
    private Perfil perfilAtual() {
        Utilizador utilizador = Sessao.getUtilizador();
        if (utilizador == null) {
            return null;
        }
        return utilizador.getPerfil();
    }
}