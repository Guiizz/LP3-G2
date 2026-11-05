package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ServicoReservaBLLTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 4, 10, 0);

    // Base de dados falsa (em memória): guarda os serviços juntos às reservas e recusa
    // as reservas que entretanto foram fechadas, como faz o SQL da DAL verdadeira
    private static class ServicoReservaDALFalsa implements IServicoReservaDAL {
        final List<ServicoReserva> tabela = new ArrayList<>();
        final Set<Integer> reservasFechadas = new HashSet<>();
        private int proximoId = 1;

        @Override
        public boolean inserirSeAberta(ServicoReserva servicoReserva) {
            if (reservasFechadas.contains(servicoReserva.getReservaId()) || !servicoReserva.getServico().isAtivo()) {
                return false;
            }
            servicoReserva.setId(proximoId++);
            servicoReserva.setAdicionadoEm(AGORA);
            tabela.add(servicoReserva);
            return true;
        }

        @Override
        public List<ServicoReserva> listarPorReserva(int reservaId) {
            List<ServicoReserva> daReserva = new ArrayList<>();
            for (ServicoReserva s : tabela) {
                if (s.getReservaId() == reservaId) {
                    daReserva.add(s);
                }
            }
            return daReserva;
        }

        @Override
        public boolean apagarSeAberta(int id, int reservaId) {
            Iterator<ServicoReserva> iterador = tabela.iterator();
            while (iterador.hasNext()) {
                ServicoReserva s = iterador.next();
                if (s.getId() == id && s.getReservaId() == reservaId && !reservasFechadas.contains(reservaId)) {
                    iterador.remove();
                    return true;
                }
            }
            return false;
        }
    }

    private static class ServicoDALFalsa implements IServicoDAL {
        final List<Servico> tabela = new ArrayList<>();

        @Override
        public List<Servico> listar() {
            return new ArrayList<>(tabela);
        }

        @Override
        public Optional<Servico> procurarPorId(int id) {
            for (Servico s : tabela) {
                if (s.getId() == id) {
                    return Optional.of(s);
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean existeDesignacao(String designacao, int idIgnorar) {
            return false;
        }

        @Override
        public int inserir(Servico servico) {
            tabela.add(servico);
            return servico.getId();
        }

        @Override
        public boolean atualizar(Servico servico) {
            return true;
        }

        @Override
        public boolean atualizarAtivo(int id, boolean ativo) {
            return true;
        }

        @Override
        public boolean apagar(int id) {
            Iterator<Servico> iterador = tabela.iterator();
            while (iterador.hasNext()) {
                if (iterador.next().getId() == id) {
                    iterador.remove();
                    return true;
                }
            }
            return false;
        }
    }

    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);
    private static final Categoria REFEICAO = new Categoria(1, "Refeição");

    private ServicoReservaDALFalsa servicoReservaDAL;
    private ServicoDALFalsa servicoDAL;
    private ServicoReservaBLL instance;
    private Servico pequenoAlmoco;
    private Servico jantar;
    private Reserva reserva;

    @BeforeEach
    public void setUp() {
        servicoReservaDAL = new ServicoReservaDALFalsa();
        servicoDAL = new ServicoDALFalsa();
        pequenoAlmoco = new Servico(1, "Pequeno-almoço", new BigDecimal("12.00"), REFEICAO, true);
        jantar = new Servico(2, "Jantar", new BigDecimal("22.00"), REFEICAO, true);
        servicoDAL.inserir(pequenoAlmoco);
        servicoDAL.inserir(jantar);
        servicoDAL.inserir(new Servico(3, "Almoço antigo", new BigDecimal("15.00"), REFEICAO, false));
        reserva = new Reserva(10, 5, 201, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), 2,
                EstadoReserva.CONFIRMADA, new BigDecimal("200.00"), null, null);
        instance = new ServicoReservaBLL(servicoReservaDAL, servicoDAL);
    }

    @AfterEach
    public void tearDown() {
        servicoReservaDAL.tabela.clear();
        servicoDAL.tabela.clear();
    }

    @Test
    public void testListarServicosAtivos() {
        System.out.println("listarServicosAtivos");
        List<Servico> expResult = List.of(pequenoAlmoco, jantar);
        List<Servico> result = instance.listarServicosAtivos();
        assertEquals(expResult, result);
    }

    @Test
    public void testAdicionar() {
        System.out.println("adicionar");
        ServicoReserva expResult = new ServicoReserva(1, 10, pequenoAlmoco, 3, new BigDecimal("12.00"), AGORA);
        ServicoReserva result = instance.adicionar(RECECAO, reserva, pequenoAlmoco, 3);
        assertEquals(expResult, result);
    }

    @Test
    public void testAdicionarPrecoVezesQuantidade() {
        System.out.println("adicionar (preço da tabela vezes a quantidade)");
        BigDecimal expResult = new BigDecimal("36.00");
        BigDecimal result = instance.adicionar(RECECAO, reserva, pequenoAlmoco, 3).getTotal();
        assertEquals(expResult, result);
    }

    @Test
    public void testAdicionarAtualizaTotalDaReserva() {
        System.out.println("adicionar (atualiza o total da reserva)");
        instance.adicionar(RECECAO, reserva, pequenoAlmoco, 3);
        instance.adicionar(RECECAO, reserva, jantar, 1);
        BigDecimal expResult = new BigDecimal("258.00");
        BigDecimal result = reserva.getTotal();
        assertEquals(expResult, result);
    }

    @Test
    public void testAdicionarGuardaPrecoDoMomento() {
        System.out.println("adicionar (o preço fica o do momento em que foi junto)");
        ServicoReserva junto = instance.adicionar(RECECAO, reserva, pequenoAlmoco, 1);
        pequenoAlmoco.setPreco(new BigDecimal("15.00"));
        BigDecimal expResult = new BigDecimal("12.00");
        BigDecimal result = junto.getPrecoUnitario();
        assertEquals(expResult, result);
    }

    @Test
    public void testAdicionarDuranteEstadia() {
        System.out.println("adicionar (durante a estadia)");
        reserva.setEstado(EstadoReserva.EM_CURSO);
        instance.adicionar(RECECAO, reserva, jantar, 2);
        int expResult = 1;
        int result = servicoReservaDAL.tabela.size();
        assertEquals(expResult, result);
    }

    @Test
    public void testAdicionarReservaCancelada() {
        System.out.println("adicionar (reserva cancelada)");
        reserva.setEstado(EstadoReserva.CANCELADA);
        try {
            instance.adicionar(RECECAO, reserva, jantar, 1);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("A reserva 10 está cancelada: já não é possível juntar serviços", expected.getMessage());
            assertEquals(0, servicoReservaDAL.tabela.size());
        }
    }

    @Test
    public void testAdicionarReservaConcluida() {
        System.out.println("adicionar (reserva concluída)");
        reserva.setEstado(EstadoReserva.CONCLUIDA);
        try {
            instance.adicionar(RECECAO, reserva, jantar, 1);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("A reserva 10 está concluída: já não é possível juntar serviços", expected.getMessage());
        }
    }

    @Test
    public void testAdicionarReservaFechadaEntretanto() {
        System.out.println("adicionar (reserva cancelada noutro computador)");
        servicoReservaDAL.reservasFechadas.add(10);
        try {
            instance.adicionar(RECECAO, reserva, jantar, 1);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals(new BigDecimal("0"), reserva.getTotalServicos());
        }
    }

    @Test
    public void testAdicionarServicoDesativado() {
        System.out.println("adicionar (serviço desativado)");
        Servico desativado = servicoDAL.procurarPorId(3).orElseThrow();
        try {
            instance.adicionar(RECECAO, reserva, desativado, 1);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("O serviço Almoço antigo está desativado", expected.getMessage());
        }
    }

    @Test
    public void testAdicionarSemServico() {
        System.out.println("adicionar (sem serviço)");
        try {
            instance.adicionar(RECECAO, reserva, null, 1);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("Escolha um serviço", expected.getMessage());
        }
    }

    @Test
    public void testAdicionarQuantidadeZero() {
        System.out.println("adicionar (quantidade zero)");
        try {
            instance.adicionar(RECECAO, reserva, jantar, 0);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("A quantidade tem de ser pelo menos 1", expected.getMessage());
        }
    }

    @Test
    public void testAdicionarQuantidadeAcimaDoMaximo() {
        System.out.println("adicionar (quantidade acima de 99)");
        try {
            instance.adicionar(RECECAO, reserva, jantar, 100);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("A quantidade não pode ser maior que 99", expected.getMessage());
        }
    }

    @Test
    public void testAdicionarSemSessao() {
        System.out.println("adicionar (sem sessão)");
        try {
            instance.adicionar(null, reserva, jantar, 1);
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals(0, servicoReservaDAL.tabela.size());
        }
    }

    @Test
    public void testListarDaReserva() {
        System.out.println("listarDaReserva");
        ServicoReserva junto = instance.adicionar(RECECAO, reserva, jantar, 1);
        List<ServicoReserva> expResult = List.of(junto);
        List<ServicoReserva> result = instance.listarDaReserva(reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testRetirar() {
        System.out.println("retirar");
        ServicoReserva junto = instance.adicionar(RECECAO, reserva, jantar, 2);
        instance.retirar(RECECAO, reserva, junto);
        BigDecimal expResult = new BigDecimal("200.00");
        BigDecimal result = reserva.getTotal();
        assertEquals(expResult, result);
        assertEquals(0, servicoReservaDAL.tabela.size());
    }

    @Test
    public void testRetirarReservaCancelada() {
        System.out.println("retirar (reserva cancelada)");
        ServicoReserva junto = instance.adicionar(RECECAO, reserva, jantar, 2);
        reserva.setEstado(EstadoReserva.CANCELADA);
        try {
            instance.retirar(RECECAO, reserva, junto);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals("A reserva 10 está cancelada: já não é possível retirar serviços", expected.getMessage());
            assertEquals(1, servicoReservaDAL.tabela.size());
        }
    }

    @Test
    public void testRetirarDeOutraReserva() {
        System.out.println("retirar (o serviço é de outra reserva)");
        ServicoReserva junto = instance.adicionar(RECECAO, reserva, jantar, 2);
        Reserva outra = new Reserva(11, 5, 202, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), 2,
                EstadoReserva.CONFIRMADA, new BigDecimal("200.00"), null, null);
        try {
            instance.retirar(RECECAO, outra, junto);
            fail("Devia ter lançado ValidacaoException, mas não lançou");
        } catch (ValidacaoException expected) {
            assertEquals(1, servicoReservaDAL.tabela.size());
        }
    }

    @Test
    public void testSomar() {
        System.out.println("somar");
        List<ServicoReserva> servicos = List.of(
                new ServicoReserva(1, 10, pequenoAlmoco, 2, new BigDecimal("12.00"), AGORA),
                new ServicoReserva(2, 10, jantar, 1, new BigDecimal("22.00"), AGORA));
        BigDecimal expResult = new BigDecimal("46.00");
        BigDecimal result = ServicoReservaBLL.somar(servicos);
        assertEquals(expResult, result);
    }
}
