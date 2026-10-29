package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;

import java.util.Optional;

public interface ISugestaoQueixaDAL {

    int inserir(SugestaoQueixa sugestao);

    Optional<SugestaoQueixa> procurarPorId(int id);

    boolean apagar(int id);
}
