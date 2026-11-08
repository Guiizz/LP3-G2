package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.LocalHotel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AcessoCartaoDAL implements IAcessoCartaoDAL {

    private static final String REGISTAR =
            "INSERT INTO AcessoCartao (cartao, localAcesso, quartoNumero, reservaId) "
            + "OUTPUT INSERTED.id, INSERTED.registadoEm VALUES (?, ?, NULLIF(?, 0), NULLIF(?, 0))";
    private static final String LISTAR_POR_CARTAO =
            "SELECT id, cartao, localAcesso, quartoNumero, reservaId, registadoEm FROM AcessoCartao "
            + "WHERE cartao = ? ORDER BY registadoEm DESC, id DESC";
    private static final String APAGAR = "DELETE FROM AcessoCartao WHERE id = ?";

    @Override
    public void registar(AcessoCartao acesso) {
        ConexaoBD.select(REGISTAR, new DadosGeradosMapper(acesso), acesso.getCartao(), acesso.getLocal().name(),
                acesso.getQuartoNumero(), acesso.getReservaId());
    }

    @Override
    public List<AcessoCartao> listarPorCartao(String cartao) {
        return ConexaoBD.select(LISTAR_POR_CARTAO, new AcessoCartaoMapper(), cartao);
    }

    @Override
    public boolean apagar(int id) {
        return ConexaoBD.executar(APAGAR, id) > 0;
    }

    private static class AcessoCartaoMapper implements RowMapper<AcessoCartao> {
        @Override
        public AcessoCartao map(ResultSet rs) throws SQLException {
            return new AcessoCartao(rs.getInt("id"), rs.getString("cartao"),
                    LocalHotel.valueOf(rs.getString("localAcesso")), rs.getInt("quartoNumero"),
                    rs.getInt("reservaId"), rs.getTimestamp("registadoEm").toLocalDateTime());
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
