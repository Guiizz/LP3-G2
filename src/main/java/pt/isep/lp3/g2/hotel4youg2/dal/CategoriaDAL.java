package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.ICategoriaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação do {@link ICategoriaDAL} sobre a tabela Categoria do SQL Server.
 */
public class CategoriaDAL implements ICategoriaDAL {

    private static final String SELECT_TODAS = "SELECT id, nome FROM Categoria ORDER BY nome";
    private static final String EXISTE_NOME = "SELECT 1 FROM Categoria WHERE LOWER(nome) = LOWER(?)";
    private static final String INSERIR = "INSERT INTO Categoria (nome) OUTPUT INSERTED.id VALUES (?)";
    private static final String APAGAR = "DELETE FROM Categoria WHERE id = ?";

    @Override
    public List<Categoria> listar() {
        return ConexaoBD.select(SELECT_TODAS, new CategoriaMapper());
    }

    @Override
    public boolean existeNome(String nome) {
        return !ConexaoBD.select(EXISTE_NOME, new InteiroMapper(), nome).isEmpty();
    }

    @Override
    public int inserir(Categoria categoria) {
        int id = ConexaoBD.select(INSERIR, new InteiroMapper(), categoria.getNome()).get(0);
        categoria.setId(id);
        return id;
    }

    @Override
    public boolean apagar(int id) {
        return ConexaoBD.executar(APAGAR, id) > 0;
    }

    private static class CategoriaMapper implements RowMapper<Categoria> {
        @Override
        public Categoria map(ResultSet rs) throws SQLException {
            return new Categoria(rs.getInt("id"), rs.getString("nome"));
        }
    }
}
