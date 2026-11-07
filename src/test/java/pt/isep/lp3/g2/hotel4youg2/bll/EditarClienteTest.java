package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/** Testa a edição dos dados de um cliente (US19) sem ir à base de dados. */
class EditarClienteTest {

    private static final LocalDate ADULTO = LocalDate.now().minusYears(30);

    /** Repositório falso: guarda o último cliente que lhe pediram para atualizar. */
    private static class ClienteDALFalsa implements IClienteDAL {
        private final boolean clienteExiste;
        private Cliente atualizado;

        ClienteDALFalsa(boolean clienteExiste) {
            this.clienteExiste = clienteExiste;
        }

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

    private String esperaErro(ClienteDALFalsa repo, Cliente cliente, String nome, LocalDate data,
                              String telemovel, String email) {
        try {
            new ClienteBLL(repo).editar(cliente, nome, data, telemovel, email, null, null);
            fail("Devia ter sido recusado");
            return null;
        } catch (ValidacaoException e) {
            return e.getMessage();
        }
    }

    private Cliente ana() {
        return new Cliente(7, "Ana Silva", null, "123456789", "Rua A", "912345678", ADULTO, "ana@mail.pt");
    }

    @Test
    void dadosValidosFicamGuardados() {
        ClienteDALFalsa repo = new ClienteDALFalsa(true);
        Cliente cliente = ana();
        new ClienteBLL(repo).editar(cliente, " Ana Costa ", ADULTO, "934567890", "ana@costa.pt",
                "12345678", "Rua B");

        assertEquals(7, repo.atualizado.getId());
        assertEquals("Ana Costa", repo.atualizado.getNome());
        assertEquals("934567890", repo.atualizado.getTelemovel());
        assertEquals("Rua B", cliente.getMorada());
        assertEquals("Ana Costa", cliente.getNome());
    }

    @Test
    void oNifNuncaMuda() {
        ClienteDALFalsa repo = new ClienteDALFalsa(true);
        Cliente cliente = ana();
        new ClienteBLL(repo).editar(cliente, "Ana Costa", ADULTO, "912345678", null, null, null);

        assertEquals("123456789", repo.atualizado.getNif());
        assertEquals("123456789", cliente.getNif());
    }

    @Test
    void camposOpcionaisPodemFicarVazios() {
        ClienteDALFalsa repo = new ClienteDALFalsa(true);
        Cliente cliente = ana();
        new ClienteBLL(repo).editar(cliente, "Ana Silva", ADULTO, "912345678", "  ", "", " ");

        assertNull(cliente.getEmail());
        assertNull(cliente.getCc());
        assertNull(cliente.getMorada());
    }

    @Test
    void semClienteEscolhidoFalha() {
        ClienteDALFalsa repo = new ClienteDALFalsa(true);
        esperaErro(repo, null, "Ana", ADULTO, "912345678", null);
        assertNull(repo.atualizado);
    }

    @Test
    void nomeVazioFalhaENaoMudaNada() {
        ClienteDALFalsa repo = new ClienteDALFalsa(true);
        Cliente cliente = ana();
        esperaErro(repo, cliente, "  ", ADULTO, "912345678", null);
        assertNull(repo.atualizado);
        assertEquals("Ana Silva", cliente.getNome());
    }

    @Test
    void menorDe18Falha() {
        ClienteDALFalsa repo = new ClienteDALFalsa(true);
        esperaErro(repo, ana(), "Ana", LocalDate.now().minusYears(17), "912345678", null);
        assertNull(repo.atualizado);
    }

    @Test
    void semTelemovelNemEmailFalha() {
        ClienteDALFalsa repo = new ClienteDALFalsa(true);
        esperaErro(repo, ana(), "Ana", ADULTO, " ", null);
        assertNull(repo.atualizado);
    }

    @Test
    void emailInvalidoFalha() {
        ClienteDALFalsa repo = new ClienteDALFalsa(true);
        esperaErro(repo, ana(), "Ana", ADULTO, null, "ana.mail.pt");
        assertNull(repo.atualizado);
    }

    @Test
    void clienteQueJaNaoExisteFalhaENaoMudaOObjeto() {
        ClienteDALFalsa repo = new ClienteDALFalsa(false);
        Cliente cliente = ana();
        String mensagem = esperaErro(repo, cliente, "Ana Costa", ADULTO, "912345678", null);
        assertEquals("O cliente já não existe", mensagem);
        assertEquals("Ana Silva", cliente.getNome());
    }
}
