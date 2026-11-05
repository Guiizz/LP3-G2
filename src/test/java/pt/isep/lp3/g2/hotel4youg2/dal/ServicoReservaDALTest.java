package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServicoReservaDALTest {

    private static final String PESSOAS_TESTE = "(SELECT id FROM Pessoa WHERE nome LIKE 'Teste SR %')";

    private ServicoReservaDAL instance;
    private ReservaDAL reservaDAL;
    private ServicoDAL servicoDAL;
    private CategoriaDAL categoriaDAL;
    private Cliente cliente;
    private Categoria categoria;
    private Servico servico;
    private Reserva reserva;

    private static class InteiroMapper implements RowMapper<Integer> {
        @Override
        public Integer map(ResultSet rs) throws SQLException {
            return rs.getInt(1);
        }
    }

    // Apaga só o que estes testes criam, se uma execução anterior não tiver chegado ao tearDown
    @BeforeAll
    public static void limparRestosDeTestes() {
        try {
            ConexaoBD.executar("DELETE FROM Reserva WHERE clienteId IN " + PESSOAS_TESTE);
            ConexaoBD.executar("DELETE FROM Cliente WHERE pessoaId IN " + PESSOAS_TESTE);
            ConexaoBD.executar("DELETE FROM Pessoa WHERE nome LIKE 'Teste SR %'");
            ConexaoBD.executar("DELETE FROM Servico WHERE designacao LIKE 'Teste SR %'");
            ConexaoBD.executar("DELETE FROM Categoria WHERE nome LIKE 'Teste SR %'");
        } catch (DataAccessException e) {
            // sem BD ou sem a tabela ReservaServico: os testes ficam ignorados no setUp
        }
    }

    // Prepara o contexto: um cliente, uma categoria, um serviço de 10 € e uma reserva confirmada, só para o teste
    @BeforeEach
    public void setUp() {
        int quarto = 0;
        long marca = System.nanoTime();
        cliente = new Cliente(0, "Teste SR " + marca, null, String.format("%09d", Math.floorMod(marca, 1_000_000_000L)),
                null, null, null, null);
        try {
            List<Integer> tabela = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('ReservaServico') IS NOT NULL "
                    + "THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabela.get(0) == 1, "Falta correr o 09_servicos_reserva.sql");
            List<Integer> quartos = ConexaoBD.select("SELECT TOP 1 numero FROM Quarto WHERE ativo = 1", new InteiroMapper());
            Assumptions.assumeTrue(!quartos.isEmpty(), "Faltam quartos na BD");
            quarto = quartos.get(0);
            new ClienteDAL().inserirCliente(cliente);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new ServicoReservaDAL();
        reservaDAL = new ReservaDAL();
        servicoDAL = new ServicoDAL();
        categoriaDAL = new CategoriaDAL();
        categoria = new Categoria(0, "Teste SR " + marca);
        categoriaDAL.inserir(categoria);
        servico = new Servico(0, "Teste SR " + marca, new BigDecimal("10.00"), categoria, true);
        servicoDAL.inserir(servico);
        LocalDate entrada = LocalDate.now().plusYears(70).plusDays(Math.floorMod(marca, 300L));
        reserva = new Reserva(0, cliente.getId(), quarto, entrada, entrada.plusDays(1), 1, EstadoReserva.CONFIRMADA,
                new BigDecimal("100.00"), null, null);
        Assumptions.assumeTrue(reservaDAL.inserirSeLivre(reserva), "O quarto de teste já estava ocupado nessas datas");
    }

    // Desfaz o que o setUp fez: a reserva (e os serviços juntos a ela), o serviço, a categoria e o cliente de teste
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
        if (cliente != null && cliente.getId() > 0) {
            ConexaoBD.executar("DELETE FROM Cliente WHERE pessoaId = ?", cliente.getId());
            ConexaoBD.executar("DELETE FROM Pessoa WHERE id = ?", cliente.getId());
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
        boolean result = instance.apagarSeAberta(junto.getId(), reserva.getId());
        assertEquals(expResult, result);
    }

    @Test
    public void testApagarDeOutraReserva() {
        System.out.println("apagarSeAberta (o serviço é de outra reserva)");
        ServicoReserva junto = novoServico(1);
        instance.inserirSeAberta(junto);
        boolean expResult = false;
        boolean result = instance.apagarSeAberta(junto.getId(), reserva.getId() + 1);
        assertEquals(expResult, result);
    }

    // Processo completo pedido na Sprint Review: inserir, listar e apagar
    @Test
    public void testCrudCompleto() {
        System.out.println("CRUD completo");
        ServicoReserva junto = novoServico(3);

        assertEquals(true, instance.inserirSeAberta(junto));
        assertEquals(List.of(junto), instance.listarPorReserva(reserva.getId()));

        assertEquals(true, instance.apagarSeAberta(junto.getId(), reserva.getId()));
        assertEquals(List.of(), instance.listarPorReserva(reserva.getId()));
    }
}
