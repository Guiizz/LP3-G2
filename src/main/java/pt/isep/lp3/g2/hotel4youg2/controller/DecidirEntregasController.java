package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.EntregaBLL;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoEntregaHotel;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã onde o pessoal do hotel vê as entregas dos fornecedores e aceita ou recusa
 * as que estão pendentes (US49).
 */
public class DecidirEntregasController {

    private final EntregaBLL entregaBLL = new EntregaBLL();

    @FXML
    private TableView<ResumoEntregaHotel> tabela;
    @FXML
    private TextField motivoField;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        atualizar();
    }

    @FXML
    private void aoAceitar() {
        ResumoEntregaHotel escolhida = escolherPendente();
        if (escolhida == null) {
            return;
        }
        try {
            entregaBLL.aceitar(Sessao.getUtilizador(), escolhida.getEntregaId());
            mostrarMensagem("Entrega " + escolhida.getNumeroEncomenda() + " aceite. "
                    + escolhida.getTotalUnidades() + " unidades entraram no stock.", "green");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível aceitar a entrega. Tente novamente.", "red");
        }
        atualizar();
    }

    @FXML
    private void aoRecusar() {
        ResumoEntregaHotel escolhida = escolherPendente();
        if (escolhida == null) {
            return;
        }
        try {
            entregaBLL.recusar(Sessao.getUtilizador(), escolhida.getEntregaId(), motivoField.getText());
            motivoField.clear();
            mostrarMensagem("Entrega " + escolhida.getNumeroEncomenda() + " recusada.", "green");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível recusar a entrega. Tente novamente.", "red");
        }
        atualizar();
    }

    @FXML
    private void aoAtualizar() {
        atualizar();
        mensagemLabel.setText("");
    }

    // a entrega escolhida na tabela, se ainda estiver pendente; senão avisa e devolve null
    private ResumoEntregaHotel escolherPendente() {
        ResumoEntregaHotel escolhida = tabela.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            mostrarMensagem("Escolha primeiro uma entrega na tabela.", "red");
            return null;
        }
        if (escolhida.getEstado() != EstadoEntrega.PENDENTE) {
            mostrarMensagem("Esta entrega já foi decidida.", "red");
            return null;
        }
        return escolhida;
    }

    private void atualizar() {
        try {
            tabela.getItems().setAll(entregaBLL.listarParaHotel(Sessao.getUtilizador()));
        } catch (IllegalStateException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível obter as entregas.", "red");
        }
    }

    private void mostrarMensagem(String texto, String cor) {
        Mensagens.mostrar(mensagemLabel, texto, cor);
    }
}
