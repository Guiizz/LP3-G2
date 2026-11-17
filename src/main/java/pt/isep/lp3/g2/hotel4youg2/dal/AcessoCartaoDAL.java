package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IAcessoCartaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.CartaoAtivo;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
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
            + "p.nome AS funcionarioNome, c.nome AS clienteNome FROM AcessoCartao a "
            + "LEFT JOIN Pessoa p ON p.id = a.funcionarioId "
            + "LEFT JOIN Reserva r ON r.id = a.reservaId LEFT JOIN Pessoa c ON c.id = r.clienteId ";
    private static final String ORDEM = " ORDER BY a.registadoEm DESC, a.id DESC";
    private static final String LISTAR_POR_CARTAO = LISTAR + "WHERE a.cartao = ?" + ORDEM;
    private static final String APAGAR = "DELETE FROM AcessoCartao WHERE id = ?";
    private static final String CARTOES_USADOS =
            "SELECT DISTINCT a.cartao, COALESCE(p.nome, c.nome) AS titular FROM AcessoCartao a "
            + "LEFT JOIN Pessoa p ON p.id = a.funcionarioId "
            + "LEFT JOIN Reserva r ON r.id = a.reservaId LEFT JOIN Pessoa c ON c.id = r.clienteId "
            + "ORDER BY a.cartao, titular";

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
    public List<AcessoCartao> listar(String cartao, int quartoNumero, LocalHotel local, LocalDate dia) {
        StringBuilder sql = new StringBuilder(LISTAR).append("WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (cartao != null && !cartao.isEmpty()) {
            sql.append(" AND a.cartao = ?");
            params.add(cartao);
        }
        if (quartoNumero > 0) {
            sql.append(" AND a.quartoNumero = ?");
            params.add(quartoNumero);
        }
        if (local != null) {
            sql.append(" AND a.localAcesso = ?");
            params.add(local.name());
        }
        if (dia != null) {
            sql.append(" AND a.registadoEm >= ? AND a.registadoEm < ?");
            params.add(Timestamp.valueOf(dia.atStartOfDay()));
            params.add(Timestamp.valueOf(dia.plusDays(1).atStartOfDay()));
        }
        sql.append(ORDEM);
        return ConexaoBD.select(sql.toString(), new AcessoCartaoMapper(), params.toArray());
    }

    @Override
    public boolean apagar(int id) {
        return ConexaoBD.executar(APAGAR, id) > 0;
    }

    @Override
    public List<CartaoAtivo> listarCartoesUsados() {
        return ConexaoBD.select(CARTOES_USADOS, new CartaoUsadoMapper());
    }

    private static class CartaoUsadoMapper implements RowMapper<CartaoAtivo> {
        @Override
        public CartaoAtivo map(ResultSet rs) throws SQLException {
            String cartao = rs.getString("cartao");
            String titular = rs.getString("titular");
            if (titular == null) {
                titular = AcessoCartao.CARTAO_MESTRE.equals(cartao) ? "Gestor (cartão mestre)" : "Sem titular";
            }
            return new CartaoAtivo(cartao, titular);
        }
    }

    private static class AcessoCartaoMapper implements RowMapper<AcessoCartao> {
        @Override
        public AcessoCartao map(ResultSet rs) throws SQLException {
            AcessoCartao acesso = new AcessoCartao(rs.getInt("id"), rs.getString("cartao"),
                    LocalHotel.valueOf(rs.getString("localAcesso")), rs.getInt("quartoNumero"),
                    rs.getInt("reservaId"), rs.getTimestamp("registadoEm").toLocalDateTime());
            acesso.setFuncionario(rs.getInt("funcionarioId"), rs.getString("funcionarioNome"));
            acesso.setClienteNome(rs.getString("clienteNome"));
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
