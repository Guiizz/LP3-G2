package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.PedidoExtraBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.PedidoExtra;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

/**
 * Controlador do ecrã "Pedidos dos clientes": a receção aceita ou recusa os pedidos extra.
 */
public class PedidosExtraController implements AtualizadorDeTabelas.Ecra {

    private static final String ERRO_BD = "Não foi possível comunicar com a base de dados. Tente novamente.";
    private static final AtualizadorDeTabelas.Chave<PedidoExtra> PELO_ID = new IdDoPedido();

    private final PedidoExtraBLL pedidoExtraBLL = new PedidoExtraBLL();

    @FXML
    private TableView<PedidoExtra> tabela;
    @FXML
    private TextField motivoField;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        carregar();
        AtualizadorDeTabelas.ligar(tabela, this);
    }

    @Override
    public void recarregar() {
        carregar();
    }

    @FXML
    private void aoAceitar() {
        PedidoExtra pedido = tabela.getSelectionModel().getSelectedItem();
        try {
            pedidoExtraBLL.aceitar(Sessao.getUtilizador(), pedido);
            Mensagens.sucesso(mensagemLabel, textoDoAceite(pedido));
        } catch (ValidacaoException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, ERRO_BD);
        }
        carregar();
    }

    @FXML
    private void aoRecusar() {
        PedidoExtra pedido = tabela.getSelectionModel().getSelectedItem();
        try {
            pedidoExtraBLL.recusar(Sessao.getUtilizador(), pedido, motivoField.getText());
            Mensagens.sucesso(mensagemLabel, "Pedido recusado: " + pedido.getQuantidade() + " x "
                    + pedido.getDescricao() + " do quarto " + pedido.getQuartoNumero() + ".");
            motivoField.clear();
        } catch (ValidacaoException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, ERRO_BD);
        }
        carregar();
    }

    private String textoDoAceite(PedidoExtra pedido) {
        String inicio = "Pedido aceite: " + pedido.getQuantidade() + " x " + pedido.getDescricao() + " para o quarto "
                + pedido.getQuartoNumero();
        if (pedido.isServico()) {
            return inicio + ". O serviço entrou na reserva " + pedido.getReservaId() + ".";
        }
        if (pedido.isConsumivel()) {
            return inicio + ". Saída definitiva do stock.";
        }
        return inicio + ". Saída temporária do stock: volta no check-out.";
    }

    private void carregar() {
        try {
            AtualizadorDeTabelas.mostrar(tabela, pedidoExtraBLL.listarPendentes(Sessao.getUtilizador()), PELO_ID);
        } catch (IllegalStateException e) {
            tabela.getItems().clear();
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            tabela.getItems().clear();
            Mensagens.erro(mensagemLabel, ERRO_BD);
        }
    }

    private static class IdDoPedido implements AtualizadorDeTabelas.Chave<PedidoExtra> {
        @Override
        public Object de(PedidoExtra pedido) {
            return pedido.getId();
        }
    }
}
