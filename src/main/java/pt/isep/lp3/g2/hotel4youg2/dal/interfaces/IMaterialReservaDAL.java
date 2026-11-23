package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;

import java.util.List;

/**
 * Acesso à base de dados dos materiais de cada reserva (US76): por defeito são os da configuração do tipo do
 * quarto (US75); se o rececionista ajustar as quantidades, passam a ser os ajustados, só para essa reserva.
 */
public interface IMaterialReservaDAL {

    /**
     * @param reservaId o id da reserva
     * @return os materiais que a reserva leva e as quantidades (os ajustados, se houver, ou os da configuração
     * do tipo do quarto), ordenados pela descrição; vazio se o tipo do quarto não estiver configurado
     */
    List<MaterialQuarto> listar(int reservaId);

    /**
     * Muda a quantidade de um material só nesta reserva, sem mudar a configuração do tipo de quarto. Da primeira
     * vez, copia para a reserva a configuração do tipo do quarto e só depois muda a quantidade, tudo numa
     * só transação.
     *
     * @param reservaId  o id da reserva
     * @param materialId o id do material
     * @param quantidade a nova quantidade (0 quer dizer que a reserva não leva este material)
     */
    void ajustar(int reservaId, int materialId, int quantidade);
}
