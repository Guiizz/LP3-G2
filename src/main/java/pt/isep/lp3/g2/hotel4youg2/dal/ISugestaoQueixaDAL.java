package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoSugestao;

import java.util.List;
import java.util.Optional;

public interface ISugestaoQueixaDAL {

    int inserir(SugestaoQueixa sugestao);

    Optional<SugestaoQueixa> procurarPorId(int id);

    boolean apagar(int id);

    List<SugestaoQueixa> listar(TipoSugestao tipo);
}
