package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoSugestao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SugestaoQueixaDAL implements ISugestaoQueixaDAL {

    // a data não vai no INSERT: é a BD que a põe (DEFAULT SYSDATETIME()) e devolve-a com o id
    private static final String INSERIR =
            "INSERT INTO SugestaoQueixa (clienteId, tipo, descricao, registadaPor) "
            + "OUTPUT INSERTED.id, INSERTED.registadaEm VALUES (?, ?, ?, ?)";
    private static final String PROCURAR_POR_ID =
            "SELECT id, clienteId, tipo, descricao, registadaPor, registadaEm FROM SugestaoQueixa WHERE id = ?";
    private static final String APAGAR = "DELETE FROM SugestaoQueixa WHERE id = ?";

    @Override
    public int inserir(SugestaoQueixa sugestao) {
        ConexaoBD.select(INSERIR, rs -> {
            sugestao.setId(rs.getInt("id"));
            sugestao.setRegistadaEm(rs.getTimestamp("registadaEm").toLocalDateTime());
            return sugestao;
        }, sugestao.getClienteId(), sugestao.getTipo().name(), sugestao.getDescricao(), sugestao.getRegistadaPor());
        return sugestao.getId();
    }

    @Override
    public Optional<SugestaoQueixa> procurarPorId(int id) {
        return ConexaoBD.select(PROCURAR_POR_ID, this::mapear, id).stream().findFirst();
    }

    @Override
    public boolean apagar(int id) {
        return ConexaoBD.executar(APAGAR, id) > 0;
    }

    private SugestaoQueixa mapear(ResultSet rs) throws SQLException {
        return new SugestaoQueixa(rs.getInt("id"), rs.getInt("clienteId"), TipoSugestao.valueOf(rs.getString("tipo")),
                rs.getString("descricao"), rs.getInt("registadaPor"), rs.getTimestamp("registadaEm").toLocalDateTime());
    }
}
