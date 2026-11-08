package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class FuncionarioClienteBLLTest {

    private static final String NIF_FUNCIONARIO = "222333444";
    private static final String NIF_CLIENTE = "555666777";
    private static final LocalDate ADULTO = LocalDate.now().minusYears(30);

    private static class ClienteDALFalsa implements IClienteDAL {
        Cliente funcionario = new Cliente(9, "Rui Costa", null, NIF_FUNCIONARIO, null, null, null, null);
        Cliente tornado;
        boolean inseriuPessoaNova;
        boolean outroJaRegistou;

        @Override
        public int inserirCliente(Cliente c) {
            inseriuPessoaNova = true;
            c.setId(50);
            return 50;
        }

        @Override
        public boolean existeNif(String nif) {
            return NIF_FUNCIONARIO.equals(nif) || NIF_CLIENTE.equals(nif);
        }

        @Override
        public List<Cliente> pesquisarClientes(String termo) {
            return new ArrayList<>();
        }

        @Override
        public Cliente procurarPorNif(String nif) {
            return null;
        }

        @Override
        public boolean atualizarCliente(Cliente c) {
            return true;
        }

        @Override
        public Cliente procurarFuncionarioNaoCliente(String nif) {
            return funcionario != null && funcionario.getNif().equals(nif) ? funcionario : null;
        }

        @Override
        public boolean tornarCliente(Cliente c) {
            if (outroJaRegistou) {
                return false;
            }
            tornado = c;
            funcionario = null;
            return true;
        }
    }

    private ClienteDALFalsa clienteDAL;
    private ClienteBLL instance;

    @BeforeEach
    public void setUp() {
        clienteDAL = new ClienteDALFalsa();
        instance = new ClienteBLL(clienteDAL);
    }

    @AfterEach
    public void tearDown() {
        clienteDAL = null;
        instance = null;
    }

    @Test
    public void testRegistarFuncionario() {
        System.out.println("registar (NIF de um funcionário)");
        int expResult = 9;
        int result = instance.registar("Rui Costa", NIF_FUNCIONARIO, ADULTO, "912345678", null, null, null).getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarFuncionarioNaoCriaOutraPessoa() {
        System.out.println("registar (funcionário não cria outra pessoa)");
        instance.registar("Rui Costa", NIF_FUNCIONARIO, ADULTO, "912345678", null, null, null);
        boolean expResult = false;
        boolean result = clienteDAL.inseriuPessoaNova;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarFuncionarioFicaComDadosDoFormulario() {
        System.out.println("registar (funcionário fica com os dados do formulário)");
        instance.registar("Rui Costa", NIF_FUNCIONARIO, ADULTO, "912345678", "rui@mail.pt", null, "Rua A");
        String expResult = "912345678";
        String result = clienteDAL.tornado.getTelemovel();
        assertEquals(expResult, result);
        assertEquals("rui@mail.pt", clienteDAL.tornado.getEmail());
    }

    @Test
    public void testRegistarFuncionarioMantemONome() {
        System.out.println("registar (o nome do funcionário não muda)");
        instance.registar("Nome Errado", NIF_FUNCIONARIO, ADULTO, "912345678", null, null, null);
        String expResult = "Rui Costa";
        String result = clienteDAL.tornado.getNome();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarFuncionarioNaoApagaDados() {
        System.out.println("registar (um campo vazio não apaga o que o funcionário já tem)");
        clienteDAL.funcionario = new Cliente(9, "Rui Costa", "11112222", NIF_FUNCIONARIO, "Rua B", null, null, null);
        instance.registar("Rui Costa", NIF_FUNCIONARIO, ADULTO, "912345678", null, "  ", null);
        String expResult = "11112222";
        String result = clienteDAL.tornado.getCc();
        assertEquals(expResult, result);
        assertEquals("Rua B", clienteDAL.tornado.getMorada());
    }

    @Test
    public void testRegistarFuncionarioRegistadoEntretanto() {
        System.out.println("registar (outro rececionista registou-o ao mesmo tempo)");
        clienteDAL.outroJaRegistou = true;
        try {
            instance.registar("Rui Costa", NIF_FUNCIONARIO, ADULTO, "912345678", null, null, null);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("Este funcionário já foi registado como cliente", expected.getMessage());
        }
    }

    @Test
    public void testRegistarFuncionarioValidaComoCliente() {
        System.out.println("registar (funcionário sem contacto)");
        try {
            instance.registar("Rui Costa", NIF_FUNCIONARIO, ADULTO, null, null, null, null);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("Indique pelo menos o telemóvel ou o email", expected.getMessage());
            assertEquals(null, clienteDAL.tornado);
        }
    }

    @Test
    public void testRegistarFuncionarioDuasVezes() {
        System.out.println("registar (o mesmo funcionário duas vezes)");
        instance.registar("Rui Costa", NIF_FUNCIONARIO, ADULTO, "912345678", null, null, null);
        try {
            instance.registar("Rui Costa", NIF_FUNCIONARIO, ADULTO, "912345678", null, null, null);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("Já existe uma pessoa registada com o NIF " + NIF_FUNCIONARIO, expected.getMessage());
        }
    }

    @Test
    public void testRegistarClienteRepetido() {
        System.out.println("registar (NIF de outro cliente)");
        try {
            instance.registar("Ana Silva", NIF_CLIENTE, ADULTO, "912345678", null, null, null);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("Já existe uma pessoa registada com o NIF " + NIF_CLIENTE, expected.getMessage());
            assertEquals(false, clienteDAL.inseriuPessoaNova);
        }
    }

    @Test
    public void testRegistarNifNovo() {
        System.out.println("registar (NIF novo continua a criar a pessoa)");
        boolean expResult = true;
        instance.registar("Ana Silva", "111222333", ADULTO, "912345678", null, null, null);
        boolean result = clienteDAL.inseriuPessoaNova;
        assertEquals(expResult, result);
    }

    @Test
    public void testFuncionarioComNif() {
        System.out.println("funcionarioComNif");
        String expResult = "Rui Costa";
        String result = instance.funcionarioComNif(" " + NIF_FUNCIONARIO + " ").getNome();
        assertEquals(expResult, result);
    }

    @Test
    public void testFuncionarioComNifDeCliente() {
        System.out.println("funcionarioComNif (não é funcionário)");
        Cliente expResult = null;
        Cliente result = instance.funcionarioComNif(NIF_CLIENTE);
        assertEquals(expResult, result);
    }

    @Test
    public void testFuncionarioComNifInvalido() {
        System.out.println("funcionarioComNif (NIF mal escrito)");
        Cliente expResult = null;
        Cliente result = instance.funcionarioComNif("abc");
        assertEquals(expResult, result);
    }
}
