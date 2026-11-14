package pt.isep.lp3.g2.hotel4youg2.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AreaControllerTest {

    private OpcaoMenu opcao;

    @BeforeEach
    public void setUp() {
        opcao = new OpcaoMenu("Gerir reservas", "gerir-reservas-view.fxml", false);
    }

    @AfterEach
    public void tearDown() {
        opcao = null;
    }

    @Test
    public void testTextoDoErroSemBaseDeDados() {
        System.out.println("textoDoErro (o ecrã não abriu porque a base de dados não respondeu)");
        IOException erro = new IOException("erro ao carregar o FXML",
                new DataAccessException("Não foi possível ligar à base de dados. Tente outra vez."));
        String expResult = "Não foi possível ligar à base de dados. Tente outra vez.";
        String result = AreaController.textoDoErro(opcao, erro);
        assertEquals(expResult, result);
    }

    @Test
    public void testTextoDoErroDoEcra() {
        System.out.println("textoDoErro (o ecrã não abriu por outro motivo)");
        IOException erro = new IOException("ficheiro FXML com erros");
        String expResult = "Não foi possível abrir Gerir reservas.";
        String result = AreaController.textoDoErro(opcao, erro);
        assertEquals(expResult, result);
    }
}
