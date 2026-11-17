package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.ConfiguracaoQuartoBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.Permissoes;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Material;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "Materiais dos quartos" (US75): mostra os materiais, consumíveis ou não, e os materiais
 * que cada tipo de quarto leva por defeito. O gestor muda os materiais para consumíveis ou não e junta, altera
 * ou retira materiais da configuração; o rececionista só consulta.
 */
public class MateriaisQuartosController {

    private final ConfiguracaoQuartoBLL configuracaoBLL = new ConfiguracaoQuartoBLL();

    @FXML
    private TableView<Material> materiaisTabela;
    @FXML
    private Button consumivelButton;
    @FXML
    private ComboBox<TipoQuarto> tipoCombo;
    @FXML
    private TableView<MaterialQuarto> configuracaoTabela;
    @FXML
    private TextField quantidadeField;
    @FXML
    private Button guardarButton;
    @FXML
    private Button retirarButton;
    @FXML
    private Label permissaoLabel;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        boolean gestor = Permissoes.eGestor(Sessao.getUtilizador());
        consumivelButton.setDisable(!gestor);
        quantidadeField.setDisable(!gestor);
        guardarButton.setDisable(!gestor);
        retirarButton.setDisable(!gestor);
        if (!gestor) {
            permissaoLabel.setText("Só o gestor pode alterar os materiais dos quartos; aqui só pode consultar.");
        }
        tipoCombo.getItems().setAll(TipoQuarto.values());
        atualizarMateriais();
        tipoCombo.setValue(TipoQuarto.INDIVIDUAL);
    }

    @FXML
    private void aoEscolherTipo() {
        atualizarConfiguracao();
    }

    @FXML
    private void aoMudarConsumivel() {
        Material material = materiaisTabela.getSelectionModel().getSelectedItem();
        try {
            boolean consumivel = material == null || !material.isConsumivel();
            configuracaoBLL.alterarConsumivel(Sessao.getUtilizador(), material, consumivel);
            Mensagens.sucesso(mensagemLabel, material.getDescricao() + " passou a "
                    + material.getConsumivelTexto().toLowerCase() + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível alterar o material. Tente novamente.");
        }
        atualizarMateriais();
        atualizarConfiguracao();
    }

    /**
     * Junta à configuração o material escolhido na lista de materiais ou, se não houver nenhum escolhido, muda a
     * quantidade do material escolhido na configuração.
     */
    @FXML
    private void aoGuardar() {
        Material material = materiaisTabela.getSelectionModel().getSelectedItem();
        MaterialQuarto linha = configuracaoTabela.getSelectionModel().getSelectedItem();
        if (material == null && linha != null) {
            material = linha.getMaterial();
        }
        try {
            configuracaoBLL.guardar(Sessao.getUtilizador(), tipoCombo.getValue(), material, quantidadeField.getText());
            Mensagens.sucesso(mensagemLabel, "O quarto " + nomeDoTipo() + " leva " + quantidadeField.getText().trim()
                    + " × " + material.getDescricao() + ".");
            quantidadeField.clear();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível guardar a configuração. Tente novamente.");
        }
        atualizarConfiguracao();
    }

    @FXML
    private void aoRetirar() {
        MaterialQuarto linha = configuracaoTabela.getSelectionModel().getSelectedItem();
        try {
            configuracaoBLL.remover(Sessao.getUtilizador(), linha);
            Mensagens.sucesso(mensagemLabel, linha.getDescricao() + " saiu da configuração do quarto "
                    + nomeDoTipo() + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível retirar o material. Tente novamente.");
        }
        atualizarConfiguracao();
    }

    private String nomeDoTipo() {
        return tipoCombo.getValue() == null ? "" : tipoCombo.getValue().name().toLowerCase();
    }

    private void atualizarMateriais() {
        try {
            materiaisTabela.getItems().setAll(configuracaoBLL.listarMateriais(Sessao.getUtilizador()));
        } catch (IllegalStateException e) {
            materiaisTabela.getItems().clear();
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            materiaisTabela.getItems().clear();
            Mensagens.erro(mensagemLabel, "Não foi possível carregar os materiais. Tente novamente.");
        }
    }

    private void atualizarConfiguracao() {
        try {
            configuracaoTabela.getItems().setAll(configuracaoBLL.configuracao(Sessao.getUtilizador(),
                    tipoCombo.getValue()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            configuracaoTabela.getItems().clear();
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            configuracaoTabela.getItems().clear();
            Mensagens.erro(mensagemLabel, "Não foi possível carregar a configuração. Tente novamente.");
        }
    }
}
