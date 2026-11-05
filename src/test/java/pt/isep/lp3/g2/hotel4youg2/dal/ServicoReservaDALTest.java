package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServicoReservaDALTest {

    // as reservas de teste ficam em 2099, para nunca se cruzarem com reservas verdadeiras
    private static final LocalDate INICIO_DATAS_TESTE = LocalDate.of(2099, 1, 1);

    private ServicoReservaDAL instance;
    private ReservaDAL reservaDAL;
    private ServicoDAL servicoDAL;
    private CategoriaDAL categoriaDAL;
    private Categoria categoria;
    private Servico servico;
    private Reserva reserva;

    // Apaga os restos de execuções anteriores que não chegaram ao tearDown
    @BeforeAll
    @AfterAll
    public static void limparRestosDeTestes() {
        try {
            ConexaoBD.executar("DELETE FROM Reserva WHERE dataEntrada >= ?", INICIO_DATAS_TESTE);
            ConexaoBD.executar("DELETE FROM Servico WHERE designacao LIKE 'Teste SR %'");
            ConexaoBD.executar("DELETE FROM Categoria WHERE nome LIKE 'Teste SR %'");
        } catch (DataAccessException e) {
            // sem BD ou sem a tabela ReservaServico: os testes ficam ignorados no setUp
        }
    }

    // Prepara o contexto: uma categoria, um serviço de 10 € e uma reserva confirmada de um cliente e quarto que já existam
    @BeforeEach
    public void setUp() {
        int clienteId = 0;
        int quarto = 0;
        try {
            List<Integer> tabela = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('ReservaServico') IS NOT NULL "
                    + "THEN 1 ELSE 0 END", rs -> rs.getInt(1));
            Assumptions.assumeTrue(tabela.get(0) == 1, "Falta correr o 09_servicos_reserva.sql");
            List<Integer> clientes = ConexaoBD.select("SELECT TOP 1 pessoaId FROM Cliente", rs -> rs.getInt(1));
            List<Integer> quartos = ConexaoBD.select("SELECT TOP 1 numero FROM Quarto WHERE ativo = 1", rs -> rs.getInt(1));
            Assumptions.assumeTrue(!clientes.isEmpty() && !quartos.isEmpty(), "Faltam clientes ou quartos na BD");
            clienteId = clientes.get(0);
            quarto = quartos.get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new ServicoReservaDAL();
        reservaDAL = new ReservaDAL();
        servicoDAL = new ServicoDAL();
        categoriaDAL = new CategoriaDAL();
        long marca = System.nanoTime();
        categoria = new Categoria(0, "Teste SR " + marca);
        categoriaDAL.inserir(categoria);
        servico = new Servico(0, "Teste SR " + marca, new BigDecimal("10.00"), categoria, true);
        servicoDAL.inserir(servico);
        LocalDate entrada = INICIO_DATAS_TESTE.plusDays(Math.floorMod(marca, 3000));
        reserva = new Reserva(0, clienteId, quarto, entrada, entrada.plusDays(1), 1, EstadoReserva.CONFIRMADA,
                new BigDecimal("100.00"), null, null);
        Assumptions.assumeTrue(reservaDAL.inserirSeLivre(reserva), "O quarto de teste já estava ocupado nessas datas");
    }

    // Desfaz o que o setUp fez: a reserva (e os serviços juntos a ela), o serviço e a categoria de teste
    @AfterEach
    public void tearDown() {
        if (reserva != null && reserva.getId() > 0) {
            reservaDAL.apagar(reserva.getId());
        }
        if (servico != null && servico.getId() > 0) {
            servicoDAL.apagar(servico.getId());
        }
        if (categoria != null && categoria.getId() > 0) {
            categoriaDAL.apagar(categoria.getId());
        }
    }

    private ServicoReserva novoServico(int quantidade) {
        return new ServicoReserva(0, reserva.getId(), servico, quantidade, servico.getPreco(), null);
    }

    @Test
    public void testInserirSeAberta() {
        System.out.println("inserirSeAberta");
        boolean expResult = true;
        boolean result = instance.inserirSeAberta(novoServico(2));
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorReserva() {
        System.out.println("listarPorReserva");
        ServicoReserva junto = novoServico(2);
        instance.inserirSeAberta(junto);
        List<ServicoReserva> expResult = List.of(junto);
        List<ServicoReserva> result = instance.listarPorReserva(reserva.getId());
        assertEquals(expResult, result);
    }

    @Test
    public void testTotalServicosNaReserva() {
        System.out.println("total dos serviços na consulta da reserva");
        instance.inserirSeAberta(novoServico(2));
        instance.inserirSeAberta(novoServico(1));
        BigDecimal expResult = new BigDecimal("130.00");
        BigDecimal result = reservaDAL.procurarPorId(reserva.getId()).getTotal();
        assertEquals(0, expResult.compareTo(result));
    }

    @Test
    public void testInserirReservaCancelada() {
        System.out.println("inserirSeAberta (reserva cancelada)");
        reservaDAL.atualizarEstado(reserva.getId(), EstadoReserva.CANCELADA);
        boolean expResult = false;
        boolean result = instance.inserirSeAberta(novoServico(1));
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirReservaConcluida() {
        System.out.println("inserirSeAberta (reserva concluída)");
        reservaDAL.atualizarEstado(reserva.getId(), EstadoReserva.CONCLUIDA);
        boolean expResult = false;
        boolean result = instance.inserirSeAberta(novoServico(1));
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirServicoDesativado() {
        System.out.println("inserirSeAberta (serviço desativado)");
        servicoDAL.atualizarAtivo(servico.getId(), false);
        boolean expResult = false;
        boolean result = instance.inserirSeAberta(novoServico(1));
        assertEquals(expResult, result);
    }

    @Test
    public void testApagarReservaConcluida() {
        System.out.println("apagarSeAberta (reserva concluída)");
        ServicoReserva junto = novoServico(1);
        instance.inserirSeAberta(junto);
        reservaDAL.atualizarEstado(reserva.getId(), EstadoReserva.CONCLUIDA);
        boolean expResult = false;
        boolean result = instance.apagarSeAberta(junto.getId());
        assertEquals(expResult, result);
    }

    // Processo completo pedido na Sprint Review: inserir, listar e apagar
    @Test
    public void testCrudCompleto() {
        System.out.println("CRUD completo");
        ServicoReserva junto = novoServico(3);

        assertEquals(true, instance.inserirSeAberta(junto));
        assertEquals(List.of(junto), instance.listarPorReserva(reserva.getId()));

        assertEquals(true, instance.apagarSeAberta(junto.getId()));
        assertEquals(List.of(), instance.listarPorReserva(reserva.getId()));
    }
}
