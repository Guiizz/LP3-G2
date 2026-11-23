package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Conta;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

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

    private static class FornecedorDALFalso implements IFornecedorDAL {
        private final List<Fornecedor> fornecedores = new ArrayList<>();

        @Override
        public Optional<Fornecedor> procurarPorCodigo(String codigo) {
            return Optional.empty();
        }

        @Override
        public int criar(Fornecedor fornecedor) {
            return 0;
        }

        @Override
        public boolean definirPassword(int fornecedorId, String passwordHash, String passwordSalt) {
            return false;
        }

        @Override
        public List<Fornecedor> listar() {
            return fornecedores;
        }
    }

    private UtilizadorDALFalso dal;
    private FornecedorDALFalso fornecedorDAL;
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
        fornecedorDAL = new FornecedorDALFalso();
        Fornecedor comPassword = new Fornecedor(1, "F001", "Lavandaria Norte", null, null, null, null, null, null);
        comPassword.definirPassword("h", "s");
        fornecedorDAL.fornecedores.add(comPassword);
        fornecedorDAL.fornecedores.add(new Fornecedor(2, "F002", "Padaria Sul", null, null, null, null, null, null));
        instance = new UtilizadorBLL(dal, fornecedorDAL);

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

    private List<String> textos(List<Conta> contas) {
        List<String> textos = new ArrayList<>();
        for (Conta conta : contas) {
            textos.add(conta.toString());
        }
        return textos;
    }

    @Test
    public void testListarContas() {
        System.out.println("listarContas (funcionários e fornecedores, com o tipo e o estado)");
        List<String> expResult = List.of("gestor (Gestor, Ativa)", "rece (Rececionista, Ativa)",
                "antiga (Rececionista, Desativada)", "F001 - Lavandaria Norte (Fornecedor, Ativa)",
                "F002 - Padaria Sul (Fornecedor, Sem password)");
        List<String> result = textos(instance.listarContas(gestor));
        assertEquals(expResult, result);
    }

    @Test
    public void testListarContasRececionista() {
        System.out.println("listarContas (só o gestor)");
        String expResult = "Só o gestor pode abrir este ecrã";
        String result = null;
        try {
            instance.listarContas(rececionista);
        } catch (IllegalStateException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testDesativarContaDeFuncionario() {
        System.out.println("desativarConta (conta de um funcionário)");
        instance.desativarConta(gestor, new Conta(rececionista));
        boolean expResult = false;
        boolean result = rececionista.isAtivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testReativarContaDeFuncionario() {
        System.out.println("reativarConta (conta de um funcionário)");
        instance.reativarConta(gestor, new Conta(desativada));
        boolean expResult = true;
        boolean result = desativada.isAtivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testDesativarContaDeFornecedor() {
        System.out.println("desativarConta (as contas dos fornecedores não se desativam)");
        String expResult = "Só as contas dos funcionários podem ser desativadas ou reativadas.";
        String result = null;
        try {
            instance.desativarConta(gestor, new Conta(fornecedorDAL.fornecedores.get(0)));
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testDesativarContaSemContaEscolhida() {
        System.out.println("desativarConta (sem conta escolhida)");
        String expResult = "Escolha uma conta.";
        String result = null;
        try {
            instance.desativarConta(gestor, null);
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    private String erroAoRepor(Utilizador quemAge, Conta conta) {
        try {
            instance.reporPassword(quemAge, conta);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testReporPassword() {
        System.out.println("reporPassword (a temporária entra e a conta fica a pedir uma nova)");
        String temporaria = instance.reporPassword(gestor, new Conta(rececionista));
        boolean expResult = true;
        boolean result = new PasswordUtils().verificarPassword(temporaria, rececionista.getPasswordSalt(),
                rececionista.getPasswordHash());
        assertEquals(expResult, result);
        assertEquals(true, rececionista.isMudarPassword());
    }

    @Test
    public void testReporPasswordGuardaNaBaseDeDados() {
        System.out.println("reporPassword (fica gravada com hash e salt, não em texto)");
        String temporaria = instance.reporPassword(gestor, new Conta(rececionista));
        Utilizador guardada = dal.contas.get(1);
        boolean expResult = false;
        boolean result = temporaria.equals(guardada.getPasswordHash()) || "h".equals(guardada.getPasswordHash());
        assertEquals(expResult, result);
    }

    @Test
    public void testReporPasswordTemporariaComDezCaracteres() {
        System.out.println("reporPassword (a temporária tem 10 caracteres)");
        int expResult = 10;
        int result = instance.reporPassword(gestor, new Conta(rececionista)).length();
        assertEquals(expResult, result);
    }

    @Test
    public void testReporPasswordDeFornecedor() {
        System.out.println("reporPassword (só contas de funcionários)");
        Conta fornecedor = new Conta(fornecedorDAL.fornecedores.get(0));
        String expResult = "Só se repõe a palavra-passe das contas dos funcionários.";
        String result = erroAoRepor(gestor, fornecedor);
        assertEquals(expResult, result);
    }

    @Test
    public void testReporPasswordSemConta() {
        System.out.println("reporPassword (sem conta escolhida)");
        String expResult = "Escolha uma conta.";
        String result = erroAoRepor(gestor, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testReporPasswordComoRececionista() {
        System.out.println("reporPassword (só o gestor)");
        String expResult = "Só o gestor pode gerir contas.";
        String result = erroAoRepor(rececionista, new Conta(desativada));
        assertEquals(expResult, result);
        assertEquals(false, desativada.isMudarPassword());
    }
}
