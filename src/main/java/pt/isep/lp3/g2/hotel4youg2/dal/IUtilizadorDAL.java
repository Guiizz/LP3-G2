package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.util.List;
import java.util.Optional;

/**
 * Acesso aos utilizadores guardados na base de dados.
 */
public interface IUtilizadorDAL {

    /**
     * Procura um utilizador pelo nome de utilizador.
     *
     * @param nomeUtilizador o nome de utilizador
     * @return o utilizador, ou {@link Optional#empty()} se não existir
     * @throws DataAccessException se não for possível consultar a base de dados
     */
    Optional<Utilizador> procurarPorNomeUtilizador(String nomeUtilizador);

    /**
     * @return todas as contas, ordenadas pelo nome de utilizador
     * @throws DataAccessException se não for possível consultar a base de dados
     */
    List<Utilizador> listar();

    /**
     * Ativa ou desativa uma conta.
     *
     * @param utilizadorId o id da conta
     * @param ativo        {@code true} para ativar, {@code false} para desativar
     * @return {@code true} se a conta existia e foi alterada
     * @throws DataAccessException se não for possível alterar a base de dados
     */
    boolean atualizarAtivo(int utilizadorId, boolean ativo);
}
