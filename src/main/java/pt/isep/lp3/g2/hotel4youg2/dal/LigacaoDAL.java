package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.ILigacaoDAL;

/**
 * Implementação do {@link ILigacaoDAL} sobre o SQL Server.
 */
public class LigacaoDAL implements ILigacaoDAL {

    private static final String TESTAR = "SELECT 1";

    /**
     * Faz um {@code SELECT 1} para confirmar que a base de dados responde.
     *
     * @throws pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException se não for possível ligar
     */
    @Override
    public void testar() {
        ConexaoBD.select(TESTAR, new InteiroMapper());
    }

    @Override
    public void preparar() {
        ConexaoBD.preparar();
    }
}
