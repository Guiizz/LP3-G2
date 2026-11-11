package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.Servico;

import java.util.List;
import java.util.Optional;

/**
 * Acesso aos serviços extra na base de dados.
 */
public interface IServicoDAL {

    /**
     * @return todos os serviços, ativos e desativados, ordenados pela categoria e pela designação
     */
    List<Servico> listar();

    /**
     * @param id o id do serviço
     * @return o serviço, ou vazio se não existir
     */
    Optional<Servico> procurarPorId(int id);

    /**
     * Diz se já existe outro serviço com a mesma designação (sem distinguir maiúsculas).
     *
     * @param designacao a designação a procurar
     * @param idIgnorar  o id do serviço a ignorar, para não o comparar consigo próprio ao editar
     *                   (0 ao criar)
     * @return {@code true} se já existir
     */
    boolean existeDesignacao(String designacao, int idIgnorar);

    /**
     * Grava o serviço e atualiza o seu id.
     *
     * @param servico o serviço a gravar
     * @return o id gerado
     */
    int inserir(Servico servico);

    /**
     * Atualiza a designação, o preço e a categoria do serviço.
     * Não mexe no estado (ativo ou desativado).
     *
     * @param servico o serviço com os novos dados
     * @return {@code true} se o serviço existia
     */
    boolean atualizar(Servico servico);

    /**
     * @param id    o id do serviço
     * @param ativo {@code true} para reativar o serviço, {@code false} para o desativar
     * @return {@code true} se o serviço existia
     */
    boolean atualizarAtivo(int id, boolean ativo);

    /**
     * Apaga o serviço. Na aplicação os serviços nunca se apagam, só se desativam;
     * isto serve para limpar dados de teste.
     *
     * @param id o id do serviço
     * @return {@code true} se o serviço existia e foi apagado
     */
    boolean apagar(int id);
}
