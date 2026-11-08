package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;

import java.util.List;

public interface IStockDAL {

    List<MaterialStock> listar();

    boolean registarSaida(int materialId, int quantidade, int funcionarioId);
}
