package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Testa as regras de acesso por perfil (US12). */
class PermissoesTest {

    private Utilizador comPerfil(Perfil perfil) {
        return new Utilizador(1, 1, "teste", "hash", "salt", perfil, true);
    }

    @Test
    void gestorEGestor() {
        assertTrue(Permissoes.eGestor(comPerfil(Perfil.GESTOR)));
    }

    @Test
    void rececionistaNaoEGestor() {
        assertFalse(Permissoes.eGestor(comPerfil(Perfil.RECECIONISTA)));
    }

    @Test
    void semSessaoNaoEGestor() {
        assertFalse(Permissoes.eGestor(null));
    }

    @Test
    void gestorPodeAbrirEcraDeGestor() {
        Permissoes.exigirGestor(comPerfil(Perfil.GESTOR));
    }

    @Test
    void rececionistaNaoPodeAbrirEcraDeGestor() {
        try {
            Permissoes.exigirGestor(comPerfil(Perfil.RECECIONISTA));
            fail("Devia ter sido recusado");
        } catch (IllegalStateException e) {
            assertEquals("Só o gestor pode abrir este ecrã", e.getMessage());
        }
    }

    @Test
    void semSessaoNaoPodeAbrirEcraDeGestor() {
        try {
            Permissoes.exigirGestor(null);
            fail("Devia ter sido recusado");
        } catch (IllegalStateException e) {
            assertEquals("Só o gestor pode abrir este ecrã", e.getMessage());
        }
    }
}
