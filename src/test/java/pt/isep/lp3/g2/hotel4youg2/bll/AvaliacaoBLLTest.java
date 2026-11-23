package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IAvaliacaoDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Avaliacao;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class AvaliacaoBLLTest {

    private static class AvaliacaoDALFalsa implements IAvaliacaoDAL {
        private final List<Avaliacao> guardadas = new ArrayList<>();
        private LocalDate pedidoDe;
        private LocalDate pedidoAte;

        @Override
        public boolean inserir(Avaliacao avaliacao) {
            if (procurarPorReserva(avaliacao.getReservaId()) != null) {
                return false;
            }
            avaliacao.setId(guardadas.size() + 1);
            guardadas.add(avaliacao);
            return true;
        }

        @Override
        public Avaliacao procurarPorReserva(int reservaId) {
            for (Avaliacao avaliacao : guardadas) {
                if (avaliacao.getReservaId() == reservaId) {
                    return avaliacao;
                }
            }
            return null;
        }

        @Override
        public List<Avaliacao> listar(LocalDate inicio, LocalDate fim) {
            pedidoDe = inicio;
            pedidoAte = fim;
            return guardadas;
        }
    }

    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);
    private static final ContaCliente ANA = new ContaCliente(1, 30, "123456789", "Ana Silva", "h", "s", false);
    private static final ContaCliente RUI = new ContaCliente(2, 31, "987654321", "Rui Costa", "h", "s", false);

    private AvaliacaoDALFalsa avaliacaoDAL;
    private AvaliacaoBLL instance;
    private Reserva concluida;

    @BeforeEach
    public void setUp() {
        avaliacaoDAL = new AvaliacaoDALFalsa();
        instance = new AvaliacaoBLL(avaliacaoDAL);
        concluida = reserva(12, EstadoReserva.CONCLUIDA);
    }

    @AfterEach
    public void tearDown() {
        avaliacaoDAL.guardadas.clear();
        instance = null;
    }

    private static Reserva reserva(int id, EstadoReserva estado) {
        return new Reserva(id, ANA.getClienteId(), 201, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13), 2,
                estado, new BigDecimal("300.00"), null, null);
    }

    private static Avaliacao avaliacao(int id, int reservaId, int estrelas, String comentario) {
        return new Avaliacao(id, reservaId, estrelas, comentario, null);
    }

    private static String texto(int tamanho) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < tamanho; i++) {
            texto.append('a');
        }
        return texto.toString();
    }

    @Test
    public void testAvaliar() {
        System.out.println("avaliar (4 estrelas e um comentário depois do check-out)");
        Avaliacao expResult = avaliacao(1, 12, 4, "Quarto limpo e pessoal simpático.");
        Avaliacao result = instance.avaliar(ANA, concluida, 4, "  Quarto limpo e pessoal simpático.  ");
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarFicaGuardada() {
        System.out.println("avaliar (a avaliação fica guardada para a reserva)");
        Avaliacao expResult = instance.avaliar(ANA, concluida, 5, "Excelente");
        Avaliacao result = avaliacaoDAL.procurarPorReserva(12);
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarSemComentario() {
        System.out.println("avaliar (o comentário é opcional)");
        Avaliacao expResult = avaliacao(1, 12, 3, null);
        Avaliacao result = instance.avaliar(ANA, concluida, 3, "   ");
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarUmaEstrela() {
        System.out.println("avaliar (1 estrela é o mínimo)");
        int expResult = 1;
        int result = instance.avaliar(ANA, concluida, 1, null).getEstrelas();
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarCincoEstrelas() {
        System.out.println("avaliar (5 estrelas é o máximo)");
        int expResult = 5;
        int result = instance.avaliar(ANA, concluida, 5, null).getEstrelas();
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarComentarioNoLimite() {
        System.out.println("avaliar (comentário com 500 caracteres)");
        String expResult = texto(500);
        String result = instance.avaliar(ANA, concluida, 4, texto(500)).getComentario();
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurar() {
        System.out.println("procurar (mostra a avaliação que o cliente já fez)");
        Avaliacao expResult = instance.avaliar(ANA, concluida, 4, "Bom");
        Avaliacao result = instance.procurar(ANA, concluida);
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarAindaSemAvaliacao() {
        System.out.println("procurar (estadia ainda por avaliar)");
        Avaliacao expResult = null;
        Avaliacao result = instance.procurar(ANA, concluida);
        assertEquals(expResult, result);
    }

    @Test
    public void testDoMes() {
        System.out.println("doMes (o gestor vê as avaliações do mês)");
        Avaliacao boa = avaliacao(1, 12, 5, "Excelente");
        avaliacaoDAL.guardadas.add(boa);
        List<Avaliacao> expResult = List.of(boa);
        List<Avaliacao> result = instance.doMes(GESTOR, YearMonth.of(2026, 11));
        assertEquals(expResult, result);
    }

    @Test
    public void testDoMesPrimeiroDia() {
        System.out.println("doMes (procura a partir do dia 1 do mês)");
        instance.doMes(GESTOR, YearMonth.of(2026, 2));
        LocalDate expResult = LocalDate.of(2026, 2, 1);
        LocalDate result = avaliacaoDAL.pedidoDe;
        assertEquals(expResult, result);
    }

    @Test
    public void testDoMesUltimoDia() {
        System.out.println("doMes (procura até ao último dia do mês)");
        instance.doMes(GESTOR, YearMonth.of(2026, 2));
        LocalDate expResult = LocalDate.of(2026, 2, 28);
        LocalDate result = avaliacaoDAL.pedidoAte;
        assertEquals(expResult, result);
    }

    @Test
    public void testMedia() {
        System.out.println("media (5, 4 e 4 estrelas dão 4,3)");
        List<Avaliacao> avaliacoes = List.of(avaliacao(1, 1, 5, null), avaliacao(2, 2, 4, null),
                avaliacao(3, 3, 4, null));
        BigDecimal expResult = new BigDecimal("4.3");
        BigDecimal result = AvaliacaoBLL.media(avaliacoes);
        assertEquals(expResult, result);
    }

    @Test
    public void testMediaSemAvaliacoes() {
        System.out.println("media (sem avaliações não há média)");
        BigDecimal expResult = null;
        BigDecimal result = AvaliacaoBLL.media(new ArrayList<>());
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarDuasVezes() {
        System.out.println("avaliar (a mesma estadia só se avalia uma vez)");
        instance.avaliar(ANA, concluida, 4, null);
        String expResult = "Esta estadia já foi avaliada.";
        String result = "";
        try {
            instance.avaliar(ANA, concluida, 2, "Mudei de ideias");
            fail("Devia ter recusado a segunda avaliação");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarAntesDoCheckout() {
        System.out.println("avaliar (reserva em curso ainda não se avalia)");
        String expResult = "Só pode avaliar a estadia depois do check-out.";
        String result = "";
        try {
            instance.avaliar(ANA, reserva(13, EstadoReserva.EM_CURSO), 4, null);
            fail("Devia ter recusado antes do check-out");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarReservaCancelada() {
        System.out.println("avaliar (reserva cancelada não se avalia)");
        String expResult = "Só pode avaliar a estadia depois do check-out.";
        String result = "";
        try {
            instance.avaliar(ANA, reserva(14, EstadoReserva.CANCELADA), 4, null);
            fail("Devia ter recusado uma reserva cancelada");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarSemEstrelas() {
        System.out.println("avaliar (sem escolher estrelas)");
        String expResult = "Escolha de 1 a 5 estrelas.";
        String result = "";
        try {
            instance.avaliar(ANA, concluida, 0, "Sem estrelas");
            fail("Devia ter pedido as estrelas");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarSeisEstrelas() {
        System.out.println("avaliar (mais de 5 estrelas)");
        String expResult = "Escolha de 1 a 5 estrelas.";
        String result = "";
        try {
            instance.avaliar(ANA, concluida, 6, null);
            fail("Devia ter recusado 6 estrelas");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarComentarioComprido() {
        System.out.println("avaliar (comentário com mais de 500 caracteres)");
        String expResult = "O comentário pode ter até 500 caracteres.";
        String result = "";
        try {
            instance.avaliar(ANA, concluida, 4, texto(501));
            fail("Devia ter recusado o comentário");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarEstadiaDeOutroCliente() {
        System.out.println("avaliar (o Rui não pode avaliar a estadia da Ana)");
        String expResult = "Só pode avaliar as suas estadias.";
        String result = "";
        try {
            instance.avaliar(RUI, concluida, 1, null);
            fail("Devia ter recusado outro cliente");
        } catch (IllegalStateException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarSemConta() {
        System.out.println("avaliar (sem sessão de cliente)");
        String expResult = "Entre na sua conta para avaliar a estadia.";
        String result = "";
        try {
            instance.avaliar(null, concluida, 4, null);
            fail("Devia ter pedido para entrar na conta");
        } catch (IllegalStateException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testAvaliarSemReserva() {
        System.out.println("avaliar (sem estadia escolhida)");
        String expResult = "Escolha a estadia que quer avaliar.";
        String result = "";
        try {
            instance.avaliar(ANA, null, 4, null);
            fail("Devia ter pedido a estadia");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarEstadiaDeOutroCliente() {
        System.out.println("procurar (o Rui não vê a avaliação da Ana)");
        String expResult = "Só pode avaliar as suas estadias.";
        String result = "";
        try {
            instance.procurar(RUI, concluida);
            fail("Devia ter recusado outro cliente");
        } catch (IllegalStateException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testDoMesSoOGestor() {
        System.out.println("doMes (o rececionista não vê as avaliações)");
        String expResult = "Só o gestor pode ver as avaliações das estadias.";
        String result = "";
        try {
            instance.doMes(RECECAO, YearMonth.of(2026, 11));
            fail("Devia ter recusado o rececionista");
        } catch (IllegalStateException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testDoMesSemMes() {
        System.out.println("doMes (sem mês escolhido)");
        String expResult = "Escolha o mês.";
        String result = "";
        try {
            instance.doMes(GESTOR, null);
            fail("Devia ter pedido o mês");
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        }
        assertEquals(expResult, result);
    }
}
