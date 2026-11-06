package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IHorarioReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testes unitários da classe ReservaBLL (US27).
 */
public class ReservaBLLTest {

    /** Repositório de teste: guarda a reserva em memória em vez de ir à base de dados. */
    private static class ReservaDALFalsa implements IReservaDAL {
        private boolean quartoLivre = true;
        private Reserva gravada;
        private boolean podeCancelarNaBD = true;
        private Integer canceladaId;
        private boolean quartoLivreNasNovasDatas = true;
        private Reserva alterada;
        private List<Reserva> reservasDoCliente = new ArrayList<>();
        private Integer clientePedido;
        private boolean podeFazerCheckinNaBD = true;
        private Reserva comCheckin;
        private boolean podeFazerCheckoutNaBD = true;
        private Reserva comCheckout;

        @Override
        public boolean cancelar(int id) {
            if (!podeCancelarNaBD) {
                return false;
            }
            canceladaId = id;
            return true;
        }

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
        public List<Reserva> listarPorCliente(int clienteId) {
            clientePedido = clienteId;
            return new ArrayList<>(reservasDoCliente);
        }

        @Override
        public boolean atualizarEstado(int id, EstadoReserva estado) {
            return true;
        }

        @Override
        public boolean apagar(int id) {
            return true;
        }

        @Override
        public boolean alterarDatasSeLivre(Reserva reserva) {
            if (!quartoLivreNasNovasDatas) {
                return false;
            }
            alterada = reserva;
            return true;
        }

        @Override
        public boolean registarCheckin(Reserva reserva) {
            if (!podeFazerCheckinNaBD) {
                return false;
            }
            comCheckin = reserva;
            return true;
        }

        @Override
        public boolean registarCheckout(Reserva reserva) {
            if (!podeFazerCheckoutNaBD) {
                return false;
            }
            comCheckout = reserva;
            return true;
        }
    }

    private ReservaDALFalsa repositorio;
    private ReservaBLL instance;
    private Cliente cliente;
    private Quarto quarto;
    private LocalDate entrada;

    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO = new Utilizador(2, 2, "rececao", "h", "s", Perfil.RECECIONISTA, true);

    private Reserva reservaConfirmada() {
        return new Reserva(5, 7, 201, entrada, entrada.plusDays(2), 1,
                EstadoReserva.CONFIRMADA, new BigDecimal("200.00"), null, null);
    }

    private static class QuartoDALFalsa implements IQuartoDAL {
        private List<Quarto> livres = new ArrayList<>();
        private Boolean earlyPedido;
        private Boolean latePedido;

        @Override
        public List<Quarto> listarComTarifa() {
            return new ArrayList<>();
        }

        @Override
        public List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida) {
            return listarDisponiveis(entrada, saida, false, false);
        }

        @Override
        public List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida, boolean earlyCheckin,
                                              boolean lateCheckout) {
            earlyPedido = earlyCheckin;
            latePedido = lateCheckout;
            return new ArrayList<>(livres);
        }

        @Override
        public List<Quarto> listarTodos() {
            return new ArrayList<>();
        }

        @Override
        public boolean temReservasFuturas(int numero) {
            return false;
        }

        @Override
        public boolean atualizarAtivo(int numero, boolean ativo) {
            return true;
        }
    }

    private static class HorarioReservaDALFalso implements IHorarioReservaDAL {
        private HorarioReserva horario = new HorarioReserva(LocalTime.of(15, 0), LocalTime.of(12, 0),
                LocalTime.of(13, 0), LocalTime.of(14, 0), new BigDecimal("20.00"), new BigDecimal("25.00"));

        @Override
        public HorarioReserva obter() {
            return horario;
        }

        @Override
        public boolean atualizar(HorarioReserva novo) {
            horario = novo;
            return true;
        }
    }

    private QuartoDALFalsa quartos;
    private HorarioReservaDALFalso horarios;

    private static Quarto quartoLivre(int numero, TipoQuarto tipo, Vista vista, int capacidade, String precoNoite) {
        Quarto livre = new Quarto(numero, numero / 100, tipo, vista, capacidade);
        livre.setTarifa(new Tarifa(tipo, vista, new BigDecimal(precoNoite)));
        return livre;
    }

    @BeforeEach
    public void setUp() {
        repositorio = new ReservaDALFalsa();
        quartos = new QuartoDALFalsa();
        horarios = new HorarioReservaDALFalso();
        instance = new ReservaBLL(repositorio, quartos, horarios);
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

    @Test
    public void testCancelar() {
        System.out.println("cancelar");
        Reserva reserva = reservaConfirmada();
        instance.cancelar(GESTOR, reserva);
        Integer expResult = 5;
        Integer result = repositorio.canceladaId;
        assertEquals(expResult, result);
    }

    @Test
    public void testCancelarRececionista() {
        System.out.println("cancelar (rececionista)");
        try {
            instance.cancelar(RECECAO, reservaConfirmada());
            fail("Devia ter recusado o rececionista");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.canceladaId);
        }
    }

    @Test
    public void testCancelarSemReserva() {
        System.out.println("cancelar (sem reserva)");
        try {
            instance.cancelar(GESTOR, null);
            fail("Devia ter recusado sem reserva");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.canceladaId);
        }
    }

    @Test
    public void testCancelarJaCancelada() {
        System.out.println("cancelar (já cancelada)");
        Reserva reserva = reservaConfirmada();
        reserva.setEstado(EstadoReserva.CANCELADA);
        try {
            instance.cancelar(GESTOR, reserva);
            fail("Devia ter recusado uma reserva já cancelada");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.canceladaId);
        }
    }

    @Test
    public void testCancelarComCheckin() {
        System.out.println("cancelar (com check-in)");
        Reserva reserva = reservaConfirmada();
        reserva.setEstado(EstadoReserva.EM_CURSO);
        reserva.setCheckinEm(LocalDateTime.now());
        try {
            instance.cancelar(GESTOR, reserva);
            fail("Devia ter recusado uma reserva com check-in");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.canceladaId);
        }
    }

    @Test
    public void testCancelarQueJaNaoEstaConfirmadaNaBD() {
        System.out.println("cancelar (não existe ou check-in entretanto)");
        repositorio.podeCancelarNaBD = false;
        try {
            instance.cancelar(GESTOR, reservaConfirmada());
            fail("Devia ter recusado");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.canceladaId);
        }
    }

    @Test
    public void testAlterarDatas() {
        System.out.println("alterarDatas");
        Reserva reserva = reservaConfirmada();
        instance.alterarDatas(RECECAO, reserva, entrada.plusDays(5), entrada.plusDays(8));
        Reserva expResult = new Reserva(5, 7, 201, entrada.plusDays(5), entrada.plusDays(8), 1,
                EstadoReserva.CONFIRMADA, new BigDecimal("300.00"), null, null);
        Reserva result = repositorio.alterada;
        assertEquals(expResult, result);
    }

    @Test
    public void testAlterarDatasSemReserva() {
        System.out.println("alterarDatas (sem reserva)");
        try {
            instance.alterarDatas(RECECAO, null, entrada, entrada.plusDays(2));
            fail("Devia ter recusado sem reserva");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.alterada);
        }
    }

    @Test
    public void testAlterarDatasEntradaNoPassado() {
        System.out.println("alterarDatas (entrada no passado)");
        try {
            instance.alterarDatas(RECECAO, reservaConfirmada(), LocalDate.now().minusDays(1), entrada.plusDays(2));
            fail("Devia ter recusado a entrada no passado");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.alterada);
        }
    }

    @Test
    public void testAlterarDatasSaidaAntesDaEntrada() {
        System.out.println("alterarDatas (saída antes da entrada)");
        try {
            instance.alterarDatas(RECECAO, reservaConfirmada(), entrada.plusDays(5), entrada.plusDays(3));
            fail("Devia ter recusado a saída antes da entrada");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.alterada);
        }
    }

    @Test
    public void testAlterarDatasReservaCancelada() {
        System.out.println("alterarDatas (reserva cancelada)");
        Reserva reserva = reservaConfirmada();
        reserva.setEstado(EstadoReserva.CANCELADA);
        try {
            instance.alterarDatas(RECECAO, reserva, entrada.plusDays(5), entrada.plusDays(8));
            fail("Devia ter recusado uma reserva cancelada");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.alterada);
        }
    }

    @Test
    public void testAlterarDatasQuartoOcupado() {
        System.out.println("alterarDatas (quarto ocupado)");
        repositorio.quartoLivreNasNovasDatas = false;
        try {
            instance.alterarDatas(RECECAO, reservaConfirmada(), entrada.plusDays(5), entrada.plusDays(8));
            fail("Devia ter recusado o quarto ocupado");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.alterada);
        }
    }

    private Reserva reservaDaAna(int id, LocalDate entradaReserva, EstadoReserva estado) {
        return new Reserva(id, 7, 201, entradaReserva, entradaReserva.plusDays(2), 1,
                estado, new BigDecimal("200.00"), null, null);
    }

    @Test
    public void testConsultarDoCliente() {
        System.out.println("consultarDoCliente");
        Reserva antiga = reservaDaAna(1, LocalDate.of(2026, 1, 10), EstadoReserva.CONCLUIDA);
        Reserva recente = reservaDaAna(2, LocalDate.of(2026, 12, 20), EstadoReserva.CONFIRMADA);
        Reserva meio = reservaDaAna(3, LocalDate.of(2026, 6, 5), EstadoReserva.CANCELADA);
        repositorio.reservasDoCliente.add(antiga);
        repositorio.reservasDoCliente.add(recente);
        repositorio.reservasDoCliente.add(meio);

        List<Reserva> expResult = new ArrayList<>();
        expResult.add(recente);
        expResult.add(meio);
        expResult.add(antiga);
        List<Reserva> result = instance.consultarDoCliente(cliente);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarDoClienteMesmaEntrada() {
        System.out.println("consultarDoCliente (mesma data de entrada)");
        LocalDate dia = LocalDate.of(2026, 8, 1);
        Reserva primeira = reservaDaAna(4, dia, EstadoReserva.CANCELADA);
        Reserva segunda = reservaDaAna(9, dia, EstadoReserva.CONFIRMADA);
        repositorio.reservasDoCliente.add(primeira);
        repositorio.reservasDoCliente.add(segunda);

        List<Reserva> expResult = new ArrayList<>();
        expResult.add(segunda);
        expResult.add(primeira);
        List<Reserva> result = instance.consultarDoCliente(cliente);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarDoClienteSemReservas() {
        System.out.println("consultarDoCliente (sem reservas)");
        List<Reserva> expResult = new ArrayList<>();
        List<Reserva> result = instance.consultarDoCliente(cliente);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarDoClientePedeAsReservasDesseCliente() {
        System.out.println("consultarDoCliente (pede pelo id do cliente)");
        instance.consultarDoCliente(cliente);
        Integer expResult = 7;
        Integer result = repositorio.clientePedido;
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarSemCliente() {
        System.out.println("consultarDoCliente (sem cliente)");
        try {
            instance.consultarDoCliente(null);
            fail("Devia ter recusado sem cliente");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.clientePedido);
        }
    }

    @Test
    public void testFazerCheckin() {
        System.out.println("fazerCheckin");
        Reserva reserva = reservaConfirmada();
        LocalDateTime agora = reserva.getDataEntrada().atTime(15, 30);
        String expResult = "C" + reserva.getQuartoNumero();
        String result = instance.fazerCheckin(RECECAO, reserva, agora);
        assertEquals(expResult, result);
    }

    @Test
    public void testFazerCheckinReservaFicaEmCurso() {
        System.out.println("fazerCheckin (reserva passa a EM_CURSO)");
        Reserva reserva = reservaConfirmada();
        LocalDateTime agora = reserva.getDataEntrada().atTime(16, 0);
        Reserva expResult = new Reserva(reserva.getId(), reserva.getClienteId(), reserva.getQuartoNumero(),
                reserva.getDataEntrada(), reserva.getDataSaida(), reserva.getNumHospedes(),
                EstadoReserva.EM_CURSO, reserva.getPrecoTotal(), agora, null);
        expResult.setCartao("C" + reserva.getQuartoNumero());
        instance.fazerCheckin(RECECAO, reserva, agora);
        Reserva result = repositorio.comCheckin;
        assertEquals(expResult, result);
    }

    @Test
    public void testFazerCheckinAs15h() {
        System.out.println("fazerCheckin (às 15h em ponto)");
        Reserva reserva = reservaConfirmada();
        LocalDateTime agora = reserva.getDataEntrada().atTime(15, 0);
        String expResult = "C" + reserva.getQuartoNumero();
        String result = instance.fazerCheckin(GESTOR, reserva, agora);
        assertEquals(expResult, result);
    }

    @Test
    public void testFazerCheckinAntesDas15h() {
        System.out.println("fazerCheckin (antes das 15h)");
        Reserva reserva = reservaConfirmada();
        try {
            instance.fazerCheckin(RECECAO, reserva, reserva.getDataEntrada().atTime(14, 59));
            fail("Devia ter recusado o check-in antes das 15h");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.comCheckin);
        }
    }

    @Test
    public void testFazerCheckinForaDoDiaDeEntrada() {
        System.out.println("fazerCheckin (fora do dia de entrada)");
        Reserva reserva = reservaConfirmada();
        try {
            instance.fazerCheckin(RECECAO, reserva, reserva.getDataEntrada().minusDays(1).atTime(16, 0));
            fail("Devia ter recusado o check-in fora do dia de entrada");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.comCheckin);
        }
    }

    @Test
    public void testFazerCheckinReservaCancelada() {
        System.out.println("fazerCheckin (reserva cancelada)");
        Reserva reserva = reservaConfirmada();
        reserva.setEstado(EstadoReserva.CANCELADA);
        String expResult = "A reserva " + reserva.getId() + " foi cancelada e não pode fazer check-in";
        try {
            instance.fazerCheckin(RECECAO, reserva, reserva.getDataEntrada().atTime(16, 0));
            fail("Devia ter recusado uma reserva cancelada");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testFazerCheckinJaFeito() {
        System.out.println("fazerCheckin (check-in já feito)");
        Reserva reserva = reservaConfirmada();
        reserva.setEstado(EstadoReserva.EM_CURSO);
        reserva.setCheckinEm(reserva.getDataEntrada().atTime(15, 10));
        String expResult = "A reserva " + reserva.getId() + " já tem o check-in feito";
        try {
            instance.fazerCheckin(RECECAO, reserva, reserva.getDataEntrada().atTime(16, 0));
            fail("Devia ter recusado um segundo check-in");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testFazerCheckinSemReserva() {
        System.out.println("fazerCheckin (sem reserva)");
        try {
            instance.fazerCheckin(RECECAO, null, LocalDateTime.now());
            fail("Devia ter recusado sem reserva");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.comCheckin);
        }
    }

    @Test
    public void testFazerCheckinSemSessao() {
        System.out.println("fazerCheckin (sem sessão)");
        Reserva reserva = reservaConfirmada();
        try {
            instance.fazerCheckin(null, reserva, reserva.getDataEntrada().atTime(16, 0));
            fail("Devia ter recusado sem sessão iniciada");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.comCheckin);
        }
    }

    @Test
    public void testFazerCheckinJaNaoPodeNaBD() {
        System.out.println("fazerCheckin (já não está confirmada na BD)");
        repositorio.podeFazerCheckinNaBD = false;
        Reserva reserva = reservaConfirmada();
        try {
            instance.fazerCheckin(RECECAO, reserva, reserva.getDataEntrada().atTime(16, 0));
            fail("Devia ter recusado");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.comCheckin);
        }
    }

    private Reserva reservaEmCurso() {
        Reserva reserva = reservaConfirmada();
        reserva.setEstado(EstadoReserva.EM_CURSO);
        reserva.setCheckinEm(reserva.getDataEntrada().atTime(15, 30));
        reserva.setCartao("C" + reserva.getQuartoNumero());
        return reserva;
    }

    @Test
    public void testTotalAPagar() {
        System.out.println("totalAPagar");
        Reserva reserva = reservaEmCurso();
        BigDecimal expResult = reserva.getPrecoTotal();
        BigDecimal result = instance.totalAPagar(RECECAO, reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testTotalAPagarComServicos() {
        System.out.println("totalAPagar (com serviços extra)");
        Reserva reserva = reservaEmCurso();
        reserva.setTotalServicos(new BigDecimal("35.50"));
        BigDecimal expResult = reserva.getPrecoTotal().add(new BigDecimal("35.50"));
        BigDecimal result = instance.totalAPagar(RECECAO, reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testTotalAPagarReservaSemCheckin() {
        System.out.println("totalAPagar (reserva sem check-in)");
        Reserva reserva = reservaConfirmada();
        String expResult = "Só é possível fazer check-out de uma reserva em curso (a reserva "
                + reserva.getId() + " está CONFIRMADA)";
        try {
            instance.totalAPagar(RECECAO, reserva);
            fail("Devia ter recusado uma reserva que não está em curso");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testTotalAPagarSemSessao() {
        System.out.println("totalAPagar (sem sessão)");
        String expResult = "Entre na aplicação como rececionista ou gestor para fazer check-out";
        try {
            instance.totalAPagar(null, reservaEmCurso());
            fail("Devia ter recusado sem sessão iniciada");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testFazerCheckout() {
        System.out.println("fazerCheckout");
        Reserva reserva = reservaEmCurso();
        LocalDateTime agora = reserva.getDataSaida().atTime(11, 0);
        BigDecimal expResult = reserva.getTotal();
        BigDecimal result = instance.fazerCheckout(RECECAO, reserva, agora);
        assertEquals(expResult, result);
    }

    @Test
    public void testFazerCheckoutReservaFicaConcluida() {
        System.out.println("fazerCheckout (reserva passa a CONCLUIDA)");
        Reserva reserva = reservaEmCurso();
        LocalDateTime agora = reserva.getDataSaida().atTime(11, 0);
        Reserva expResult = new Reserva(reserva.getId(), reserva.getClienteId(), reserva.getQuartoNumero(),
                reserva.getDataEntrada(), reserva.getDataSaida(), reserva.getNumHospedes(),
                EstadoReserva.CONCLUIDA, reserva.getPrecoTotal(), reserva.getCheckinEm(), agora);
        expResult.setCartao(reserva.getCartao());
        instance.fazerCheckout(GESTOR, reserva, agora);
        Reserva result = repositorio.comCheckout;
        assertEquals(expResult, result);
    }

    @Test
    public void testFazerCheckoutCartaoDeixaDeDarAcesso() {
        System.out.println("fazerCheckout (cartão deixa de dar acesso)");
        Reserva reserva = reservaEmCurso();
        instance.fazerCheckout(RECECAO, reserva, reserva.getDataSaida().atTime(11, 0));
        boolean expResult = false;
        boolean result = reserva.cartaoDaAcesso();
        assertEquals(expResult, result);
    }

    @Test
    public void testFazerCheckoutSemCheckin() {
        System.out.println("fazerCheckout (reserva sem check-in)");
        Reserva reserva = reservaConfirmada();
        try {
            instance.fazerCheckout(RECECAO, reserva, reserva.getDataSaida().atTime(11, 0));
            fail("Devia ter recusado uma reserva sem check-in");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.comCheckout);
        }
    }

    @Test
    public void testFazerCheckoutJaConcluida() {
        System.out.println("fazerCheckout (reserva já concluída)");
        Reserva reserva = reservaEmCurso();
        reserva.setEstado(EstadoReserva.CONCLUIDA);
        String expResult = "Só é possível fazer check-out de uma reserva em curso (a reserva "
                + reserva.getId() + " está CONCLUIDA)";
        try {
            instance.fazerCheckout(RECECAO, reserva, reserva.getDataSaida().atTime(11, 0));
            fail("Devia ter recusado uma reserva já concluída");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testFazerCheckoutSemReserva() {
        System.out.println("fazerCheckout (sem reserva)");
        try {
            instance.fazerCheckout(RECECAO, null, LocalDateTime.now());
            fail("Devia ter recusado sem reserva");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.comCheckout);
        }
    }

    @Test
    public void testFazerCheckoutSemSessao() {
        System.out.println("fazerCheckout (sem sessão)");
        Reserva reserva = reservaEmCurso();
        try {
            instance.fazerCheckout(null, reserva, reserva.getDataSaida().atTime(11, 0));
            fail("Devia ter recusado sem sessão iniciada");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.comCheckout);
        }
    }

    @Test
    public void testFazerCheckoutJaNaoPodeNaBD() {
        System.out.println("fazerCheckout (já não está em curso na BD)");
        repositorio.podeFazerCheckoutNaBD = false;
        Reserva reserva = reservaEmCurso();
        try {
            instance.fazerCheckout(RECECAO, reserva, reserva.getDataSaida().atTime(11, 0));
            fail("Devia ter recusado");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.comCheckout);
        }
    }

    private List<Integer> numeros(List<Quarto> lista) {
        List<Integer> numeros = new ArrayList<>();
        for (Quarto q : lista) {
            numeros.add(q.getNumero());
        }
        return numeros;
    }

    @Test
    public void testCalcularTaxa() {
        System.out.println("calcularTaxa (early e late)");
        BigDecimal expResult = horarios.obter().getTaxaEarlyCheckin().add(horarios.obter().getTaxaLateCheckout());
        BigDecimal result = instance.calcularTaxa(true, true);
        assertEquals(expResult, result);
    }

    @Test
    public void testCalcularTaxaSemPedido() {
        System.out.println("calcularTaxa (horas standard)");
        BigDecimal expResult = BigDecimal.ZERO;
        BigDecimal result = instance.calcularTaxa(false, false);
        assertEquals(expResult, result);
    }

    @Test
    public void testOpcoesDisponiveisUmaPorTipoEVista() {
        System.out.println("opcoesDisponiveis (uma opção por tipo e vista)");
        Quarto primeiroDuplo = quartoLivre(201, TipoQuarto.DUPLO, Vista.MAR, 2, "100.00");
        Quarto familia = quartoLivre(309, TipoQuarto.FAMILIA, Vista.MAR, 4, "150.00");
        quartos.livres.add(primeiroDuplo);
        quartos.livres.add(quartoLivre(202, TipoQuarto.DUPLO, Vista.MAR, 2, "100.00"));
        quartos.livres.add(familia);
        List<Integer> expResult = new ArrayList<>();
        expResult.add(primeiroDuplo.getNumero());
        expResult.add(familia.getNumero());
        List<Integer> result = numeros(instance.opcoesDisponiveis(entrada, entrada.plusDays(2), 2, false, false));
        assertEquals(expResult, result);
    }

    @Test
    public void testOpcoesDisponiveisSoComCapacidade() {
        System.out.println("opcoesDisponiveis (só quartos com capacidade)");
        Quarto familia = quartoLivre(309, TipoQuarto.FAMILIA, Vista.MAR, 4, "150.00");
        quartos.livres.add(quartoLivre(201, TipoQuarto.DUPLO, Vista.MAR, 2, "100.00"));
        quartos.livres.add(familia);
        List<Integer> expResult = new ArrayList<>();
        expResult.add(familia.getNumero());
        List<Integer> result = numeros(instance.opcoesDisponiveis(entrada, entrada.plusDays(2), 3, false, false));
        assertEquals(expResult, result);
    }

    @Test
    public void testOpcoesDisponiveisPedeEarlyELateAoDAL() {
        System.out.println("opcoesDisponiveis (passa o pedido de early e late)");
        instance.opcoesDisponiveis(entrada, entrada.plusDays(2), 1, true, true);
        List<Boolean> expResult = new ArrayList<>();
        expResult.add(true);
        expResult.add(true);
        List<Boolean> result = new ArrayList<>();
        result.add(quartos.earlyPedido);
        result.add(quartos.latePedido);
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarComEarlyCheckin() {
        System.out.println("criar (com early check-in)");
        Quarto livre = quartoLivre(201, TipoQuarto.DUPLO, Vista.MAR, 2, "100.00");
        quartos.livres.add(livre);
        LocalDate saida = entrada.plusDays(2);
        Reserva expResult = new Reserva(1, cliente.getId(), livre.getNumero(), entrada, saida, 2,
                EstadoReserva.CONFIRMADA, new BigDecimal("200.00"), null, null);
        expResult.setEarlyCheckin(true);
        expResult.setTaxaHorario(horarios.obter().getTaxaEarlyCheckin());
        Reserva result = instance.criar(cliente, TipoQuarto.DUPLO, Vista.MAR, entrada, saida, 2, true, false);
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarComEarlyELateSomaAsDuasTaxasAoTotal() {
        System.out.println("criar (taxas somadas ao total)");
        quartos.livres.add(quartoLivre(201, TipoQuarto.DUPLO, Vista.MAR, 2, "100.00"));
        Reserva result = instance.criar(cliente, TipoQuarto.DUPLO, Vista.MAR, entrada, entrada.plusDays(2), 2,
                true, true);
        BigDecimal expResult = result.getPrecoTotal()
                .add(horarios.obter().getTaxaEarlyCheckin())
                .add(horarios.obter().getTaxaLateCheckout());
        assertEquals(expResult, result.getTotal());
    }

    @Test
    public void testCriarAtribuiOPrimeiroQuartoDoTipoEscolhido() {
        System.out.println("criar (a aplicação atribui o quarto)");
        Quarto duplo = quartoLivre(205, TipoQuarto.DUPLO, Vista.MAR, 2, "100.00");
        quartos.livres.add(quartoLivre(101, TipoQuarto.INDIVIDUAL, Vista.MAR, 1, "65.00"));
        quartos.livres.add(quartoLivre(201, TipoQuarto.DUPLO, Vista.MONTANHA, 2, "90.00"));
        quartos.livres.add(duplo);
        quartos.livres.add(quartoLivre(306, TipoQuarto.DUPLO, Vista.MAR, 2, "100.00"));
        Integer expResult = duplo.getNumero();
        Integer result = instance.criar(cliente, TipoQuarto.DUPLO, Vista.MAR, entrada, entrada.plusDays(1), 1,
                false, false).getQuartoNumero();
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarSemQuartoQuePermitaOPedido() {
        System.out.println("criar (nenhum quarto permite o pedido)");
        String expResult = "Nenhum quarto DUPLO com vista MAR permite o early check-in ou o late check-out "
                + "nestas datas. Pode fazer a reserva nas horas standard.";
        try {
            instance.criar(cliente, TipoQuarto.DUPLO, Vista.MAR, entrada, entrada.plusDays(2), 2, false, true);
            fail("Devia ter recusado sem quarto que permita o late check-out");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testCriarSemTipoDeQuarto() {
        System.out.println("criar (sem tipo de quarto)");
        try {
            instance.criar(cliente, null, Vista.MAR, entrada, entrada.plusDays(2), 2, false, false);
            fail("Devia ter recusado sem tipo de quarto");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.gravada);
        }
    }

    @Test
    public void testFazerCheckinComEarlyCheckin() {
        System.out.println("fazerCheckin (early check-in às 13h30)");
        Reserva reserva = reservaConfirmada();
        reserva.setEarlyCheckin(true);
        String expResult = "C" + reserva.getQuartoNumero();
        String result = instance.fazerCheckin(RECECAO, reserva, reserva.getDataEntrada().atTime(13, 30));
        assertEquals(expResult, result);
    }

    @Test
    public void testFazerCheckinComEarlyCheckinAntesDaHora() {
        System.out.println("fazerCheckin (early check-in antes das 13h)");
        Reserva reserva = reservaConfirmada();
        reserva.setEarlyCheckin(true);
        try {
            instance.fazerCheckin(RECECAO, reserva, reserva.getDataEntrada().atTime(12, 59));
            fail("Devia ter recusado antes da hora do early check-in");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.comCheckin);
        }
    }

    @Test
    public void testFazerCheckinSemEarlyAs13h30() {
        System.out.println("fazerCheckin (sem early check-in às 13h30)");
        Reserva reserva = reservaConfirmada();
        try {
            instance.fazerCheckin(RECECAO, reserva, reserva.getDataEntrada().atTime(13, 30));
            fail("Devia ter recusado: sem early check-in só a partir das 15h");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.comCheckin);
        }
    }

    @Test
    public void testTotalAPagarComTaxaDeHorario() {
        System.out.println("totalAPagar (com taxa de late check-out)");
        Reserva reserva = reservaEmCurso();
        reserva.setLateCheckout(true);
        reserva.setTaxaHorario(horarios.obter().getTaxaLateCheckout());
        BigDecimal expResult = reserva.getPrecoTotal().add(horarios.obter().getTaxaLateCheckout());
        BigDecimal result = instance.totalAPagar(RECECAO, reserva);
        assertEquals(expResult, result);
    }
}