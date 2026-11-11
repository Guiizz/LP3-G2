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
 * Testes unitários da classe ClienteBLL: registar (US13), pesquisar (US14)
 * e registar como cliente um funcionário do hotel (US43).
 */
public class ClienteBLLTest {

    private static final LocalDate ADULTO = LocalDate.now().minusYears(30);

    /** Repositório de teste: guarda os clientes em memória em vez de ir à base de dados. */
    private static class ClienteDALFalsa implements IClienteDAL {
        private List<Cliente> clientes = new ArrayList<>();
        private List<Cliente> funcionariosNaoClientes = new ArrayList<>();
        private int proximoId = 8;
        private int insercoes = 0;
        private int procurasPorNif = 0;
        private boolean tornarClienteFalha = false;
        private String ultimoTermo;

        @Override
        public int inserirCliente(Cliente c) {
            c.setId(proximoId);
            proximoId++;
            clientes.add(c);
            insercoes++;
            return c.getId();
        }

        @Override
        public boolean existeNif(String nif) {
            for (Cliente cliente : clientes) {
                if (cliente.getNif().equals(nif)) {
                    return true;
                }
            }
            for (Cliente funcionario : funcionariosNaoClientes) {
                if (funcionario.getNif().equals(nif)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<Cliente> pesquisarClientes(String termo) {
            ultimoTermo = termo;
            List<Cliente> encontrados = new ArrayList<>();
            for (Cliente cliente : clientes) {
                boolean nomeCerto = cliente.getNome().toLowerCase().contains(termo.toLowerCase());
                if (nomeCerto || cliente.getNif().equals(termo)) {
                    encontrados.add(cliente);
                }
            }
            return encontrados;
        }

        @Override
        public Cliente procurarPorNif(String nif) {
            procurasPorNif++;
            for (Cliente cliente : clientes) {
                if (cliente.getNif().equals(nif)) {
                    return cliente;
                }
            }
            return null;
        }

        @Override
        public boolean atualizarCliente(Cliente c) {
            return false;
        }

        @Override
        public Cliente procurarFuncionarioNaoCliente(String nif) {
            for (Cliente funcionario : funcionariosNaoClientes) {
                if (funcionario.getNif().equals(nif)) {
                    return funcionario;
                }
            }
            return null;
        }

        @Override
        public boolean tornarCliente(Cliente c) {
            if (tornarClienteFalha) {
                return false;
            }
            funcionariosNaoClientes.clear();
            clientes.add(c);
            insercoes++;
            return true;
        }
    }

    private ClienteDALFalsa dal;
    private ClienteBLL instance;

    @BeforeEach
    public void setUp() {
        dal = new ClienteDALFalsa();
        dal.clientes.add(new Cliente(7, "Ana Silva", null, "123456789", null, "912345678", ADULTO, null));
        dal.funcionariosNaoClientes.add(new Cliente(20, "Rui Costa", "CC123", "111222333", "Rua F", "911111111",
                ADULTO, "rui@hotel.pt"));
        instance = new ClienteBLL(dal);
    }

    @AfterEach
    public void tearDown() {
        dal = null;
        instance = null;
    }

    // devolve a mensagem de erro ao registar o cliente, ou null se o cliente foi registado
    private String mensagemAoRegistar(String nome, String nif, LocalDate data, String telemovel, String email) {
        try {
            instance.registar(nome, nif, data, telemovel, email, null, null);
        } catch (ValidacaoException e) {
            return e.getMessage();
        }
        return null;
    }

    // devolve a mensagem de erro ao pesquisar por NIF, ou null se a pesquisa foi feita
    private String mensagemAoPesquisarPorNif(String nif) {
        try {
            instance.pesquisarPorNif(nif);
        } catch (ValidacaoException e) {
            return e.getMessage();
        }
        return null;
    }

    // ---------- US13: registar cliente ----------

    @Test
    public void testRegistar() {
        System.out.println("registar");
        Cliente expResult = new Cliente(8, "Maria Costa", "12345678", "987654321", "Rua A", "923456789", ADULTO,
                "maria@mail.pt");
        Cliente result = instance.registar("Maria Costa", "987654321", ADULTO, "923456789", "maria@mail.pt",
                "12345678", "Rua A");
        assertEquals(expResult, result);
        assertEquals(expResult, dal.clientes.get(1));
        assertEquals(1, dal.insercoes);
    }

    @Test
    public void testRegistarLimpaEspacosECamposVazios() {
        System.out.println("registar (espaços e campos vazios ficam limpos)");
        Cliente expResult = new Cliente(8, "Maria Costa", null, "987654321", null, "923456789", ADULTO, null);
        Cliente result = instance.registar("  Maria Costa ", "987654321", ADULTO, "923456789", null, "", "   ");
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSoComEmail() {
        System.out.println("registar (só com email)");
        Cliente expResult = new Cliente(8, "Maria Costa", null, "987654321", null, null, ADULTO, "maria@mail.pt");
        Cliente result = instance.registar("Maria Costa", "987654321", ADULTO, null, "maria@mail.pt", null, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarFazer18AnosHoje() {
        System.out.println("registar (quem faz 18 anos hoje pode)");
        LocalDate dezoitoHoje = LocalDate.now().minusYears(18);
        Cliente expResult = new Cliente(8, "Maria Costa", null, "987654321", null, "923456789", dezoitoHoje, null);
        Cliente result = instance.registar("Maria Costa", "987654321", dezoitoHoje, "923456789", null, null, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarNomeVazio() {
        System.out.println("registar (nome vazio)");
        String expResult = "O nome é obrigatório";
        String result = mensagemAoRegistar("   ", "987654321", ADULTO, "923456789", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarNomeMuitoLongo() {
        System.out.println("registar (nome com mais de 100 caracteres)");
        String expResult = "O nome não pode ter mais de 100 caracteres";
        String result = mensagemAoRegistar("A".repeat(101), "987654321", ADULTO, "923456789", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarSemNif() {
        System.out.println("registar (sem NIF)");
        String expResult = "O NIF é obrigatório";
        String result = mensagemAoRegistar("Maria Costa", "  ", ADULTO, "923456789", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarNifComOitoDigitos() {
        System.out.println("registar (NIF com oito dígitos)");
        String expResult = "O NIF tem de ter exatamente 9 dígitos";
        String result = mensagemAoRegistar("Maria Costa", "12345678", ADULTO, "923456789", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarNifComLetras() {
        System.out.println("registar (NIF com letras)");
        String expResult = "O NIF tem de ter exatamente 9 dígitos";
        String result = mensagemAoRegistar("Maria Costa", "12AB56789", ADULTO, "923456789", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarNifRepetido() {
        System.out.println("registar (NIF repetido)");
        String expResult = "Já existe uma pessoa registada com o NIF 123456789";
        String result = mensagemAoRegistar("Maria Costa", "123456789", ADULTO, "923456789", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarSemDataDeNascimento() {
        System.out.println("registar (sem data de nascimento)");
        String expResult = "A data de nascimento é obrigatória";
        String result = mensagemAoRegistar("Maria Costa", "987654321", null, "923456789", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarDataNoFuturo() {
        System.out.println("registar (data de nascimento no futuro)");
        String expResult = "A data de nascimento não pode ser no futuro";
        String result = mensagemAoRegistar("Maria Costa", "987654321", LocalDate.now().plusDays(1), "923456789",
                null);
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarMenorDe18Anos() {
        System.out.println("registar (menor de 18 anos)");
        String expResult = "O cliente tem de ter pelo menos 18 anos";
        String result = mensagemAoRegistar("Maria Costa", "987654321",
                LocalDate.now().minusYears(18).plusDays(1), "923456789", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarSemTelemovelNemEmail() {
        System.out.println("registar (sem telemóvel nem email)");
        String expResult = "Indique pelo menos o telemóvel ou o email";
        String result = mensagemAoRegistar("Maria Costa", "987654321", ADULTO, null, "  ");
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarTelemovelInvalido() {
        System.out.println("registar (telemóvel inválido)");
        String expResult = "O telemóvel tem de ter 9 dígitos e começar por 9";
        String result = mensagemAoRegistar("Maria Costa", "987654321", ADULTO, "12345", null);
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    @Test
    public void testRegistarEmailInvalido() {
        System.out.println("registar (email inválido)");
        String expResult = "O email não é válido";
        String result = mensagemAoRegistar("Maria Costa", "987654321", ADULTO, null, "maria.mail.pt");
        assertEquals(expResult, result);
        assertEquals(0, dal.insercoes);
    }

    // ---------- US43: funcionário que passa a cliente ----------

    @Test
    public void testRegistarFuncionarioComoCliente() {
        System.out.println("registar (funcionário passa a cliente, o nome e os dados que já tinha ficam)");
        Cliente expResult = new Cliente(20, "Rui Costa", "CC123", "111222333", "Rua F", "922222222", ADULTO,
                "rui@hotel.pt");
        Cliente result = instance.registar("Outro Nome", "111222333", ADULTO, "922222222", null, null, null);
        assertEquals(expResult, result);
        assertEquals(expResult, dal.clientes.get(1));
        assertEquals(1, dal.insercoes);
    }

    @Test
    public void testRegistarFuncionarioJaRegistadoComoCliente() {
        System.out.println("registar (funcionário já foi registado como cliente)");
        dal.tornarClienteFalha = true;
        String expResult = "Este funcionário já foi registado como cliente";
        String result = mensagemAoRegistar("Rui Costa", "111222333", ADULTO, "922222222", null);
        assertEquals(expResult, result);
        assertEquals(1, dal.clientes.size());
    }

    @Test
    public void testFuncionarioComNif() {
        System.out.println("funcionarioComNif");
        Cliente expResult = new Cliente(20, "Rui Costa", "CC123", "111222333", "Rua F", "911111111", ADULTO,
                "rui@hotel.pt");
        Cliente result = instance.funcionarioComNif(" 111222333 ");
        assertEquals(expResult, result);
    }

    @Test
    public void testFuncionarioComNifQueNaoEFuncionario() {
        System.out.println("funcionarioComNif (o NIF não é de um funcionário)");
        Cliente expResult = null;
        Cliente result = instance.funcionarioComNif("987654321");
        assertEquals(expResult, result);
    }

    @Test
    public void testFuncionarioComNifInvalido() {
        System.out.println("funcionarioComNif (NIF inválido)");
        Cliente expResult = null;
        Cliente result = instance.funcionarioComNif("1234");
        assertEquals(expResult, result);
    }

    @Test
    public void testFuncionarioComNifVazio() {
        System.out.println("funcionarioComNif (sem NIF)");
        Cliente expResult = null;
        Cliente result = instance.funcionarioComNif(null);
        assertEquals(expResult, result);
    }

    // ---------- US14: pesquisar clientes ----------

    @Test
    public void testPesquisar() {
        System.out.println("pesquisar");
        List<Cliente> expResult = new ArrayList<>();
        expResult.add(new Cliente(7, "Ana Silva", null, "123456789", null, "912345678", ADULTO, null));
        List<Cliente> result = instance.pesquisar("  ana ");
        assertEquals(expResult, result);
        assertEquals("ana", dal.ultimoTermo);
    }

    @Test
    public void testPesquisarSemResultados() {
        System.out.println("pesquisar (nenhum cliente encontrado)");
        List<Cliente> expResult = new ArrayList<>();
        List<Cliente> result = instance.pesquisar("Zé");
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarSemTermoMostraTodos() {
        System.out.println("pesquisar (sem termo mostra todos)");
        List<Cliente> expResult = dal.clientes;
        List<Cliente> result = instance.pesquisar(null);
        assertEquals(expResult, result);
        assertEquals("", dal.ultimoTermo);
    }

    @Test
    public void testPesquisarPorNif() {
        System.out.println("pesquisarPorNif");
        Cliente expResult = new Cliente(7, "Ana Silva", null, "123456789", null, "912345678", ADULTO, null);
        Cliente result = instance.pesquisarPorNif("123456789");
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarPorNifInexistente() {
        System.out.println("pesquisarPorNif (o NIF não existe)");
        Cliente expResult = null;
        Cliente result = instance.pesquisarPorNif("987654321");
        assertEquals(expResult, result);
        assertEquals(1, dal.procurasPorNif);
    }

    @Test
    public void testPesquisarPorNifIgnoraEspacos() {
        System.out.println("pesquisarPorNif (espaços à volta do NIF)");
        Cliente expResult = new Cliente(7, "Ana Silva", null, "123456789", null, "912345678", ADULTO, null);
        Cliente result = instance.pesquisarPorNif("  123456789 ");
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarPorNifVazio() {
        System.out.println("pesquisarPorNif (sem NIF)");
        String expResult = "O NIF é obrigatório";
        String result = mensagemAoPesquisarPorNif("   ");
        assertEquals(expResult, result);
        assertEquals(0, dal.procurasPorNif);
    }

    @Test
    public void testPesquisarPorNifComLetras() {
        System.out.println("pesquisarPorNif (NIF com letras)");
        String expResult = "O NIF tem de ter exatamente 9 dígitos";
        String result = mensagemAoPesquisarPorNif("12AB56789");
        assertEquals(expResult, result);
        assertEquals(0, dal.procurasPorNif);
    }

    @Test
    public void testPesquisarPorNifCurto() {
        System.out.println("pesquisarPorNif (NIF curto)");
        String expResult = "O NIF tem de ter exatamente 9 dígitos";
        String result = mensagemAoPesquisarPorNif("12345678");
        assertEquals(expResult, result);
        assertEquals(0, dal.procurasPorNif);
    }

    @Test
    public void testPesquisarPorNifLongo() {
        System.out.println("pesquisarPorNif (NIF longo)");
        String expResult = "O NIF tem de ter exatamente 9 dígitos";
        String result = mensagemAoPesquisarPorNif("1234567890");
        assertEquals(expResult, result);
        assertEquals(0, dal.procurasPorNif);
    }
}
