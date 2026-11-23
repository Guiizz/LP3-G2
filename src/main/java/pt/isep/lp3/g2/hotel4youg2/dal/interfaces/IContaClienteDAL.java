package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;

/**
 * Acesso aos dados das contas dos clientes.
 */
public interface IContaClienteDAL {

    /**
     * Grava uma conta nova, ainda no primeiro acesso. Preenche o id da conta.
     *
     * @param conta a conta a gravar
     * @return o id da conta
     */
    int inserir(ContaCliente conta);

    /**
     * @param nif o NIF do cliente
     * @return a conta do cliente, ou {@code null} se não tiver conta
     */
    ContaCliente procurarPorNif(String nif);

    /**
     * Guarda a palavra-passe escolhida pelo cliente no primeiro acesso.
     *
     * @param contaId      o id da conta
     * @param passwordHash o hash da palavra-passe
     * @param passwordSalt o salt da palavra-passe
     * @return {@code true} se foi guardada; {@code false} se a conta já não estava no primeiro acesso
     */
    boolean definirPassword(int contaId, String passwordHash, String passwordSalt);

    /**
     * Guarda um código novo da receção e volta a pôr a conta no primeiro acesso.
     *
     * @param contaId      o id da conta
     * @param passwordHash o hash do código
     * @param passwordSalt o salt do código
     * @return {@code true} se a conta existe e foi alterada
     */
    boolean novoCodigo(int contaId, String passwordHash, String passwordSalt);

    /**
     * Apaga uma conta (usado nos testes).
     *
     * @param contaId o id da conta
     * @return {@code true} se a conta existia e foi apagada
     */
    boolean apagar(int contaId);
}
