package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.ReservaRepositorio;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testes unitários da classe ReservaService (US27).
 */
public class ReservaServiceTest {

    /** Repositório de teste: guarda a reserva em memória em vez de ir à base de dados. */
    private static class RepositorioEmMemoria implements ReservaRepositorio {
        private boolean quartoLivre = true;
        private Reserva gravada;

        @Override
        public boolean inserirSeLivre(Reserva reserva) {
            if (!quartoLivre) {
                return false;
            }
            reserva.setId(1);
            gravada = reserva;
            return true;
        }

        @Override
        public Reserva procurarPorId(int id) {
            return gravada;
        }

        @Override
        public List<Reserva> listar() {
            return new ArrayList<>();
        }

        @Override
        public boolean atualizarEstado(int id, EstadoReserva estado) {
            return true;
        }

        @Override
        public boolean apagar(int id) {
            return true;
        }
    }

    private RepositorioEmMemoria repositorio;
    private ReservaService instance;
    private Cliente cliente;
    private Quarto quarto;
    private LocalDate entrada;

    @BeforeEach
    public void setUp() {
        repositorio = new RepositorioEmMemoria();
        instance = new ReservaService(repositorio);
        cliente = new Cliente(7, "Ana Silva", null, "123456789", null, "912345678",
                LocalDate.of(1990, 5, 20), null);
        quarto = new Quarto(201, 2, TipoQuarto.DUPLO, Vista.MAR, 2);
        quarto.setTarifa(new Tarifa(TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("100.00")));
        entrada = LocalDate.now().plusDays(1);
    }

    @AfterEach
    public void tearDown() {
        repositorio = null;
        instance = null;
    }

    @Test
    public void testCalcularPreco() {
        System.out.println("calcularPreco");
        LocalDate saida = entrada.plusDays(3);
        BigDecimal expResult = new BigDecimal("300.00");
        BigDecimal result = instance.calcularPreco(quarto, entrada, saida);
        assertEquals(expResult, result);
    }

    @Test
    public void testCalcularPrecoUmaNoite() {
        System.out.println("calcularPreco (1 noite)");
        LocalDate saida = entrada.plusDays(1);
        BigDecimal expResult = new BigDecimal("100.00");
        BigDecimal result = instance.calcularPreco(quarto, entrada, saida);
        assertEquals(expResult, result);
    }

    @Test
    public void testCriar() {
        System.out.println("criar");
        LocalDate saida = entrada.plusDays(2);
        Reserva expResult = new Reserva(1, 7, 201, entrada, saida, 2,
                EstadoReserva.CONFIRMADA, new BigDecimal("200.00"), null, null);
        Reserva result = instance.criar(cliente, quarto, entrada, saida, 2);
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarGuardaAReservaNoRepositorio() {
        System.out.println("criar (guarda no repositório)");
        LocalDate saida = entrada.plusDays(2);
        Reserva expResult = new Reserva(1, 7, 201, entrada, saida, 1,
                EstadoReserva.CONFIRMADA, new BigDecimal("200.00"), null, null);
        instance.criar(cliente, quarto, entrada, saida, 1);
        Reserva result = repositorio.gravada;
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarParaHoje() {
        System.out.println("criar (entrada hoje)");
        LocalDate hoje = LocalDate.now();
        Reserva expResult = new Reserva(1, 7, 201, hoje, hoje.plusDays(1), 1,
                EstadoReserva.CONFIRMADA, new BigDecimal("100.00"), null, null);
        Reserva result = instance.criar(cliente, quarto, hoje, hoje.plusDays(1), 1);
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarSemCliente() {
        System.out.println("criar (sem cliente)");
        try {
            instance.criar(null, quarto, entrada, entrada.plusDays(1), 1);
            fail("Devia ter recusado uma reserva sem cliente");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.gravada);
        }
    }

    @Test
    public void testCriarSemQuarto() {
        System.out.println("criar (sem quarto)");
        try {
            instance.criar(cliente, null, entrada, entrada.plusDays(1), 1);
            fail("Devia ter recusado uma reserva sem quarto");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.gravada);
        }
    }

    @Test
    public void testCriarComEntradaNoPassado() {
        System.out.println("criar (entrada no passado)");
        try {
            instance.criar(cliente, quarto, LocalDate.now().minusDays(1), entrada, 1);
            fail("Devia ter recusado uma entrada no passado");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.gravada);
        }
    }

    @Test
    public void testCriarComSaidaIgualAEntrada() {
        System.out.println("criar (saída igual à entrada)");
        try {
            instance.criar(cliente, quarto, entrada, entrada, 1);
            fail("Devia ter recusado zero noites");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.gravada);
        }
    }

    @Test
    public void testCriarComSaidaAntesDaEntrada() {
        System.out.println("criar (saída antes da entrada)");
        try {
            instance.criar(cliente, quarto, entrada.plusDays(2), entrada, 1);
            fail("Devia ter recusado a saída antes da entrada");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.gravada);
        }
    }

    @Test
    public void testCriarSemHospedes() {
        System.out.println("criar (0 hóspedes)");
        try {
            instance.criar(cliente, quarto, entrada, entrada.plusDays(1), 0);
            fail("Devia ter recusado 0 hóspedes");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.gravada);
        }
    }

    @Test
    public void testCriarComMaisHospedesQueACapacidade() {
        System.out.println("criar (mais hóspedes que a capacidade)");
        try {
            instance.criar(cliente, quarto, entrada, entrada.plusDays(1), 3);
            fail("Devia ter recusado 3 hóspedes num quarto duplo");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.gravada);
        }
    }

    @Test
    public void testCriarComQuartoJaReservado() {
        System.out.println("criar (quarto já reservado nessas datas)");
        repositorio.quartoLivre = false;
        String expResult = "O quarto 201 já está reservado nessas datas. Pesquise outra vez os quartos disponíveis.";
        try {
            instance.criar(cliente, quarto, entrada, entrada.plusDays(2), 2);
            fail("Devia ter recusado um quarto já reservado");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }
}