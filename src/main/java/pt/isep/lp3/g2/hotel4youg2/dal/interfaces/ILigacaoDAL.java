package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

/**
 * Acesso à base de dados para testar a ligação.
 */
public interface ILigacaoDAL {

    /**
     * Faz uma consulta simples à base de dados.
     *
     * @throws pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException se não for possível ligar
     */
    void testar();

    void preparar();
}
