package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import pt.isep.lp3.g2.hotel4youg2.bll.HorarioReservaBLL;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;
import pt.isep.lp3.g2.hotel4youg2.util.Sessao;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class HorarioReservaController {

    private static final String ERRO_BD = "Não foi possível comunicar com a base de dados. Tente novamente.";

    private final HorarioReservaBLL horarioService = new HorarioReservaBLL();

    @FXML
    private TextField horaCheckinField;
    @FXML
    private TextField horaCheckoutField;
    @FXML
    private TextField horaEarlyField;
    @FXML
    private TextField horaLateField;
    @FXML
    private TextField taxaEarlyField;
    @FXML
    private TextField taxaLateField;
    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        carregar();
    }

    @FXML
    private void aoGuardar() {
        try {
            HorarioReserva novo = new HorarioReserva(
                    hora(horaCheckinField), hora(horaCheckoutField),
                    hora(horaEarlyField), hora(horaLateField),
                    valor(taxaEarlyField), valor(taxaLateField));
            horarioService.alterar(Sessao.getUtilizador(), novo);
            mostrarSucesso("Horas e taxas guardadas.");
            carregar();
        } catch (DateTimeParseException e) {
            mostrarErro("Escreva as horas no formato HH:mm (por exemplo, 15:00)");
        } catch (NumberFormatException e) {
            mostrarErro("Escreva as taxas como números (por exemplo, 20.00)");
        } catch (DataAccessException e) {
            mostrarErro(ERRO_BD);
        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }
    }

    private void carregar() {
        try {
            HorarioReserva horario = horarioService.obter();
            horaCheckinField.setText(horario.getHoraCheckin().toString());
            horaCheckoutField.setText(horario.getHoraCheckout().toString());
            horaEarlyField.setText(horario.getHoraEarlyCheckin().toString());
            horaLateField.setText(horario.getHoraLateCheckout().toString());
            taxaEarlyField.setText(horario.getTaxaEarlyCheckin().toPlainString());
            taxaLateField.setText(horario.getTaxaLateCheckout().toPlainString());
        } catch (DataAccessException e) {
            mostrarErro(ERRO_BD);
        }
    }

    private LocalTime hora(TextField campo) {
        return LocalTime.parse(campo.getText().trim());
    }

    private BigDecimal valor(TextField campo) {
        return new BigDecimal(campo.getText().trim().replace(',', '.'));
    }

    private void mostrarErro(String texto) {
        Mensagens.erro(mensagemLabel, texto);
    }

    private void mostrarSucesso(String texto) {
        Mensagens.sucesso(mensagemLabel, texto);
    }
}
