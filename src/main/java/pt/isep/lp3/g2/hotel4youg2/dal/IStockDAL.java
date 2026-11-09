package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;

import java.util.List;

/**
 * Acesso à base de dados do stock de materiais e das saídas (US42).
 */
public interface IStockDAL {

    /**
     * @return o stock de cada material, com o fornecedor, ordenado pela descrição do material
     */
    List<MaterialStock> listar();

    /**
     * Tira a quantidade do stock e regista o movimento de saída, numa só transação.
     * Só tira se houver quantidade suficiente.
     *
     * @param materialId    o id do material
     * @param quantidade    quantas unidades saem
     * @param funcionarioId o id do funcionário que regista a saída
     * @return {@code true} se a saída foi registada; {@code false} se não havia quantidade suficiente
     */
    boolean registarSaida(int materialId, int quantidade, int funcionarioId);
}
