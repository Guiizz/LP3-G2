package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.ClienteRepositorio;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Testa as regras do registo (US13) e da pesquisa por NIF (US14) sem ir à base de dados. */
class ClienteServiceTest {

    private static final LocalDate ADULTO = LocalDate.now().minusYears(30);

    /** Repositório falso: não usa a BD e regista se alguém tentou gravar. */
    private static class RepositorioFalso implements ClienteRepositorio {
        private final boolean nifExiste;
        private boolean gravou = false;
        private Cliente clienteNaBD = null;
        private int procuras = 0;

        RepositorioFalso(boolean nifExiste) {
            this.nifExiste = nifExiste;
        }

        @Override
        public int inserirCliente(Cliente c) {
            gravou = true;
            c.setId(1);
            return 1;
        }

        @Override
        public boolean existeNif(String nif) {
            return nifExiste;
        }

        @Override
        public List<Cliente> pesquisarClientes(String termo) {
            return new ArrayList<>();
        }

        @Override
        public boolean atualizarCliente(Cliente c) {
            return true;
        }

        @Override
        public Cliente procurarPorNif(String nif) {
            procuras++;
            if (clienteNaBD != null && clienteNaBD.getNif().equals(nif)) {
                return clienteNaBD;
            }
            return null;
        }
    }

    private RepositorioFalso repositorioComAna() {
        RepositorioFalso repo = new RepositorioFalso(true);
        repo.clienteNaBD = new Cliente(7, "Ana Silva", null, "123456789", null, "912345678", ADULTO, null);
        return repo;
    }

    private void esperaErroNaPesquisa(String nif) {
        RepositorioFalso repo = repositorioComAna();
        try {
            new ClienteService(repo).pesquisarPorNif(nif);
            fail("Devia ter sido recusado");
        } catch (ValidacaoException e) {
            assertEquals(0, repo.procuras, "Não devia ter ido à base de dados");
        }
    }

    private void esperaErro(RepositorioFalso repo, String nome, String nif, LocalDate data,
                            String telemovel, String email) {
        ClienteService servico = new ClienteService(repo);
        try {
            servico.registar(nome, nif, data, telemovel, email, null, null);
            fail("Devia ter sido recusado");
        } catch (ValidacaoException e) {
            assertFalse(repo.gravou, "Não devia ter gravado nada");
        }
    }

    private void esperaErro(String nome, String nif, LocalDate data, String telemovel, String email) {
        esperaErro(new RepositorioFalso(false), nome, nif, data, telemovel, email);
    }

    @Test
    void clienteValidoFicaGravado() {
        RepositorioFalso repo = new RepositorioFalso(false);
        Cliente c = new ClienteService(repo).registar(
                "  Ana Silva ", "123456789", ADULTO, "912345678", null, "", "   ");
        assertTrue(repo.gravou);
        assertEquals(1, c.getId());
        assertEquals("Ana Silva", c.getNome());
        assertNull(c.getCc());
        assertNull(c.getMorada());
    }

    @Test
    void nomeVazioFalha() {
        esperaErro("   ", "123456789", ADULTO, "912345678", null);
    }

    @Test
    void nifComOitoDigitosFalha() {
        esperaErro("Ana", "12345678", ADULTO, "912345678", null);
    }

    @Test
    void nifComLetrasFalha() {
        esperaErro("Ana", "12AB56789", ADULTO, "912345678", null);
    }

    @Test
    void nifRepetidoFalha() {
        esperaErro(new RepositorioFalso(true), "Ana", "123456789", ADULTO, "912345678", null);
    }

    @Test
    void semDataDeNascimentoFalha() {
        esperaErro("Ana", "123456789", null, "912345678", null);
    }

    @Test
    void dataNoFuturoFalha() {
        esperaErro("Ana", "123456789", LocalDate.now().plusDays(1), "912345678", null);
    }

    @Test
    void menorDe18Falha() {
        esperaErro("Ana", "123456789", LocalDate.now().minusYears(18).plusDays(1), "912345678", null);
    }

    @Test
    void quemFaz18HojePodeRegistar() {
        RepositorioFalso repo = new RepositorioFalso(false);
        new ClienteService(repo).registar("Ana", "123456789", LocalDate.now().minusYears(18),
                "912345678", null, null, null);
        assertTrue(repo.gravou);
    }

    @Test
    void semTelemovelNemEmailFalha() {
        esperaErro("Ana", "123456789", ADULTO, null, "  ");
    }

    @Test
    void soComEmailFunciona() {
        RepositorioFalso repo = new RepositorioFalso(false);
        new ClienteService(repo).registar("Ana", "123456789", ADULTO, null, "ana@mail.pt", null, null);
        assertTrue(repo.gravou);
    }

    @Test
    void telemovelInvalidoFalha() {
        esperaErro("Ana", "123456789", ADULTO, "12345", null);
    }

    @Test
    void emailInvalidoFalha() {
        esperaErro("Ana", "123456789", ADULTO, null, "ana.mail.pt");
    }

    @Test
    void mensagemDoNifRepetidoIndicaOMotivo() {
        ClienteService servico = new ClienteService(new RepositorioFalso(true));
        try {
            servico.registar("Ana", "123456789", ADULTO, "912345678", null, null, null);
            fail("Devia ter sido recusado");
        } catch (ValidacaoException e) {
            assertEquals("Já existe uma pessoa registada com o NIF 123456789", e.getMessage());
        }
    }

    // ---------- US14: pesquisar cliente por NIF ----------

    @Test
    void pesquisaComNifExistenteDevolveOsDados() {
        Cliente c = new ClienteService(repositorioComAna()).pesquisarPorNif("123456789");
        assertEquals(7, c.getId());
        assertEquals("Ana Silva", c.getNome());
        assertEquals("912345678", c.getTelemovel());
    }

    @Test
    void pesquisaComNifInexistenteDevolveNull() {
        RepositorioFalso repo = repositorioComAna();
        assertNull(new ClienteService(repo).pesquisarPorNif("987654321"));
        assertEquals(1, repo.procuras);
    }

    @Test
    void pesquisaIgnoraEspacosAntesEDepois() {
        Cliente c = new ClienteService(repositorioComAna()).pesquisarPorNif("  123456789 ");
        assertEquals("Ana Silva", c.getNome());
    }

    @Test
    void pesquisaSemNifFalha() {
        esperaErroNaPesquisa("   ");
    }

    @Test
    void pesquisaComNifComLetrasFalhaAntesDePesquisar() {
        esperaErroNaPesquisa("12AB56789");
    }

    @Test
    void pesquisaComNifCurtoFalhaAntesDePesquisar() {
        esperaErroNaPesquisa("12345678");
    }

    @Test
    void pesquisaComNifLongoFalhaAntesDePesquisar() {
        esperaErroNaPesquisa("1234567890");
    }
}