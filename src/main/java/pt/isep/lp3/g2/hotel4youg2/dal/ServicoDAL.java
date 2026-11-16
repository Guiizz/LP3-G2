package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do {@link IServicoDAL} sobre as tabelas Servico e Categoria do SQL Server.
 */
public class ServicoDAL implements IServicoDAL {

    private static final String COLUNAS = "SELECT s.id, s.designacao, s.preco, s.ativo, c.id AS categoriaId, c.nome AS categoriaNome " + "FROM Servico s JOIN Categoria c ON c.id = s.categoriaId ";
    private static final String SELECT_TODOS = COLUNAS + "ORDER BY c.nome, s.designacao";
    private static final String SELECT_POR_ID = COLUNAS + "WHERE s.id = ?";
    private static final String EXISTE_DESIGNACAO = "SELECT 1 FROM Servico WHERE LOWER(designacao) = LOWER(?) AND id <> ?";
    private static final String INSERIR = "INSERT INTO Servico (designacao, preco, categoriaId, ativo) " + "OUTPUT INSERTED.id VALUES (?, ?, ?, ?)";
    private static final String ATUALIZAR = "UPDATE Servico SET designacao = ?, preco = ?, categoriaId = ? WHERE id = ?";
    private static final String ATUALIZAR_ATIVO = "UPDATE Servico SET ativo = ? WHERE id = ?";
    private static final String APAGAR = "DELETE FROM Servico WHERE id = ?";

    @Override
    public List<Servico> listar() {
        return ConexaoBD.select(SELECT_TODOS, new ServicoMapper());
    }

    @Override
    public Optional<Servico> procurarPorId(int id) {
        List<Servico> servicos = ConexaoBD.select(SELECT_POR_ID, new ServicoMapper(), id);
        return servicos.isEmpty() ? Optional.empty() : Optional.of(servicos.get(0));
    }

    @Override
    public boolean existeDesignacao(String designacao, int idIgnorar) {
        return !ConexaoBD.select(EXISTE_DESIGNACAO, new InteiroMapper(), designacao, idIgnorar).isEmpty();
    }

    @Override
    public int inserir(Servico servico) {
        int id = ConexaoBD.select(INSERIR, new InteiroMapper(), servico.getDesignacao(), servico.getPreco(), servico.getCategoria().getId(), servico.isAtivo()).get(0);
        servico.setId(id);
        return id;
    }

    @Override
    public boolean atualizar(Servico servico) {
        return ConexaoBD.executar(ATUALIZAR, servico.getDesignacao(), servico.getPreco(), servico.getCategoria().getId(), servico.getId()) > 0;
    }

    @Override
    public boolean atualizarAtivo(int id, boolean ativo) {
        return ConexaoBD.executar(ATUALIZAR_ATIVO, ativo, id) > 0;
    }

    @Override
    public boolean apagar(int id) {
        return ConexaoBD.executar(APAGAR, id) > 0;
    }

    private static class ServicoMapper implements RowMapper<Servico> {
        @Override
        public Servico map(ResultSet rs) throws SQLException {
            return new Servico(rs.getInt("id"), rs.getString("designacao"), rs.getBigDecimal("preco"), new Categoria(rs.getInt("categoriaId"), rs.getString("categoriaNome")), rs.getBoolean("ativo"));
        }
    }
}
