package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import java.util.List;

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

    /**
     * Procura o funcionário de um cartão, pelo número de funcionário (US52).
     *
     * @param numeroFuncionario o número que vem a seguir ao F no cartão
     * @return o funcionário, ou {@code null} se não existir
     * @throws DataAccessException se não for possível ler a base de dados
     */
    Funcionario procurarPorNumero(int numeroFuncionario);

    /**
     * @param pessoaId o id da pessoa do funcionário
     * @return o funcionário, ou {@code null} se não existir (US52)
     * @throws DataAccessException se não for possível ler a base de dados
     */
    Funcionario procurarPorId(int pessoaId);

    /**
     * @param pessoaId o id da pessoa do funcionário
     * @return {@code true} se o funcionário tiver uma conta ativa; só esses podem picar o cartão (US52)
     * @throws DataAccessException se não for possível ler a base de dados
     */
    boolean temContaAtiva(int pessoaId);

    /**
     * @return os funcionários com a conta ativa, pelo número de funcionário (US52)
     * @throws DataAccessException se não for possível ler a base de dados
     */
    List<Funcionario> listarComContaAtiva();

    boolean editar(Funcionario funcionario, int utilizadorId, Perfil perfil);
}
