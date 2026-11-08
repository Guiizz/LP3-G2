package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IFuncionarioDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Testes unitários da classe FuncionarioBLL.
 */
public class FuncionarioBLLTest {

    /** Repositório de teste: guarda a conta em memória em vez de ir à base de dados. */
    private static class FuncionarioDALFalso implements IFuncionarioDAL {
        private boolean nifExiste = false;
        private boolean nomeExiste = false;
        private int contasCriadas = 0;
        private Funcionario funcionarioGravado;
        private Utilizador contaGravada;

        @Override
        public boolean existeNif(String nif) {
            return nifExiste;
        }

        @Override
        public boolean existeNomeUtilizador(String nomeUtilizador) {
            return nomeExiste;
        }

        @Override
        public void criarConta(Funcionario funcionario, Utilizador utilizador) {
            contasCriadas++;
            funcionarioGravado = funcionario;
            contaGravada = utilizador;
        }
    }

    private FuncionarioDALFalso dal;
    private PasswordUtils passwordUtils;
    private FuncionarioBLL instance;
    private Utilizador gestor;
    private Funcionario dados;

    @BeforeEach
    public void setUp() {
        dal = new FuncionarioDALFalso();
        passwordUtils = new PasswordUtils();
        instance = new FuncionarioBLL(dal, passwordUtils);
        gestor = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
        dados = new Funcionario(0, "Ana Silva", null, "123456789", null, null, null, null, 0, null);
    }

    @AfterEach
    public void tearDown() {
        dal = null;
        instance = null;
        dados = null;
    }

    // devolve a mensagem de erro ao criar a conta, ou null se a conta foi criada
    private String mensagemAoCriar(Utilizador quemAge, Funcionario funcionario, String nomeUtilizador,
                                   String password, Perfil perfil) {
        try {
            instance.criarConta(quemAge, funcionario, nomeUtilizador, password, perfil);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testCriarContaGravaOFuncionario() {
        System.out.println("criarConta (grava o funcionário)");
        Funcionario expResult = new Funcionario(0, "Ana Silva", null, "123456789", null, null, null, null, 0,
                LocalDate.now());
        instance.criarConta(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        Funcionario result = dal.funcionarioGravado;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaGravaAConta() {
        System.out.println("criarConta (grava a conta de acesso)");
        instance.criarConta(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        Utilizador result = dal.contaGravada;
        Utilizador expResult = new Utilizador(0, 0, "ana.silva",
                passwordUtils.hashPassword("password123", result.getPasswordSalt()), result.getPasswordSalt(),
                Perfil.RECECIONISTA, true);
        assertEquals(expResult, result);
        assertEquals(1, dal.contasCriadas);
    }

    @Test
    public void testCriarContaPasswordNaoFicaEmTexto() {
        System.out.println("criarConta (password guardada com hash e salt)");
        instance.criarConta(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        Utilizador conta = dal.contaGravada;
        assertNotEquals("password123", conta.getPasswordHash());
        assertEquals(64, conta.getPasswordHash().length());
        assertEquals(32, conta.getPasswordSalt().length());
    }

    @Test
    public void testCriarContaPasswordConfere() {
        System.out.println("criarConta (a password confere com o hash)");
        instance.criarConta(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        Utilizador conta = dal.contaGravada;
        boolean expResult = true;
        boolean result = passwordUtils.verificarPassword("password123", conta.getPasswordSalt(),
                conta.getPasswordHash());
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaCamposOpcionaisVaziosFicamNulos() {
        System.out.println("criarConta (campos opcionais vazios ficam nulos)");
        Funcionario comVazios = new Funcionario(0, "Ana Silva", "  ", "123456789", "", " ", null, "", 0, null);
        Funcionario expResult = new Funcionario(0, "Ana Silva", null, "123456789", null, null, null, null, 0,
                LocalDate.now());
        instance.criarConta(gestor, comVazios, "ana.silva", "password123", Perfil.RECECIONISTA);
        Funcionario result = dal.funcionarioGravado;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaComoRececionista() {
        System.out.println("criarConta (rececionista não pode)");
        Utilizador rececionista = new Utilizador(2, 2, "rece", "h", "s", Perfil.RECECIONISTA, true);
        String expResult = "Só o gestor pode criar contas.";
        String result = mensagemAoCriar(rececionista, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaSemUtilizador() {
        System.out.println("criarConta (sem utilizador com sessão)");
        String expResult = "Só o gestor pode criar contas.";
        String result = mensagemAoCriar(null, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaSemDados() {
        System.out.println("criarConta (sem dados do funcionário)");
        String expResult = "Faltam os dados do funcionário.";
        String result = mensagemAoCriar(gestor, null, "ana.silva", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaSemNome() {
        System.out.println("criarConta (nome vazio)");
        dados.setNome("  ");
        String expResult = "O nome é obrigatório (até 100 caracteres).";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaNifCurto() {
        System.out.println("criarConta (NIF com poucos algarismos)");
        dados.setNif("12345");
        String expResult = "O NIF tem de ter 9 algarismos.";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaNifComLetra() {
        System.out.println("criarConta (NIF com letra)");
        dados.setNif("12345678A");
        String expResult = "O NIF tem de ter 9 algarismos.";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaNifRepetido() {
        System.out.println("criarConta (NIF repetido)");
        dal.nifExiste = true;
        String expResult = "Já existe uma pessoa com esse NIF.";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaNomeDeUtilizadorRepetido() {
        System.out.println("criarConta (nome de utilizador repetido)");
        dal.nomeExiste = true;
        String expResult = "Esse nome de utilizador já existe.";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaNomeDeUtilizadorCurto() {
        System.out.println("criarConta (nome de utilizador curto)");
        String expResult = "O nome de utilizador tem de ter entre 3 e 50 caracteres.";
        String result = mensagemAoCriar(gestor, dados, "an", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaPasswordCurta() {
        System.out.println("criarConta (password curta)");
        String expResult = "A password tem de ter pelo menos 8 caracteres.";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "1234567", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaSemPerfil() {
        System.out.println("criarConta (sem perfil)");
        String expResult = "Escolha o perfil.";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaEmailInvalido() {
        System.out.println("criarConta (email inválido)");
        dados.setEmail("sem-arroba");
        String expResult = "O email não é válido.";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }

    @Test
    public void testCriarContaNascimentoNoFuturo() {
        System.out.println("criarConta (data de nascimento no futuro)");
        dados.setDataNascimento(LocalDate.now().plusDays(1));
        String expResult = "A data de nascimento não pode ser no futuro.";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        assertEquals(expResult, result);
        assertEquals(0, dal.contasCriadas);
    }
}
