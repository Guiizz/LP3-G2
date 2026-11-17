package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.AcessoCartaoBLL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.CartaoAtivo;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controlador do ecrã "Registos de acesso" (US41 e US74): o gestor vê todos os acessos com cartão e filtra-os
 * por cartão, por quarto, por local e por dia.
 */
public class ConsultarAcessosController {

    private final AcessoCartaoBLL acessoCartaoBLL = new AcessoCartaoBLL();

    @FXML
    private TextField cartaoField;
    @FXML
    private TextField quartoField;
    @FXML
    private ComboBox<LocalHotel> localCombo;
    @FXML
    private DatePicker diaPicker;
    @FXML
    private TableView<AcessoCartao> tabela;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        localCombo.getItems().setAll(LocalHotel.values());
        aoFiltrar();
    }

    @FXML
    private void aoFiltrar() {
        tabela.getItems().clear();
        try {
            mostrar(acessoCartaoBLL.consultar(Sessao.getUtilizador(), cartaoField.getText(), quartoField.getText(),
                    localCombo.getValue(), diaEscolhido()));
        } catch (DateTimeParseException e) {
            Mensagens.erro(mensagemLabel, "O dia não é válido. Escolha-o no calendário.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível consultar os registos. Tente novamente.");
        }
    }

    @FXML
    private void aoEscolherCartao() {
        try {
            CartaoAtivo escolhido = EscolherPessoaController.escolher("Escolher cartão",
                    acessoCartaoBLL.listarCartoesUsados(Sessao.getUtilizador()));
            if (escolhido != null) {
                cartaoField.setText(escolhido.getCartao());
                aoFiltrar();
            }
        } catch (IllegalStateException e) {
            Mensagens.erro(mensagemLabel, e.getMessage());
        } catch (IOException | DataAccessException e) {
            Mensagens.erro(mensagemLabel, "Não foi possível abrir a lista de cartões. Tente novamente.");
        }
    }

    @FXML
    private void aoLimpar() {
        cartaoField.clear();
        quartoField.clear();
        localCombo.setValue(null);
        diaPicker.setValue(null);
        diaPicker.getEditor().clear();
        aoFiltrar();
    }

    private LocalDate diaEscolhido() {
        String texto = diaPicker.getEditor().getText();
        if (texto == null || texto.isBlank()) {
            diaPicker.setValue(null);
            return null;
        }
        LocalDate dia = diaPicker.getConverter().fromString(texto.trim());
        diaPicker.setValue(dia);
        return dia;
    }

    private void mostrar(List<AcessoCartao> acessos) {
        tabela.getItems().setAll(acessos);
        if (acessos.isEmpty()) {
            Mensagens.info(mensagemLabel, "Não há registos de acesso com estes filtros.");
        } else {
            Mensagens.info(mensagemLabel, acessos.size() + " registo(s), do mais recente para o mais antigo.");
        }
    }
}
