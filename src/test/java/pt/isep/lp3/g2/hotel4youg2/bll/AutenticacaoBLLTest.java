package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe AutenticacaoBLL.
 */
public class AutenticacaoBLLTest {

    /** Repositório de teste: guarda as contas em memória em vez de ir à base de dados. */
    private static class UtilizadorDALFalso implements IUtilizadorDAL {
        private List<Utilizador> contas = new ArrayList<>();

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
            return false;
        }

        @Override
        public boolean definirPassword(int utilizadorId, String passwordHash, String passwordSalt,
                                       boolean mudarPassword) {
            for (Utilizador conta : contas) {
                if (conta.getId() == utilizadorId) {
                    conta.definirPassword(passwordHash, passwordSalt, mudarPassword);
                    return true;
                }
            }
            return false;
        }
    }

    private UtilizadorDALFalso dal;
    private PasswordUtils passwordUtils;
    private AutenticacaoBLL instance;
    private Utilizador gestor;
    private Utilizador desativada;

    @BeforeEach
    public void setUp() {
        dal = new UtilizadorDALFalso();
        passwordUtils = new PasswordUtils();
        gestor = criarConta(1, "gestor", "Gestor#2026", Perfil.GESTOR, true);
        desativada = criarConta(2, "antigo", "Antigo#2026", Perfil.RECECIONISTA, false);
        dal.contas.add(gestor);
        dal.contas.add(desativada);
        instance = new AutenticacaoBLL(dal, passwordUtils);
    }

    @AfterEach
    public void tearDown() {
        dal = null;
        instance = null;
        gestor = null;
        desativada = null;
    }

    // cria uma conta com a password guardada com hash e salt, como na base de dados
    private Utilizador criarConta(int id, String nome, String password, Perfil perfil, boolean ativo) {
        String salt = passwordUtils.gerarSalt();
        return new Utilizador(id, id, nome, passwordUtils.hashPassword(password, salt), salt, perfil, ativo);
    }

    // devolve a mensagem de erro ao autenticar, ou null se a autenticação correu bem
    private String mensagemAoAutenticar(String nomeUtilizador, String password) {
        try {
            instance.autenticar(nomeUtilizador, password);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testAutenticar() {
        System.out.println("autenticar");
        Utilizador expResult = gestor;
        Utilizador result = instance.autenticar("gestor", "Gestor#2026");
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarIgnoraEspacos() {
        System.out.println("autenticar (espaços à volta do nome de utilizador)");
        Utilizador expResult = gestor;
        Utilizador result = instance.autenticar("  gestor ", "Gestor#2026");
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarNomeVazio() {
        System.out.println("autenticar (nome de utilizador vazio)");
        String expResult = "Escreva o nome de utilizador.";
        String result = mensagemAoAutenticar("", "Gestor#2026");
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarSemNome() {
        System.out.println("autenticar (sem nome de utilizador)");
        String expResult = "Escreva o nome de utilizador.";
        String result = mensagemAoAutenticar(null, "Gestor#2026");
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarPasswordVazia() {
        System.out.println("autenticar (password vazia)");
        String expResult = "Escreva a password.";
        String result = mensagemAoAutenticar("gestor", "");
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarSemPassword() {
        System.out.println("autenticar (sem password)");
        String expResult = "Escreva a password.";
        String result = mensagemAoAutenticar("gestor", null);
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarContaInexistente() {
        System.out.println("autenticar (a conta não existe)");
        String expResult = "Não existe nenhuma conta com o nome de utilizador \"naoexiste\".";
        String result = mensagemAoAutenticar("naoexiste", "Gestor#2026");
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarPasswordErrada() {
        System.out.println("autenticar (password errada)");
        String expResult = "A password está errada.";
        String result = mensagemAoAutenticar("gestor", "errada");
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarContaDesativada() {
        System.out.println("autenticar (conta desativada)");
        String expResult = "Esta conta está desativada. Contacte o gestor.";
        String result = mensagemAoAutenticar("antigo", "Antigo#2026");
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarPasswordErradaEmContaDesativada() {
        System.out.println("autenticar (password errada numa conta desativada)");
        String expResult = "A password está errada.";
        String result = mensagemAoAutenticar("antigo", "errada");
        assertEquals(expResult, result);
    }

    private Utilizador comPasswordReposta() {
        Utilizador conta = criarConta(3, "rita", "Temporaria1", Perfil.RECECIONISTA, true);
        conta.setMudarPassword(true);
        dal.contas.add(conta);
        return conta;
    }

    private String erroAoMudar(Utilizador conta, String nova, String confirmacao) {
        try {
            instance.mudarPassword(conta, nova, confirmacao);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testAutenticarComPasswordReposta() {
        System.out.println("autenticar (a conta com a palavra-passe reposta tem de a mudar)");
        comPasswordReposta();
        boolean expResult = true;
        boolean result = instance.autenticar("rita", "Temporaria1").isMudarPassword();
        assertEquals(expResult, result);
    }

    @Test
    public void testMudarPassword() {
        System.out.println("mudarPassword (depois entra com a nova e já não tem de mudar)");
        Utilizador conta = comPasswordReposta();
        instance.mudarPassword(conta, "NovaPassword9", "NovaPassword9");
        Utilizador expResult = conta;
        Utilizador result = instance.autenticar("rita", "NovaPassword9");
        assertEquals(expResult, result);
        assertEquals(false, result.isMudarPassword());
    }

    @Test
    public void testMudarPasswordATemporariaDeixaDeEntrar() {
        System.out.println("mudarPassword (a temporária deixa de funcionar)");
        Utilizador conta = comPasswordReposta();
        instance.mudarPassword(conta, "NovaPassword9", "NovaPassword9");
        String expResult = "A password está errada.";
        String result;
        try {
            instance.autenticar("rita", "Temporaria1");
            result = null;
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testMudarPasswordCurta() {
        System.out.println("mudarPassword (menos de 8 caracteres)");
        Utilizador conta = comPasswordReposta();
        String expResult = "A palavra-passe tem de ter pelo menos 8 caracteres.";
        String result = erroAoMudar(conta, "curta", "curta");
        assertEquals(expResult, result);
        assertEquals(true, conta.isMudarPassword());
    }

    @Test
    public void testMudarPasswordDiferentes() {
        System.out.println("mudarPassword (as duas não são iguais)");
        Utilizador conta = comPasswordReposta();
        String expResult = "As duas palavras-passe não são iguais.";
        String result = erroAoMudar(conta, "NovaPassword9", "NovaPassword8");
        assertEquals(expResult, result);
    }

    @Test
    public void testMudarPasswordIgualATemporaria() {
        System.out.println("mudarPassword (igual à temporária)");
        Utilizador conta = comPasswordReposta();
        String expResult = "Escolha uma palavra-passe diferente da temporária.";
        String result = erroAoMudar(conta, "Temporaria1", "Temporaria1");
        assertEquals(expResult, result);
    }

    @Test
    public void testMudarPasswordSemReposicao() {
        System.out.println("mudarPassword (a conta não teve a palavra-passe reposta)");
        String expResult = "A palavra-passe desta conta não foi reposta.";
        String result = erroAoMudar(gestor, "NovaPassword9", "NovaPassword9");
        assertEquals(expResult, result);
    }

    @Test
    public void testMudarPasswordSemConta() {
        System.out.println("mudarPassword (sem conta)");
        String expResult = "Entre com a palavra-passe temporária.";
        String result = erroAoMudar(null, "NovaPassword9", "NovaPassword9");
        assertEquals(expResult, result);
    }
}
