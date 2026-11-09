package pt.isep.lp3.g2.hotel4youg2.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpcaoMenuTest {

    private OpcaoMenu ecra;
    private OpcaoMenu formulario;

    @BeforeEach
    public void setUp() {
        ecra = new OpcaoMenu("Gerir reservas", "gerir-reservas-view.fxml", false);
        formulario = new OpcaoMenu("Criar reserva", "criar-reserva-view.fxml", true);
    }

    @AfterEach
    public void tearDown() {
        ecra = null;
        formulario = null;
    }

    @Test
    public void testGetTexto() {
        System.out.println("getTexto");
        String expResult = "Gerir reservas";
        String result = ecra.getTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFicheiro() {
        System.out.println("getFicheiro");
        String expResult = "criar-reserva-view.fxml";
        String result = formulario.getFicheiro();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsFormularioEcraDaArea() {
        System.out.println("isFormulario (ecrã que abre dentro da área)");
        boolean result = ecra.isFormulario();
        assertFalse(result);
    }

    @Test
    public void testIsFormularioPopUp() {
        System.out.println("isFormulario (formulário que abre em pop-up)");
        boolean result = formulario.isFormulario();
        assertTrue(result);
    }

    @Test
    public void testFicheiroExiste() {
        System.out.println("o ficheiro da opção existe");
        String expResult = "/pt/isep/lp3/g2/hotel4youg2/view/gerir-reservas-view.fxml";
        String result = Navegador.localizar(ecra.getFicheiro()).getPath();
        assertTrue(result.endsWith(expResult));
    }
}
