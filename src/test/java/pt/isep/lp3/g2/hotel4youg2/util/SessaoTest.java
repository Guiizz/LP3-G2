package pt.isep.lp3.g2.hotel4youg2.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe Sessao.
 */
public class SessaoTest {

    private Utilizador gestor;
    private Utilizador rececionista;
    private Fornecedor fornecedor;

    @BeforeEach
    public void setUp() {
        Sessao.terminar();
        gestor = new Utilizador(1, 1, "gestor", "hash", "salt", Perfil.GESTOR, true);
        rececionista = new Utilizador(2, 2, "rececao", "hash", "salt", Perfil.RECECIONISTA, true);
        fornecedor = new Fornecedor(1, "F-1", "Fornecedor", null, null, null, null, null, null);
    }

    @AfterEach
    public void tearDown() {
        Sessao.terminar();
        gestor = null;
        rececionista = null;
        fornecedor = null;
    }

    @Test
    public void testSemLogin() {
        System.out.println("getUtilizador (sem login)");
        boolean expResult = false;
        boolean result = Sessao.getUtilizador() != null;
        assertEquals(expResult, result);
    }

    @Test
    public void testGetUtilizadorSemLogin() {
        System.out.println("getUtilizador (sem login)");
        Utilizador expResult = null;
        Utilizador result = Sessao.getUtilizador();
        assertEquals(expResult, result);
    }

    @Test
    public void testIniciar() {
        System.out.println("iniciar");
        Sessao.iniciar(gestor);
        Utilizador expResult = gestor;
        Utilizador result = Sessao.getUtilizador();
        assertEquals(expResult, result);
    }

    @Test
    public void testDepoisDoLogin() {
        System.out.println("getUtilizador (depois do login)");
        Sessao.iniciar(gestor);
        boolean expResult = true;
        boolean result = Sessao.getUtilizador() != null;
        assertEquals(expResult, result);
    }

    @Test
    public void testTerminar() {
        System.out.println("terminar (esquece o utilizador)");
        Sessao.iniciar(gestor);
        Sessao.terminar();
        Utilizador expResult = null;
        Utilizador result = Sessao.getUtilizador();
        assertEquals(expResult, result);
        assertEquals(false, Sessao.getUtilizador() != null);
    }

    @Test
    public void testTerminarSemSessaoIniciada() {
        System.out.println("terminar (sem sessão iniciada não dá erro)");
        Sessao.terminar();
        boolean expResult = false;
        boolean result = Sessao.getUtilizador() != null;
        assertEquals(expResult, result);
    }

    @Test
    public void testIniciarDepoisDeTerminar() {
        System.out.println("iniciar (outro utilizador pode entrar depois de terminar)");
        Sessao.iniciar(gestor);
        Sessao.terminar();
        Sessao.iniciar(rececionista);
        Utilizador expResult = rececionista;
        Utilizador result = Sessao.getUtilizador();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFornecedorSemLogin() {
        System.out.println("getFornecedor (sem login)");
        Fornecedor expResult = null;
        Fornecedor result = Sessao.getFornecedor();
        assertEquals(expResult, result);
    }

    @Test
    public void testIniciarFornecedor() {
        System.out.println("iniciarFornecedor");
        Sessao.iniciarFornecedor(fornecedor);
        Fornecedor expResult = fornecedor;
        Fornecedor result = Sessao.getFornecedor();
        assertEquals(expResult, result);
    }

    @Test
    public void testIniciarFornecedorNaoIniciaSessaoDeUtilizador() {
        System.out.println("iniciarFornecedor (não conta como utilizador com sessão)");
        Sessao.iniciarFornecedor(fornecedor);
        boolean expResult = false;
        boolean result = Sessao.getUtilizador() != null;
        assertEquals(expResult, result);
    }

    @Test
    public void testTerminarEsqueceOFornecedor() {
        System.out.println("terminar (esquece o fornecedor)");
        Sessao.iniciarFornecedor(fornecedor);
        Sessao.terminar();
        Fornecedor expResult = null;
        Fornecedor result = Sessao.getFornecedor();
        assertEquals(expResult, result);
    }
}
