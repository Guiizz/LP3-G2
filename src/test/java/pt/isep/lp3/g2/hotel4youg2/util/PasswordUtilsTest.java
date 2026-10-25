package pt.isep.lp3.g2.hotel4youg2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilsTest {

    private final PasswordUtils passwordUtils = new PasswordUtils();

    @Test
    void hashCoincideComOGestorDosDadosDeTeste() {
        // Mesmos valores dos dados de teste (gestor / Gestor#2026)
        assertEquals("1589b18ade723c24d28e360f7d4f431e5c280cd3beabe1585aee022fce93a135",
                passwordUtils.hashPassword("Gestor#2026", "587b9e80be35fe6c14e8c0eecdce3e81"));
    }

    @Test
    void tamanhosCabemNasColunasDaBaseDeDados() {
        // passwordHash CHAR(64) e passwordSalt CHAR(32)
        String salt = passwordUtils.gerarSalt();
        assertEquals(32, salt.length());
        assertEquals(64, passwordUtils.hashPassword("Segredo#123", salt).length());
    }

    @Test
    void verificaAPasswordCorreta() {
        String salt = passwordUtils.gerarSalt();
        String hash = passwordUtils.hashPassword("Segredo#123", salt);
        assertTrue(passwordUtils.verificarPassword("Segredo#123", salt, hash));
    }

    @Test
    void recusaUmaPasswordErrada() {
        String salt = passwordUtils.gerarSalt();
        String hash = passwordUtils.hashPassword("Segredo#123", salt);
        assertFalse(passwordUtils.verificarPassword("segredo#123", salt, hash));
    }

    @Test
    void saltsDiferentesDaoHashesDiferentes() {
        assertNotEquals(passwordUtils.hashPassword("Segredo#123", passwordUtils.gerarSalt()),
                passwordUtils.hashPassword("Segredo#123", passwordUtils.gerarSalt()));
    }
}
