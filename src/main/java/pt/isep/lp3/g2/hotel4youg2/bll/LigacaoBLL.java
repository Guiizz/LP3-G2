package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.LigacaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.ILigacaoDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;

/**
 * Verifica se a aplicação consegue ligar-se à base de dados.
 */
public class LigacaoBLL {

    private final ILigacaoDAL ligacaoDAL;

    /**
     * Cria o BLL ligado ao SQL Server.
     */
    public LigacaoBLL() {
        this(new LigacaoDAL());
    }

    /**
     * Cria o BLL com outro DAL (usado nos testes).
     *
     * @param ligacaoDAL onde testar a ligação
     */
    public LigacaoBLL(ILigacaoDAL ligacaoDAL) {
        this.ligacaoDAL = ligacaoDAL;
    }

    /**
     * Faz uma consulta simples para testar a ligação.
     *
     * @return uma mensagem a dizer se a ligação funcionou ou porque falhou
     */
    public String testarLigacao() {
        try {
            ligacaoDAL.testar();
            return "Ligação à base de dados estabelecida";
        } catch (DataAccessException e) {
            String causa = e.getCause() != null ? e.getCause().getMessage() : "sem detalhe";
            return "Não foi possível ligar: " + e.getMessage() + " (" + causa + ")";
        }
    }

    public void prepararLigacao() {
        ligacaoDAL.preparar();
    }
}
