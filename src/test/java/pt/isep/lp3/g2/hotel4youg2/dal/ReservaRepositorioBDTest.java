package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReservaRepositorioBDTest {

    private ReservaRepositorioBD instance;
    private Cliente cliente;
    private Reserva reserva;

    @BeforeEach
    public void setUp() {
        instance = new ReservaRepositorioBD();
        long marca = System.nanoTime();
        String nif = String.valueOf(900000000L + marca % 100000000L);
        cliente = new Cliente(0, "Teste " + marca, null, nif, null, null, null, null);
        try {
            new ClienteRepositorioBD().inserirCliente(cliente);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        LocalDate entrada = LocalDate.now().plusYears(50).plusDays(marca % 300);
        reserva = new Reserva(0, cliente.getId(), 201, entrada, entrada.plusDays(2), 1,
                EstadoReserva.CONFIRMADA, new BigDecimal("100.00"), null, null);
        instance.inserirSeLivre(reserva);
    }

    @AfterEach
    public void tearDown() {
        if (reserva != null && reserva.getId() > 0) {
            instance.apagar(reserva.getId());
        }
        if (cliente != null && cliente.getId() > 0) {
            ConexaoBD.executar("DELETE FROM Cliente WHERE pessoaId = ?", cliente.getId());
            ConexaoBD.executar("DELETE FROM Pessoa WHERE id = ?", cliente.getId());
        }
    }

    @Test
    public void testInserir() {
        System.out.println("inserir");
        boolean expResult = true;
        boolean result = reserva.getId() > 0;
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorId() {
        System.out.println("procurarPorId");
        Reserva expResult = reserva;
        Reserva result = instance.procurarPorId(reserva.getId());
        assertEquals(expResult, result);
    }

    @Test
    public void testListar() {
        System.out.println("listar");
        boolean expResult = true;
        boolean result = instance.listar().contains(reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testCancelar() {
        System.out.println("cancelar");
        reserva.cancelar();
        instance.atualizarEstado(reserva.getId(), reserva.getEstado());
        EstadoReserva expResult = EstadoReserva.CANCELADA;
        EstadoReserva result = instance.procurarPorId(reserva.getId()).getEstado();
        assertEquals(expResult, result);
    }

    @Test
    public void testCanceladaNaoPodeSerCanceladaOutraVez() {
        System.out.println("cancelar (já cancelada)");
        reserva.cancelar();
        instance.atualizarEstado(reserva.getId(), reserva.getEstado());
        boolean expResult = false;
        boolean result = instance.procurarPorId(reserva.getId()).podeSerCancelada();
        assertEquals(expResult, result);
    }

    @Test
    public void testApagar() {
        System.out.println("apagar");
        instance.apagar(reserva.getId());
        Reserva expResult = null;
        Reserva result = instance.procurarPorId(reserva.getId());
        assertEquals(expResult, result);
        reserva.setId(0);
    }

    @Test
    public void testCancelarDepoisDoCheckin() {
        System.out.println("cancelar (check-in feito entretanto)");
        ConexaoBD.executar("UPDATE Reserva SET estado = 'EM_CURSO', checkinEm = GETDATE() WHERE id = ?", reserva.getId());
        boolean expResult = false;
        boolean result = instance.cancelar(reserva.getId());
        assertEquals(expResult, result);
    }
}