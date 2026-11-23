package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Material;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes da classe ConfiguracaoQuartoDAL na base de dados (US75). Cada teste cria um fornecedor e dois materiais
 * só para ele e apaga-os no fim; a configuração usa estes materiais, por isso não mexe na dos outros.
 */
public class ConfiguracaoQuartoDALTest {

    private ConfiguracaoQuartoDAL instance;
    private String codigo;
    private int fornecedorId;
    private Material toalha;
    private Material champo;

    @BeforeAll
    public static void limparRestosDosTestes() {
        RestosDosTestes.limpar();
    }

    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabelas = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('ConfiguracaoQuarto') IS NOT NULL "
                    + "AND COL_LENGTH('Material', 'consumivel') IS NOT NULL THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabelas.get(0) == 1, "Falta correr o 22_materiais_quartos.sql");
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new ConfiguracaoQuartoDAL();
        codigo = "TQ" + Math.floorMod(System.nanoTime(), 100_000_000L);
        String fornecedor = "Fornecedor " + codigo;
        fornecedorId = ConexaoBD.select("INSERT INTO Fornecedor (codigo, nome) OUTPUT INSERTED.id VALUES (?, ?)",
                new InteiroMapper(), codigo, fornecedor).get(0);
        toalha = new Material(inserirMaterial("T-1", "Teste toalha " + codigo), "T-1", "Teste toalha " + codigo,
                fornecedor, true);
        champo = new Material(inserirMaterial("C-1", "Teste champô " + codigo), "C-1", "Teste champô " + codigo,
                fornecedor, true);
    }

    @AfterEach
    public void tearDown() {
        if (fornecedorId > 0) {
            ConexaoBD.executar("DELETE FROM ConfiguracaoQuarto WHERE materialId IN "
                    + "(SELECT id FROM Material WHERE fornecedorId = ?)", fornecedorId);
            ConexaoBD.executar("DELETE FROM Material WHERE fornecedorId = ?", fornecedorId);
            ConexaoBD.executar("DELETE FROM Fornecedor WHERE id = ?", fornecedorId);
        }
    }

    private int inserirMaterial(String codigoMaterial, String descricao) {
        return ConexaoBD.select("INSERT INTO Material (fornecedorId, codigo, descricao) OUTPUT INSERTED.id "
                + "VALUES (?, ?, ?)", new InteiroMapper(), fornecedorId, codigoMaterial, descricao).get(0);
    }

    private List<Material> osMateriaisDoTeste() {
        List<Material> doTeste = new ArrayList<>();
        for (Material material : instance.listarMateriais()) {
            if (material.getFornecedor().equals(toalha.getFornecedor())) {
                doTeste.add(material);
            }
        }
        return doTeste;
    }

    private List<MaterialQuarto> configuracaoDoTeste(TipoQuarto tipo) {
        List<MaterialQuarto> doTeste = new ArrayList<>();
        for (MaterialQuarto linha : instance.listarConfiguracao(tipo)) {
            if (linha.getFornecedor().equals(toalha.getFornecedor())) {
                doTeste.add(linha);
            }
        }
        return doTeste;
    }

    @Test
    public void testListarMateriais() {
        System.out.println("listarMateriais (com o fornecedor; os materiais novos são consumíveis)");
        List<Material> expResult = List.of(champo, toalha);
        List<Material> result = osMateriaisDoTeste();
        assertEquals(expResult, result);
    }

    @Test
    public void testAlterarConsumivel() {
        System.out.println("alterarConsumivel (a toalha passa a não consumível)");
        boolean expResult = true;
        boolean result = instance.alterarConsumivel(toalha.getId(), false);
        toalha.setConsumivel(false);
        assertEquals(expResult, result);
        assertEquals(List.of(champo, toalha), osMateriaisDoTeste());
    }

    @Test
    public void testGuardar() {
        System.out.println("guardar (o quarto duplo leva 2 toalhas)");
        instance.guardar(TipoQuarto.DUPLO, toalha.getId(), 2);
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2));
        List<MaterialQuarto> result = configuracaoDoTeste(TipoQuarto.DUPLO);
        assertEquals(expResult, result);
    }

    @Test
    public void testGuardarDuasVezesMudaAQuantidade() {
        System.out.println("guardar (o mesmo material outra vez muda a quantidade, sem repetir)");
        instance.guardar(TipoQuarto.DUPLO, toalha.getId(), 2);
        instance.guardar(TipoQuarto.DUPLO, toalha.getId(), 3);
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 3));
        List<MaterialQuarto> result = configuracaoDoTeste(TipoQuarto.DUPLO);
        assertEquals(expResult, result);
    }

    @Test
    public void testGuardarEmTiposDiferentes() {
        System.out.println("guardar (cada tipo de quarto tem a sua configuração)");
        instance.guardar(TipoQuarto.DUPLO, toalha.getId(), 2);
        instance.guardar(TipoQuarto.FAMILIA, toalha.getId(), 4);
        instance.guardar(TipoQuarto.FAMILIA, champo.getId(), 4);
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2));
        List<MaterialQuarto> result = configuracaoDoTeste(TipoQuarto.DUPLO);
        assertEquals(expResult, result);
        assertEquals(List.of(new MaterialQuarto(TipoQuarto.FAMILIA, champo, 4),
                new MaterialQuarto(TipoQuarto.FAMILIA, toalha, 4)), configuracaoDoTeste(TipoQuarto.FAMILIA));
    }

    @Test
    public void testRemover() {
        System.out.println("remover (tira as toalhas do quarto duplo)");
        instance.guardar(TipoQuarto.DUPLO, toalha.getId(), 2);
        boolean expResult = true;
        boolean result = instance.remover(TipoQuarto.DUPLO, toalha.getId());
        assertEquals(expResult, result);
        assertEquals(new ArrayList<MaterialQuarto>(), configuracaoDoTeste(TipoQuarto.DUPLO));
    }

    @Test
    public void testAlterarConsumivelMaterialQueNaoExiste() {
        System.out.println("alterarConsumivel (material que não existe)");
        boolean expResult = false;
        boolean result = instance.alterarConsumivel(-1, false);
        assertEquals(expResult, result);
    }

    @Test
    public void testRemoverQueNaoEstaNaConfiguracao() {
        System.out.println("remover (material que não está na configuração)");
        boolean expResult = false;
        boolean result = instance.remover(TipoQuarto.INDIVIDUAL, toalha.getId());
        assertEquals(expResult, result);
    }

    @Test
    public void testListarConfiguracaoTipoSemMateriais() {
        System.out.println("listarConfiguracao (tipo sem materiais deste teste)");
        List<MaterialQuarto> expResult = new ArrayList<>();
        List<MaterialQuarto> result = configuracaoDoTeste(TipoQuarto.INDIVIDUAL);
        assertEquals(expResult, result);
    }
}
