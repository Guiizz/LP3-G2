package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import pt.isep.lp3.g2.hotel4youg2.bll.QuartoBLL;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

public class GestaoQuartosController {
    private final QuartoBLL quartoService = new QuartoBLL();

    @FXML
    private TableView<Quarto> tabela;

    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        carregar();
    }

    @FXML
    private void aoDesativar() {
        executar(false);
    }

    @FXML
    private void aoReativar() {
        executar(true);
    }

    private void executar(boolean ativar) {
        Quarto escolhido = tabela.getSelectionModel().getSelectedItem();
        if (escolhido == null) {
            Mensagens.erro(mensagemLabel, "Escolha um quarto na tabela.");
            return;
        }
        try {
            Perfil perfil = Sessao.getUtilizador().getPerfil();
            if (ativar) {
                quartoService.reativar(perfil, escolhido.getNumero());
            } else {
                quartoService.desativar(perfil, escolhido.getNumero());
            }
            Mensagens.sucesso(mensagemLabel, ativar ? "Quarto reativado." : "Quarto desativado.");
            carregar();
        } catch (RuntimeException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        }
    }

    private void carregar() {
        try {
            tabela.getItems().setAll(quartoService.listarTodos(Sessao.getUtilizador().getPerfil()));
        } catch (RuntimeException e) {
            tabela.getItems().clear();
            Mensagens.erro(mensagemLabel, "Não foi possível carregar os quartos: " + e.getMessage());
        }
    }
}
