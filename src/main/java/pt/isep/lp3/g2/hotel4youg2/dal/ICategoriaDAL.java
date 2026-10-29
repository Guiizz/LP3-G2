package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Categoria;

import java.util.List;

public interface ICategoriaDAL {

    List<Categoria> listar();

    boolean existeNome(String nome);

    int inserir(Categoria categoria);

    boolean apagar(int id);
}
