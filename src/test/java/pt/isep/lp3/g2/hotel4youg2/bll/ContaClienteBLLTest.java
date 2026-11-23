package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IContaClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ContaClienteBLLTest {

    private static class ContaClienteDALFalsa implements IContaClienteDAL {
        private final List<ContaCliente> contas = new ArrayList<>();

        @Override
        public int inserir(ContaCliente conta) {
            conta.setId(contas.size() + 1);
            contas.add(copia(conta));
            return conta.getId();
        }

        @Override
        public ContaCliente procurarPorNif(String nif) {
            for (ContaCliente conta : contas) {
                if (conta.getNif().equals(nif)) {
                    return copia(conta);
                }
            }
            return null;
        }

        @Override
        public boolean definirPassword(int contaId, String passwordHash, String passwordSalt) {
            ContaCliente conta = porId(contaId);
            if (conta == null || !conta.isPrimeiroAcesso()) {
                return false;
            }
            conta.definirPassword(passwordHash, passwordSalt, false);
            return true;
        }

        @Override
        public boolean novoCodigo(int contaId, String passwordHash, String passwordSalt) {
            ContaCliente conta = porId(contaId);
            if (conta == null) {
                return false;
            }
            conta.definirPassword(passwordHash, passwordSalt, true);
            return true;
        }

        @Override
        public boolean apagar(int contaId) {
            return contas.remove(porId(contaId));
        }

        private ContaCliente porId(int contaId) {
            for (ContaCliente conta : contas) {
                if (conta.getId() == contaId) {
                    return conta;
                }
            }
            return null;
        }

        private static ContaCliente copia(ContaCliente conta) {
            return new ContaCliente(conta.getId(), conta.getClienteId(), conta.getNif(), conta.getNome(),
                    conta.getPasswordHash(), conta.getPasswordSalt(), conta.isPrimeiroAcesso());
        }
    }

    private static class ClienteDALFalsa implements IClienteDAL {
        private final Cliente rui = new Cliente(15, "Rui Costa", null, "123456789", null, null,
                LocalDate.of(1990, 5, 1), null);

        @Override
        public Cliente procurarPorNif(String nif) {
            return rui.getNif().equals(nif) ? rui : null;
        }

        @Override
        public int inserirCliente(Cliente c) {
            return 0;
        }

        @Override
        public boolean existeNif(String nif) {
            return rui.getNif().equals(nif);
        }

        @Override
        public List<Cliente> pesquisarClientes(String termo) {
            return new ArrayList<>();
        }

        @Override
        public boolean atualizarCliente(Cliente c) {
            return false;
        }

        @Override
        public Cliente procurarFuncionarioNaoCliente(String nif) {
            return null;
        }

        @Override
        public boolean tornarCliente(Cliente c) {
            return false;
        }
    }

    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);
    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final String NIF = "123456789";

    private final PasswordUtils passwordUtils = new PasswordUtils();
    private ContaClienteDALFalsa contaDAL;
    private ContaClienteBLL instance;

    @BeforeEach
    public void setUp() {
        contaDAL = new ContaClienteDALFalsa();
        instance = new ContaClienteBLL(contaDAL, new ClienteDALFalsa(), passwordUtils);
    }

    @AfterEach
    public void tearDown() {
        contaDAL.contas.clear();
    }

    private ContaCliente contaAtivada(String palavraPasse) {
        String codigo = instance.criarConta(RECECAO, NIF);
        ContaCliente conta = instance.autenticar(NIF, codigo);
        instance.definirPalavraPasse(conta, palavraPasse, palavraPasse);
        return conta;
    }

    @Test
    public void testCriarConta() {
        System.out.println("criarConta (a conta fica à espera do primeiro acesso, com o código cifrado)");
        String codigo = instance.criarConta(RECECAO, NIF);
        ContaCliente result = contaDAL.procurarPorNif(NIF);
        ContaCliente expResult = new ContaCliente(1, 15, NIF, "Rui Costa",
                passwordUtils.hashPassword(codigo, result.getPasswordSalt()), result.getPasswordSalt(), true);
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaCodigoComSeisDigitos() {
        System.out.println("criarConta (o código tem 6 dígitos)");
        boolean expResult = true;
        boolean result = instance.criarConta(RECECAO, NIF).matches("\\d{6}");
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaPeloGestor() {
        System.out.println("criarConta (o gestor também pode criar)");
        instance.criarConta(GESTOR, NIF);
        int expResult = 1;
        int result = contaDAL.contas.size();
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarContaComEspacosNoNif() {
        System.out.println("criarConta (NIF com espaços à volta)");
        instance.criarConta(RECECAO, "  123456789 ");
        String expResult = "Rui Costa";
        String result = contaDAL.procurarPorNif(NIF).getNome();
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarPrimeiroAcesso() {
        System.out.println("autenticar (com o código, a conta está no primeiro acesso)");
        String codigo = instance.criarConta(RECECAO, NIF);
        ContaCliente expResult = contaDAL.procurarPorNif(NIF);
        ContaCliente result = instance.autenticar(NIF, codigo);
        assertEquals(expResult, result);
        assertEquals(true, result.isPrimeiroAcesso());
    }

    @Test
    public void testDefinirPalavraPasse() {
        System.out.println("definirPalavraPasse (depois disso entra com a palavra-passe)");
        contaAtivada("segredo123");
        ContaCliente expResult = contaDAL.procurarPorNif(NIF);
        ContaCliente result = instance.autenticar(NIF, "segredo123");
        assertEquals(expResult, result);
        assertEquals(false, result.isPrimeiroAcesso());
    }

    @Test
    public void testDefinirPalavraPasseAtualizaAConta() {
        System.out.println("definirPalavraPasse (a conta da sessão deixa de estar no primeiro acesso)");
        ContaCliente conta = contaAtivada("segredo123");
        ContaCliente expResult = contaDAL.procurarPorNif(NIF);
        ContaCliente result = conta;
        assertEquals(expResult, result);
    }

    @Test
    public void testGerarNovoCodigo() {
        System.out.println("gerarNovoCodigo (o cliente volta ao primeiro acesso com o código novo)");
        contaAtivada("segredo123");
        String codigo = instance.gerarNovoCodigo(RECECAO, NIF);
        ContaCliente expResult = contaDAL.procurarPorNif(NIF);
        ContaCliente result = instance.autenticar(NIF, codigo);
        assertEquals(expResult, result);
        assertEquals(true, result.isPrimeiroAcesso());
    }

    @Test
    public void testCriarContaRepetida() {
        System.out.println("criarConta (o cliente já tem conta)");
        instance.criarConta(RECECAO, NIF);
        String expResult = "O cliente Rui Costa já tem conta. Se ele se esqueceu da palavra-passe, gere um código novo.";
        try {
            instance.criarConta(RECECAO, NIF);
            fail("Devia ter recusado a segunda conta");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(1, contaDAL.contas.size());
        }
    }

    @Test
    public void testCriarContaClienteQueNaoExiste() {
        System.out.println("criarConta (NIF sem cliente)");
        String expResult = "Não existe nenhum cliente com o NIF 999999999.";
        try {
            instance.criarConta(RECECAO, "999999999");
            fail("Devia ter recusado o NIF");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testCriarContaNifInvalido() {
        System.out.println("criarConta (NIF com letras)");
        String expResult = "O NIF tem de ter exatamente 9 dígitos";
        try {
            instance.criarConta(RECECAO, "12345abc9");
            fail("Devia ter recusado o NIF");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testCriarContaSemSessao() {
        System.out.println("criarConta (sem sessão)");
        String expResult = "Só a receção e o gestor podem tratar das contas dos clientes.";
        try {
            instance.criarConta(null, NIF);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(0, contaDAL.contas.size());
        }
    }

    @Test
    public void testAutenticarCodigoErrado() {
        System.out.println("autenticar (código errado)");
        String codigo = instance.criarConta(RECECAO, NIF);
        String expResult = "O código do primeiro acesso está errado.";
        try {
            instance.autenticar(NIF, codigo + "1");
            fail("Devia ter recusado o código");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAutenticarSemConta() {
        System.out.println("autenticar (NIF sem conta)");
        String expResult = "Não existe nenhuma conta de cliente com o NIF 123456789. Peça à receção para a criar.";
        try {
            instance.autenticar(NIF, "123456");
            fail("Devia ter recusado");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAutenticarSemNif() {
        System.out.println("autenticar (sem NIF)");
        String expResult = "Escreva o seu NIF.";
        try {
            instance.autenticar("  ", "123456");
            fail("Devia ter pedido o NIF");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAutenticarSemPalavraPasse() {
        System.out.println("autenticar (sem palavra-passe)");
        String expResult = "Escreva a palavra-passe ou o código do primeiro acesso.";
        try {
            instance.autenticar(NIF, "");
            fail("Devia ter pedido a palavra-passe");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testDefinirPalavraPasseOCodigoDeixaDeServir() {
        System.out.println("definirPalavraPasse (o código já não entra)");
        String codigo = instance.criarConta(RECECAO, NIF);
        ContaCliente conta = instance.autenticar(NIF, codigo);
        instance.definirPalavraPasse(conta, "segredo123", "segredo123");
        String expResult = "A palavra-passe está errada.";
        try {
            instance.autenticar(NIF, codigo);
            fail("O código já não devia entrar");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testDefinirPalavraPasseCurta() {
        System.out.println("definirPalavraPasse (menos de 6 caracteres)");
        String codigo = instance.criarConta(RECECAO, NIF);
        ContaCliente conta = instance.autenticar(NIF, codigo);
        String expResult = "A palavra-passe tem de ter pelo menos 6 caracteres.";
        try {
            instance.definirPalavraPasse(conta, "abc", "abc");
            fail("Devia ter recusado a palavra-passe");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(true, contaDAL.procurarPorNif(NIF).isPrimeiroAcesso());
        }
    }

    @Test
    public void testDefinirPalavraPasseDiferentes() {
        System.out.println("definirPalavraPasse (as duas não são iguais)");
        String codigo = instance.criarConta(RECECAO, NIF);
        ContaCliente conta = instance.autenticar(NIF, codigo);
        String expResult = "As duas palavras-passe não são iguais.";
        try {
            instance.definirPalavraPasse(conta, "segredo123", "segredo124");
            fail("Devia ter recusado");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testDefinirPalavraPasseIgualAoCodigo() {
        System.out.println("definirPalavraPasse (igual ao código)");
        String codigo = instance.criarConta(RECECAO, NIF);
        ContaCliente conta = instance.autenticar(NIF, codigo);
        String expResult = "Escolha uma palavra-passe diferente do código que a receção lhe deu.";
        try {
            instance.definirPalavraPasse(conta, codigo, codigo);
            fail("Devia ter recusado");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testDefinirPalavraPasseDuasVezes() {
        System.out.println("definirPalavraPasse (só no primeiro acesso)");
        ContaCliente conta = contaAtivada("segredo123");
        String expResult = "A palavra-passe desta conta já foi escolhida.";
        try {
            instance.definirPalavraPasse(conta, "outra12345", "outra12345");
            fail("Devia ter recusado");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testDefinirPalavraPasseSemConta() {
        System.out.println("definirPalavraPasse (sem conta)");
        String expResult = "Entre com o NIF e o código do primeiro acesso.";
        try {
            instance.definirPalavraPasse(null, "segredo123", "segredo123");
            fail("Devia ter exigido a conta");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testGerarNovoCodigoSemConta() {
        System.out.println("gerarNovoCodigo (o cliente ainda não tem conta)");
        String expResult = "O cliente Rui Costa ainda não tem conta. Crie a conta primeiro.";
        try {
            instance.gerarNovoCodigo(RECECAO, NIF);
            fail("Devia ter pedido para criar a conta");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }
}
