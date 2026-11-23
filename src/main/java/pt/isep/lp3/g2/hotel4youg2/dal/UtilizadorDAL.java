package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do {@link IUtilizadorDAL} sobre a tabela Utilizador do SQL Server.
 */
public class UtilizadorDAL implements IUtilizadorDAL {

    private static final String SELECT_POR_NOME =
            "SELECT id, funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil, ativo, mudarPassword "
            + "FROM Utilizador WHERE nomeUtilizador = ?";
    private static final String SELECT_TODOS =
            "SELECT id, funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil, ativo, mudarPassword "
            + "FROM Utilizador ORDER BY nomeUtilizador";
    private static final String UPDATE_ATIVO = "UPDATE Utilizador SET ativo = ? WHERE id = ?";
    private static final String UPDATE_PASSWORD =
            "UPDATE Utilizador SET passwordHash = ?, passwordSalt = ?, mudarPassword = ? WHERE id = ?";

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Utilizador> procurarPorNomeUtilizador(String nomeUtilizador) {
        List<Utilizador> resultados = ConexaoBD.select(SELECT_POR_NOME, new UtilizadorMapper(), nomeUtilizador);
        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }

    @Override
    public List<Utilizador> listar() {
        return ConexaoBD.select(SELECT_TODOS, new UtilizadorMapper());
    }

    @Override
    public boolean atualizarAtivo(int utilizadorId, boolean ativo) {
        return ConexaoBD.executar(UPDATE_ATIVO, ativo, utilizadorId) > 0;
    }

    @Override
    public boolean definirPassword(int utilizadorId, String passwordHash, String passwordSalt,
                                   boolean mudarPassword) {
        return ConexaoBD.executar(UPDATE_PASSWORD, passwordHash, passwordSalt, mudarPassword, utilizadorId) > 0;
    }

    private static class UtilizadorMapper implements RowMapper<Utilizador> {
        @Override
        public Utilizador map(ResultSet rs) throws SQLException {
            Utilizador utilizador = new Utilizador(
                    rs.getInt("id"),
                    rs.getInt("funcionarioId"),
                    rs.getString("nomeUtilizador"),
                    rs.getString("passwordHash").trim(),
                    rs.getString("passwordSalt").trim(),
                    Perfil.valueOf(rs.getString("perfil")),
                    rs.getBoolean("ativo"));
            utilizador.setMudarPassword(rs.getBoolean("mudarPassword"));
            return utilizador;
        }
    }
}
