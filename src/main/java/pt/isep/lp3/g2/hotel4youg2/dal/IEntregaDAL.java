package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Entrega;

import java.util.Optional;

public interface IEntregaDAL {

    boolean existeEntrega(String codigoFornecedor, String numeroEncomenda);

    int registar(Entrega entrega, int funcionarioId);

    Optional<Entrega> procurarPorId(int id);

    boolean apagar(int id);
}
