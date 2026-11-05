package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;

import java.util.Optional;

/**
 * Acesso aos fornecedores e às suas contas na base de dados.
 */
public interface IFornecedorDAL {

    /**
     * Procura um fornecedor pelo código, com o hash e o salt da password (se já tiver conta).
     *
     * @param codigo o código do fornecedor
     * @return o fornecedor, ou {@link Optional#empty()} se não existir
     * @throws DataAccessException se não for possível consultar a base de dados
     */
    Optional<Fornecedor> procurarPorCodigo(String codigo);
}
