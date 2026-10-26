package pt.isep.lp3.g2.hotel4youg2.dal;

/**
 * Erro ao aceder à base de dados (sem ligação, SQL errado, etc.).
 * É uma exceção não verificada para não obrigar a declarar {@code throws} em todas as camadas.
 */
public class DataAccessException extends RuntimeException {

    /**
     * @param message a explicação do erro
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * @param message a explicação do erro
     * @param cause   o erro original (normalmente uma {@code SQLException})
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
