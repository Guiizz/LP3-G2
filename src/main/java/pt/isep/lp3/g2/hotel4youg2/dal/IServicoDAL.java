package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.TipoCobranca;

import java.util.List;
import java.util.Optional;

public interface IServicoDAL {

    List<Servico> listar();

    Optional<Servico> procurarPorId(int id);

    boolean existeDesignacao(String designacao, TipoCobranca tipoCobranca, int idIgnorar);

    int inserir(Servico servico);

    boolean atualizar(Servico servico);

    boolean atualizarAtivo(int id, boolean ativo);

    boolean apagar(int id);
}
