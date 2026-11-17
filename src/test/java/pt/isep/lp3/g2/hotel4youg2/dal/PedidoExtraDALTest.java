package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.PedidoExtra;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoPedido;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PedidoExtraDALTest {

    private PedidoExtraDAL instance;
    private int reservaId;
    private int clienteId;
    private int servicoId;
    private int funcionarioId;
    private final List<PedidoExtra> criados = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabela = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('PedidoExtra') IS NOT NULL "
                    + "THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabela.get(0) == 1, "Falta correr o 27_pedidos_extra.sql");
            List<Integer> reservas = ConexaoBD.select("SELECT TOP 1 id FROM Reserva WHERE estado = 'EM_CURSO' "
                    + "ORDER BY id", new InteiroMapper());
            Assumptions.assumeTrue(!reservas.isEmpty(), "Falta uma reserva em curso na BD");
            reservaId = reservas.get(0);
            clienteId = ConexaoBD.select("SELECT clienteId FROM Reserva WHERE id = ?", new InteiroMapper(),
                    reservaId).get(0);
            List<Integer> servicos = ConexaoBD.select("SELECT TOP 1 id FROM Servico WHERE ativo = 1 ORDER BY id",
                    new InteiroMapper());
            Assumptions.assumeTrue(!servicos.isEmpty(), "Falta um serviço ativo na BD");
            servicoId = servicos.get(0);
            funcionarioId = ConexaoBD.select("SELECT TOP 1 pessoaId FROM Funcionario ORDER BY pessoaId",
                    new InteiroMapper()).get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new PedidoExtraDAL();
    }

    @AfterEach
    public void tearDown() {
        for (PedidoExtra pedido : criados) {
            if (pedido.getId() > 0) {
                instance.apagar(pedido.getId());
            }
        }
        criados.clear();
    }

    private PedidoExtra pedir(int quantidade) {
        PedidoExtra pedido = new PedidoExtra(0, reservaId, servicoId, 0, null, quantidade, EstadoPedido.PENDENTE);
        instance.inserirSeEmCurso(pedido);
        criados.add(pedido);
        return pedido;
    }

    private PedidoExtra procurar(List<PedidoExtra> lista, int id) {
        for (PedidoExtra pedido : lista) {
            if (pedido.getId() == id) {
                return pedido;
            }
        }
        return null;
    }

    @Test
    public void testInserirSeEmCurso() {
        System.out.println("inserirSeEmCurso (fica com o id e a hora)");
        PedidoExtra pedido = pedir(2);
        boolean expResult = true;
        boolean result = pedido.getId() > 0 && pedido.getPedidoEm() != null;
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirNumaReservaQueNaoEstaEmCurso() {
        System.out.println("inserirSeEmCurso (reserva que não está em curso)");
        List<Integer> outras = ConexaoBD.select("SELECT TOP 1 id FROM Reserva WHERE estado <> 'EM_CURSO' ORDER BY id",
                new InteiroMapper());
        Assumptions.assumeTrue(!outras.isEmpty(), "Falta uma reserva que não esteja em curso");
        PedidoExtra pedido = new PedidoExtra(0, outras.get(0), servicoId, 0, null, 1, EstadoPedido.PENDENTE);
        boolean expResult = false;
        boolean result = instance.inserirSeEmCurso(pedido);
        criados.add(pedido);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPendentes() {
        System.out.println("listarPendentes (o pedido novo aparece pendente)");
        PedidoExtra pedido = pedir(3);
        EstadoPedido expResult = EstadoPedido.PENDENTE;
        EstadoPedido result = procurar(instance.listarPendentes(), pedido.getId()).getEstado();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorCliente() {
        System.out.println("listarPorCliente (com a quantidade e a reserva)");
        PedidoExtra pedido = pedir(4);
        PedidoExtra lido = procurar(instance.listarPorCliente(clienteId), pedido.getId());
        int expResult = 4;
        int result = lido.getQuantidade();
        assertEquals(expResult, result);
        assertEquals(reservaId, lido.getReservaId());
    }

    @Test
    public void testRecusar() {
        System.out.println("recusar (sai dos pendentes e fica com o motivo)");
        PedidoExtra pedido = pedir(1);
        instance.recusar(pedido.getId(), funcionarioId, "Teste: sem disponibilidade");
        PedidoExtra lido = procurar(instance.listarPorCliente(clienteId), pedido.getId());
        String expResult = "Teste: sem disponibilidade";
        String result = lido.getMotivoRecusa();
        assertEquals(expResult, result);
        assertEquals(null, procurar(instance.listarPendentes(), pedido.getId()));
    }

    @Test
    public void testRecusarDuasVezes() {
        System.out.println("recusar (só os pendentes)");
        PedidoExtra pedido = pedir(1);
        instance.recusar(pedido.getId(), funcionarioId, "Teste");
        boolean expResult = false;
        boolean result = instance.recusar(pedido.getId(), funcionarioId, "Teste outra vez");
        assertEquals(expResult, result);
    }

    @Test
    public void testApagar() {
        System.out.println("apagar");
        PedidoExtra pedido = pedir(1);
        instance.apagar(pedido.getId());
        PedidoExtra expResult = null;
        PedidoExtra result = procurar(instance.listarPorCliente(clienteId), pedido.getId());
        assertEquals(expResult, result);
    }
}
