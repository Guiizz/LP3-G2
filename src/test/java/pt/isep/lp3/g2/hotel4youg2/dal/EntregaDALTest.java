package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoEntregaHotel;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Unidade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class EntregaDALTest {

    private EntregaDAL instance;
    private Entrega entrega;
    private Entrega entregaPendente;
    private String codigoFornecedor;
    private int funcionarioId;

    // Prepara o contexto: um funcionário que já exista e uma entrega de teste com um fornecedor só para o teste
    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabelas = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('Entrega') IS NOT NULL "
                    + "AND OBJECT_ID('LinhaEntrega') IS NOT NULL THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabelas.get(0) == 1, "Falta correr o 05_entregas.sql");
            List<Integer> funcionarios = ConexaoBD.select("SELECT TOP 1 pessoaId FROM Funcionario", new InteiroMapper());
            Assumptions.assumeTrue(!funcionarios.isEmpty(), "Não há funcionários na BD");
            funcionarioId = funcionarios.get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new EntregaDAL();
        long marca = System.nanoTime() % 1_000_000_000L;
        codigoFornecedor = "T" + marca;
        Fornecedor fornecedor = new Fornecedor(0, codigoFornecedor, "Fornecedor de teste", "Rua A", "nº1", "Porto",
                "4000-000", "Portugal", "PT");
        entrega = new Entrega(0, "TESTE" + marca, LocalDate.of(2026, 10, 5), fornecedor);
        entrega.adicionarLinha(new LinhaEntrega(1, "M-1", "Toalha de teste", 50, Unidade.BOX, 500, new BigDecimal("2.51"),
                new BigDecimal("125.50"), 23, new BigDecimal("28.87"), new BigDecimal("154.37")));
        entrega.adicionarLinha(new LinhaEntrega(2, "M-2", "Champô de teste", 200, Unidade.UNIT, 200, new BigDecimal("3.49"),
                new BigDecimal("698.00"), 23, new BigDecimal("160.54"), new BigDecimal("858.54")));
        instance.registar(entrega, funcionarioId);
    }

    // Desfaz o que o setUp fez: apaga a entrega (e as linhas), os materiais e o fornecedor de teste
    @AfterEach
    public void tearDown() {
        if (instance == null) {
            return;
        }
        if (entrega != null && entrega.getId() > 0) {
            instance.apagar(entrega.getId());
        }
        if (entregaPendente != null && entregaPendente.getId() > 0) {
            instance.apagar(entregaPendente.getId());
        }
        ConexaoBD.executar("DELETE FROM Stock WHERE materialId IN (SELECT id FROM Material WHERE fornecedorId IN "
                + "(SELECT id FROM Fornecedor WHERE codigo = ?))", codigoFornecedor);
        ConexaoBD.executar("DELETE FROM Material WHERE fornecedorId IN (SELECT id FROM Fornecedor WHERE codigo = ?)",
                codigoFornecedor);
        ConexaoBD.executar("DELETE FROM Fornecedor WHERE codigo = ?", codigoFornecedor);
    }

    @Test
    public void testRegistar() {
        System.out.println("registar");
        boolean expResult = true;
        boolean result = entrega.getId() > 0;
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorId() {
        System.out.println("procurarPorId");
        Entrega expResult = entrega;
        Entrega result = instance.procurarPorId(entrega.getId()).orElseThrow();
        assertEquals(expResult, result);
    }

    @Test
    public void testExisteEntrega() {
        System.out.println("existeEntrega");
        boolean expResult = true;
        boolean result = instance.existeEntrega(codigoFornecedor, entrega.getNumeroEncomenda());
        assertEquals(expResult, result);
    }

    @Test
    public void testExisteEntregaOutroNumero() {
        System.out.println("existeEntrega (outro número)");
        boolean expResult = false;
        boolean result = instance.existeEntrega(codigoFornecedor, "NAO-EXISTE");
        assertEquals(expResult, result);
    }

    @Test
    public void testApagar() {
        System.out.println("apagar");
        instance.apagar(entrega.getId());
        Optional<Entrega> expResult = Optional.empty();
        Optional<Entrega> result = instance.procurarPorId(entrega.getId());
        assertEquals(expResult, result);
        entrega.setId(0);
    }

    // uma entrega pendente do mesmo fornecedor de teste, como se tivesse sido carregada por ele
    private Entrega carregarPendente(String numero) {
        Entrega pendente = new Entrega(0, numero, LocalDate.of(2026, 11, 4), entrega.getFornecedor());
        pendente.adicionarLinha(new LinhaEntrega(1, "M-1", "Toalha de teste", 10, Unidade.BOX, 100, new BigDecimal("2.51"),
                new BigDecimal("25.10"), 23, new BigDecimal("5.77"), new BigDecimal("30.87")));
        instance.carregar(pendente);
        return pendente;
    }

    @Test
    public void testRegistarFicaAceite() {
        System.out.println("registar (fica aceite)");
        EstadoEntrega expResult = EstadoEntrega.ACEITE;
        EstadoEntrega result = instance.procurarPorId(entrega.getId()).orElseThrow().getEstado();
        assertEquals(expResult, result);
    }

    @Test
    public void testCarregar() {
        System.out.println("carregar");
        entregaPendente = carregarPendente("TESTE-PENDENTE");
        Entrega result = instance.procurarPorId(entregaPendente.getId()).orElseThrow();
        assertEquals(entregaPendente, result);
        assertEquals(EstadoEntrega.PENDENTE, result.getEstado());
    }

    @Test
    public void testContarPendentes() {
        System.out.println("contarPendentes");
        int expResult = 1;
        entregaPendente = carregarPendente("TESTE-PENDENTE");
        int result = instance.contarPendentes(codigoFornecedor);
        assertEquals(expResult, result);
    }

    @Test
    public void testContarPendentesNaoContaAsAceites() {
        System.out.println("contarPendentes (a entrega aceite não conta)");
        int expResult = 0;
        int result = instance.contarPendentes(codigoFornecedor);
        assertEquals(expResult, result);
    }

    @Test
    public void testExisteEntregaIgnoraAsRecusadas() {
        System.out.println("existeEntrega (recusada pode ser enviada outra vez)");
        ConexaoBD.executar("UPDATE Entrega SET estado = 'RECUSADA' WHERE id = ?", entrega.getId());
        boolean expResult = false;
        boolean result = instance.existeEntrega(codigoFornecedor, entrega.getNumeroEncomenda());
        assertEquals(expResult, result);
    }

    @Test
    public void testCarregarDepoisDeRecusada() {
        System.out.println("carregar (mesmo número depois de recusada)");
        ConexaoBD.executar("UPDATE Entrega SET estado = 'RECUSADA' WHERE id = ?", entrega.getId());
        entregaPendente = carregarPendente(entrega.getNumeroEncomenda());
        boolean expResult = true;
        boolean result = entregaPendente.getId() > 0;
        assertEquals(expResult, result);
    }

    @Test
    public void testListarResumosDoFornecedor() {
        System.out.println("listarResumosDoFornecedor");
        entregaPendente = carregarPendente("TESTE-PENDENTE");
        List<ResumoEntrega> result = instance.listarResumosDoFornecedor(codigoFornecedor);
        assertEquals(2, result.size());
        ResumoEntrega maisRecente = result.get(0);
        assertEquals("TESTE-PENDENTE", maisRecente.getNumeroEncomenda());
        assertEquals(1, maisRecente.getTiposDeMaterial());
        assertEquals(new BigDecimal("30.87"), maisRecente.getTotal());
        assertEquals(EstadoEntrega.PENDENTE, maisRecente.getEstado());
        ResumoEntrega maisAntiga = result.get(1);
        assertEquals(entrega.getNumeroEncomenda(), maisAntiga.getNumeroEncomenda());
        assertEquals(2, maisAntiga.getTiposDeMaterial());
        assertEquals(new BigDecimal("1012.91"), maisAntiga.getTotal());
        assertEquals(EstadoEntrega.ACEITE, maisAntiga.getEstado());
    }

    @Test
    public void testListarResumosDoFornecedorSemEntregas() {
        System.out.println("listarResumosDoFornecedor (sem entregas)");
        List<ResumoEntrega> expResult = List.of();
        List<ResumoEntrega> result = instance.listarResumosDoFornecedor("NAO-EXISTE");
        assertEquals(expResult, result);
    }

    // quantidade em stock de um material do fornecedor de teste (0 se ainda não tem linha)
    private int quantidadeEmStock(String codigoMaterial) {
        List<Integer> quantidades = ConexaoBD.select("SELECT s.quantidade FROM Stock s "
                + "JOIN Material m ON m.id = s.materialId JOIN Fornecedor f ON f.id = m.fornecedorId "
                + "WHERE f.codigo = ? AND m.codigo = ?", new InteiroMapper(), codigoFornecedor, codigoMaterial);
        if (quantidades.isEmpty()) {
            return 0;
        }
        return quantidades.get(0);
    }

    // posição de uma entrega na lista do hotel (-1 se não aparecer)
    private int posicao(List<ResumoEntregaHotel> lista, String numeroEncomenda) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getNumeroEncomenda().equals(numeroEncomenda)) {
                return i;
            }
        }
        return -1;
    }

    @Test
    public void testRegistarSomaAoStock() {
        System.out.println("registar (soma ao stock)");
        assertEquals(500, quantidadeEmStock("M-1"));
        assertEquals(200, quantidadeEmStock("M-2"));
    }

    @Test
    public void testAceitar() {
        System.out.println("aceitar");
        entregaPendente = carregarPendente("TESTE-PENDENTE");
        boolean expResult = true;
        boolean result = instance.aceitar(entregaPendente.getId(), funcionarioId);
        assertEquals(expResult, result);
        assertEquals(EstadoEntrega.ACEITE, instance.procurarPorId(entregaPendente.getId()).orElseThrow().getEstado());
        assertEquals(600, quantidadeEmStock("M-1"));
    }

    @Test
    public void testAceitarDuasVezes() {
        System.out.println("aceitar (a segunda vez falha e não soma outra vez)");
        entregaPendente = carregarPendente("TESTE-PENDENTE");
        instance.aceitar(entregaPendente.getId(), funcionarioId);
        boolean expResult = false;
        boolean result = instance.aceitar(entregaPendente.getId(), funcionarioId);
        assertEquals(expResult, result);
        assertEquals(600, quantidadeEmStock("M-1"));
    }

    @Test
    public void testAceitarEntregaJaRecusada() {
        System.out.println("aceitar (entrega recusada)");
        entregaPendente = carregarPendente("TESTE-PENDENTE");
        instance.recusar(entregaPendente.getId(), funcionarioId, null);
        boolean expResult = false;
        boolean result = instance.aceitar(entregaPendente.getId(), funcionarioId);
        assertEquals(expResult, result);
        assertEquals(500, quantidadeEmStock("M-1"));
    }

    @Test
    public void testAceitarEntregaSemUnidades() {
        System.out.println("aceitar (linha sem o total em unidades)");
        entregaPendente = carregarPendente("TESTE-PENDENTE");
        ConexaoBD.executar("UPDATE LinhaEntrega SET unidades = NULL WHERE entregaId = ? AND numeroLinha = 1",
                entregaPendente.getId());
        String expResult = "A entrega tem linhas sem o total em unidades. Peça ao fornecedor para a voltar a enviar.";
        String result = null;
        try {
            instance.aceitar(entregaPendente.getId(), funcionarioId);
            fail("Devia ter recusado a entrega");
        } catch (IllegalStateException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
        assertEquals(EstadoEntrega.PENDENTE, instance.procurarPorId(entregaPendente.getId()).orElseThrow().getEstado());
        assertEquals(500, quantidadeEmStock("M-1"));
    }

    @Test
    public void testRecusar() {
        System.out.println("recusar");
        entregaPendente = carregarPendente("TESTE-PENDENTE");
        boolean expResult = true;
        boolean result = instance.recusar(entregaPendente.getId(), funcionarioId, "Faltam toalhas");
        assertEquals(expResult, result);
        assertEquals(EstadoEntrega.RECUSADA, instance.procurarPorId(entregaPendente.getId()).orElseThrow().getEstado());
        assertEquals(500, quantidadeEmStock("M-1"));
        ResumoEntrega maisRecente = instance.listarResumosDoFornecedor(codigoFornecedor).get(0);
        assertEquals("Faltam toalhas", maisRecente.getMotivoRecusa());
    }

    @Test
    public void testRecusarSemMotivo() {
        System.out.println("recusar (sem motivo)");
        entregaPendente = carregarPendente("TESTE-PENDENTE");
        instance.recusar(entregaPendente.getId(), funcionarioId, null);
        ResumoEntrega maisRecente = instance.listarResumosDoFornecedor(codigoFornecedor).get(0);
        assertNull(maisRecente.getMotivoRecusa());
    }

    @Test
    public void testRecusarEntregaJaAceite() {
        System.out.println("recusar (entrega aceite)");
        boolean expResult = false;
        boolean result = instance.recusar(entrega.getId(), funcionarioId, "Tarde de mais");
        assertEquals(expResult, result);
        assertEquals(EstadoEntrega.ACEITE, instance.procurarPorId(entrega.getId()).orElseThrow().getEstado());
    }

    @Test
    public void testListarParaHotel() {
        System.out.println("listarParaHotel");
        entregaPendente = carregarPendente("TESTE-PENDENTE");
        List<ResumoEntregaHotel> lista = instance.listarParaHotel();
        int posicaoPendente = posicao(lista, "TESTE-PENDENTE");
        int posicaoAceite = posicao(lista, entrega.getNumeroEncomenda());
        assertTrue(posicaoPendente >= 0 && posicaoAceite >= 0);
        assertTrue(posicaoPendente < posicaoAceite);
        ResumoEntregaHotel pendente = lista.get(posicaoPendente);
        assertEquals(EstadoEntrega.PENDENTE, pendente.getEstado());
        assertNull(pendente.getDecididaPor());
        assertEquals("", pendente.getDecididaEmTexto());
        ResumoEntregaHotel aceite = lista.get(posicaoAceite);
        assertEquals(EstadoEntrega.ACEITE, aceite.getEstado());
        assertNotNull(aceite.getDecididaPor());
        assertEquals("Fornecedor de teste", aceite.getFornecedor());
        assertEquals(100, pendente.getTotalUnidades());
        assertEquals(700, aceite.getTotalUnidades());
    }
}
