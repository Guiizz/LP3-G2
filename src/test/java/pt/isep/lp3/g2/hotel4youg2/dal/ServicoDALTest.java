package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServicoDALTest {

    private ServicoDAL instance;
    private CategoriaDAL categoriaDAL;
    private Categoria categoria;
    private Categoria outraCategoria;
    private Servico servico;

    @BeforeAll
    @AfterAll
    public static void limparRestosDeTestes() {
        try {
            ConexaoBD.executar("IF OBJECT_ID('ReservaServico') IS NOT NULL DELETE FROM ReservaServico "
                    + "WHERE servicoId IN (SELECT id FROM Servico WHERE designacao LIKE 'Teste %')");
            ConexaoBD.executar("DELETE FROM Servico WHERE designacao LIKE 'Teste %' "
                    + "OR categoriaId IN (SELECT id FROM Categoria WHERE nome LIKE 'Teste %')");
            ConexaoBD.executar("DELETE FROM Categoria WHERE nome LIKE 'Teste %'");
        } catch (DataAccessException e) {
        }
    }

    // Prepara o contexto: duas categorias e um serviço de teste gravados na BD
    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabelas = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('Categoria') IS NOT NULL "
                    + "AND COL_LENGTH('Servico', 'categoriaId') IS NOT NULL THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabelas.get(0) == 1, "Falta correr o 04_servicos.sql");
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new ServicoDAL();
        categoriaDAL = new CategoriaDAL();
        long marca = System.nanoTime();
        categoria = new Categoria(0, "Teste " + marca);
        outraCategoria = new Categoria(0, "Teste B " + marca);
        categoriaDAL.inserir(categoria);
        categoriaDAL.inserir(outraCategoria);
        servico = new Servico(0, "Teste " + marca, new BigDecimal("99.90"), categoria, true);
        instance.inserir(servico);
    }

    @AfterEach
    public void tearDown() {
        int idCategoria = categoria != null ? categoria.getId() : 0;
        int idOutraCategoria = outraCategoria != null ? outraCategoria.getId() : 0;
        if (idCategoria > 0 || idOutraCategoria > 0) {
            ConexaoBD.executar("DELETE FROM Servico WHERE categoriaId IN (?, ?)", idCategoria, idOutraCategoria);
        }
        if (categoria != null && categoria.getId() > 0) {
            categoriaDAL.apagar(categoria.getId());
        }
        if (outraCategoria != null && outraCategoria.getId() > 0) {
            categoriaDAL.apagar(outraCategoria.getId());
        }
    }

    @Test
    public void testInserir() {
        System.out.println("inserir");
        boolean expResult = true;
        boolean result = servico.getId() > 0;
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorId() {
        System.out.println("procurarPorId");
        Servico expResult = servico;
        Servico result = instance.procurarPorId(servico.getId()).orElseThrow();
        assertEquals(expResult, result);
    }

    @Test
    public void testListar() {
        System.out.println("listar");
        boolean expResult = true;
        boolean result = instance.listar().contains(servico);
        assertEquals(expResult, result);
    }

    @Test
    public void testExisteDesignacao() {
        System.out.println("existeDesignacao");
        boolean expResult = true;
        boolean result = instance.existeDesignacao(servico.getDesignacao().toUpperCase(), 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testExisteDesignacaoIgnoraOProprio() {
        System.out.println("existeDesignacao (o próprio serviço, ao editar)");
        boolean expResult = false;
        boolean result = instance.existeDesignacao(servico.getDesignacao(), servico.getId());
        assertEquals(expResult, result);
    }

    @Test
    public void testAtualizar() {
        System.out.println("atualizar");
        servico.setPreco(new BigDecimal("120.00"));
        servico.setCategoria(outraCategoria);
        instance.atualizar(servico);
        Servico expResult = servico;
        Servico result = instance.procurarPorId(servico.getId()).orElseThrow();
        assertEquals(expResult, result);
    }

    @Test
    public void testAtualizarAtivo() {
        System.out.println("atualizarAtivo");
        instance.atualizarAtivo(servico.getId(), false);
        boolean expResult = false;
        boolean result = instance.procurarPorId(servico.getId()).orElseThrow().isAtivo();
        assertEquals(expResult, result);
    }

    @Test
    public void testApagar() {
        System.out.println("apagar");
        instance.apagar(servico.getId());
        Optional<Servico> expResult = Optional.empty();
        Optional<Servico> result = instance.procurarPorId(servico.getId());
        assertEquals(expResult, result);
        servico.setId(0);
    }

    // Processo completo pedido na Sprint Review: inserir, listar, atualizar e apagar
    @Test
    public void testCrudCompleto() {
        System.out.println("CRUD completo");
        Servico novo = new Servico(0, "Teste CRUD " + System.nanoTime(), new BigDecimal("999.00"), categoria, true);

        instance.inserir(novo);
        assertEquals(true, instance.listar().contains(novo));
        assertEquals(novo, instance.procurarPorId(novo.getId()).orElseThrow());

        novo.setPreco(new BigDecimal("1000.00"));
        instance.atualizar(novo);
        assertEquals(novo, instance.procurarPorId(novo.getId()).orElseThrow());

        instance.apagar(novo.getId());
        assertEquals(false, instance.listar().contains(novo));
    }

    @Test
    public void testListarCategorias() {
        System.out.println("listar categorias");
        boolean expResult = true;
        boolean result = categoriaDAL.listar().contains(categoria);
        assertEquals(expResult, result);
    }
}
