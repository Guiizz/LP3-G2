package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IAvaliacaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.Avaliacao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public class AvaliacaoDAL implements IAvaliacaoDAL {

    private static final String INSERIR =
            "INSERT INTO AvaliacaoEstadia (reservaId, estrelas, comentario) OUTPUT INSERTED.id "
            + "SELECT ?, ?, ? WHERE NOT EXISTS "
            + "(SELECT 1 FROM AvaliacaoEstadia WITH (UPDLOCK, HOLDLOCK) WHERE reservaId = ?)";
    private static final String COLUNAS =
            "SELECT a.id, a.reservaId, a.estrelas, a.comentario, a.avaliadaEm, p.nome, r.quartoNumero "
            + "FROM AvaliacaoEstadia a JOIN Reserva r ON r.id = a.reservaId JOIN Pessoa p ON p.id = r.clienteId ";
    private static final String PROCURAR_POR_RESERVA = COLUNAS + "WHERE a.reservaId = ?";
    private static final String LISTAR =
            COLUNAS + "WHERE a.avaliadaEm >= ? AND a.avaliadaEm < ? ORDER BY a.avaliadaEm DESC, a.id DESC";

    @Override
    public boolean inserir(Avaliacao avaliacao) {
        List<Integer> ids = ConexaoBD.select(INSERIR, new InteiroMapper(), avaliacao.getReservaId(),
                avaliacao.getEstrelas(), avaliacao.getComentario(), avaliacao.getReservaId());
        if (ids.isEmpty()) {
            return false;
        }
        avaliacao.setId(ids.get(0));
        return true;
    }

    @Override
    public Avaliacao procurarPorReserva(int reservaId) {
        List<Avaliacao> encontradas = ConexaoBD.select(PROCURAR_POR_RESERVA, new AvaliacaoMapper(), reservaId);
        return encontradas.isEmpty() ? null : encontradas.get(0);
    }

    @Override
    public List<Avaliacao> listar(LocalDate inicio, LocalDate fim) {
        return ConexaoBD.select(LISTAR, new AvaliacaoMapper(), Timestamp.valueOf(inicio.atStartOfDay()),
                Timestamp.valueOf(fim.plusDays(1).atStartOfDay()));
    }

    private static class AvaliacaoMapper implements RowMapper<Avaliacao> {
        @Override
        public Avaliacao map(ResultSet rs) throws SQLException {
            Avaliacao avaliacao = new Avaliacao(rs.getInt("id"), rs.getInt("reservaId"), rs.getInt("estrelas"),
                    rs.getString("comentario"), rs.getTimestamp("avaliadaEm").toLocalDateTime());
            avaliacao.setNomeCliente(rs.getString("nome"));
            avaliacao.setQuartoNumero(rs.getInt("quartoNumero"));
            return avaliacao;
        }
    }
}
