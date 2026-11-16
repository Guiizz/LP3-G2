package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação em SQL Server de {@link IServicoReservaDAL}. O gravar e o retirar verificam
 * o estado da reserva na mesma instrução, com {@code UPDLOCK}, para não juntar serviços
 * a uma reserva que outro funcionário esteja a cancelar ao mesmo tempo.
 */
public class ServicoReservaDAL implements IServicoReservaDAL {

    private static final String INSERIR_SE_ABERTA =
            "INSERT INTO ReservaServico (reservaId, servicoId, quantidade, precoUnitario) "
            + "OUTPUT INSERTED.id, INSERTED.adicionadoEm "
            + "SELECT ?, ?, ?, ? "
            + "WHERE EXISTS (SELECT 1 FROM Reserva WITH (UPDLOCK, HOLDLOCK) "
            + "WHERE id = ? AND estado IN ('CONFIRMADA', 'EM_CURSO')) "
            + "AND EXISTS (SELECT 1 FROM Servico WHERE id = ? AND ativo = 1)";
    private static final String LISTAR_POR_RESERVA =
            "SELECT rs.id, rs.reservaId, rs.quantidade, rs.precoUnitario, rs.adicionadoEm, "
            + "s.id AS servicoId, s.designacao, s.preco, s.ativo, c.id AS categoriaId, c.nome AS categoriaNome "
            + "FROM ReservaServico rs JOIN Servico s ON s.id = rs.servicoId JOIN Categoria c ON c.id = s.categoriaId "
            + "WHERE rs.reservaId = ? ORDER BY rs.adicionadoEm, rs.id";
    private static final String APAGAR_SE_ABERTA =
            "DELETE FROM ReservaServico WHERE id = ? AND reservaId = ? "
            + "AND EXISTS (SELECT 1 FROM Reserva WITH (UPDLOCK, HOLDLOCK) "
            + "WHERE id = ? AND estado IN ('CONFIRMADA', 'EM_CURSO'))";

    @Override
    public boolean inserirSeAberta(ServicoReserva servicoReserva) {
        List<ServicoReserva> gravado = ConexaoBD.select(INSERIR_SE_ABERTA, new DadosGeradosMapper(servicoReserva),
                servicoReserva.getReservaId(), servicoReserva.getServico().getId(), servicoReserva.getQuantidade(),
                servicoReserva.getPrecoUnitario(), servicoReserva.getReservaId(), servicoReserva.getServico().getId());
        return !gravado.isEmpty();
    }

    @Override
    public List<ServicoReserva> listarPorReserva(int reservaId) {
        return ConexaoBD.select(LISTAR_POR_RESERVA, new ServicoReservaMapper(), reservaId);
    }

    @Override
    public boolean apagarSeAberta(int id, int reservaId) {
        return ConexaoBD.executar(APAGAR_SE_ABERTA, id, reservaId, reservaId) > 0;
    }

    private static class ServicoReservaMapper implements RowMapper<ServicoReserva> {
        @Override
        public ServicoReserva map(ResultSet rs) throws SQLException {
            Servico servico = new Servico(rs.getInt("servicoId"), rs.getString("designacao"), rs.getBigDecimal("preco"),
                    new Categoria(rs.getInt("categoriaId"), rs.getString("categoriaNome")), rs.getBoolean("ativo"));
            return new ServicoReserva(rs.getInt("id"), rs.getInt("reservaId"), servico, rs.getInt("quantidade"),
                    rs.getBigDecimal("precoUnitario"), rs.getTimestamp("adicionadoEm").toLocalDateTime());
        }
    }

    private static class DadosGeradosMapper implements RowMapper<ServicoReserva> {
        private final ServicoReserva servicoReserva;

        DadosGeradosMapper(ServicoReserva servicoReserva) {
            this.servicoReserva = servicoReserva;
        }

        @Override
        public ServicoReserva map(ResultSet rs) throws SQLException {
            servicoReserva.setId(rs.getInt("id"));
            servicoReserva.setAdicionadoEm(rs.getTimestamp("adicionadoEm").toLocalDateTime());
            return servicoReserva;
        }
    }
}
