package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe Permissoes (US12).
 */
public class PermissoesTest {

    private Utilizador gestor;
    private Utilizador rececionista;

    @BeforeEach
    public void setUp() {
        gestor = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
        rececionista = new Utilizador(2, 2, "rece", "h", "s", Perfil.RECECIONISTA, true);
    }

    @AfterEach
    public void tearDown() {
        gestor = null;
        rececionista = null;
    }

    // devolve a mensagem de erro ao exigir o gestor, ou null se o utilizador pode continuar
    private String mensagemAoExigirGestor(Utilizador utilizador) {
        try {
            Permissoes.exigirGestor(utilizador);
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testEGestor() {
        System.out.println("eGestor");
        boolean expResult = true;
        boolean result = Permissoes.eGestor(gestor);
        assertEquals(expResult, result);
    }

    @Test
    public void testEGestorRececionista() {
        System.out.println("eGestor (rececionista)");
        boolean expResult = false;
        boolean result = Permissoes.eGestor(rececionista);
        assertEquals(expResult, result);
    }

    @Test
    public void testEGestorSemSessao() {
        System.out.println("eGestor (sem sessão)");
        boolean expResult = false;
        boolean result = Permissoes.eGestor(null);
        assertEquals(expResult, result);
    }

    @Test
    public void testExigirGestor() {
        System.out.println("exigirGestor");
        String expResult = null;
        String result = mensagemAoExigirGestor(gestor);
        assertEquals(expResult, result);
    }

    @Test
    public void testExigirGestorComoRececionista() {
        System.out.println("exigirGestor (rececionista não pode)");
        String expResult = "Só o gestor pode abrir este ecrã";
        String result = mensagemAoExigirGestor(rececionista);
        assertEquals(expResult, result);
    }

    @Test
    public void testExigirGestorSemSessao() {
        System.out.println("exigirGestor (sem sessão)");
        String expResult = "Só o gestor pode abrir este ecrã";
        String result = mensagemAoExigirGestor(null);
        assertEquals(expResult, result);
    }

    @Test
    public void testPodeGerirReservasGestor() {
        System.out.println("podeGerirReservas (gestor)");
        boolean expResult = true;
        boolean result = Permissoes.podeGerirReservas(gestor);
        assertEquals(expResult, result);
    }

    @Test
    public void testPodeGerirReservasRececionista() {
        System.out.println("podeGerirReservas (rececionista)");
        boolean expResult = true;
        boolean result = Permissoes.podeGerirReservas(rececionista);
        assertEquals(expResult, result);
    }

    @Test
    public void testPodeGerirReservasSemSessao() {
        System.out.println("podeGerirReservas (sem sessão)");
        boolean expResult = false;
        boolean result = Permissoes.podeGerirReservas(null);
        assertEquals(expResult, result);
    }
}
