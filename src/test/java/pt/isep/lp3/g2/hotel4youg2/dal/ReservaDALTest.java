package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReservaDALTest {

    private ReservaDAL instance;
    private Cliente cliente;
    private Reserva reserva;
    private final List<Reserva> outras = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        instance = new ReservaDAL();
        long marca = System.nanoTime();
        String nif = String.valueOf(900000000L + marca % 100000000L);
        cliente = new Cliente(0, "Teste " + marca, null, nif, null, null, null, null);
        try {
            new ClienteDAL().inserirCliente(cliente);
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
        for (Reserva outra : outras) {
            if (outra.getId() > 0) {
                instance.apagar(outra.getId());
            }
        }
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

    @Test
    public void testAlterarDatas() {
        System.out.println("alterarDatasSeLivre");
        LocalDate novaEntrada = reserva.getDataEntrada().plusDays(10);
        reserva.alterarDatas(novaEntrada, novaEntrada.plusDays(3));
        instance.alterarDatasSeLivre(reserva);
        Reserva expResult = reserva;
        Reserva result = instance.procurarPorId(reserva.getId());
        assertEquals(expResult, result);
    }

    @Test
    public void testAlterarDatasSobreAPropriaReserva() {
        System.out.println("alterarDatasSeLivre (a própria reserva não conta)");
        LocalDate novaEntrada = reserva.getDataEntrada().plusDays(1);
        reserva.alterarDatas(novaEntrada, novaEntrada.plusDays(2));
        boolean expResult = true;
        boolean result = instance.alterarDatasSeLivre(reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testAlterarDatasQuartoOcupado() {
        System.out.println("alterarDatasSeLivre (quarto ocupado)");
        LocalDate outraEntrada = reserva.getDataEntrada().plusDays(10);
        Reserva outra = new Reserva(0, cliente.getId(), 201, outraEntrada, outraEntrada.plusDays(2), 1,
                EstadoReserva.CONFIRMADA, new BigDecimal("100.00"), null, null);
        instance.inserirSeLivre(outra);
        try {
            reserva.alterarDatas(outraEntrada.plusDays(1), outraEntrada.plusDays(4));
            boolean expResult = false;
            boolean result = instance.alterarDatasSeLivre(reserva);
            assertEquals(expResult, result);
        } finally {
            instance.apagar(outra.getId());
        }
    }

    @Test
    public void testAlterarDatasDepoisDoCheckin() {
        System.out.println("alterarDatasSeLivre (check-in feito entretanto)");
        ConexaoBD.executar("UPDATE Reserva SET estado = 'EM_CURSO', checkinEm = GETDATE() WHERE id = ?", reserva.getId());
        LocalDate novaEntrada = reserva.getDataEntrada().plusDays(10);
        reserva.alterarDatas(novaEntrada, novaEntrada.plusDays(2));
        boolean expResult = false;
        boolean result = instance.alterarDatasSeLivre(reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarCheckin() {
        System.out.println("registarCheckin");
        Assumptions.assumeTrue(reserva.getId() > 0, "O quarto estava ocupado nessas datas");
        reserva.setEstado(EstadoReserva.EM_CURSO);
        reserva.setCheckinEm(reserva.getDataEntrada().atTime(15, 30));
        reserva.setCartao("C" + reserva.getQuartoNumero());
        boolean expResult = true;
        boolean result = instance.registarCheckin(reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarCheckinSegundaVezDevolveFalse() {
        System.out.println("registarCheckin (segunda tentativa)");
        Assumptions.assumeTrue(reserva.getId() > 0, "O quarto estava ocupado nessas datas");
        reserva.setEstado(EstadoReserva.EM_CURSO);
        reserva.setCheckinEm(reserva.getDataEntrada().atTime(15, 30));
        reserva.setCartao("C" + reserva.getQuartoNumero());
        instance.registarCheckin(reserva);
        boolean expResult = false;
        boolean result = instance.registarCheckin(reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarCheckout() {
        System.out.println("registarCheckout");
        Assumptions.assumeTrue(reserva.getId() > 0, "O quarto estava ocupado nessas datas");
        reserva.setEstado(EstadoReserva.EM_CURSO);
        reserva.setCheckinEm(reserva.getDataEntrada().atTime(15, 30));
        reserva.setCartao("C" + reserva.getQuartoNumero());
        assertEquals(true, instance.registarCheckin(reserva), "O check-in de preparação devia ter sido registado");
        reserva.setEstado(EstadoReserva.CONCLUIDA);
        reserva.setCheckoutEm(reserva.getDataSaida().atTime(11, 0));
        boolean expResult = true;
        boolean result = instance.registarCheckout(reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarCheckoutSemCheckinDevolveFalse() {
        System.out.println("registarCheckout (sem check-in)");
        Assumptions.assumeTrue(reserva.getId() > 0, "O quarto estava ocupado nessas datas");
        reserva.setCheckoutEm(reserva.getDataSaida().atTime(11, 0));
        boolean expResult = false;
        boolean result = instance.registarCheckout(reserva);
        assertEquals(expResult, result);
    }

    private Reserva outraReserva(LocalDate entrada, LocalDate saida) {
        Reserva outra = new Reserva(0, cliente.getId(), reserva.getQuartoNumero(), entrada, saida, 1,
                EstadoReserva.CONFIRMADA, new BigDecimal("100.00"), null, null);
        outras.add(outra);
        return outra;
    }

    @Test
    public void testInserirSeLivreNoDiaDeSaidaDeUmLateCheckoutFalha() {
        System.out.println("inserirSeLivre (entrada no dia de um late check-out)");
        LocalDate dia = reserva.getDataSaida().plusDays(10);
        Reserva comLate = outraReserva(dia.minusDays(2), dia);
        comLate.setLateCheckout(true);
        Assumptions.assumeTrue(instance.inserirSeLivre(comLate), "O quarto estava ocupado nessas datas");
        boolean expResult = false;
        boolean result = instance.inserirSeLivre(outraReserva(dia, dia.plusDays(2)));
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirSeLivreNoDiaDeSaidaSemLateCheckoutEAceite() {
        System.out.println("inserirSeLivre (entrada no dia de uma saída standard)");
        LocalDate dia = reserva.getDataSaida().plusDays(10);
        Assumptions.assumeTrue(instance.inserirSeLivre(outraReserva(dia.minusDays(2), dia)),
                "O quarto estava ocupado nessas datas");
        boolean expResult = true;
        boolean result = instance.inserirSeLivre(outraReserva(dia, dia.plusDays(2)));
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirSeLivreComEarlyCheckinNoDiaDeUmaSaidaFalha() {
        System.out.println("inserirSeLivre (early check-in no dia de uma saída)");
        LocalDate dia = reserva.getDataSaida().plusDays(10);
        Assumptions.assumeTrue(instance.inserirSeLivre(outraReserva(dia.minusDays(2), dia)),
                "O quarto estava ocupado nessas datas");
        Reserva comEarly = outraReserva(dia, dia.plusDays(2));
        comEarly.setEarlyCheckin(true);
        boolean expResult = false;
        boolean result = instance.inserirSeLivre(comEarly);
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarEmCursoPorCartao() {
        System.out.println("procurarEmCursoPorCartao");
        Assumptions.assumeTrue(reserva.getId() > 0, "O quarto estava ocupado nessas datas");
        reserva.setEstado(EstadoReserva.EM_CURSO);
        reserva.setCheckinEm(LocalDateTime.now().withNano(0));
        reserva.setCartao("C" + reserva.getQuartoNumero());
        assertEquals(true, instance.registarCheckin(reserva), "O check-in de preparação devia ter sido registado");
        Integer expResult = reserva.getId();
        Reserva encontrada = instance.procurarEmCursoPorCartao(reserva.getCartao());
        Integer result = encontrada == null ? null : encontrada.getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarEmCursoPorCartaoInexistente() {
        System.out.println("procurarEmCursoPorCartao (cartão sem reserva em curso)");
        Reserva expResult = null;
        Reserva result = instance.procurarEmCursoPorCartao("C999");
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarEmCursoPorCartaoDepoisDoCheckout() {
        System.out.println("procurarEmCursoPorCartao (depois do check-out)");
        Assumptions.assumeTrue(reserva.getId() > 0, "O quarto estava ocupado nessas datas");
        reserva.setEstado(EstadoReserva.EM_CURSO);
        reserva.setCheckinEm(LocalDateTime.now().withNano(0));
        reserva.setCartao("C" + reserva.getQuartoNumero());
        assertEquals(true, instance.registarCheckin(reserva), "O check-in de preparação devia ter sido registado");
        reserva.setEstado(EstadoReserva.CONCLUIDA);
        reserva.setCheckoutEm(reserva.getCheckinEm().plusHours(1));
        assertEquals(true, instance.registarCheckout(reserva), "O check-out de preparação devia ter sido registado");
        boolean expResult = false;
        Reserva encontrada = instance.procurarEmCursoPorCartao(reserva.getCartao());
        boolean result = encontrada != null && encontrada.getId() == reserva.getId();
        assertEquals(expResult, result);
    }
}
