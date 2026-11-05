package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Entrega;

import java.util.Optional;

/**
 * Acesso às entregas de fornecedores na base de dados.
 */
public interface IEntregaDAL {

    /**
     * @param codigoFornecedor o código do fornecedor
     * @param numeroEncomenda  o número da encomenda
     * @return {@code true} se já houver uma entrega com este número para este fornecedor,
     * sem contar as que foram recusadas (essas podem ser enviadas outra vez)
     */
    boolean existeEntrega(String codigoFornecedor, String numeroEncomenda);

    /**
     * @param codigoFornecedor o código do fornecedor
     * @return quantas entregas deste fornecedor estão pendentes, à espera da decisão do hotel
     */
    int contarPendentes(String codigoFornecedor);

    /**
     * Grava uma entrega carregada pelo fornecedor, como pendente e sem funcionário associado,
     * com o fornecedor, os materiais e as linhas numa só transação. Não mexe no stock.
     * Atualiza o id e o estado da entrega e o id do fornecedor.
     *
     * @param entrega a entrega a gravar
     * @return o id da entrega gravada
     */
    int carregar(Entrega entrega);

    /**
     * Grava o fornecedor, os materiais, a entrega e as suas linhas numa só transação:
     * ou fica tudo gravado, ou nada. Atualiza o id da entrega e o do fornecedor.
     *
     * @param entrega       a entrega a gravar
     * @param funcionarioId o id do funcionário que a registou
     * @return o id da entrega gravada
     */
    int registar(Entrega entrega, int funcionarioId);

    /**
     * @param id o id da entrega
     * @return a entrega com as suas linhas, ou vazio se não existir
     */
    Optional<Entrega> procurarPorId(int id);

    /**
     * Apaga a entrega e as suas linhas.
     *
     * @param id o id da entrega
     * @return {@code true} se a entrega existia e foi apagada
     */
    boolean apagar(int id);
}
