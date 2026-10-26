package pt.isep.lp3.g2.hotel4youg2.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Calcula e verifica hashes de passwords com SHA-256 e um salt aleatório.
 * Os valores são guardados em hexadecimal: o hash tem 64 caracteres e o salt 32,
 * que é o tamanho das colunas {@code passwordHash CHAR(64)} e {@code passwordSalt CHAR(32)}.
 * Usa apenas {@code java.security}, sem bibliotecas externas.
 */
public class PasswordUtils {

    private static final int TAMANHO_SALT = 16;
    private static final HexFormat HEX = HexFormat.of();
    private final SecureRandom random = new SecureRandom();

    /**
     * Gera um salt aleatório novo, para usar ao criar ou mudar uma password.
     *
     * @return o salt em hexadecimal (32 caracteres)
     */
    public String gerarSalt() {
        byte[] salt = new byte[TAMANHO_SALT];
        random.nextBytes(salt);
        return HEX.formatHex(salt);
    }

    /**
     * Calcula o hash SHA-256 de uma password com o salt indicado.
     *
     * @param password a password em texto
     * @param salt     o salt em hexadecimal
     * @return o hash em hexadecimal (64 caracteres)
     */
    public String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(HEX.parseHex(salt.trim()));
            byte[] resultado = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(resultado);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não está disponível nesta máquina", e);
        }
    }

    /**
     * Verifica se uma password corresponde ao hash guardado.
     *
     * @param password     a password escrita pelo utilizador
     * @param salt         o salt guardado com o hash
     * @param hashGuardado o hash guardado na base de dados
     * @return {@code true} se a password está correta
     */
    public boolean verificarPassword(String password, String salt, String hashGuardado) {
        byte[] calculado = hashPassword(password, salt).getBytes(StandardCharsets.UTF_8);
        byte[] guardado = hashGuardado.trim().toLowerCase().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(calculado, guardado);
    }
}
