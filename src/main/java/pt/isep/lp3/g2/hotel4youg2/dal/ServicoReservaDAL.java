package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ServicoReservaDAL implements IServicoReservaDAL {

    // só grava se a reserva ainda aceitar serviços e o serviço estiver ativo, mesmo que alguém a cancele ao mesmo tempo
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
            "DELETE FROM ReservaServico WHERE id = ? "
            + "AND reservaId IN (SELECT id FROM Reserva WHERE estado IN ('CONFIRMADA', 'EM_CURSO'))";

    @Override
    public boolean inserirSeAberta(ServicoReserva servicoReserva) {
        List<ServicoReserva> gravado = ConexaoBD.select(INSERIR_SE_ABERTA, rs -> {
            servicoReserva.setId(rs.getInt("id"));
            servicoReserva.setAdicionadoEm(rs.getTimestamp("adicionadoEm").toLocalDateTime());
            return servicoReserva;
        }, servicoReserva.getReservaId(), servicoReserva.getServico().getId(), servicoReserva.getQuantidade(),
                servicoReserva.getPrecoUnitario(), servicoReserva.getReservaId(), servicoReserva.getServico().getId());
        return !gravado.isEmpty();
    }

    @Override
    public List<ServicoReserva> listarPorReserva(int reservaId) {
        return ConexaoBD.select(LISTAR_POR_RESERVA, this::mapear, reservaId);
    }

    @Override
    public boolean apagarSeAberta(int id) {
        return ConexaoBD.executar(APAGAR_SE_ABERTA, id) > 0;
    }

    private ServicoReserva mapear(ResultSet rs) throws SQLException {
        Servico servico = new Servico(rs.getInt("servicoId"), rs.getString("designacao"), rs.getBigDecimal("preco"),
                new Categoria(rs.getInt("categoriaId"), rs.getString("categoriaNome")), rs.getBoolean("ativo"));
        return new ServicoReserva(rs.getInt("id"), rs.getInt("reservaId"), servico, rs.getInt("quantidade"),
                rs.getBigDecimal("precoUnitario"), rs.getTimestamp("adicionadoEm").toLocalDateTime());
    }
}
