package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do {@link IUtilizadorDAL} sobre a tabela Utilizador do SQL Server.
 */
public class UtilizadorDAL implements IUtilizadorDAL {

    private static final String SELECT_POR_NOME =
            "SELECT id, funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil, ativo "
            + "FROM Utilizador WHERE nomeUtilizador = ?";
    private static final String SELECT_TODOS =
            "SELECT id, funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil, ativo "
            + "FROM Utilizador ORDER BY nomeUtilizador";
    private static final String UPDATE_ATIVO = "UPDATE Utilizador SET ativo = ? WHERE id = ?";
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Utilizador> procurarPorNomeUtilizador(String nomeUtilizador) {
        List<Utilizador> resultados = ConexaoBD.select(SELECT_POR_NOME, this::mapear, nomeUtilizador);
        return resultados.stream().findFirst();
    }

    @Override
    public List<Utilizador> listar() {
        return ConexaoBD.select(SELECT_TODOS, this::mapear);
    }

    @Override
    public boolean atualizarAtivo(int utilizadorId, boolean ativo) {
        return ConexaoBD.executar(UPDATE_ATIVO, ativo, utilizadorId) > 0;
    }

    private Utilizador mapear(ResultSet rs) throws SQLException {
        return new Utilizador(
                rs.getInt("id"),
                rs.getInt("funcionarioId"),
                rs.getString("nomeUtilizador"),
                rs.getString("passwordHash").trim(),
                rs.getString("passwordSalt").trim(),
                Perfil.valueOf(rs.getString("perfil")),
                rs.getBoolean("ativo"));
    }
}
