package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IEstatisticaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.Estadia;
import pt.isep.lp3.g2.hotel4youg2.model.Estatistica;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public class EstatisticaDAL implements IEstatisticaDAL {

    private static final String NAO_CANCELADA = "r.estado <> 'CANCELADA' AND r.dataEntrada BETWEEN ? AND ? ";
    private static final String FEITA = "r.estado IN ('EM_CURSO', 'CONCLUIDA') AND r.dataEntrada BETWEEN ? AND ? ";

    private static final String CONTAR_QUARTOS_ATIVOS = "SELECT COUNT(*) FROM Quarto WHERE ativo = 1";
    private static final String LISTAR_ESTADIAS =
            "SELECT dataEntrada, dataSaida FROM Reserva "
            + "WHERE estado IN ('EM_CURSO', 'CONCLUIDA') AND dataEntrada <= ? AND dataSaida > ? "
            + "ORDER BY dataEntrada, id";
    private static final String RECEITA_POR_TIPO_QUARTO =
            "SELECT q.tipo AS rotulo, SUM(r.precoTotal + r.taxaHorario) AS valor "
            + "FROM Reserva r JOIN Quarto q ON q.numero = r.quartoNumero WHERE " + FEITA
            + "GROUP BY q.tipo ORDER BY q.tipo";
    private static final String RECEITA_POR_SERVICO =
            "SELECT s.designacao AS rotulo, SUM(rs.quantidade * rs.precoUnitario) AS valor "
            + "FROM ReservaServico rs JOIN Servico s ON s.id = rs.servicoId JOIN Reserva r ON r.id = rs.reservaId "
            + "WHERE " + FEITA + "GROUP BY s.id, s.designacao ORDER BY valor DESC, s.designacao";
    private static final String RECEITA_PREVISTA =
            "SELECT (SELECT ISNULL(SUM(precoTotal + taxaHorario), 0) FROM Reserva "
            + "WHERE estado = 'CONFIRMADA' AND dataEntrada BETWEEN ? AND ?) "
            + "+ (SELECT ISNULL(SUM(rs.quantidade * rs.precoUnitario), 0) "
            + "FROM ReservaServico rs JOIN Reserva r ON r.id = rs.reservaId "
            + "WHERE r.estado = 'CONFIRMADA' AND r.dataEntrada BETWEEN ? AND ?) AS valor";
    private static final String NOITES_VENDIDAS =
            "SELECT ISNULL(SUM(DATEDIFF(DAY, r.dataEntrada, r.dataSaida)), 0) FROM Reserva r WHERE " + FEITA;
    private static final String DURACAO_MEDIA =
            "SELECT AVG(CAST(DATEDIFF(DAY, r.dataEntrada, r.dataSaida) AS DECIMAL(10,2))) AS valor "
            + "FROM Reserva r WHERE " + NAO_CANCELADA;
    private static final String GUARDA_DATA_DE_CRIACAO =
            "SELECT CASE WHEN COL_LENGTH('Reserva', 'criadaEm') IS NULL THEN 0 ELSE 1 END";
    private static final String ANTECEDENCIA_MEDIA =
            "SELECT AVG(CAST(DATEDIFF(DAY, CAST(r.criadaEm AS DATE), r.dataEntrada) AS DECIMAL(10,2))) AS valor "
            + "FROM Reserva r WHERE r.criadaEm IS NOT NULL AND " + NAO_CANCELADA;
    private static final String RESERVAS_POR_ESTADO =
            "SELECT r.estado AS rotulo, COUNT(*) AS valor FROM Reserva r WHERE r.dataEntrada BETWEEN ? AND ? "
            + "GROUP BY r.estado";
    private static final String SERVICOS_MAIS_USADOS =
            "SELECT s.designacao AS rotulo, SUM(rs.quantidade) AS valor "
            + "FROM ReservaServico rs JOIN Servico s ON s.id = rs.servicoId JOIN Reserva r ON r.id = rs.reservaId "
            + "WHERE " + FEITA + "GROUP BY s.id, s.designacao ORDER BY valor DESC, s.designacao";
    private static final String MATERIAIS_MAIS_GASTOS =
            "SELECT m.descricao AS rotulo, SUM(mv.quantidade) AS valor "
            + "FROM MovimentoStock mv JOIN Material m ON m.id = mv.materialId "
            + "WHERE mv.tipo = 'DEFINITIVA' AND (mv.motivo IS NULL OR mv.motivo <> 'DEVOLVIDO_FORNECEDOR') "
            + "AND mv.registadoEm >= ? AND mv.registadoEm < ? "
            + "GROUP BY m.descricao ORDER BY valor DESC, m.descricao";

    @Override
    public int contarQuartosAtivos() {
        return ConexaoBD.select(CONTAR_QUARTOS_ATIVOS, new InteiroMapper()).get(0);
    }

    @Override
    public List<Estadia> listarEstadias(LocalDate inicio, LocalDate fim) {
        return ConexaoBD.select(LISTAR_ESTADIAS, new EstadiaMapper(), Date.valueOf(fim), Date.valueOf(inicio));
    }

    @Override
    public List<Estatistica> receitaPorTipoQuarto(LocalDate inicio, LocalDate fim) {
        return porDia(RECEITA_POR_TIPO_QUARTO, inicio, fim);
    }

    @Override
    public List<Estatistica> receitaPorServico(LocalDate inicio, LocalDate fim) {
        return porDia(RECEITA_POR_SERVICO, inicio, fim);
    }

    @Override
    public BigDecimal receitaPrevista(LocalDate inicio, LocalDate fim) {
        return ConexaoBD.select(RECEITA_PREVISTA, new ValorMapper(), Date.valueOf(inicio), Date.valueOf(fim),
                Date.valueOf(inicio), Date.valueOf(fim)).get(0);
    }

    @Override
    public int noitesVendidas(LocalDate inicio, LocalDate fim) {
        return ConexaoBD.select(NOITES_VENDIDAS, new InteiroMapper(), Date.valueOf(inicio), Date.valueOf(fim)).get(0);
    }

    @Override
    public BigDecimal duracaoMedia(LocalDate inicio, LocalDate fim) {
        return ConexaoBD.select(DURACAO_MEDIA, new ValorOuNuloMapper(), Date.valueOf(inicio), Date.valueOf(fim))
                .get(0);
    }

    @Override
    public boolean guardaDataDeCriacao() {
        return ConexaoBD.select(GUARDA_DATA_DE_CRIACAO, new InteiroMapper()).get(0) == 1;
    }

    @Override
    public BigDecimal antecedenciaMedia(LocalDate inicio, LocalDate fim) {
        return ConexaoBD.select(ANTECEDENCIA_MEDIA, new ValorOuNuloMapper(), Date.valueOf(inicio), Date.valueOf(fim))
                .get(0);
    }

    @Override
    public List<Estatistica> reservasPorEstado(LocalDate inicio, LocalDate fim) {
        return porDia(RESERVAS_POR_ESTADO, inicio, fim);
    }

    @Override
    public List<Estatistica> servicosMaisUsados(LocalDate inicio, LocalDate fim) {
        return porDia(SERVICOS_MAIS_USADOS, inicio, fim);
    }

    @Override
    public List<Estatistica> materiaisMaisGastos(LocalDate inicio, LocalDate fim) {
        return ConexaoBD.select(MATERIAIS_MAIS_GASTOS, new EstatisticaMapper(),
                Timestamp.valueOf(inicio.atStartOfDay()), Timestamp.valueOf(fim.plusDays(1).atStartOfDay()));
    }

    private static List<Estatistica> porDia(String sql, LocalDate inicio, LocalDate fim) {
        return ConexaoBD.select(sql, new EstatisticaMapper(), Date.valueOf(inicio), Date.valueOf(fim));
    }

    private static class EstadiaMapper implements RowMapper<Estadia> {
        @Override
        public Estadia map(ResultSet rs) throws SQLException {
            return new Estadia(rs.getDate("dataEntrada").toLocalDate(), rs.getDate("dataSaida").toLocalDate());
        }
    }

    private static class ValorMapper implements RowMapper<BigDecimal> {
        @Override
        public BigDecimal map(ResultSet rs) throws SQLException {
            BigDecimal valor = rs.getBigDecimal("valor");
            return valor == null ? BigDecimal.ZERO : valor;
        }
    }

    private static class ValorOuNuloMapper implements RowMapper<BigDecimal> {
        @Override
        public BigDecimal map(ResultSet rs) throws SQLException {
            return rs.getBigDecimal("valor");
        }
    }

    private static class EstatisticaMapper implements RowMapper<Estatistica> {
        @Override
        public Estatistica map(ResultSet rs) throws SQLException {
            return new Estatistica(rs.getString("rotulo"), rs.getBigDecimal("valor"));
        }
    }
}
