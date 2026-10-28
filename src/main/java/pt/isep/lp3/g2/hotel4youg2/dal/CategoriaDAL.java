package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Categoria;

import java.util.List;

public class CategoriaDAL implements ICategoriaDAL {

    private static final String SELECT_TODAS = "SELECT id, nome FROM Categoria ORDER BY nome";
    private static final String EXISTE_NOME = "SELECT 1 FROM Categoria WHERE LOWER(nome) = LOWER(?)";
    private static final String INSERIR = "INSERT INTO Categoria (nome) OUTPUT INSERTED.id VALUES (?)";
    private static final String APAGAR = "DELETE FROM Categoria WHERE id = ?";

    @Override
    public List<Categoria> listar() {
        return ConexaoBD.select(SELECT_TODAS, rs -> new Categoria(rs.getInt("id"), rs.getString("nome")));
    }

    @Override
    public boolean existeNome(String nome) {
        return !ConexaoBD.select(EXISTE_NOME, rs -> rs.getInt(1), nome).isEmpty();
    }

    @Override
    public int inserir(Categoria categoria) {
        int id = ConexaoBD.select(INSERIR, rs -> rs.getInt(1), categoria.getNome()).get(0);
        categoria.setId(id);
        return id;
    }

    @Override
    public boolean apagar(int id) {
        return ConexaoBD.executar(APAGAR, id) > 0;
    }
}
