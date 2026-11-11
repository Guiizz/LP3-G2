package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
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

    /**
     * Grava um fornecedor novo, só com o código, o nome e a conta (hash e salt da password).
     * Atualiza o id do fornecedor.
     *
     * @param fornecedor o fornecedor a gravar
     * @return o id do fornecedor gravado
     * @throws DataAccessException se não for possível gravar na base de dados
     */
    int criar(Fornecedor fornecedor);

    /**
     * Define a conta (hash e salt da password) de um fornecedor que já existe mas ainda não tem
     * password, por exemplo porque foi criado a partir de uma entrega. Não altera uma password
     * que já exista.
     *
     * @param fornecedorId o id do fornecedor
     * @param passwordHash o hash da password, em hexadecimal
     * @param passwordSalt o salt usado, em hexadecimal
     * @return {@code true} se o fornecedor existia sem password e foi alterado
     * @throws DataAccessException se não for possível alterar a base de dados
     */
    boolean definirPassword(int fornecedorId, String passwordHash, String passwordSalt);
}
