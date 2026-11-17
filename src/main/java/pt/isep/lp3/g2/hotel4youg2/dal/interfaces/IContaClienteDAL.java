package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;

public interface IContaClienteDAL {

    int inserir(ContaCliente conta);

    ContaCliente procurarPorNif(String nif);

    boolean definirPassword(int contaId, String passwordHash, String passwordSalt);

    boolean novoCodigo(int contaId, String passwordHash, String passwordSalt);

    boolean apagar(int contaId);
}
