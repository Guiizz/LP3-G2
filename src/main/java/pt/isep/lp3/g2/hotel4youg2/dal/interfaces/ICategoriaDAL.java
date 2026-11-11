package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.Categoria;

import java.util.List;

/**
 * Acesso às categorias dos serviços extra na base de dados.
 */
public interface ICategoriaDAL {

    /**
     * @return todas as categorias, ordenadas pelo nome
     */
    List<Categoria> listar();

    /**
     * @param nome o nome a procurar
     * @return {@code true} se já existir uma categoria com este nome (sem distinguir maiúsculas)
     */
    boolean existeNome(String nome);

    /**
     * Grava a categoria e atualiza o seu id.
     *
     * @param categoria a categoria a gravar
     * @return o id gerado
     */
    int inserir(Categoria categoria);

    /**
     * Apaga a categoria. Só funciona se nenhum serviço a estiver a usar.
     *
     * @param id o id da categoria
     * @return {@code true} se a categoria existia e foi apagada
     */
    boolean apagar(int id);
}
