package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.ICategoriaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.TipoCobranca;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ServicoBLLTest {

    // Base de dados falsa (em memória), para os testes não dependerem da BD do ISEP
    private static class ServicoDALFalsa implements IServicoDAL {
        final List<Servico> tabela = new ArrayList<>();
        private int proximoId = 1;

        @Override
        public List<Servico> listar() {
            return new ArrayList<>(tabela);
        }

        @Override
        public Optional<Servico> procurarPorId(int id) {
            return tabela.stream().filter(s -> s.getId() == id).findFirst();
        }

        @Override
        public boolean existeDesignacao(String designacao, TipoCobranca tipoCobranca, int idIgnorar) {
            return tabela.stream().anyMatch(s -> s.getId() != idIgnorar
                    && s.getTipoCobranca() == tipoCobranca
                    && s.getDesignacao().equalsIgnoreCase(designacao));
        }

        @Override
        public int inserir(Servico servico) {
            servico.setId(proximoId++);
            tabela.add(new Servico(servico.getId(), servico.getDesignacao(), servico.getPreco(),
                    servico.getTipoCobranca(), servico.getCategoria(), servico.isAtivo()));
            return servico.getId();
        }

        @Override
        public boolean atualizar(Servico servico) {
            Optional<Servico> atual = procurarPorId(servico.getId());
            atual.ifPresent(s -> {
                s.setDesignacao(servico.getDesignacao());
                s.setPreco(servico.getPreco());
                s.setTipoCobranca(servico.getTipoCobranca());
                s.setCategoria(servico.getCategoria());
            });
            return atual.isPresent();
        }

        @Override
        public boolean atualizarAtivo(int id, boolean ativo) {
            Optional<Servico> atual = procurarPorId(id);
            atual.ifPresent(s -> s.setAtivo(ativo));
            return atual.isPresent();
        }

        @Override
        public boolean apagar(int id) {
            return tabela.removeIf(s -> s.getId() == id);
        }
    }

    private static class CategoriaDALFalsa implements ICategoriaDAL {
        final List<Categoria> tabela = new ArrayList<>();
        private int proximoId = 1;

        @Override
        public List<Categoria> listar() {
            return new ArrayList<>(tabela);
        }

        @Override
        public boolean existeNome(String nome) {
            return tabela.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nome));
        }

        @Override
        public int inserir(Categoria categoria) {
            categoria.setId(proximoId++);
            tabela.add(new Categoria(categoria.getId(), categoria.getNome()));
            return categoria.getId();
        }

        @Override
        public boolean apagar(int id) {
            return tabela.removeIf(c -> c.getId() == id);
        }
    }

    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO = new Utilizador(2, 2, "rececao", "h", "s", Perfil.RECECIONISTA, true);

    private ServicoDALFalsa servicoDAL;
    private CategoriaDALFalsa categoriaDAL;
    private ServicoBLL instance;
    private Categoria lazer;
    private Categoria spa;

    @BeforeEach
    public void setUp() {
        servicoDAL = new ServicoDALFalsa();
        categoriaDAL = new CategoriaDALFalsa();
        lazer = new Categoria(0, "Lazer");
        spa = new Categoria(0, "Spa");
        categoriaDAL.inserir(lazer);
        categoriaDAL.inserir(spa);
        instance = new ServicoBLL(servicoDAL, categoriaDAL);
    }

    @AfterEach
    public void tearDown() {
        servicoDAL.tabela.clear();
        categoriaDAL.tabela.clear();
    }

    // ---------- listar ----------

    @Test
    public void testListar() {
        System.out.println("listar");
        instance.criar(GESTOR, "Sauna", new BigDecimal("15.00"), TipoCobranca.POR_UTILIZACAO, spa);
        instance.criar(GESTOR, "Piscina", new BigDecimal("5.00"), TipoCobranca.POR_UTILIZACAO, lazer);
        int expResult = 2;
        int result = instance.listar().size();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarCategorias() {
        System.out.println("listarCategorias");
        List<Categoria> expResult = List.of(lazer, spa);
        List<Categoria> result = instance.listarCategorias();
        assertEquals(expResult, result);
    }

    // ---------- criar ----------

    @Test
    public void testCriar() {
        System.out.println("criar");
        String designacao = "Massagem";
        BigDecimal preco = new BigDecimal("45.00");
        TipoCobranca tipoCobranca = TipoCobranca.POR_UTILIZACAO;
        Servico expResult = new Servico(1, "Massagem", new BigDecimal("45.00"), TipoCobranca.POR_UTILIZACAO, spa, true);
        Servico result = instance.criar(GESTOR, designacao, preco, tipoCobranca, spa);
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarTiraEspacosAMais() {
        System.out.println("criar (espaços a mais)");
        String expResult = "Banho turco";
        Servico servico = instance.criar(GESTOR, "  Banho   turco ", new BigDecimal("20"), TipoCobranca.POR_UTILIZACAO, spa);
        String result = servico.getDesignacao();
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarMesmoNomeOutraCobranca() {
        System.out.println("criar (mesmo nome, outra cobrança)");
        instance.criar(GESTOR, "Ginásio", new BigDecimal("5"), TipoCobranca.POR_UTILIZACAO, lazer);
        instance.criar(GESTOR, "Ginásio", new BigDecimal("25"), TipoCobranca.POR_ESTADIA, lazer);
        int expResult = 2;
        int result = servicoDAL.tabela.size();
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarRececionista() {
        System.out.println("criar (rececionista)");
        try {
            instance.criar(RECECAO, "Sauna", new BigDecimal("15"), TipoCobranca.POR_UTILIZACAO, spa);
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals(0, servicoDAL.tabela.size());
        }
    }

    @Test
    public void testCriarDesignacaoVazia() {
        System.out.println("criar (designação vazia)");
        try {
            instance.criar(GESTOR, "   ", new BigDecimal("15"), TipoCobranca.POR_UTILIZACAO, spa);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escreva a designação do serviço.", expected.getMessage());
        }
    }

    @Test
    public void testCriarPrecoZero() {
        System.out.println("criar (preço zero)");
        try {
            instance.criar(GESTOR, "Wi-Fi", BigDecimal.ZERO, TipoCobranca.POR_ESTADIA, lazer);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("O preço tem de ser maior que zero.", expected.getMessage());
        }
    }

    @Test
    public void testCriarPrecoNegativo() {
        System.out.println("criar (preço negativo)");
        try {
            instance.criar(GESTOR, "Sauna", new BigDecimal("-5"), TipoCobranca.POR_UTILIZACAO, spa);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("O preço tem de ser maior que zero.", expected.getMessage());
        }
    }

    @Test
    public void testCriarPrecoComTresCasasDecimais() {
        System.out.println("criar (3 casas decimais)");
        try {
            instance.criar(GESTOR, "Sauna", new BigDecimal("12.505"), TipoCobranca.POR_UTILIZACAO, spa);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("O preço só pode ter 2 casas decimais.", expected.getMessage());
        }
    }

    @Test
    public void testCriarSemCobranca() {
        System.out.println("criar (sem cobrança)");
        try {
            instance.criar(GESTOR, "Sauna", new BigDecimal("15"), null, spa);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha o tipo de cobrança.", expected.getMessage());
        }
    }

    @Test
    public void testCriarSemCategoria() {
        System.out.println("criar (sem categoria)");
        try {
            instance.criar(GESTOR, "Sauna", new BigDecimal("15"), TipoCobranca.POR_UTILIZACAO, null);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha a categoria.", expected.getMessage());
        }
    }

    @Test
    public void testCriarRepetido() {
        System.out.println("criar (repetido)");
        instance.criar(GESTOR, "Sauna", new BigDecimal("15"), TipoCobranca.POR_UTILIZACAO, spa);
        try {
            instance.criar(GESTOR, " SAUNA ", new BigDecimal("18"), TipoCobranca.POR_UTILIZACAO, spa);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals(1, servicoDAL.tabela.size());
        }
    }

    // ---------- editar ----------

    @Test
    public void testEditar() {
        System.out.println("editar");
        Servico sauna = instance.criar(GESTOR, "Sauna", new BigDecimal("15.00"), TipoCobranca.POR_UTILIZACAO, spa);
        Servico expResult = new Servico(sauna.getId(), "Sauna finlandesa", new BigDecimal("18.50"),
                TipoCobranca.POR_ESTADIA, lazer, true);
        instance.editar(GESTOR, sauna.getId(), "Sauna finlandesa", new BigDecimal("18.50"), TipoCobranca.POR_ESTADIA, lazer);
        Servico result = servicoDAL.procurarPorId(sauna.getId()).orElseThrow();
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarDesativadoContinuaDesativado() {
        System.out.println("editar (serviço desativado)");
        Servico sauna = instance.criar(GESTOR, "Sauna", new BigDecimal("15.00"), TipoCobranca.POR_UTILIZACAO, spa);
        instance.desativar(GESTOR, sauna.getId());
        boolean expResult = false;
        Servico editado = instance.editar(GESTOR, sauna.getId(), "Sauna", new BigDecimal("20.00"), TipoCobranca.POR_UTILIZACAO, spa);
        boolean result = editado.isAtivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testEditarQueNaoExiste() {
        System.out.println("editar (não existe)");
        try {
            instance.editar(GESTOR, 99, "Sauna", new BigDecimal("15"), TipoCobranca.POR_UTILIZACAO, spa);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha um serviço da lista.", expected.getMessage());
        }
    }

    // ---------- desativar e reativar ----------

    @Test
    public void testDesativar() {
        System.out.println("desativar");
        Servico sauna = instance.criar(GESTOR, "Sauna", new BigDecimal("15.00"), TipoCobranca.POR_UTILIZACAO, spa);
        instance.desativar(GESTOR, sauna.getId());
        boolean expResult = false;
        boolean result = servicoDAL.procurarPorId(sauna.getId()).orElseThrow().isAtivo();
        assertEquals(expResult, result);
        assertEquals(1, instance.listar().size());
    }

    @Test
    public void testReativar() {
        System.out.println("reativar");
        Servico sauna = instance.criar(GESTOR, "Sauna", new BigDecimal("15.00"), TipoCobranca.POR_UTILIZACAO, spa);
        instance.desativar(GESTOR, sauna.getId());
        instance.reativar(GESTOR, sauna.getId());
        boolean expResult = true;
        boolean result = servicoDAL.procurarPorId(sauna.getId()).orElseThrow().isAtivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testDesativarRececionista() {
        System.out.println("desativar (rececionista)");
        Servico sauna = instance.criar(GESTOR, "Sauna", new BigDecimal("15.00"), TipoCobranca.POR_UTILIZACAO, spa);
        try {
            instance.desativar(RECECAO, sauna.getId());
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals(true, servicoDAL.procurarPorId(sauna.getId()).orElseThrow().isAtivo());
        }
    }

    // ---------- categorias ----------

    @Test
    public void testCriarCategoria() {
        System.out.println("criarCategoria");
        Categoria expResult = new Categoria(3, "Transporte");
        Categoria result = instance.criarCategoria(GESTOR, "  Transporte ");
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarCategoriaRepetida() {
        System.out.println("criarCategoria (repetida)");
        try {
            instance.criarCategoria(GESTOR, "LAZER");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals(2, categoriaDAL.tabela.size());
        }
    }

    @Test
    public void testCriarCategoriaRececionista() {
        System.out.println("criarCategoria (rececionista)");
        try {
            instance.criarCategoria(RECECAO, "Transporte");
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals(2, categoriaDAL.tabela.size());
        }
    }

    // ---------- lerPreco ----------

    @Test
    public void testLerPreco() {
        System.out.println("lerPreco");
        String texto = "12,50";
        BigDecimal expResult = new BigDecimal("12.50");
        BigDecimal result = ServicoBLL.lerPreco(texto);
        assertEquals(expResult, result);
    }

    @Test
    public void testLerPrecoInvalido() {
        System.out.println("lerPreco (inválido)");
        try {
            ServicoBLL.lerPreco("doze");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escreva um preço válido (por exemplo, 12,50).", expected.getMessage());
        }
    }
}
