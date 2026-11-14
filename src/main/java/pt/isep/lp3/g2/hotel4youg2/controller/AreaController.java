package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;

import java.io.IOException;
import java.util.List;

/**
 * Controlador de uma área do menu (US44): mostra o título, os botões do submenu no topo e o ecrã
 * da opção escolhida por baixo. A opção aberta fica destacada.
 */
public class AreaController {

    @FXML
    private BorderPane area;

    @FXML
    private Label tituloLabel;

    @FXML
    private HBox submenu;

    private List<OpcaoMenu> opcoesAbertas;

    /**
     * Prepara a área com o título e os botões das opções, e abre a primeira opção se não for um formulário.
     *
     * @param titulo o nome da área
     * @param opcoes as opções do submenu, pela ordem em que aparecem
     */
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
            mostrarErro(opcao, e);
        }
    }

    private void mostrarErro(OpcaoMenu opcao, IOException e) {
        Label erro = new Label(textoDoErro(opcao, e));
        erro.getStyleClass().add("mensagem-erro");
        erro.setWrapText(true);
        Button tentarOutraVez = new Button("Tentar outra vez");
        tentarOutraVez.setOnAction(new AoEscolherOpcao(opcao));
        VBox caixa = new VBox(10, erro, tentarOutraVez);
        caixa.setPadding(new Insets(20));
        area.setCenter(caixa);
    }

    static String textoDoErro(OpcaoMenu opcao, Throwable erro) {
        Throwable causa = erro;
        while (causa != null) {
            if (causa instanceof DataAccessException) {
                return causa.getMessage();
            }
            causa = causa.getCause();
        }
        return "Não foi possível abrir " + opcao.getTexto() + ".";
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