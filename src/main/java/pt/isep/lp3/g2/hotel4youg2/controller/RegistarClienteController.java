package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.ClienteBLL;
import pt.isep.lp3.g2.hotel4youg2.bll.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Controlador do ecrã de clientes: registar (US13), incluindo funcionários (US43), pesquisar (US14) e editar (US19).
 */
public class RegistarClienteController {

    private final ClienteBLL clienteService = new ClienteBLL();

    @FXML
    private TextField nomeField;
    @FXML
    private TextField nifField;
    @FXML
    private DatePicker dataNascimentoPicker;
    @FXML
    private TextField telemovelField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField ccField;
    @FXML
    private TextField moradaField;
    @FXML
    private Label mensagemLabel;
    @FXML
    private TextField pesquisaField;
    @FXML
    private ListView<Cliente> resultadosList;
    @FXML
    private Label tituloLabel;
    @FXML
    private Button registarButton;
    @FXML
    private Button cancelarButton;

    /** O cliente que está a ser editado, ou {@code null} quando o ecrã está em modo de registo. */
    private Cliente clienteEmEdicao;

    /** Mostra logo todos os clientes quando o ecrã abre. */
    @FXML
    private void initialize() {
        atualizarLista();
    }

    @FXML
    private void aoRegistar() {
        if (clienteEmEdicao != null) {
            guardarEdicao();
            return;
        }
        try {
            String funcionario = clienteService.nomeDoFuncionarioComNif(nifField.getText());
            if (funcionario != null && !confirmarFuncionario(funcionario)) {
                return;
            }
            Cliente cliente = clienteService.registar(
                    nomeField.getText(),
                    nifField.getText(),
                    dataNascimentoPicker.getValue(),
                    telemovelField.getText(),
                    emailField.getText(),
                    ccField.getText(),
                    moradaField.getText());
            mostrarMensagem("Cliente " + cliente.getNome() + " registado com sucesso", "green");
            limparCampos();
            atualizarLista();
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível guardar o cliente. Tente novamente.", "red");
        }
    }

    private boolean confirmarFuncionario(String nome) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Este NIF é do funcionário " + nome + ".\n"
                        + "Vai ficar também registado como cliente, e os dados pessoais dele passam a ser os deste "
                        + "formulário. A conta dele não muda. Continuar?",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Funcionário como cliente");
        alerta.setHeaderText(null);
        Optional<ButtonType> resposta = alerta.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.YES;
    }

    @FXML
    private void aoPesquisar() {
        try {
            List<Cliente> clientes = clienteService.pesquisar(pesquisaField.getText());
            resultadosList.getItems().setAll(clientes);
            if (clientes.isEmpty()) {
                mostrarMensagem("Nenhum cliente encontrado", "black");
            }
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível pesquisar. Tente novamente.", "red");
        }
    }

    /**
     * US19: carrega no formulário os dados do cliente escolhido na pesquisa,
     * com o NIF bloqueado, para os poder alterar.
     */
    @FXML
    private void aoEditarSelecionado() {
        Cliente cliente = resultadosList.getSelectionModel().getSelectedItem();
        if (cliente == null) {
            mostrarMensagem("Escolha um cliente na lista para editar", "red");
            return;
        }
        clienteEmEdicao = cliente;
        nomeField.setText(cliente.getNome());
        nifField.setText(cliente.getNif());
        nifField.setDisable(true);
        dataNascimentoPicker.setValue(cliente.getDataNascimento());
        telemovelField.setText(cliente.getTelemovel());
        emailField.setText(cliente.getEmail());
        ccField.setText(cliente.getCc());
        moradaField.setText(cliente.getMorada());

        tituloLabel.setText("Editar cliente");
        registarButton.setText("Guardar alterações");
        cancelarButton.setVisible(true);
        cancelarButton.setManaged(true);
        mostrarMensagem("A editar " + cliente.getNome() + " (o NIF não pode ser alterado)", "black");
    }

    @FXML
    private void aoCancelarEdicao() {
        sairDaEdicao();
        mostrarMensagem("", "black");
    }

    private void guardarEdicao() {
        try {
            Cliente cliente = clienteService.editar(
                    clienteEmEdicao,
                    nomeField.getText(),
                    dataNascimentoPicker.getValue(),
                    telemovelField.getText(),
                    emailField.getText(),
                    ccField.getText(),
                    moradaField.getText());
            sairDaEdicao();
            atualizarLista();
            mostrarMensagem("Dados de " + cliente.getNome() + " guardados com sucesso", "green");
        } catch (ValidacaoException e) {
            mostrarMensagem(e.getMessage(), "red");
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível guardar as alterações. Tente novamente.", "red");
        }
    }

    /**
     * Volta a carregar a lista com o texto que está na pesquisa
     * (com a pesquisa vazia, mostra todos os clientes).
     */
    private void atualizarLista() {
        try {
            resultadosList.getItems().setAll(clienteService.pesquisar(pesquisaField.getText()));
        } catch (DataAccessException e) {
            mostrarMensagem("Não foi possível carregar os clientes. Tente novamente.", "red");
        }
    }

    private void sairDaEdicao() {
        clienteEmEdicao = null;
        nifField.setDisable(false);
        tituloLabel.setText("Registar cliente");
        registarButton.setText("Registar");
        cancelarButton.setVisible(false);
        cancelarButton.setManaged(false);
        limparCampos();
    }

    private void mostrarMensagem(String texto, String cor) {
        mensagemLabel.setStyle("-fx-text-fill: " + cor + ";");
        mensagemLabel.setText(texto);
    }

    private void limparCampos() {
        nomeField.clear();
        nifField.clear();
        dataNascimentoPicker.setValue(null);
        telemovelField.clear();
        emailField.clear();
        ccField.clear();
        moradaField.clear();
    }

    /**
     * US14: abre o registo já com o NIF que não foi encontrado na pesquisa.
     *
     * @param nif o NIF a preencher
     */
    public void preencherNif(String nif) {
        nifField.setText(nif);
    }
}