package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.FuncionarioBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;

/**
 * Controlador do ecrã "Editar funcionário": o gestor muda o nome, os contactos e o perfil.
 */
public class EditarFuncionarioController {

    private final FuncionarioBLL funcionarioBLL = new FuncionarioBLL();
    private Utilizador conta;
    private Funcionario funcionario;

    @FXML
    private Label tituloLabel;
    @FXML
    private TextField campoNome;
    @FXML
    private TextField campoTelemovel;
    @FXML
    private TextField campoEmail;
    @FXML
    private TextField campoMorada;
    @FXML
    private ComboBox<Perfil> campoPerfil;
    @FXML
    private Label empresaLabel;
    @FXML
    private TextField campoEmpresa;
    @FXML
    private Label mensagemLabel;

    /**
     * Abre a edição do funcionário de uma conta.
     *
     * @param conta a conta escolhida
     * @throws IOException se não for possível ler o FXML
     */
    public static void abrir(Utilizador conta) throws IOException {
        FXMLLoader loader = Navegador.carregar("editar-funcionario-view.fxml");
        EditarFuncionarioController controller = loader.getController();
        controller.mostrar(conta);
        Navegador.abrirFormulario((Parent) loader.getRoot(), "Editar funcionário");
    }

    @FXML
    private void initialize() {
        campoPerfil.getItems().setAll(Perfil.values());
        aoEscolherPerfil();
    }

    private void mostrar(Utilizador contaEscolhida) {
        conta = contaEscolhida;
        tituloLabel.setText("Editar a conta " + conta.getNomeUtilizador());
        try {
            funcionario = funcionarioBLL.procurarDaConta(Sessao.getUtilizador(), conta);
            campoNome.setText(funcionario.getNome());
            campoTelemovel.setText(funcionario.getTelemovel());
            campoEmail.setText(funcionario.getEmail());
            campoMorada.setText(funcionario.getMorada());
            campoEmpresa.setText(funcionario.getEmpresa());
            campoPerfil.setValue(conta.getPerfil());
            aoEscolherPerfil();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível carregar os dados do funcionário. Tente novamente.");
        }
    }

    @FXML
    private void aoEscolherPerfil() {
        boolean restauracao = campoPerfil.getValue() == Perfil.RESTAURACAO;
        empresaLabel.setVisible(restauracao);
        empresaLabel.setManaged(restauracao);
        campoEmpresa.setVisible(restauracao);
        campoEmpresa.setManaged(restauracao);
    }

    @FXML
    private void aoGuardar() {
        if (funcionario == null) {
            Mensagens.erro(mensagemLabel, "Não há dados do funcionário para guardar.");
            return;
        }
        Funcionario dados = new Funcionario(funcionario.getId(), campoNome.getText(), funcionario.getCc(),
                funcionario.getNif(), campoMorada.getText(), campoTelemovel.getText(),
                funcionario.getDataNascimento(), campoEmail.getText(), funcionario.getNumeroFuncionario(),
                funcionario.getDataAdmissao());
        dados.setEmpresa(campoEmpresa.getText());
        try {
            funcionarioBLL.editar(Sessao.getUtilizador(), conta, dados, campoPerfil.getValue());
            Mensagens.sucesso(mensagemLabel, "Dados guardados.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível guardar. Tente novamente.");
        }
    }

    @FXML
    private void aoFechar() {
        Navegador.fecharFormulario(campoNome);
    }
}
