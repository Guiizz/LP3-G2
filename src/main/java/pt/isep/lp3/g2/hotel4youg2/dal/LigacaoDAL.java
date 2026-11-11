package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.ILigacaoDAL;

public class LigacaoDAL implements ILigacaoDAL {

    private static final String TESTAR = "SELECT 1";

    @Override
    public void testar() {
        ConexaoBD.select(TESTAR, new InteiroMapper());
    }
}
