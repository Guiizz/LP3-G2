package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

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
}
