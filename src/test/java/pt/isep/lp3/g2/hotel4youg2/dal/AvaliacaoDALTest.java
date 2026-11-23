package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Avaliacao;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AvaliacaoDALTest {

    private AvaliacaoDAL instance;
    private ReservaDAL reservaDAL;
    private Cliente cliente;
    private Reserva reserva;

    @BeforeAll
    public static void limparRestosDosTestes() {
        RestosDosTestes.limpar();
    }

    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabela = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('AvaliacaoEstadia') IS NULL "
                    + "THEN 0 ELSE 1 END", new InteiroMapper());
            Assumptions.assumeTrue(tabela.get(0) == 1, "Falta correr o 29_avaliacoes_estadia.sql");
            long marca = System.nanoTime();
            cliente = new Cliente(0, "Teste " + marca, null, String.valueOf(900000000L + marca % 100000000L),
                    null, null, null, null);
            new ClienteDAL().inserirCliente(cliente);
            reservaDAL = new ReservaDAL();
            LocalDate entrada = LocalDate.now().plusYears(60).plusDays(marca % 300);
            reserva = new Reserva(0, cliente.getId(), 201, entrada, entrada.plusDays(2), 1,
                    EstadoReserva.CONFIRMADA, new BigDecimal("100.00"), null, null);
            Assumptions.assumeTrue(reservaDAL.inserirSeLivre(reserva), "O quarto 201 já estava ocupado nessas datas");
            reservaDAL.atualizarEstado(reserva.getId(), EstadoReserva.CONCLUIDA);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new AvaliacaoDAL();
    }

    @AfterEach
    public void tearDown() {
        if (reserva != null && reserva.getId() > 0) {
            reservaDAL.apagar(reserva.getId());
        }
        if (cliente != null && cliente.getId() > 0) {
            ConexaoBD.executar("DELETE FROM Cliente WHERE pessoaId = ?", cliente.getId());
            ConexaoBD.executar("DELETE FROM Pessoa WHERE id = ?", cliente.getId());
        }
    }

    private static String descrever(Avaliacao avaliacao) {
        return avaliacao.getId() + " / " + avaliacao.getEstrelas() + " / " + avaliacao.getComentario() + " / "
                + avaliacao.getNomeCliente() + " / " + avaliacao.getQuartoNumero();
    }

    private static Avaliacao procurar(List<Avaliacao> lista, int id) {
        for (Avaliacao avaliacao : lista) {
            if (avaliacao.getId() == id) {
                return avaliacao;
            }
        }
        return null;
    }

    @Test
    public void testInserir() {
        System.out.println("inserir (fica com o id e aparece com o nome do cliente e o quarto)");
        Avaliacao avaliacao = new Avaliacao(0, reserva.getId(), 4, "Quarto limpo.", null);
        instance.inserir(avaliacao);
        String expResult = avaliacao.getId() + " / 4 / Quarto limpo. / " + cliente.getNome() + " / 201";
        String result = descrever(instance.procurarPorReserva(reserva.getId()));
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirDevolveTrue() {
        System.out.println("inserir (a primeira avaliação da estadia)");
        boolean expResult = true;
        boolean result = instance.inserir(new Avaliacao(0, reserva.getId(), 5, null, null));
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirDuasVezes() {
        System.out.println("inserir (a mesma estadia pela segunda vez)");
        instance.inserir(new Avaliacao(0, reserva.getId(), 5, null, null));
        boolean expResult = false;
        boolean result = instance.inserir(new Avaliacao(0, reserva.getId(), 1, "Outra vez", null));
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirReservaNaoConcluida() {
        System.out.println("inserir (reserva que ainda não fez check-out)");
        reservaDAL.atualizarEstado(reserva.getId(), EstadoReserva.EM_CURSO);
        boolean expResult = false;
        boolean result = instance.inserir(new Avaliacao(0, reserva.getId(), 3, null, null));
        assertEquals(expResult, result);
    }

    @Test
    public void testListarMesSemAvaliacoes() {
        System.out.println("listar (mês sem avaliações)");
        int expResult = 0;
        int result = instance.listar(LocalDate.of(1990, 1, 1), LocalDate.of(1990, 1, 31)).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarEsteMes() {
        System.out.println("listar (a avaliação feita hoje aparece no mês de hoje)");
        Avaliacao avaliacao = new Avaliacao(0, reserva.getId(), 2, "Barulho à noite.", null);
        instance.inserir(avaliacao);
        YearMonth mes = YearMonth.now();
        String expResult = avaliacao.getId() + " / 2 / Barulho à noite. / " + cliente.getNome() + " / 201";
        String result = descrever(procurar(instance.listar(mes.atDay(1), mes.atEndOfMonth()), avaliacao.getId()));
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorReservaQueNaoExiste() {
        System.out.println("procurarPorReserva (reserva sem avaliação)");
        Avaliacao expResult = null;
        Avaliacao result = instance.procurarPorReserva(-1);
        assertEquals(expResult, result);
    }
}
