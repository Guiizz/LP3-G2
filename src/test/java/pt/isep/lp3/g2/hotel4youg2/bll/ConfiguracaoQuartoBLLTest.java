package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IConfiguracaoQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Material;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testes unitários da classe ConfiguracaoQuartoBLL (US75).
 */
public class ConfiguracaoQuartoBLLTest {

    // Base de dados falsa (em memória): os materiais e as linhas da configuração de todos os tipos de quarto
    private static class ConfiguracaoQuartoDALFalsa implements IConfiguracaoQuartoDAL {
        private final List<Material> materiais = new ArrayList<>();
        private final List<MaterialQuarto> configuracao = new ArrayList<>();
        private int alteracoes;

        @Override
        public List<Material> listarMateriais() {
            return materiais;
        }

        @Override
        public boolean alterarConsumivel(int materialId, boolean consumivel) {
            for (Material material : materiais) {
                if (material.getId() == materialId) {
                    alteracoes++;
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<MaterialQuarto> listarConfiguracao(TipoQuarto tipo) {
            List<MaterialQuarto> doTipo = new ArrayList<>();
            for (MaterialQuarto linha : configuracao) {
                if (linha.getTipoQuarto() == tipo) {
                    doTipo.add(linha);
                }
            }
            return doTipo;
        }

        // como o MERGE da BD: se o material já estiver neste tipo de quarto, troca a quantidade
        @Override
        public void guardar(TipoQuarto tipo, int materialId, int quantidade) {
            MaterialQuarto existente = procurar(tipo, materialId);
            if (existente != null) {
                configuracao.remove(existente);
            }
            for (Material material : materiais) {
                if (material.getId() == materialId) {
                    configuracao.add(new MaterialQuarto(tipo, material, quantidade));
                }
            }
            alteracoes++;
        }

        @Override
        public boolean remover(TipoQuarto tipo, int materialId) {
            MaterialQuarto existente = procurar(tipo, materialId);
            if (existente == null) {
                return false;
            }
            configuracao.remove(existente);
            alteracoes++;
            return true;
        }

        private MaterialQuarto procurar(TipoQuarto tipo, int materialId) {
            for (MaterialQuarto linha : configuracao) {
                if (linha.getTipoQuarto() == tipo && linha.getMaterialId() == materialId) {
                    return linha;
                }
            }
            return null;
        }
    }

    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);

    private ConfiguracaoQuartoDALFalsa configuracaoDAL;
    private ConfiguracaoQuartoBLL instance;
    private Material toalha;
    private Material champo;

    // dois materiais: uma toalha (não consumível) e um champô (consumível); o quarto duplo leva 2 toalhas
    @BeforeEach
    public void setUp() {
        configuracaoDAL = new ConfiguracaoQuartoDALFalsa();
        toalha = new Material(4, "PZ-1989", "Toalha de banho 400g cinza", "ProductHero", false);
        champo = new Material(5, "XQF6324", "Rituals Champô 200ml", "ProductHero", true);
        configuracaoDAL.materiais.add(toalha);
        configuracaoDAL.materiais.add(champo);
        configuracaoDAL.configuracao.add(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2));
        instance = new ConfiguracaoQuartoBLL(configuracaoDAL);
    }

    @AfterEach
    public void tearDown() {
        configuracaoDAL.materiais.clear();
        configuracaoDAL.configuracao.clear();
        instance = null;
    }

    // tenta guardar uma linha que devia ser recusada e confirma que nada mudou
    private void esperarErroAoGuardar(TipoQuarto tipo, Material material, String quantidade, String expResult) {
        try {
            instance.guardar(GESTOR, tipo, material, quantidade);
            fail("Devia ter recusado a configuração");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(0, configuracaoDAL.alteracoes);
        }
    }

    // ---------------- o que a funcionalidade faz ----------------

    @Test
    public void testListarMateriais() {
        System.out.println("listarMateriais");
        List<Material> expResult = List.of(toalha, champo);
        List<Material> result = instance.listarMateriais(GESTOR);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarMateriaisRececionista() {
        System.out.println("listarMateriais (o rececionista pode consultar)");
        List<Material> expResult = List.of(toalha, champo);
        List<Material> result = instance.listarMateriais(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testAlterarConsumivel() {
        System.out.println("alterarConsumivel (a toalha passa a consumível)");
        instance.alterarConsumivel(GESTOR, toalha, true);
        boolean expResult = true;
        boolean result = toalha.isConsumivel();
        assertEquals(expResult, result);
        assertEquals(1, configuracaoDAL.alteracoes);
    }

    @Test
    public void testAlterarParaNaoConsumivel() {
        System.out.println("alterarConsumivel (o champô passa a não consumível)");
        instance.alterarConsumivel(GESTOR, champo, false);
        boolean expResult = false;
        boolean result = champo.isConsumivel();
        assertEquals(expResult, result);
    }

    @Test
    public void testConfiguracao() {
        System.out.println("configuracao (o quarto duplo leva 2 toalhas)");
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2));
        List<MaterialQuarto> result = instance.configuracao(GESTOR, TipoQuarto.DUPLO);
        assertEquals(expResult, result);
    }

    @Test
    public void testConfiguracaoRececionista() {
        System.out.println("configuracao (o rececionista pode consultar)");
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2));
        List<MaterialQuarto> result = instance.configuracao(RECECAO, TipoQuarto.DUPLO);
        assertEquals(expResult, result);
    }

    @Test
    public void testConfiguracaoTipoSemMateriais() {
        System.out.println("configuracao (tipo de quarto ainda sem materiais)");
        List<MaterialQuarto> expResult = new ArrayList<>();
        List<MaterialQuarto> result = instance.configuracao(GESTOR, TipoQuarto.INDIVIDUAL);
        assertEquals(expResult, result);
    }

    @Test
    public void testGuardarMaterialNovo() {
        System.out.println("guardar (junta 2 champôs ao quarto duplo)");
        instance.guardar(GESTOR, TipoQuarto.DUPLO, champo, "2");
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2),
                new MaterialQuarto(TipoQuarto.DUPLO, champo, 2));
        List<MaterialQuarto> result = instance.configuracao(GESTOR, TipoQuarto.DUPLO);
        assertEquals(expResult, result);
    }

    @Test
    public void testGuardarMudaAQuantidade() {
        System.out.println("guardar (material que já lá está: muda a quantidade, sem repetir)");
        instance.guardar(GESTOR, TipoQuarto.DUPLO, toalha, "4");
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 4));
        List<MaterialQuarto> result = instance.configuracao(GESTOR, TipoQuarto.DUPLO);
        assertEquals(expResult, result);
    }

    @Test
    public void testGuardarNaoMudaOutroTipo() {
        System.out.println("guardar (configurar o quarto família não muda o duplo)");
        instance.guardar(GESTOR, TipoQuarto.FAMILIA, toalha, "4");
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2));
        List<MaterialQuarto> result = instance.configuracao(GESTOR, TipoQuarto.DUPLO);
        assertEquals(expResult, result);
    }

    @Test
    public void testGuardarQuantidadeComEspacos() {
        System.out.println("guardar (espaços à volta da quantidade)");
        instance.guardar(GESTOR, TipoQuarto.INDIVIDUAL, champo, " 1 ");
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.INDIVIDUAL, champo, 1));
        List<MaterialQuarto> result = instance.configuracao(GESTOR, TipoQuarto.INDIVIDUAL);
        assertEquals(expResult, result);
    }

    @Test
    public void testGuardarQuantidadeMaxima() {
        System.out.println("guardar (quantidade máxima, 99)");
        instance.guardar(GESTOR, TipoQuarto.FAMILIA, champo, "99");
        int expResult = ConfiguracaoQuartoBLL.QUANTIDADE_MAXIMA;
        int result = instance.configuracao(GESTOR, TipoQuarto.FAMILIA).get(0).getQuantidade();
        assertEquals(expResult, result);
    }

    @Test
    public void testRemover() {
        System.out.println("remover (tira as toalhas do quarto duplo)");
        instance.remover(GESTOR, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2));
        List<MaterialQuarto> expResult = new ArrayList<>();
        List<MaterialQuarto> result = instance.configuracao(GESTOR, TipoQuarto.DUPLO);
        assertEquals(expResult, result);
    }

    // ---------------- erros ----------------

    @Test
    public void testListarMateriaisSemSessao() {
        System.out.println("listarMateriais (sem sessão iniciada)");
        try {
            instance.listarMateriais(null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals("Entre na aplicação como gestor ou rececionista para ver os materiais dos quartos.",
                    expected.getMessage());
        }
    }

    @Test
    public void testAlterarConsumivelRececionista() {
        System.out.println("alterarConsumivel (o rececionista não pode)");
        try {
            instance.alterarConsumivel(RECECAO, toalha, true);
            fail("Devia ter recusado");
        } catch (IllegalStateException expected) {
            assertEquals("Só o gestor pode alterar os materiais dos quartos.", expected.getMessage());
            assertEquals(false, toalha.isConsumivel());
            assertEquals(0, configuracaoDAL.alteracoes);
        }
    }

    @Test
    public void testAlterarConsumivelSemMaterial() {
        System.out.println("alterarConsumivel (sem material escolhido)");
        try {
            instance.alterarConsumivel(GESTOR, null, true);
            fail("Devia ter pedido o material");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha o material na tabela.", expected.getMessage());
        }
    }

    @Test
    public void testAlterarConsumivelMaterialQueNaoExiste() {
        System.out.println("alterarConsumivel (material que já não existe)");
        Material apagado = new Material(99, "X", "Apagado", "ProductHero", true);
        try {
            instance.alterarConsumivel(GESTOR, apagado, false);
            fail("Devia ter recusado");
        } catch (IllegalArgumentException expected) {
            assertEquals("O material já não existe.", expected.getMessage());
            assertEquals(true, apagado.isConsumivel());
        }
    }

    @Test
    public void testConfiguracaoSemTipo() {
        System.out.println("configuracao (sem tipo de quarto)");
        try {
            instance.configuracao(GESTOR, null);
            fail("Devia ter pedido o tipo");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha o tipo de quarto.", expected.getMessage());
        }
    }

    @Test
    public void testConfiguracaoSemSessao() {
        System.out.println("configuracao (sem sessão iniciada)");
        try {
            instance.configuracao(null, TipoQuarto.DUPLO);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals("Entre na aplicação como gestor ou rececionista para ver os materiais dos quartos.",
                    expected.getMessage());
        }
    }

    @Test
    public void testGuardarRececionista() {
        System.out.println("guardar (o rececionista não pode)");
        try {
            instance.guardar(RECECAO, TipoQuarto.DUPLO, champo, "2");
            fail("Devia ter recusado");
        } catch (IllegalStateException expected) {
            assertEquals("Só o gestor pode alterar os materiais dos quartos.", expected.getMessage());
            assertEquals(0, configuracaoDAL.alteracoes);
        }
    }

    @Test
    public void testGuardarSemTipo() {
        System.out.println("guardar (sem tipo de quarto)");
        esperarErroAoGuardar(null, champo, "2", "Escolha o tipo de quarto.");
    }

    @Test
    public void testGuardarSemMaterial() {
        System.out.println("guardar (sem material)");
        esperarErroAoGuardar(TipoQuarto.DUPLO, null, "2", "Escolha o material na tabela.");
    }

    @Test
    public void testGuardarSemQuantidade() {
        System.out.println("guardar (sem quantidade)");
        esperarErroAoGuardar(TipoQuarto.DUPLO, champo, "  ", "Indique a quantidade.");
    }

    @Test
    public void testGuardarQuantidadeZero() {
        System.out.println("guardar (quantidade zero)");
        esperarErroAoGuardar(TipoQuarto.DUPLO, champo, "0", "A quantidade tem de ser um número inteiro de 1 a 99.");
    }

    @Test
    public void testGuardarQuantidadeAcimaDoMaximo() {
        System.out.println("guardar (quantidade acima de 99)");
        esperarErroAoGuardar(TipoQuarto.DUPLO, champo, "100", "A quantidade tem de ser um número inteiro de 1 a 99.");
    }

    @Test
    public void testGuardarQuantidadeComLetras() {
        System.out.println("guardar (quantidade com letras)");
        esperarErroAoGuardar(TipoQuarto.DUPLO, champo, "dois", "A quantidade tem de ser um número inteiro de 1 a 99.");
    }

    @Test
    public void testRemoverRececionista() {
        System.out.println("remover (o rececionista não pode)");
        try {
            instance.remover(RECECAO, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2));
            fail("Devia ter recusado");
        } catch (IllegalStateException expected) {
            assertEquals("Só o gestor pode alterar os materiais dos quartos.", expected.getMessage());
            assertEquals(1, configuracaoDAL.configuracao.size());
        }
    }

    @Test
    public void testRemoverSemLinha() {
        System.out.println("remover (sem material escolhido na configuração)");
        try {
            instance.remover(GESTOR, null);
            fail("Devia ter pedido o material");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha o material na configuração.", expected.getMessage());
        }
    }

    @Test
    public void testRemoverJaRetirado() {
        System.out.println("remover (o material já tinha sido retirado)");
        try {
            instance.remover(GESTOR, new MaterialQuarto(TipoQuarto.FAMILIA, toalha, 2));
            fail("Devia ter recusado");
        } catch (IllegalArgumentException expected) {
            assertEquals("Este material já não está na configuração.", expected.getMessage());
        }
    }
}
