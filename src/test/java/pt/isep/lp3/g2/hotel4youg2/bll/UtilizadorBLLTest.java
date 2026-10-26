package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UtilizadorBLLTest {

    private static class DALFalsa implements IUtilizadorDAL {
        boolean existe = true;
        int chamadas = 0;
        int ultimoId;
        boolean ultimoAtivo;

        @Override
        public Optional<Utilizador> procurarPorNomeUtilizador(String nomeUtilizador) {
            return Optional.empty();
        }

        @Override
        public List<Utilizador> listar() {
            return List.of();
        }

        @Override
        public boolean atualizarAtivo(int utilizadorId, boolean ativo) {
            chamadas++;
            ultimoId = utilizadorId;
            ultimoAtivo = ativo;
            return existe;
        }
    }

    private final DALFalsa dal = new DALFalsa();
    private final UtilizadorBLL bll = new UtilizadorBLL(dal);

    private static Utilizador conta(int id, Perfil perfil) {
        return new Utilizador(id, id, "user" + id, "h", "s", perfil, true);
    }

    @Test
    void gestorDesativaOutraConta() {
        bll.desativar(conta(1, Perfil.GESTOR), conta(2, Perfil.RECECIONISTA));

        assertEquals(1, dal.chamadas);
        assertEquals(2, dal.ultimoId);
        assertFalse(dal.ultimoAtivo);
    }

    @Test
    void gestorReativaConta() {
        bll.reativar(conta(1, Perfil.GESTOR), conta(2, Perfil.RECECIONISTA));

        assertTrue(dal.ultimoAtivo);
    }

    @Test
    void rececionistaNaoPodeDesativar() {
        assertThrows(IllegalStateException.class,
                () -> bll.desativar(conta(2, Perfil.RECECIONISTA), conta(3, Perfil.RECECIONISTA)));
        assertEquals(0, dal.chamadas);
    }

    @Test
    void gestorNaoPodeDesativarASiProprio() {
        Utilizador gestor = conta(1, Perfil.GESTOR);

        assertThrows(IllegalArgumentException.class, () -> bll.desativar(gestor, gestor));
        assertEquals(0, dal.chamadas);
    }

    @Test
    void contaInexistenteDaErro() {
        dal.existe = false;

        assertThrows(IllegalArgumentException.class,
                () -> bll.desativar(conta(1, Perfil.GESTOR), conta(9, Perfil.RECECIONISTA)));
    }

    @Test
    void semContaEscolhidaDaErro() {
        assertThrows(IllegalArgumentException.class,
                () -> bll.desativar(conta(1, Perfil.GESTOR), null));
    }
}