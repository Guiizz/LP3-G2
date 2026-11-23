package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Material;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testes na base de dados dos materiais da reserva e do stock no check-in e no check-out (US76). Cada teste cria
 * um cliente, uma reserva no quarto 201, um fornecedor e dois materiais (uma toalha não consumível e um champô
 * consumível), com 10 unidades de cada em stock, e apaga tudo no fim.
 */
public class MaterialReservaDALTest {

    private MaterialReservaDAL instance;
    private ReservaDAL reservaDAL;
    private Cliente cliente;
    private Reserva reserva;
    private int fornecedorId;
    private String fornecedor;
    private TipoQuarto tipoDoQuarto;
    private Material toalha;
    private Material champo;
    private int funcionarioId;

    @BeforeAll
    public static void limparRestosDosTestes() {
        RestosDosTestes.limpar();
    }

    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabelas = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('MaterialReserva') IS NOT NULL "
                    + "AND COL_LENGTH('MovimentoStock', 'reservaId') IS NOT NULL "
                    + "AND COL_LENGTH('MovimentoStock', 'tipo') IS NOT NULL THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabelas.get(0) == 1, "Falta correr o 23_materiais_reserva.sql");
            funcionarioId = ConexaoBD.select("SELECT TOP 1 pessoaId FROM Funcionario", new InteiroMapper()).get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new MaterialReservaDAL();
        reservaDAL = new ReservaDAL();
        long marca = System.nanoTime();
        cliente = new Cliente(0, "Teste " + marca, null, String.valueOf(900000000L + marca % 100000000L), null, null,
                null, null);
        new ClienteDAL().inserirCliente(cliente);
        LocalDate entrada = LocalDate.now().plusYears(50).plusDays(marca % 300);
        reserva = new Reserva(0, cliente.getId(), 201, entrada, entrada.plusDays(2), 1, EstadoReserva.CONFIRMADA,
                new BigDecimal("100.00"), null, null);
        Assumptions.assumeTrue(reservaDAL.inserirSeLivre(reserva), "O quarto 201 já estava ocupado nessas datas");
        tipoDoQuarto = TipoQuarto.valueOf(ConexaoBD.select("SELECT tipo FROM Quarto WHERE numero = 201",
                new TextoMapper()).get(0));
        fornecedor = "Teste fornecedor " + marca;
        fornecedorId = ConexaoBD.select("INSERT INTO Fornecedor (codigo, nome) OUTPUT INSERTED.id VALUES (?, ?)",
                new InteiroMapper(), "TR" + Math.floorMod(marca, 100_000_000L), fornecedor).get(0);
        toalha = new Material(inserirMaterial("T-1", "Teste toalha", false), "T-1", "Teste toalha", fornecedor, false);
        champo = new Material(inserirMaterial("C-1", "Teste champô", true), "C-1", "Teste champô", fornecedor, true);
    }

    @AfterEach
    public void tearDown() {
        if (fornecedorId > 0) {
            ConexaoBD.executar("DELETE FROM MovimentoStock WHERE materialId IN "
                    + "(SELECT id FROM Material WHERE fornecedorId = ?)", fornecedorId);
            ConexaoBD.executar("DELETE FROM MaterialReserva WHERE materialId IN "
                    + "(SELECT id FROM Material WHERE fornecedorId = ?)", fornecedorId);
            ConexaoBD.executar("DELETE FROM ConfiguracaoQuarto WHERE materialId IN "
                    + "(SELECT id FROM Material WHERE fornecedorId = ?)", fornecedorId);
            ConexaoBD.executar("DELETE FROM Stock WHERE materialId IN "
                    + "(SELECT id FROM Material WHERE fornecedorId = ?)", fornecedorId);
            ConexaoBD.executar("DELETE FROM Material WHERE fornecedorId = ?", fornecedorId);
            ConexaoBD.executar("DELETE FROM Fornecedor WHERE id = ?", fornecedorId);
        }
        if (reserva != null && reserva.getId() > 0) {
            ConexaoBD.executar("DELETE FROM MovimentoStock WHERE reservaId = ?", reserva.getId());
            reservaDAL.apagar(reserva.getId());
        }
        if (cliente != null && cliente.getId() > 0) {
            ConexaoBD.executar("DELETE FROM Cliente WHERE pessoaId = ?", cliente.getId());
            ConexaoBD.executar("DELETE FROM Pessoa WHERE id = ?", cliente.getId());
        }
    }

    private int inserirMaterial(String codigo, String descricao, boolean consumivel) {
        int id = ConexaoBD.select("INSERT INTO Material (fornecedorId, codigo, descricao, consumivel) OUTPUT "
                + "INSERTED.id VALUES (?, ?, ?, ?)", new InteiroMapper(), fornecedorId, codigo, descricao,
                consumivel).get(0);
        ConexaoBD.executar("INSERT INTO Stock (materialId, quantidade) VALUES (?, 10)", id);
        return id;
    }

    private void configurar(Material material, int quantidade) {
        ConexaoBD.executar("INSERT INTO ConfiguracaoQuarto (tipoQuarto, materialId, quantidade) VALUES (?, ?, ?)",
                tipoDoQuarto.name(), material.getId(), quantidade);
    }

    private void darAReserva(Material material, int quantidade) {
        ConexaoBD.executar("INSERT INTO MaterialReserva (reservaId, materialId, quantidade) VALUES (?, ?, ?)",
                reserva.getId(), material.getId(), quantidade);
    }

    private int emStock(Material material) {
        return ConexaoBD.select("SELECT quantidade FROM Stock WHERE materialId = ?", new InteiroMapper(),
                material.getId()).get(0);
    }

    private String saidaDaReserva(Material material) {
        return ConexaoBD.select("SELECT tipo + '/' + ISNULL(motivo, '-') + '/' + "
                + "CASE WHEN devolvidaEm IS NULL THEN 'fora' ELSE 'devolvida' END FROM MovimentoStock "
                + "WHERE reservaId = ? AND materialId = ?", new TextoMapper(), reserva.getId(),
                material.getId()).get(0);
    }

    private List<MaterialQuarto> osDoTeste() {
        List<MaterialQuarto> doTeste = new ArrayList<>();
        for (MaterialQuarto linha : instance.listar(reserva.getId())) {
            if (linha.getFornecedor().equals(fornecedor)) {
                doTeste.add(linha);
            }
        }
        return doTeste;
    }

    private boolean fazerCheckin() {
        reserva.fazerCheckin(reserva.getDataEntrada().atTime(16, 0), LocalTime.of(15, 0));
        return reservaDAL.registarCheckin(reserva, funcionarioId);
    }

    private static class TextoMapper implements RowMapper<String> {
        @Override
        public String map(ResultSet rs) throws SQLException {
            return rs.getString(1);
        }
    }

    @Test
    public void testListarCarregaAConfiguracaoDoQuarto() {
        System.out.println("listar (sem ajustes, a reserva leva a configuração do tipo do quarto)");
        configurar(toalha, 2);
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(tipoDoQuarto, toalha, 2));
        List<MaterialQuarto> result = osDoTeste();
        assertEquals(expResult, result);
    }

    @Test
    public void testAjustar() {
        System.out.println("ajustar (uma só toalha nesta reserva; o padrão do quarto não muda)");
        configurar(toalha, 2);
        instance.ajustar(reserva.getId(), toalha.getId(), 1);
        List<Object> expResult = Arrays.asList(List.of(new MaterialQuarto(tipoDoQuarto, toalha, 1)), 2);
        int padrao = ConexaoBD.select("SELECT quantidade FROM ConfiguracaoQuarto WHERE tipoQuarto = ? "
                + "AND materialId = ?", new InteiroMapper(), tipoDoQuarto.name(), toalha.getId()).get(0);
        List<Object> result = Arrays.asList(osDoTeste(), padrao);
        assertEquals(expResult, result);
    }

    @Test
    public void testCheckinDaBaixaDoStock() {
        System.out.println("registarCheckin (os dois materiais saem do stock)");
        darAReserva(toalha, 2);
        darAReserva(champo, 3);
        List<Object> expResult = Arrays.asList(true, 8, 7);
        boolean feito = fazerCheckin();
        List<Object> result = Arrays.asList(feito, emStock(toalha), emStock(champo));
        assertEquals(expResult, result);
    }

    @Test
    public void testCheckinConsumivelSaiDeVezENaoConsumivelTemporario() {
        System.out.println("registarCheckin (champô sai de vez, toalha sai temporariamente)");
        darAReserva(toalha, 2);
        darAReserva(champo, 3);
        fazerCheckin();
        String expResult = "TEMPORARIA/-/fora / DEFINITIVA/CONSUMO_RESERVA/fora";
        String result = saidaDaReserva(toalha) + " / " + saidaDaReserva(champo);
        assertEquals(expResult, result);
    }

    @Test
    public void testCheckinComQuantidadeZeroNaoTira() {
        System.out.println("registarCheckin (um material com 0 nesta reserva não sai)");
        darAReserva(toalha, 0);
        darAReserva(champo, 1);
        fazerCheckin();
        String expResult = "10 / 9";
        String result = emStock(toalha) + " / " + emStock(champo);
        assertEquals(expResult, result);
    }

    @Test
    public void testCheckoutDevolveOsNaoConsumiveis() {
        System.out.println("registarCheckout (a toalha volta ao stock, o champô não)");
        darAReserva(toalha, 2);
        darAReserva(champo, 3);
        fazerCheckin();
        reserva.fazerCheckout(reserva.getDataSaida().atTime(11, 0));
        List<Object> expResult = Arrays.asList(true, 10, 7, "TEMPORARIA/-/devolvida");
        boolean feito = reservaDAL.registarCheckout(reserva, funcionarioId);
        List<Object> result = Arrays.asList(feito, emStock(toalha), emStock(champo), saidaDaReserva(toalha));
        assertEquals(expResult, result);
    }

    @Test
    public void testCheckinRepetidoNaoTiraDuasVezes() {
        System.out.println("registarCheckin (segundo check-in da mesma reserva não tira outra vez)");
        darAReserva(toalha, 2);
        fazerCheckin();
        boolean obtido = reservaDAL.registarCheckin(reserva, funcionarioId);
        String expResult = "false / 8";
        String result = obtido + " / " + emStock(toalha);
        assertEquals(expResult, result);
    }

    @Test
    public void testCheckinSemStockNaoFazNada() {
        System.out.println("registarCheckin (sem stock suficiente: avisa e não muda nada)");
        darAReserva(toalha, 2);
        darAReserva(champo, 11);
        List<Object> expResult = Arrays.asList(true, 10, 10, EstadoReserva.CONFIRMADA);
        boolean avisou = false;
        try {
            fazerCheckin();
            fail("Devia ter recusado o check-in");
        } catch (IllegalStateException expected) {
            avisou = expected.getMessage().contains("Teste champô (precisa de 11, há 10)");
        }
        List<Object> result = Arrays.asList(avisou, emStock(toalha), emStock(champo),
                reservaDAL.procurarPorId(reserva.getId()).getEstado());
        assertEquals(expResult, result);
    }
}
