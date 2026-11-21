package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;
import pt.isep.lp3.g2.hotel4youg2.bll.QuartoBLL;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "Gestão de quartos": o gestor vê todos os quartos e desativa ou reativa cada um.
 */
public class GestaoQuartosController {
    private final QuartoBLL quartoBLL = new QuartoBLL();

    @FXML
    private TableView<Quarto> tabela;

    @FXML
    private Label mensagemLabel;

    @FXML
    private TextField numeroField;

    @FXML
    private TextField pisoField;

    @FXML
    private ComboBox<TipoQuarto> tipoCombo;

    @FXML
    private ComboBox<Vista> vistaCombo;

    @FXML
    private void initialize() {
        tipoCombo.getItems().setAll(TipoQuarto.values());
        vistaCombo.getItems().setAll(Vista.values());
        carregar();
    }

    @FXML
    private void aoCriar() {
        try {
            Quarto quarto = quartoBLL.criar(Sessao.getUtilizador().getPerfil(), numeroField.getText(),
                    pisoField.getText(), tipoCombo.getValue(), vistaCombo.getValue());
            Mensagens.sucesso(mensagemLabel, "Quarto " + quarto.getNumero() + " criado, para "
                    + quarto.getCapacidade() + (quarto.getCapacidade() == 1 ? " pessoa" : " pessoas")
                    + ". Já aparece nas reservas e leva os materiais configurados para o tipo " + quarto.getTipo()
                    + ".");
            numeroField.clear();
            pisoField.clear();
            tipoCombo.setValue(null);
            vistaCombo.setValue(null);
            carregar();
        } catch (RuntimeException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        }
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
                quartoBLL.reativar(perfil, escolhido.getNumero());
            } else {
                quartoBLL.desativar(perfil, escolhido.getNumero());
            }
            Mensagens.sucesso(mensagemLabel, ativar ? "Quarto reativado." : "Quarto desativado.");
            carregar();
        } catch (RuntimeException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        }
    }

    private void carregar() {
        try {
            tabela.getItems().setAll(quartoBLL.listarTodos(Sessao.getUtilizador().getPerfil()));
        } catch (RuntimeException e) {
            tabela.getItems().clear();
            Mensagens.erro(mensagemLabel, "Não foi possível carregar os quartos: " + e.getMessage());
        }
    }
}
