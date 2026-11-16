package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IAcessoCartaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.CartaoAtivo;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação em SQL Server de {@link IAcessoCartaoDAL}.
 */
public class AcessoCartaoDAL implements IAcessoCartaoDAL {

    private static final String REGISTAR =
            "INSERT INTO AcessoCartao (cartao, localAcesso, quartoNumero, reservaId, funcionarioId) "
            + "OUTPUT INSERTED.id, INSERTED.registadoEm VALUES (?, ?, NULLIF(?, 0), NULLIF(?, 0), NULLIF(?, 0))";
    private static final String LISTAR =
            "SELECT a.id, a.cartao, a.localAcesso, a.quartoNumero, a.reservaId, a.registadoEm, a.funcionarioId, "
            + "p.nome AS funcionarioNome FROM AcessoCartao a LEFT JOIN Pessoa p ON p.id = a.funcionarioId ";
    private static final String LISTAR_POR_CARTAO =
            LISTAR + "WHERE a.cartao = ? ORDER BY a.registadoEm DESC, a.id DESC";
    private static final String LISTAR_POR_QUARTO =
            LISTAR + "WHERE a.quartoNumero = ? ORDER BY a.registadoEm DESC, a.id DESC";
    private static final String APAGAR = "DELETE FROM AcessoCartao WHERE id = ?";
    private static final String CARTOES_DE_CLIENTES =
            "SELECT r.cartao, p.nome, r.quartoNumero FROM Reserva r JOIN Pessoa p ON p.id = r.clienteId "
            + "WHERE r.estado = 'EM_CURSO' AND r.cartao IS NOT NULL ORDER BY r.quartoNumero";

    @Override
    public void registar(AcessoCartao acesso) {
        ConexaoBD.select(REGISTAR, new DadosGeradosMapper(acesso), acesso.getCartao(), acesso.getLocal().name(),
                acesso.getQuartoNumero(), acesso.getReservaId(), acesso.getFuncionarioId());
    }

    @Override
    public List<AcessoCartao> listarPorCartao(String cartao) {
        return ConexaoBD.select(LISTAR_POR_CARTAO, new AcessoCartaoMapper(), cartao);
    }

    @Override
    public List<AcessoCartao> listarPorQuarto(int quartoNumero) {
        return ConexaoBD.select(LISTAR_POR_QUARTO, new AcessoCartaoMapper(), quartoNumero);
    }

    @Override
    public boolean apagar(int id) {
        return ConexaoBD.executar(APAGAR, id) > 0;
    }

    @Override
    public List<CartaoAtivo> listarCartoesDeClientes() {
        return ConexaoBD.select(CARTOES_DE_CLIENTES, new CartaoDeClienteMapper());
    }

    private static class CartaoDeClienteMapper implements RowMapper<CartaoAtivo> {
        @Override
        public CartaoAtivo map(ResultSet rs) throws SQLException {
            return new CartaoAtivo(rs.getString("cartao"),
                    rs.getString("nome") + " (quarto " + rs.getInt("quartoNumero") + ")");
        }
    }

    private static class AcessoCartaoMapper implements RowMapper<AcessoCartao> {
        @Override
        public AcessoCartao map(ResultSet rs) throws SQLException {
            AcessoCartao acesso = new AcessoCartao(rs.getInt("id"), rs.getString("cartao"),
                    LocalHotel.valueOf(rs.getString("localAcesso")), rs.getInt("quartoNumero"),
                    rs.getInt("reservaId"), rs.getTimestamp("registadoEm").toLocalDateTime());
            acesso.setFuncionario(rs.getInt("funcionarioId"), rs.getString("funcionarioNome"));
            return acesso;
        }
    }

    private static class DadosGeradosMapper implements RowMapper<AcessoCartao> {
        private final AcessoCartao acesso;

        DadosGeradosMapper(AcessoCartao acesso) {
            this.acesso = acesso;
        }

        @Override
        public AcessoCartao map(ResultSet rs) throws SQLException {
            acesso.setId(rs.getInt("id"));
            acesso.setRegistadoEm(rs.getTimestamp("registadoEm").toLocalDateTime());
            return acesso;
        }
    }
}
