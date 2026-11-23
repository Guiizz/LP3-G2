package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import pt.isep.lp3.g2.hotel4youg2.bll.MaterialReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;

/**
 * Controlador do ecrã "Materiais da reserva" (US76): mostra os materiais que a reserva leva (os da configuração do
 * tipo do quarto ou os ajustados) e deixa o rececionista mudar as quantidades antes do check-in, só para essa
 * reserva. Depois do check-in, a lista fica só para consulta.
 */
public class MateriaisReservaController {

    private final MaterialReservaBLL materialReservaBLL = new MaterialReservaBLL();
    private Reserva reserva;

    @FXML
    private Label tituloLabel;
    @FXML
    private Label infoLabel;
    @FXML
    private TableView<MaterialQuarto> tabela;
    @FXML
    private HBox ajustarBox;
    @FXML
    private TextField quantidadeField;
    @FXML
    private Label mensagemLabel;

    /**
     * Abre os materiais da reserva no lugar dos detalhes. O botão Voltar regressa aos detalhes.
     *
     * @param reserva         a reserva escolhida
     * @param ecraDosDetalhes o ecrã dos detalhes da reserva que está aberto (fica guardado para voltar)
     * @throws IOException se não for possível ler o FXML
     */
    public static void abrir(Reserva reserva, Node ecraDosDetalhes) throws IOException {
        FXMLLoader loader = Navegador.carregar("materiais-reserva-view.fxml");
        MateriaisReservaController controlador = loader.getController();
        controlador.reserva = reserva;
        controlador.mostrar();
        Navegador.mostrarDetalhe(ecraDosDetalhes, (Parent) loader.getRoot());
    }

    private void mostrar() {
        tituloLabel.setText("Materiais da reserva n.º " + reserva.getId() + " (quarto " + reserva.getQuartoNumero()
                + ")");
        boolean podeAjustar = reserva.podeSerAlterada();
        ajustarBox.setVisible(podeAjustar);
        ajustarBox.setManaged(podeAjustar);
        if (podeAjustar) {
            infoLabel.setText("No check-in, os consumíveis saem do stock de vez e os não consumíveis saem até ao "
                    + "check-out. Ajustar aqui só muda esta reserva.");
        } else {
            infoLabel.setText("A reserva já não está por fazer: os materiais são só para consulta.");
        }
        atualizar();
    }

    private void atualizar() {
        try {
            tabela.getItems().setAll(materialReservaBLL.listar(Sessao.getUtilizador(), reserva));
        } catch (IllegalArgumentException | IllegalStateException e) {
            tabela.getItems().clear();
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            tabela.getItems().clear();
            Mensagens.erro(mensagemLabel, "Não foi possível carregar os materiais da reserva. Tente novamente.");
        }
    }

    @FXML
    private void aoAlterarQuantidade() {
        MaterialQuarto linha = tabela.getSelectionModel().getSelectedItem();
        try {
            materialReservaBLL.ajustar(Sessao.getUtilizador(), reserva, linha, quantidadeField.getText());
            Mensagens.sucesso(mensagemLabel, "Esta reserva passa a levar " + quantidadeField.getText().trim()
                    + " × " + linha.getDescricao() + ".");
            quantidadeField.clear();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível ajustar o material. Tente novamente.");
        }
        atualizar();
    }

    @FXML
    private void aoVoltar() {
        Navegador.voltar();
    }
}
