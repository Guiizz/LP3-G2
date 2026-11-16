package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Controlador do ecrã que mostra uma entrega como uma fatura: dados do fornecedor e da encomenda,
 * uma linha por material e os totais. As contas que não batem certo ficam a vermelho (US54).
 */
public class FaturaEntregaController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CLASSE_ERRO = "linha-com-erro";

    @FXML
    private Label tituloLabel;
    @FXML
    private Label estadoLabel;
    @FXML
    private Label fornecedorLabel;
    @FXML
    private Label nifLabel;
    @FXML
    private Label moradaLabel;
    @FXML
    private Label encomendaLabel;
    @FXML
    private Label dataLabel;
    @FXML
    private Label moedaLabel;
    @FXML
    private TableView<LinhaEntrega> tabela;
    @FXML
    private Label totalSemIvaLabel;
    @FXML
    private Label totalIvaLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label avisoLabel;

    /**
     * Abre a fatura da entrega no lugar da lista, dentro da mesma área. O botão Voltar regressa à lista
     * tal como estava.
     *
     * @param entrega      a entrega completa, com as linhas
     * @param ecraDaLista  o ecrã da lista que está aberto (fica guardado para voltar)
     * @throws IOException se não for possível ler o FXML
     */
    public static void abrir(Entrega entrega, Node ecraDaLista) throws IOException {
        FXMLLoader loader = Navegador.carregar("fatura-entrega-view.fxml");
        FaturaEntregaController controlador = loader.getController();
        controlador.mostrar(entrega);
        Navegador.mostrarDetalhe(ecraDaLista, (Parent) loader.getRoot());
    }

    @FXML
    private void initialize() {
        tabela.setRowFactory(new CriarLinhas());
    }

    private void mostrar(Entrega entrega) {
        String moeda = entrega.getMoeda();
        tituloLabel.setText("Entrega " + entrega.getNumeroEncomenda());
        estadoLabel.setText(entrega.getEstado().name());
        fornecedorLabel.setText(entrega.getFornecedor().getNome());
        nifLabel.setText(entrega.getFornecedor().getCodigo());
        moradaLabel.setText(entrega.getFornecedor().getMoradaCompleta());
        encomendaLabel.setText(entrega.getNumeroEncomenda());
        dataLabel.setText(entrega.getDataEncomenda().format(DATA));
        moedaLabel.setText(moeda);
        tabela.getItems().setAll(entrega.getLinhas());

        totalSemIvaLabel.setText(LinhaEntrega.valor(entrega.getTotalSemIva()) + " " + moeda);
        totalIvaLabel.setText(LinhaEntrega.valor(entrega.getTotalIva()) + " " + moeda);
        totalLabel.setText(LinhaEntrega.valor(entrega.getTotal()) + " " + moeda);

        if (!entrega.isTotalCerto()) {
            totalLabel.getStyleClass().add("mensagem-erro");
            Mensagens.erro(avisoLabel, "O total guardado (" + LinhaEntrega.valor(entrega.getTotalRegistado()) + " "
                    + moeda + ") não bate certo com a soma das linhas.");
        } else if (!entrega.isContasCertas()) {
            Mensagens.erro(avisoLabel, "As linhas a vermelho têm contas que não batem certo "
                    + "(quantidade × preço, IVA ou total).");
        } else {
            avisoLabel.setText("");
        }
    }

    @FXML
    private void aoVoltar() {
        Navegador.voltar();
    }

    // diz à tabela como criar cada linha: uma linha que pinta a vermelho as contas erradas
    private static class CriarLinhas implements Callback<TableView<LinhaEntrega>, TableRow<LinhaEntrega>> {
        @Override
        public TableRow<LinhaEntrega> call(TableView<LinhaEntrega> tabela) {
            return new LinhaDaFatura();
        }
    }

    // a mesma linha da tabela é reutilizada para várias linhas da entrega, por isso a cor é sempre revista
    private static class LinhaDaFatura extends TableRow<LinhaEntrega> {
        @Override
        protected void updateItem(LinhaEntrega linha, boolean vazia) {
            super.updateItem(linha, vazia);
            getStyleClass().remove(CLASSE_ERRO);
            if (!vazia && linha != null && !linha.isContasCertas()) {
                getStyleClass().add(CLASSE_ERRO);
            }
        }
    }
}
