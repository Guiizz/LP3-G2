package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Unidade;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EntregaDAL implements IEntregaDAL {

    private static final String EXISTE_ENTREGA =
            "SELECT 1 FROM Entrega e JOIN Fornecedor f ON f.id = e.fornecedorId "
            + "WHERE f.codigo = ? AND e.numeroEncomenda = ?";
    private static final String PROCURAR_FORNECEDOR = "SELECT id FROM Fornecedor WHERE codigo = ?";
    private static final String INSERIR_FORNECEDOR =
            "INSERT INTO Fornecedor (codigo, nome, morada1, morada2, cidade, codigoPostal, pais, codigoPais) "
            + "OUTPUT INSERTED.id VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String ATUALIZAR_FORNECEDOR =
            "UPDATE Fornecedor SET nome = ?, morada1 = ?, morada2 = ?, cidade = ?, codigoPostal = ?, pais = ?, "
            + "codigoPais = ? WHERE id = ?";
    private static final String PROCURAR_MATERIAL = "SELECT id FROM Material WHERE fornecedorId = ? AND codigo = ?";
    private static final String INSERIR_MATERIAL =
            "INSERT INTO Material (fornecedorId, codigo, descricao) OUTPUT INSERTED.id VALUES (?, ?, ?)";
    private static final String ATUALIZAR_MATERIAL = "UPDATE Material SET descricao = ? WHERE id = ?";
    private static final String INSERIR_ENTREGA =
            "INSERT INTO Entrega (fornecedorId, numeroEncomenda, dataEncomenda, total, registadaPor) "
            + "OUTPUT INSERTED.id VALUES (?, ?, ?, ?, ?)";
    private static final String INSERIR_LINHA =
            "INSERT INTO LinhaEntrega (entregaId, numeroLinha, materialId, quantidade, unidade, precoUnitario, "
            + "valorSemIva, taxaIva, valorIva, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ENTREGA =
            "SELECT e.id, e.numeroEncomenda, e.dataEncomenda, f.codigo, f.nome, f.morada1, f.morada2, f.cidade, "
            + "f.codigoPostal, f.pais, f.codigoPais, f.id AS fornecedorId "
            + "FROM Entrega e JOIN Fornecedor f ON f.id = e.fornecedorId WHERE e.id = ?";
    private static final String SELECT_LINHAS =
            "SELECT l.numeroLinha, m.codigo, m.descricao, l.quantidade, l.unidade, l.precoUnitario, l.valorSemIva, "
            + "l.taxaIva, l.valorIva, l.total "
            + "FROM LinhaEntrega l JOIN Material m ON m.id = l.materialId WHERE l.entregaId = ? ORDER BY l.numeroLinha";
    private static final String APAGAR = "DELETE FROM Entrega WHERE id = ?";

    @Override
    public boolean existeEntrega(String codigoFornecedor, String numeroEncomenda) {
        return !ConexaoBD.select(EXISTE_ENTREGA, rs -> rs.getInt(1), codigoFornecedor, numeroEncomenda).isEmpty();
    }

    // grava o fornecedor, os materiais, a entrega e as linhas: ou fica tudo gravado, ou nada
    @Override
    public int registar(Entrega entrega, int funcionarioId) {
        try (Connection conn = ConexaoBD.abrir()) {
            conn.setAutoCommit(false);
            try {
                int fornecedorId = gravarFornecedor(conn, entrega.getFornecedor());
                int entregaId = inserir(conn, INSERIR_ENTREGA, fornecedorId, entrega.getNumeroEncomenda(),
                        Date.valueOf(entrega.getDataEncomenda()), entrega.getTotal(), funcionarioId);
                for (LinhaEntrega linha : entrega.getLinhas()) {
                    int materialId = gravarMaterial(conn, fornecedorId, linha);
                    executar(conn, INSERIR_LINHA, entregaId, linha.getNumero(), materialId, linha.getQuantidade(),
                            linha.getUnidade().name(), linha.getPrecoUnitario(), linha.getValorSemIva(),
                            linha.getTaxaIva(), linha.getValorIva(), linha.getTotal());
                }
                conn.commit();
                entrega.setId(entregaId);
                entrega.getFornecedor().setId(fornecedorId);
                return entregaId;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao registar a entrega", e);
        }
    }

    @Override
    public Optional<Entrega> procurarPorId(int id) {
        List<Entrega> entregas = ConexaoBD.select(SELECT_ENTREGA, rs -> new Entrega(
                rs.getInt("id"),
                rs.getString("numeroEncomenda"),
                rs.getDate("dataEncomenda").toLocalDate(),
                new Fornecedor(rs.getInt("fornecedorId"), rs.getString("codigo"), rs.getString("nome"),
                        rs.getString("morada1"), rs.getString("morada2"), rs.getString("cidade"),
                        rs.getString("codigoPostal"), rs.getString("pais"), rs.getString("codigoPais"))), id);
        if (entregas.isEmpty()) {
            return Optional.empty();
        }
        Entrega entrega = entregas.get(0);
        ConexaoBD.select(SELECT_LINHAS, this::mapearLinha, id).forEach(entrega::adicionarLinha);
        return Optional.of(entrega);
    }

    @Override
    public boolean apagar(int id) {
        return ConexaoBD.executar(APAGAR, id) > 0;
    }

    private int gravarFornecedor(Connection conn, Fornecedor f) throws SQLException {
        Integer id = procurarId(conn, PROCURAR_FORNECEDOR, f.getCodigo());
        if (id == null) {
            return inserir(conn, INSERIR_FORNECEDOR, f.getCodigo(), f.getNome(), f.getMorada1(), f.getMorada2(),
                    f.getCidade(), f.getCodigoPostal(), f.getPais(), f.getCodigoPais());
        }
        executar(conn, ATUALIZAR_FORNECEDOR, f.getNome(), f.getMorada1(), f.getMorada2(), f.getCidade(),
                f.getCodigoPostal(), f.getPais(), f.getCodigoPais(), id);
        return id;
    }

    private int gravarMaterial(Connection conn, int fornecedorId, LinhaEntrega linha) throws SQLException {
        Integer id = procurarId(conn, PROCURAR_MATERIAL, fornecedorId, linha.getCodigoProduto());
        if (id == null) {
            return inserir(conn, INSERIR_MATERIAL, fornecedorId, linha.getCodigoProduto(), linha.getDescricao());
        }
        executar(conn, ATUALIZAR_MATERIAL, linha.getDescricao(), id);
        return id;
    }

    private LinhaEntrega mapearLinha(ResultSet rs) throws SQLException {
        return new LinhaEntrega(rs.getInt("numeroLinha"), rs.getString("codigo"), rs.getString("descricao"),
                rs.getInt("quantidade"), Unidade.valueOf(rs.getString("unidade")), rs.getBigDecimal("precoUnitario"),
                rs.getBigDecimal("valorSemIva"), rs.getInt("taxaIva"), rs.getBigDecimal("valorIva"),
                rs.getBigDecimal("total"));
    }

    private static Integer procurarId(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            definirParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private static int inserir(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            definirParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static void executar(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            definirParametros(ps, params);
            ps.executeUpdate();
        }
    }

    private static void definirParametros(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
