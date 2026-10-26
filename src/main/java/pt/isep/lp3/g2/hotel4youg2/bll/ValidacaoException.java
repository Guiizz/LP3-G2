package pt.isep.lp3.g2.hotel4youg2.bll;

/**
 * Erro de validação dos dados de um cliente (por exemplo, NIF repetido ou menor de 18 anos).
 * A mensagem é mostrada tal como está ao utilizador.
 */
public class ValidacaoException extends RuntimeException {

    /**
     * @param mensagem a explicação do erro, para mostrar no ecrã
     */
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
