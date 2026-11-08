package pt.isep.lp3.g2.hotel4youg2.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Testes unitários da classe PasswordUtils.
 */
public class PasswordUtilsTest {

    private PasswordUtils instance;

    @BeforeEach
    public void setUp() {
        instance = new PasswordUtils();
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }

    @Test
    public void testGerarSaltTamanho() {
        System.out.println("gerarSalt (32 caracteres, como a coluna da base de dados)");
        int expResult = 32;
        int result = instance.gerarSalt().length();
        assertEquals(expResult, result);
    }

    @Test
    public void testGerarSaltEmHexadecimal() {
        System.out.println("gerarSalt (só algarismos hexadecimais)");
        boolean expResult = true;
        boolean result = instance.gerarSalt().matches("[0-9a-f]{32}");
        assertEquals(expResult, result);
    }

    @Test
    public void testGerarSaltDiferenteACadaVez() {
        System.out.println("gerarSalt (dois salts seguidos são diferentes)");
        assertNotEquals(instance.gerarSalt(), instance.gerarSalt());
    }

    @Test
    public void testHashPassword() {
        System.out.println("hashPassword (mesmos valores dos dados de teste: gestor / Gestor#2026)");
        String expResult = "1589b18ade723c24d28e360f7d4f431e5c280cd3beabe1585aee022fce93a135";
        String result = instance.hashPassword("Gestor#2026", "587b9e80be35fe6c14e8c0eecdce3e81");
        assertEquals(expResult, result);
    }

    @Test
    public void testHashPasswordTamanho() {
        System.out.println("hashPassword (64 caracteres, como a coluna da base de dados)");
        int expResult = 64;
        int result = instance.hashPassword("Segredo#123", instance.gerarSalt()).length();
        assertEquals(expResult, result);
    }

    @Test
    public void testHashPasswordMesmosDadosMesmoHash() {
        System.out.println("hashPassword (mesma password e mesmo salt dão o mesmo hash)");
        String salt = instance.gerarSalt();
        String expResult = instance.hashPassword("Segredo#123", salt);
        String result = instance.hashPassword("Segredo#123", salt);
        assertEquals(expResult, result);
    }

    @Test
    public void testHashPasswordSaltsDiferentes() {
        System.out.println("hashPassword (salts diferentes dão hashes diferentes)");
        String hashUm = instance.hashPassword("Segredo#123", instance.gerarSalt());
        String hashDois = instance.hashPassword("Segredo#123", instance.gerarSalt());
        assertNotEquals(hashUm, hashDois);
    }

    @Test
    public void testHashPasswordIgnoraEspacosNoSalt() {
        System.out.println("hashPassword (espaços à volta do salt não contam)");
        String salt = instance.gerarSalt();
        String expResult = instance.hashPassword("Segredo#123", salt);
        String result = instance.hashPassword("Segredo#123", " " + salt + " ");
        assertEquals(expResult, result);
    }

    @Test
    public void testVerificarPassword() {
        System.out.println("verificarPassword (password certa)");
        String salt = instance.gerarSalt();
        String hash = instance.hashPassword("Segredo#123", salt);
        boolean expResult = true;
        boolean result = instance.verificarPassword("Segredo#123", salt, hash);
        assertEquals(expResult, result);
    }

    @Test
    public void testVerificarPasswordErrada() {
        System.out.println("verificarPassword (password errada)");
        String salt = instance.gerarSalt();
        String hash = instance.hashPassword("Segredo#123", salt);
        boolean expResult = false;
        boolean result = instance.verificarPassword("segredo#123", salt, hash);
        assertEquals(expResult, result);
    }

    @Test
    public void testVerificarPasswordOutroSalt() {
        System.out.println("verificarPassword (salt diferente do que foi usado no hash)");
        String hash = instance.hashPassword("Segredo#123", instance.gerarSalt());
        boolean expResult = false;
        boolean result = instance.verificarPassword("Segredo#123", instance.gerarSalt(), hash);
        assertEquals(expResult, result);
    }

    @Test
    public void testVerificarPasswordHashEmMaiusculas() {
        System.out.println("verificarPassword (o hash guardado em maiúsculas e com espaços também serve)");
        String salt = instance.gerarSalt();
        String hash = instance.hashPassword("Segredo#123", salt);
        boolean expResult = true;
        boolean result = instance.verificarPassword("Segredo#123", salt, " " + hash.toUpperCase() + " ");
        assertEquals(expResult, result);
    }
}
