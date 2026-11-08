package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;

public class AreaController {

    @FXML
    private BorderPane area;

    @FXML
    private Label tituloLabel;

    @FXML
    private HBox submenu;

    private List<OpcaoMenu> opcoesAbertas;

    public void configurar(String titulo, List<OpcaoMenu> opcoes) {
        this.opcoesAbertas = opcoes;
        tituloLabel.setText(titulo);
        submenu.getChildren().clear();
        for (OpcaoMenu opcao : opcoes) {
            Button botao = new Button(opcao.getTexto());
            botao.getStyleClass().add("botao-submenu");
            botao.setOnAction(new AoEscolherOpcao(opcao));
            submenu.getChildren().add(botao);
        }
        if (!opcoes.isEmpty() && !opcoes.get(0).isFormulario()) {
            abrir(opcoes.get(0));
        }
    }

    private void abrir(OpcaoMenu opcao) {
        if (!opcao.isFormulario()) {
            marcarAtiva(opcao);
        }
        try {
            if (opcao.isFormulario()) {
                Navegador.abrirFormulario(opcao.getFicheiro(), opcao.getTexto());
            } else {
                area.setCenter(Navegador.carregar(opcao.getFicheiro()).getRoot());
            }
        } catch (IOException e) {
            Label erro = new Label("Não foi possível abrir " + opcao.getTexto() + ".");
            erro.getStyleClass().add("mensagem-erro");
            area.setCenter(erro);
        }
    }

    private void marcarAtiva(OpcaoMenu opcao) {
        for (Node filho : submenu.getChildren()) {
            filho.getStyleClass().remove("botao-submenu-ativo");
        }
        int posicao = opcoesAbertas.indexOf(opcao);
        submenu.getChildren().get(posicao).getStyleClass().add("botao-submenu-ativo");
    }

    private class AoEscolherOpcao implements EventHandler<ActionEvent> {
        private final OpcaoMenu opcao;

        AoEscolherOpcao(OpcaoMenu opcao) {
            this.opcao = opcao;
        }

        @Override
        public void handle(ActionEvent evento) {
            abrir(opcao);
        }
    }
}