package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IConfiguracaoQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.model.Material;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação em SQL Server de {@link IConfiguracaoQuartoDAL} (US75). Guardar uma linha da configuração usa
 * um {@code MERGE} com {@code HOLDLOCK}: junta o material ou, se já lá estiver, muda a quantidade, e dois gestores
 * a guardar ao mesmo tempo não dão erro de linha repetida.
 */
public class ConfiguracaoQuartoDAL implements IConfiguracaoQuartoDAL {

    private static final String LISTAR_MATERIAIS =
            "SELECT m.id, m.codigo, m.descricao, f.nome AS fornecedor, m.consumivel "
            + "FROM Material m JOIN Fornecedor f ON f.id = m.fornecedorId ORDER BY m.descricao, f.nome, m.id";
    private static final String ALTERAR_CONSUMIVEL = "UPDATE Material SET consumivel = ? WHERE id = ?";
    private static final String LISTAR_CONFIGURACAO =
            "SELECT c.quantidade, m.id, m.codigo, m.descricao, f.nome AS fornecedor, m.consumivel "
            + "FROM ConfiguracaoQuarto c JOIN Material m ON m.id = c.materialId "
            + "JOIN Fornecedor f ON f.id = m.fornecedorId WHERE c.tipoQuarto = ? ORDER BY m.descricao, f.nome, m.id";
    private static final String GUARDAR =
            "MERGE ConfiguracaoQuarto WITH (HOLDLOCK) AS c "
            + "USING (SELECT ? AS tipoQuarto, ? AS materialId, ? AS quantidade) AS n "
            + "ON c.tipoQuarto = n.tipoQuarto AND c.materialId = n.materialId "
            + "WHEN MATCHED THEN UPDATE SET quantidade = n.quantidade "
            + "WHEN NOT MATCHED THEN INSERT (tipoQuarto, materialId, quantidade) "
            + "VALUES (n.tipoQuarto, n.materialId, n.quantidade);";
    private static final String REMOVER = "DELETE FROM ConfiguracaoQuarto WHERE tipoQuarto = ? AND materialId = ?";

    @Override
    public List<Material> listarMateriais() {
        return ConexaoBD.select(LISTAR_MATERIAIS, new MaterialMapper());
    }

    @Override
    public boolean alterarConsumivel(int materialId, boolean consumivel) {
        return ConexaoBD.executar(ALTERAR_CONSUMIVEL, consumivel, materialId) > 0;
    }

    @Override
    public List<MaterialQuarto> listarConfiguracao(TipoQuarto tipo) {
        return ConexaoBD.select(LISTAR_CONFIGURACAO, new MaterialQuartoMapper(tipo), tipo.name());
    }

    @Override
    public void guardar(TipoQuarto tipo, int materialId, int quantidade) {
        ConexaoBD.executar(GUARDAR, tipo.name(), materialId, quantidade);
    }

    @Override
    public boolean remover(TipoQuarto tipo, int materialId) {
        return ConexaoBD.executar(REMOVER, tipo.name(), materialId) > 0;
    }

    private static Material material(ResultSet rs) throws SQLException {
        return new Material(rs.getInt("id"), rs.getString("codigo"), rs.getString("descricao"),
                rs.getString("fornecedor"), rs.getBoolean("consumivel"));
    }

    private static class MaterialMapper implements RowMapper<Material> {
        @Override
        public Material map(ResultSet rs) throws SQLException {
            return material(rs);
        }
    }

    private static class MaterialQuartoMapper implements RowMapper<MaterialQuarto> {
        private final TipoQuarto tipo;

        MaterialQuartoMapper(TipoQuarto tipo) {
            this.tipo = tipo;
        }

        @Override
        public MaterialQuarto map(ResultSet rs) throws SQLException {
            return new MaterialQuarto(tipo, material(rs), rs.getInt("quantidade"));
        }
    }
}
