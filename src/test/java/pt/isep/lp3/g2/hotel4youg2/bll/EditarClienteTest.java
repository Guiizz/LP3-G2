package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da edição dos dados de um cliente (US19) na classe ClienteBLL.
 */
public class EditarClienteTest {

    private static final LocalDate ADULTO = LocalDate.now().minusYears(30);

    /** Repositório de teste: guarda o último cliente que lhe pediram para atualizar. */
    private static class ClienteDALFalsa implements IClienteDAL {
        private boolean clienteExiste = true;
        private Cliente atualizado;
        private int atualizacoes = 0;

        @Override
        public int inserirCliente(Cliente c) {
            return 0;
        }

        @Override
        public boolean existeNif(String nif) {
            return false;
        }

        @Override
        public List<Cliente> pesquisarClientes(String termo) {
            return new ArrayList<>();
        }

        @Override
        public boolean atualizarCliente(Cliente c) {
            atualizacoes++;
            atualizado = c;
            return clienteExiste;
        }

        @Override
        public Cliente procurarPorNif(String nif) {
            return null;
        }

        @Override
        public Cliente procurarFuncionarioNaoCliente(String nif) {
            return null;
        }

        @Override
        public boolean tornarCliente(Cliente c) {
            return true;
        }
    }

    private ClienteDALFalsa dal;
    private ClienteBLL instance;
    private Cliente ana;

    @BeforeEach
    public void setUp() {
        dal = new ClienteDALFalsa();
        instance = new ClienteBLL(dal);
        ana = new Cliente(7, "Ana Silva", null, "123456789", "Rua A", "912345678", ADULTO, "ana@mail.pt");
    }

    @AfterEach
    public void tearDown() {
        dal = null;
        instance = null;
        ana = null;
    }

    // devolve a mensagem de erro ao editar o cliente, ou null se o cliente foi editado
    private String mensagemAoEditar(Cliente cliente, String nome, LocalDate data, String telemovel, String email) {
        try {
            instance.editar(cliente, nome, data, telemovel, email, null, null);
        } catch (ValidacaoException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testEditar() {
        System.out.println("editar");
        Cliente expResult = new Cliente(7, "Ana Costa", "12345678", "123456789", "Rua B", "934567890", ADULTO,
                "ana@costa.pt");
        Cliente result = instance.editar(ana, " Ana Costa ", ADULTO, "934567890", "ana@costa.pt", "12345678",
                "Rua B");
        assertEquals(expResult, result);
        assertEquals(1, dal.atualizacoes);
    }

    @Test
    public void testEditarGravaOsDadosNovos() {
        System.out.println("editar (o repositório recebe os dados novos)");
        Cliente expResult = new Cliente(7, "Ana Costa", "12345678", "123456789", "Rua B", "934567890", ADULTO,
                "ana@costa.pt");
        instance.editar(ana, "Ana Costa", ADULTO, "934567890", "ana@costa.pt", "12345678", "Rua B");
        Cliente result = dal.atualizado;
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarONifNaoMuda() {
        System.out.println("editar (o NIF nunca muda)");
        String expResult = "123456789";
        instance.editar(ana, "Ana Costa", ADULTO, "912345678", null, null, null);
        String result = dal.atualizado.getNif();
        assertEquals(expResult, result);
        assertEquals(expResult, ana.getNif());
    }

    @Test
    public void testEditarCamposOpcionaisVazios() {
        System.out.println("editar (campos opcionais vazios ficam nulos)");
        Cliente expResult = new Cliente(7, "Ana Silva", null, "123456789", null, "912345678", ADULTO, null);
        Cliente result = instance.editar(ana, "Ana Silva", ADULTO, "912345678", "  ", "", " ");
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarSemClienteEscolhido() {
        System.out.println("editar (sem cliente escolhido)");
        String expResult = "Escolha um cliente na pesquisa";
        String result = mensagemAoEditar(null, "Ana", ADULTO, "912345678", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.atualizacoes);
    }

    @Test
    public void testEditarNomeVazio() {
        System.out.println("editar (nome vazio, o cliente fica como estava)");
        Cliente expResult = new Cliente(7, "Ana Silva", null, "123456789", "Rua A", "912345678", ADULTO,
                "ana@mail.pt");
        String mensagem = mensagemAoEditar(ana, "  ", ADULTO, "912345678", null);
        assertEquals("O nome é obrigatório", mensagem);
        assertEquals(expResult, ana);
        assertEquals(0, dal.atualizacoes);
    }

    @Test
    public void testEditarMenorDe18Anos() {
        System.out.println("editar (menor de 18 anos)");
        String expResult = "O cliente tem de ter pelo menos 18 anos";
        String result = mensagemAoEditar(ana, "Ana", LocalDate.now().minusYears(17), "912345678", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.atualizacoes);
    }

    @Test
    public void testEditarSemDataDeNascimento() {
        System.out.println("editar (sem data de nascimento)");
        String expResult = "A data de nascimento é obrigatória";
        String result = mensagemAoEditar(ana, "Ana", null, "912345678", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.atualizacoes);
    }

    @Test
    public void testEditarSemTelemovelNemEmail() {
        System.out.println("editar (sem telemóvel nem email)");
        String expResult = "Indique pelo menos o telemóvel ou o email";
        String result = mensagemAoEditar(ana, "Ana", ADULTO, " ", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.atualizacoes);
    }

    @Test
    public void testEditarEmailInvalido() {
        System.out.println("editar (email inválido)");
        String expResult = "O email não é válido";
        String result = mensagemAoEditar(ana, "Ana", ADULTO, null, "ana.mail.pt");
        assertEquals(expResult, result);
        assertEquals(0, dal.atualizacoes);
    }

    @Test
    public void testEditarClienteQueJaNaoExiste() {
        System.out.println("editar (o cliente já não existe, o objeto fica como estava)");
        dal.clienteExiste = false;
        Cliente expResult = new Cliente(7, "Ana Silva", null, "123456789", "Rua A", "912345678", ADULTO,
                "ana@mail.pt");
        String mensagem = mensagemAoEditar(ana, "Ana Costa", ADULTO, "912345678", null);
        assertEquals("O cliente já não existe", mensagem);
        assertEquals(expResult, ana);
    }
}
