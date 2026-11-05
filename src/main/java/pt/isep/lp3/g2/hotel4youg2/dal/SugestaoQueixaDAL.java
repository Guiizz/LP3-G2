package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoSugestao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SugestaoQueixaDAL implements ISugestaoQueixaDAL {

    // a data não vai no INSERT: é a BD que a põe (DEFAULT SYSDATETIME()) e devolve-a com o id
    private static final String INSERIR =
            "INSERT INTO SugestaoQueixa (clienteId, tipo, descricao, registadaPor) "
            + "OUTPUT INSERTED.id, INSERTED.registadaEm VALUES (?, ?, ?, ?)";
    private static final String PROCURAR_POR_ID =
            "SELECT id, clienteId, tipo, descricao, registadaPor, registadaEm FROM SugestaoQueixa WHERE id = ?";
    private static final String APAGAR = "DELETE FROM SugestaoQueixa WHERE id = ?";

    private static final String SELECT_COM_CLIENTE =
            "SELECT s.id, s.clienteId, s.tipo, s.descricao, s.registadaPor, s.registadaEm, p.nome AS nomeCliente "
                    + "FROM SugestaoQueixa s JOIN Pessoa p ON p.id = s.clienteId ";

    private static final String LISTAR = SELECT_COM_CLIENTE + "ORDER BY s.registadaEm DESC, s.id DESC";

    private static final String LISTAR_POR_TIPO =
            SELECT_COM_CLIENTE + "WHERE s.tipo = ? ORDER BY s.registadaEm DESC, s.id DESC";

    @Override
    public int inserir(SugestaoQueixa sugestao) {
        ConexaoBD.select(INSERIR, new DadosGeradosMapper(sugestao), sugestao.getClienteId(), sugestao.getTipo().name(),
                sugestao.getDescricao(), sugestao.getRegistadaPor());
        return sugestao.getId();
    }

    @Override
    public Optional<SugestaoQueixa> procurarPorId(int id) {
        List<SugestaoQueixa> encontradas = ConexaoBD.select(PROCURAR_POR_ID, new SugestaoMapper(), id);
        return encontradas.isEmpty() ? Optional.empty() : Optional.of(encontradas.get(0));
    }

    @Override
    public boolean apagar(int id) {
        return ConexaoBD.executar(APAGAR, id) > 0;
    }

    @Override
    public List<SugestaoQueixa> listar(TipoSugestao tipo) {
        if (tipo == null) {
            return ConexaoBD.select(LISTAR, new SugestaoComClienteMapper());
        }
        return ConexaoBD.select(LISTAR_POR_TIPO, new SugestaoComClienteMapper(), tipo.name());
    }

    private static class SugestaoComClienteMapper implements RowMapper<SugestaoQueixa> {
        @Override
        public SugestaoQueixa map(ResultSet rs) throws SQLException {
            SugestaoQueixa s = new SugestaoQueixa(rs.getInt("id"), rs.getInt("clienteId"),
                    TipoSugestao.valueOf(rs.getString("tipo")), rs.getString("descricao"),
                    rs.getInt("registadaPor"), rs.getTimestamp("registadaEm").toLocalDateTime());
            s.setNomeCliente(rs.getString("nomeCliente"));
            return s;
        }
    }

    private static class SugestaoMapper implements RowMapper<SugestaoQueixa> {
        @Override
        public SugestaoQueixa map(ResultSet rs) throws SQLException {
            return new SugestaoQueixa(rs.getInt("id"), rs.getInt("clienteId"), TipoSugestao.valueOf(rs.getString("tipo")),
                    rs.getString("descricao"), rs.getInt("registadaPor"), rs.getTimestamp("registadaEm").toLocalDateTime());
        }
    }

    private static class DadosGeradosMapper implements RowMapper<SugestaoQueixa> {
        private final SugestaoQueixa sugestao;

        DadosGeradosMapper(SugestaoQueixa sugestao) {
            this.sugestao = sugestao;
        }

        @Override
        public SugestaoQueixa map(ResultSet rs) throws SQLException {
            sugestao.setId(rs.getInt("id"));
            sugestao.setRegistadaEm(rs.getTimestamp("registadaEm").toLocalDateTime());
            return sugestao;
        }
    }
}
