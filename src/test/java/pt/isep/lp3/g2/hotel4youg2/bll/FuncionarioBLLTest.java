package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFuncionarioDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        @Override
        public Funcionario procurarPorNumero(int numeroFuncionario) {
            return null;
        }

        @Override
        public Funcionario procurarPorId(int pessoaId) {
            return guardado != null && guardado.getId() == pessoaId ? guardado : null;
        }

        private Funcionario guardado;
        private Funcionario editado;
        private int contaEditada;
        private Perfil perfilEditado;
        private boolean contaExiste = true;

        @Override
        public boolean editar(Funcionario funcionario, int utilizadorId, Perfil perfil) {
            if (!contaExiste) {
                return false;
            }
            editado = funcionario;
            contaEditada = utilizadorId;
            perfilEditado = perfil;
            return true;
        }

        @Override
        public boolean temContaAtiva(int pessoaId) {
            return false;
        }

        @Override
        public List<Funcionario> listarComContaAtiva() {
            return new ArrayList<>();
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
        Utilizador obtido = dal.contaGravada;
        Utilizador esperado = new Utilizador(0, 0, "ana.silva",
                passwordUtils.hashPassword("password123", obtido.getPasswordSalt()), obtido.getPasswordSalt(),
                Perfil.RECECIONISTA, true);
        List<Object> expResult = Arrays.asList(esperado, 1);
        List<Object> result = Arrays.asList(obtido, dal.contasCriadas);
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaPasswordNaoFicaEmTexto() {
        System.out.println("criarConta (password guardada com hash e salt)");
        instance.criarConta(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        Utilizador conta = dal.contaGravada;
        String expResult = "false / 64 / 32";
        String result = "password123".equals(conta.getPasswordHash())
                + " / "
                + conta.getPasswordHash().length()
                + " / "
                + conta.getPasswordSalt().length();
        assertEquals(expResult, result);
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
    public void testCriarContaRestauracaoGuardaAEmpresa() {
        System.out.println("criarConta (restauração, com o nome da empresa externa)");
        dados.setEmpresa("  Sabores do Norte Lda ");
        instance.criarConta(gestor, dados, "rita.sabores", "password123", Perfil.RESTAURACAO);
        Funcionario esperado = new Funcionario(0, "Ana Silva", null, "123456789", null, null, null, null, 0,
                LocalDate.now());
        esperado.setEmpresa("Sabores do Norte Lda");
        List<Object> expResult = Arrays.asList(esperado, Perfil.RESTAURACAO);
        List<Object> result = Arrays.asList(dal.funcionarioGravado, dal.contaGravada.getPerfil());
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaRececionistaSemEmpresa() {
        System.out.println("criarConta (a empresa só fica guardada na restauração)");
        dados.setEmpresa("Sabores do Norte Lda");
        instance.criarConta(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA);
        String expResult = null;
        String result = dal.funcionarioGravado.getEmpresa();
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarDaConta() {
        System.out.println("procurarDaConta (os dados do funcionário da conta)");
        dal.guardado = ana();
        Utilizador conta = new Utilizador(5, 7, "ana.silva", "h", "s", Perfil.RECECIONISTA, true);
        Funcionario expResult = ana();
        Funcionario result = instance.procurarDaConta(gestor, conta);
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarDaContaSemFuncionario() {
        System.out.println("procurarDaConta (o funcionário já não existe)");
        Utilizador conta = new Utilizador(5, 99, "fantasma", "h", "s", Perfil.RECECIONISTA, true);
        String expResult = "O funcionário desta conta já não existe.";
        String result;
        try {
            instance.procurarDaConta(gestor, conta);
            result = null;
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarGravaOsDados() {
        System.out.println("editar (muda o nome e os contactos)");
        Utilizador conta = new Utilizador(5, 7, "ana.silva", "h", "s", Perfil.RECECIONISTA, true);
        Funcionario novos = ana();
        novos.setNome("  Ana Sofia Silva ");
        novos.setTelemovel("930000000");
        novos.setEmail("ana.sofia@hotel.pt");
        novos.setMorada("Rua B");
        instance.editar(gestor, conta, novos, Perfil.RECECIONISTA);
        Funcionario esperado = new Funcionario(7, "Ana Sofia Silva", "12345678", "123456789", "Rua B",
                "930000000", LocalDate.of(1990, 5, 1), "ana.sofia@hotel.pt", 12, LocalDate.of(2026, 10, 1));
        List<Object> expResult = Arrays.asList(esperado, 5);
        List<Object> result = Arrays.asList(dal.editado, dal.contaEditada);
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarMudaOPerfil() {
        System.out.println("editar (muda o perfil da conta)");
        Utilizador conta = new Utilizador(5, 7, "ana.silva", "h", "s", Perfil.RECECIONISTA, true);
        instance.editar(gestor, conta, ana(), Perfil.GESTOR);
        String expResult = Perfil.GESTOR + " / " + Perfil.GESTOR;
        String result = conta.getPerfil() + " / " + dal.perfilEditado;
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarParaRestauracaoComEmpresa() {
        System.out.println("editar (passar para a restauração guarda a empresa)");
        Utilizador conta = new Utilizador(5, 7, "ana.silva", "h", "s", Perfil.RECECIONISTA, true);
        Funcionario novos = ana();
        novos.setEmpresa("Sabores do Norte Lda");
        instance.editar(gestor, conta, novos, Perfil.RESTAURACAO);
        String expResult = "Sabores do Norte Lda";
        String result = dal.editado.getEmpresa();
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarSaiDaRestauracaoSemEmpresa() {
        System.out.println("editar (fora da restauração a empresa fica vazia)");
        Utilizador conta = new Utilizador(5, 7, "rita", "h", "s", Perfil.RESTAURACAO, true);
        Funcionario novos = ana();
        novos.setEmpresa("Sabores do Norte Lda");
        instance.editar(gestor, conta, novos, Perfil.RECECIONISTA);
        String expResult = null;
        String result = dal.editado.getEmpresa();
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarOProprioMantendoGestor() {
        System.out.println("editar (o gestor pode mudar os seus contactos)");
        instance.editar(gestor, gestor, ana(), Perfil.GESTOR);
        Perfil expResult = Perfil.GESTOR;
        Perfil result = dal.perfilEditado;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaComoRececionista() {
        System.out.println("criarConta (rececionista não pode)");
        Utilizador rececionista = new Utilizador(2, 2, "rece", "h", "s", Perfil.RECECIONISTA, true);
        String expResult = "Só o gestor pode criar contas. / 0";
        String result = mensagemAoCriar(rececionista, dados, "ana.silva", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaSemUtilizador() {
        System.out.println("criarConta (sem utilizador com sessão)");
        String expResult = "Só o gestor pode criar contas. / 0";
        String result = mensagemAoCriar(null, dados, "ana.silva", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaSemDados() {
        System.out.println("criarConta (sem dados do funcionário)");
        String expResult = "Faltam os dados do funcionário. / 0";
        String result = mensagemAoCriar(gestor, null, "ana.silva", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaSemNome() {
        System.out.println("criarConta (nome vazio)");
        dados.setNome("  ");
        String expResult = "O nome é obrigatório (até 100 caracteres). / 0";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaNifCurto() {
        System.out.println("criarConta (NIF com poucos algarismos)");
        dados.setNif("12345");
        String expResult = "O NIF tem de ter 9 algarismos. / 0";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaNifComLetra() {
        System.out.println("criarConta (NIF com letra)");
        dados.setNif("12345678A");
        String expResult = "O NIF tem de ter 9 algarismos. / 0";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaNifRepetido() {
        System.out.println("criarConta (NIF repetido)");
        dal.nifExiste = true;
        String expResult = "Já existe uma pessoa com esse NIF. / 0";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaNomeDeUtilizadorRepetido() {
        System.out.println("criarConta (nome de utilizador repetido)");
        dal.nomeExiste = true;
        String expResult = "Esse nome de utilizador já existe. / 0";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    private Funcionario ana() {
        return new Funcionario(7, "Ana Silva", "12345678", "123456789", "Rua A", "910000000",
                LocalDate.of(1990, 5, 1), "ana@hotel.pt", 12, LocalDate.of(2026, 10, 1));
    }

    private String erroAoEditar(Utilizador quemAge, Utilizador conta, Funcionario novos, Perfil perfil) {
        try {
            instance.editar(quemAge, conta, novos, perfil);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testCriarContaNomeDeUtilizadorCurto() {
        System.out.println("criarConta (nome de utilizador curto)");
        String expResult = "O nome de utilizador tem de ter entre 3 e 50 caracteres. / 0";
        String result = mensagemAoCriar(gestor, dados, "an", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaPasswordCurta() {
        System.out.println("criarConta (password curta)");
        String expResult = "A password tem de ter pelo menos 8 caracteres. / 0";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "1234567", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaSemPerfil() {
        System.out.println("criarConta (sem perfil)");
        String expResult = "Escolha o perfil. / 0";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", null) + " / " + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaEmailInvalido() {
        System.out.println("criarConta (email inválido)");
        dados.setEmail("sem-arroba");
        String expResult = "O email não é válido. / 0";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaRestauracaoSemEmpresa() {
        System.out.println("criarConta (restauração sem o nome da empresa)");
        dados.setEmpresa("   ");
        String expResult = "Escreva o nome da empresa de restauração (até 100 caracteres). / 0";
        String result = mensagemAoCriar(gestor, dados, "rita.sabores", "password123", Perfil.RESTAURACAO)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaRestauracaoEmpresaDemasiadoLonga() {
        System.out.println("criarConta (restauração com empresa de mais de 100 caracteres)");
        dados.setEmpresa("a".repeat(101));
        String expResult = "Escreva o nome da empresa de restauração (até 100 caracteres).";
        String result = mensagemAoCriar(gestor, dados, "rita.sabores", "password123", Perfil.RESTAURACAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaNascimentoNoFuturo() {
        System.out.println("criarConta (data de nascimento no futuro)");
        dados.setDataNascimento(LocalDate.now().plusDays(1));
        String expResult = "A data de nascimento não pode ser no futuro. / 0";
        String result = mensagemAoCriar(gestor, dados, "ana.silva", "password123", Perfil.RECECIONISTA)
                + " / "
                + dal.contasCriadas;
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarParaRestauracaoSemEmpresa() {
        System.out.println("editar (restauração sem empresa)");
        Utilizador conta = new Utilizador(5, 7, "ana.silva", "h", "s", Perfil.RECECIONISTA, true);
        String expResult = "Escreva o nome da empresa de restauração (até 100 caracteres). / null";
        String result = erroAoEditar(gestor, conta, ana(), Perfil.RESTAURACAO) + " / " + dal.editado;
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarRetirarOGestorAoProprio() {
        System.out.println("editar (o gestor não tira a si próprio o perfil de gestor)");
        String expResult = "Não pode retirar o perfil de gestor à sua própria conta. / " + Perfil.GESTOR;
        String result = erroAoEditar(gestor, gestor, ana(), Perfil.RECECIONISTA) + " / " + gestor.getPerfil();
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarSemNome() {
        System.out.println("editar (sem nome)");
        Utilizador conta = new Utilizador(5, 7, "ana.silva", "h", "s", Perfil.RECECIONISTA, true);
        Funcionario novos = ana();
        novos.setNome("   ");
        String expResult = "O nome é obrigatório (até 100 caracteres).";
        String result = erroAoEditar(gestor, conta, novos, Perfil.RECECIONISTA);
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarEmailInvalido() {
        System.out.println("editar (email inválido)");
        Utilizador conta = new Utilizador(5, 7, "ana.silva", "h", "s", Perfil.RECECIONISTA, true);
        Funcionario novos = ana();
        novos.setEmail("sem-arroba");
        String expResult = "O email não é válido.";
        String result = erroAoEditar(gestor, conta, novos, Perfil.RECECIONISTA);
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarComoRececionista() {
        System.out.println("editar (só o gestor)");
        Utilizador rececionista = new Utilizador(2, 2, "rece", "h", "s", Perfil.RECECIONISTA, true);
        Utilizador conta = new Utilizador(5, 7, "ana.silva", "h", "s", Perfil.RECECIONISTA, true);
        String expResult = "Só o gestor pode editar contas.";
        String result = erroAoEditar(rececionista, conta, ana(), Perfil.GESTOR);
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarContaQueJaNaoExiste() {
        System.out.println("editar (a conta foi apagada entretanto)");
        dal.contaExiste = false;
        Utilizador conta = new Utilizador(5, 7, "ana.silva", "h", "s", Perfil.RECECIONISTA, true);
        String expResult = "A conta já não existe.";
        String result = erroAoEditar(gestor, conta, ana(), Perfil.RECECIONISTA);
        assertEquals(expResult, result);
    }

    @Test
    public void testNomeCurto() {
        System.out.println("nomeCurto (primeiro e último nome)");
        assertEquals("Ana Ribeiro", FuncionarioBLL.nomeCurto("Ana Maria Costa Ribeiro"));
    }

    @Test
    public void testNomeCurtoSoUmNome() {
        System.out.println("nomeCurto (só um nome)");
        assertEquals("Ana", FuncionarioBLL.nomeCurto("  Ana  "));
    }
}
