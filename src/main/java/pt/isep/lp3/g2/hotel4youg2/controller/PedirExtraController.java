package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import pt.isep.lp3.g2.hotel4youg2.bll.PedidoExtraBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.PedidoExtra;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador do ecrã "Pedir extra": o cliente pede um serviço ou um material na reserva em curso.
 */
public class PedirExtraController {

    private static final String ERRO_BD = "Não foi possível comunicar com a base de dados. Tente novamente.";

    private final PedidoExtraBLL pedidoExtraBLL = new PedidoExtraBLL();
    private Reserva reserva;

    @FXML
    private Label reservaLabel;
    @FXML
    private RadioButton servicoRadio;
    @FXML
    private RadioButton materialRadio;
    @FXML
    private ComboBox<Servico> servicoCombo;
    @FXML
    private ComboBox<MaterialStock> materialCombo;
    @FXML
    private Spinner<Integer> quantidadeSpinner;
    @FXML
    private Label mensagemLabel;
    @FXML
    private TableView<PedidoExtra> tabela;

    /**
     * Abre o pedido de extras de uma reserva em curso.
     *
     * @param reserva     a reserva em curso
     * @param ecraDaLista o ecrã da lista, para onde se volta
     * @throws IOException se não for possível ler o FXML
     */
    public static void abrir(Reserva reserva, Node ecraDaLista) throws IOException {
        FXMLLoader loader = Navegador.carregar("pedir-extra-view.fxml");
        PedirExtraController controller = loader.getController();
        controller.reserva = reserva;
        controller.mostrar();
        Navegador.mostrarDetalhe(ecraDaLista, (Parent) loader.getRoot());
    }

    @FXML
    private void initialize() {
        servicoCombo.setConverter(new NomeDoServico());
        materialCombo.setConverter(new NomeDoMaterial());
        mostrarTipo();
    }

    private void mostrar() {
        reservaLabel.setText("Reserva n.º " + reserva.getId() + " · Quarto " + reserva.getQuartoNumero() + " · Cartão "
                + reserva.getCartao());
        try {
            servicoCombo.getItems().setAll(pedidoExtraBLL.listarServicos());
            materialCombo.getItems().setAll(pedidoExtraBLL.listarMateriais());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, ERRO_BD);
        }
        atualizarTabela();
    }

    @FXML
    private void aoMudarTipo() {
        mostrarTipo();
        mensagemLabel.setText("");
    }

    private void mostrarTipo() {
        boolean servico = servicoRadio.isSelected();
        servicoCombo.setVisible(servico);
        servicoCombo.setManaged(servico);
        materialCombo.setVisible(!servico);
        materialCombo.setManaged(!servico);
    }

    @FXML
    private void aoPedir() {
        try {
            PedidoExtra pedido;
            int quantidade = quantidadeSpinner.getValue();
            if (materialRadio.isSelected()) {
                pedido = pedidoExtraBLL.pedirMaterial(Sessao.getContaCliente(), reserva, materialCombo.getValue(),
                        quantidade);
            } else {
                pedido = pedidoExtraBLL.pedirServico(Sessao.getContaCliente(), reserva, servicoCombo.getValue(),
                        quantidade);
            }
            Mensagens.sucesso(mensagemLabel, "Pedido enviado: " + pedido.getQuantidade() + " x "
                    + pedido.getDescricao() + ". Fica pendente até a receção o aceitar ou recusar.");
            atualizarTabela();
        } catch (ValidacaoException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, ERRO_BD);
        }
    }

    @FXML
    private void aoAtualizar() {
        mensagemLabel.setText("");
        atualizarTabela();
    }

    private void atualizarTabela() {
        try {
            List<PedidoExtra> daReserva = new ArrayList<>();
            for (PedidoExtra pedido : pedidoExtraBLL.listarDoCliente(Sessao.getContaCliente())) {
                if (pedido.getReservaId() == reserva.getId()) {
                    daReserva.add(pedido);
                }
            }
            tabela.getItems().setAll(daReserva);
        } catch (DataAccessException | IllegalStateException e) {
            tabela.getItems().clear();
        }
    }

    @FXML
    private void aoVoltar() {
        Navegador.voltar();
    }

    private static class NomeDoServico extends StringConverter<Servico> {
        @Override
        public String toString(Servico servico) {
            return servico == null ? "" : servico.getDesignacao() + " · " + servico.getPrecoTexto();
        }

        @Override
        public Servico fromString(String texto) {
            return null;
        }
    }

    private static class NomeDoMaterial extends StringConverter<MaterialStock> {
        @Override
        public String toString(MaterialStock material) {
            return material == null ? "" : material.getDescricao();
        }

        @Override
        public MaterialStock fromString(String texto) {
            return null;
        }
    }
}
