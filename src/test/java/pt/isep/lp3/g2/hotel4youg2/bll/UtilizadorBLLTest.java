package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe UtilizadorBLL (US22).
 */
public class UtilizadorBLLTest {

    /** Repositório de teste: guarda as contas em memória em vez de ir à base de dados. */
    private static class UtilizadorDALFalso implements IUtilizadorDAL {
        private List<Utilizador> contas = new ArrayList<>();
        private int alteracoes = 0;

        @Override
        public Optional<Utilizador> procurarPorNomeUtilizador(String nomeUtilizador) {
            for (Utilizador conta : contas) {
                if (conta.getNomeUtilizador().equals(nomeUtilizador)) {
                    return Optional.of(conta);
                }
            }
            return Optional.empty();
        }

        @Override
        public List<Utilizador> listar() {
            return contas;
        }

        @Override
        public boolean atualizarAtivo(int utilizadorId, boolean ativo) {
            for (Utilizador conta : contas) {
                if (conta.getId() == utilizadorId) {
                    conta.setAtivo(ativo);
                    alteracoes++;
                    return true;
                }
            }
            return false;
        }
    }

    private UtilizadorDALFalso dal;
    private UtilizadorBLL instance;
    private Utilizador gestor;
    private Utilizador rececionista;
    private Utilizador desativada;

    @BeforeEach
    public void setUp() {
        dal = new UtilizadorDALFalso();
        gestor = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
        rececionista = new Utilizador(2, 2, "rece", "h", "s", Perfil.RECECIONISTA, true);
        desativada = new Utilizador(3, 3, "antiga", "h", "s", Perfil.RECECIONISTA, false);
        dal.contas.add(gestor);
        dal.contas.add(rececionista);
        dal.contas.add(desativada);
        instance = new UtilizadorBLL(dal);
    }

    @AfterEach
    public void tearDown() {
        dal = null;
        instance = null;
    }

    // devolve a mensagem de erro ao desativar a conta, ou null se a conta foi desativada
    private String mensagemAoDesativar(Utilizador quemAge, Utilizador conta) {
        try {
            instance.desativar(quemAge, conta);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    // devolve a mensagem de erro ao reativar a conta, ou null se a conta foi reativada
    private String mensagemAoReativar(Utilizador quemAge, Utilizador conta) {
        try {
            instance.reativar(quemAge, conta);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testListar() {
        System.out.println("listar");
        List<Utilizador> expResult = new ArrayList<>();
        expResult.add(new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true));
        expResult.add(new Utilizador(2, 2, "rece", "h", "s", Perfil.RECECIONISTA, true));
        expResult.add(new Utilizador(3, 3, "antiga", "h", "s", Perfil.RECECIONISTA, false));
        List<Utilizador> result = instance.listar();
        assertEquals(expResult, result);
    }

    @Test
    public void testDesativar() {
        System.out.println("desativar");
        Utilizador expResult = new Utilizador(2, 2, "rece", "h", "s", Perfil.RECECIONISTA, false);
        instance.desativar(gestor, rececionista);
        Utilizador result = dal.contas.get(1);
        assertEquals(expResult, result);
        assertEquals(1, dal.alteracoes);
    }

    @Test
    public void testDesativarNaoMexeNasOutrasContas() {
        System.out.println("desativar (as outras contas ficam iguais)");
        Utilizador expResult = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
        instance.desativar(gestor, rececionista);
        Utilizador result = dal.contas.get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testReativar() {
        System.out.println("reativar");
        Utilizador expResult = new Utilizador(3, 3, "antiga", "h", "s", Perfil.RECECIONISTA, true);
        instance.reativar(gestor, desativada);
        Utilizador result = dal.contas.get(2);
        assertEquals(expResult, result);
        assertEquals(1, dal.alteracoes);
    }

    @Test
    public void testDesativarComoRececionista() {
        System.out.println("desativar (rececionista não pode)");
        String expResult = "Só o gestor pode gerir contas.";
        String result = mensagemAoDesativar(rececionista, desativada);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testReativarComoRececionista() {
        System.out.println("reativar (rececionista não pode)");
        String expResult = "Só o gestor pode gerir contas.";
        String result = mensagemAoReativar(rececionista, desativada);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testDesativarSemUtilizador() {
        System.out.println("desativar (sem utilizador com sessão)");
        String expResult = "Só o gestor pode gerir contas.";
        String result = mensagemAoDesativar(null, rececionista);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testDesativarASiProprio() {
        System.out.println("desativar (a própria conta)");
        String expResult = "Não pode desativar a sua própria conta.";
        String result = mensagemAoDesativar(gestor, gestor);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testDesativarContaInexistente() {
        System.out.println("desativar (a conta não existe)");
        Utilizador inexistente = new Utilizador(9, 9, "fantasma", "h", "s", Perfil.RECECIONISTA, true);
        String expResult = "A conta não existe.";
        String result = mensagemAoDesativar(gestor, inexistente);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testDesativarSemContaEscolhida() {
        System.out.println("desativar (sem conta escolhida)");
        String expResult = "Escolha uma conta.";
        String result = mensagemAoDesativar(gestor, null);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testReativarSemContaEscolhida() {
        System.out.println("reativar (sem conta escolhida)");
        String expResult = "Escolha uma conta.";
        String result = mensagemAoReativar(gestor, null);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }
}
