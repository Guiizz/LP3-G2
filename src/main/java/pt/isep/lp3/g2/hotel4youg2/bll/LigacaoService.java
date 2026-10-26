package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.ConexaoBD;
import pt.isep.lp3.g2.hotel4youg2.dal.DataAccessException;

/**
 * Verifica se a aplicação consegue ligar-se à base de dados.
 */
public class LigacaoService {

    /**
     * Faz uma consulta simples para testar a ligação.
     *
     * @return uma mensagem a dizer se a ligação funcionou ou porque falhou
     */
    public String testarLigacao() {
        try {
            ConexaoBD.select("SELECT 1", rs -> rs.getInt(1));
            return "Ligação à base de dados estabelecida";
        } catch (DataAccessException e) {
            String causa = e.getCause() != null ? e.getCause().getMessage() : "sem detalhe";
            return "Não foi possível ligar: " + e.getMessage() + " (" + causa + ")";
        }
    }
}
