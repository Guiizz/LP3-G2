package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

/**
 * Acesso aos funcionários e às suas contas na base de dados.
 */
public interface IFuncionarioDAL {

    /**
     * @param nif o NIF a procurar
     * @return {@code true} se já existir uma pessoa (cliente ou funcionário) com este NIF
     */
    boolean existeNif(String nif);

    /**
     * @param nomeUtilizador o nome de utilizador a procurar
     * @return {@code true} se já existir uma conta com este nome
     */
    boolean existeNomeUtilizador(String nomeUtilizador);

    /**
     * Grava a pessoa, o funcionário e a conta numa só transação:
     * ou fica tudo gravado, ou nada.
     *
     * @param funcionario os dados do funcionário
     * @param utilizador  a conta, já com o hash e o salt da password
     * @throws DataAccessException se não for possível gravar
     */
    void criarConta(Funcionario funcionario, Utilizador utilizador);
}
