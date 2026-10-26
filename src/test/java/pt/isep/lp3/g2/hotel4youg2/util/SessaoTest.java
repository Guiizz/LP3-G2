package pt.isep.lp3.g2.hotel4youg2.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessaoTest {

    private final Utilizador gestor = new Utilizador(1, 1, "gestor", "hash", "salt", Perfil.GESTOR, true);

    @AfterEach
    void limpar() {
        Sessao.terminar();
    }

    @Test
    void semLoginNaoHaSessao() {
        assertFalse(Sessao.estaIniciada());
        assertNull(Sessao.getUtilizador());
    }

    @Test
    void depoisDoLoginGuardaOUtilizador() {
        Sessao.iniciar(gestor);
        assertTrue(Sessao.estaIniciada());
        assertEquals("gestor", Sessao.getUtilizador().getNomeUtilizador());
    }

    @Test
    void terminarSessaoEsqueceOUtilizador() {
        Sessao.iniciar(gestor);
        Sessao.terminar();
        assertFalse(Sessao.estaIniciada());
    }
}
