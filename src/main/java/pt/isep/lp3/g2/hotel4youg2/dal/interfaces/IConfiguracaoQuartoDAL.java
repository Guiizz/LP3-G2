package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.Material;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import java.util.List;

/**
 * Acesso à base de dados dos materiais (consumíveis ou não) e da configuração de materiais
 * de cada tipo de quarto (US75).
 */
public interface IConfiguracaoQuartoDAL {

    /**
     * @return todos os materiais, com o fornecedor e se são consumíveis, ordenados pela descrição
     */
    List<Material> listarMateriais();

    /**
     * Muda um material para consumível ou não consumível.
     *
     * @param materialId o id do material
     * @param consumivel {@code true} para consumível
     * @return {@code true} se o material existia e foi alterado
     */
    boolean alterarConsumivel(int materialId, boolean consumivel);

    /**
     * @param tipo o tipo de quarto
     * @return os materiais que esse tipo de quarto leva por defeito, com a quantidade, ordenados pela descrição;
     * vazio se ainda não estiver configurado
     */
    List<MaterialQuarto> listarConfiguracao(TipoQuarto tipo);

    /**
     * Junta um material à configuração do tipo de quarto ou, se já lá estiver, muda a quantidade.
     *
     * @param tipo       o tipo de quarto
     * @param materialId o id do material
     * @param quantidade quantas unidades o tipo de quarto leva
     */
    void guardar(TipoQuarto tipo, int materialId, int quantidade);

    /**
     * Retira um material da configuração do tipo de quarto.
     *
     * @param tipo       o tipo de quarto
     * @param materialId o id do material
     * @return {@code true} se o material estava na configuração e foi retirado
     */
    boolean remover(TipoQuarto tipo, int materialId);
}
