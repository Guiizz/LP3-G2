package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IPedidoExtraDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IStockDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.MovimentoStock;
import pt.isep.lp3.g2.hotel4youg2.model.PedidoExtra;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.SaidaStock;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoPedido;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.MotivoSaida;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PedidoExtraBLLTest {

    private static class PedidoExtraDALFalsa implements IPedidoExtraDAL {
        private final List<PedidoExtra> pedidos = new ArrayList<>();
        private final List<String> decisoes = new ArrayList<>();
        private boolean reservaEmCurso = true;
        private boolean decisaoFalha;

        @Override
        public boolean inserirSeEmCurso(PedidoExtra pedido) {
            if (!reservaEmCurso) {
                return false;
            }
            pedido.setId(pedidos.size() + 1);
            pedidos.add(pedido);
            return true;
        }

        @Override
        public List<PedidoExtra> listarPorCliente(int clienteId) {
            return clienteId == 15 ? pedidos : new ArrayList<>();
        }

        @Override
        public List<PedidoExtra> listarPendentes() {
            List<PedidoExtra> pendentes = new ArrayList<>();
            for (PedidoExtra pedido : pedidos) {
                if (pedido.isPendente()) {
                    pendentes.add(pedido);
                }
            }
            return pendentes;
        }

        @Override
        public boolean aceitarServico(PedidoExtra pedido, int funcionarioId) {
            if (decisaoFalha) {
                return false;
            }
            decisoes.add("serviço " + pedido.getId() + " por " + funcionarioId);
            return true;
        }

        @Override
        public boolean aceitarMaterial(PedidoExtra pedido, int funcionarioId, TipoSaida tipo) {
            if (decisaoFalha) {
                return false;
            }
            decisoes.add("material " + pedido.getId() + " por " + funcionarioId + " " + tipo.name());
            return true;
        }

        @Override
        public boolean recusar(int pedidoId, int funcionarioId, String motivo) {
            if (decisaoFalha) {
                return false;
            }
            decisoes.add("recusado " + pedidoId + " por " + funcionarioId + ": " + motivo);
            return true;
        }

        @Override
        public boolean apagar(int pedidoId) {
            return false;
        }
    }

    private static class ReservaDALFalsa implements IReservaDAL {
        private final List<Reserva> reservas = new ArrayList<>();

        @Override
        public List<Reserva> listarPorCliente(int clienteId) {
            List<Reserva> resultado = new ArrayList<>();
            for (Reserva reserva : reservas) {
                if (reserva.getClienteId() == clienteId) {
                    resultado.add(reserva);
                }
            }
            return resultado;
        }

        @Override
        public boolean inserirSeLivre(Reserva reserva) {
            return false;
        }

        @Override
        public boolean inserirComServicosSeLivre(Reserva reserva, List<ServicoReserva> servicos) {
            return false;
        }

        @Override
        public Reserva procurarPorId(int id) {
            return null;
        }

        @Override
        public List<Reserva> listar() {
            return reservas;
        }

        @Override
        public boolean atualizarEstado(int id, EstadoReserva estado) {
            return false;
        }

        @Override
        public boolean apagar(int id) {
            return false;
        }

        @Override
        public boolean cancelar(int id) {
            return false;
        }

        @Override
        public int cancelarSemCheckin(LocalDate hoje) {
            return 0;
        }

        @Override
        public boolean apagarSemCheckin(int id) {
            return false;
        }

        @Override
        public boolean alterarDatasSeLivre(Reserva reserva) {
            return false;
        }

        @Override
        public boolean alterarHorarioSeLivre(Reserva reserva) {
            return false;
        }

        @Override
        public boolean registarCheckin(Reserva reserva) {
            return false;
        }

        @Override
        public boolean registarCheckout(Reserva reserva) {
            return false;
        }

        @Override
        public Reserva procurarEmCursoPorCartao(String cartao) {
            return null;
        }
    }

    private static class ServicoDALFalsa implements IServicoDAL {
        @Override
        public List<Servico> listar() {
            return List.of(JANTAR, SPA_DESATIVADO);
        }

        @Override
        public Optional<Servico> procurarPorId(int id) {
            return Optional.empty();
        }

        @Override
        public boolean existeDesignacao(String designacao, int idIgnorar) {
            return false;
        }

        @Override
        public int inserir(Servico servico) {
            return 0;
        }

        @Override
        public boolean atualizar(Servico servico) {
            return false;
        }

        @Override
        public boolean atualizarAtivo(int id, boolean ativo) {
            return false;
        }

        @Override
        public boolean apagar(int id) {
            return false;
        }
    }

    private static class StockDALFalsa implements IStockDAL {
        private final List<MaterialStock> materiais = new ArrayList<>();

        @Override
        public List<MaterialStock> listar() {
            return materiais;
        }

        @Override
        public boolean registarSaida(int materialId, int quantidade, int funcionarioId, TipoSaida tipo,
                                     MotivoSaida motivo, String descricaoMotivo) {
            return false;
        }

        @Override
        public List<MovimentoStock> listarMovimentos(int materialId) {
            return new ArrayList<>();
        }

        @Override
        public List<SaidaStock> listarPorDevolver() {
            return new ArrayList<>();
        }

        @Override
        public boolean devolver(int saidaId, int funcionarioId) {
            return false;
        }
    }

    private static final Servico JANTAR = new Servico(3, "Jantar", new BigDecimal("22.00"),
            new Categoria(1, "Refeição"), true);
    private static final Servico SPA_DESATIVADO = new Servico(9, "Spa", new BigDecimal("40.00"),
            new Categoria(4, "Lazer"), false);
    private static final MaterialStock TOALHA = new MaterialStock(7, "M7", "Toalha", "Têxteis Lda", 10);
    private static final MaterialStock GEL = new MaterialStock(8, "M8", "Gel de banho", "Higiene SA", 3);
    private static final ContaCliente RUI = new ContaCliente(5, 15, "123456789", "Rui Costa", "h", "s", false);
    private static final ContaCliente EVA = new ContaCliente(6, 16, "987654321", "Eva Lima", "h", "s", false);
    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);

    private PedidoExtraDALFalsa pedidoDAL;
    private ReservaDALFalsa reservaDAL;
    private StockDALFalsa stockDAL;
    private Reserva emCurso;
    private Reserva concluida;
    private PedidoExtraBLL instance;

    @BeforeEach
    public void setUp() {
        pedidoDAL = new PedidoExtraDALFalsa();
        reservaDAL = new ReservaDALFalsa();
        stockDAL = new StockDALFalsa();
        LocalDate hoje = LocalDate.now();
        emCurso = new Reserva(30, 15, 201, hoje.minusDays(1), hoje.plusDays(2), 2, EstadoReserva.EM_CURSO,
                new BigDecimal("300.00"), hoje.minusDays(1).atTime(15, 30), null);
        emCurso.setCartao("C201");
        concluida = new Reserva(20, 15, 202, hoje.minusDays(10), hoje.minusDays(8), 1, EstadoReserva.CONCLUIDA,
                new BigDecimal("180.00"), null, null);
        reservaDAL.reservas.add(emCurso);
        reservaDAL.reservas.add(concluida);
        stockDAL.materiais.add(TOALHA);
        stockDAL.materiais.add(GEL);
        instance = new PedidoExtraBLL(pedidoDAL, reservaDAL, new ServicoDALFalsa(), stockDAL);
    }

    @AfterEach
    public void tearDown() {
        pedidoDAL.pedidos.clear();
        pedidoDAL.decisoes.clear();
        reservaDAL.reservas.clear();
        stockDAL.materiais.clear();
    }

    private PedidoExtra toalhaPendente(boolean consumivel) {
        PedidoExtra pedido = instance.pedirMaterial(RUI, emCurso, TOALHA, 2);
        pedido.setConsumivel(consumivel);
        return pedido;
    }

    @Test
    public void testListarServicosSoOsAtivos() {
        System.out.println("listarServicos (só os serviços ativos)");
        List<Servico> expResult = List.of(JANTAR);
        List<Servico> result = instance.listarServicos();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarMateriais() {
        System.out.println("listarMateriais");
        List<MaterialStock> expResult = List.of(TOALHA, GEL);
        List<MaterialStock> result = instance.listarMateriais();
        assertEquals(expResult, result);
    }

    @Test
    public void testPedirServico() {
        System.out.println("pedirServico (fica pendente)");
        PedidoExtra expResult = new PedidoExtra(1, 30, 3, 0, "Jantar", 2, EstadoPedido.PENDENTE);
        PedidoExtra result = instance.pedirServico(RUI, emCurso, JANTAR, 2);
        assertEquals(expResult, result);
    }

    @Test
    public void testPedirServicoFicaGravado() {
        System.out.println("pedirServico (o pedido fica gravado)");
        PedidoExtra pedido = instance.pedirServico(RUI, emCurso, JANTAR, 1);
        List<PedidoExtra> expResult = List.of(pedido);
        List<PedidoExtra> result = pedidoDAL.pedidos;
        assertEquals(expResult, result);
    }

    @Test
    public void testPedirMaterial() {
        System.out.println("pedirMaterial (fica pendente)");
        PedidoExtra expResult = new PedidoExtra(1, 30, 0, 7, "Toalha", 2, EstadoPedido.PENDENTE);
        PedidoExtra result = instance.pedirMaterial(RUI, emCurso, TOALHA, 2);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarDoCliente() {
        System.out.println("listarDoCliente (o cliente vê os seus pedidos e o estado)");
        PedidoExtra jantar = instance.pedirServico(RUI, emCurso, JANTAR, 1);
        PedidoExtra toalha = instance.pedirMaterial(RUI, emCurso, TOALHA, 2);
        List<PedidoExtra> expResult = List.of(jantar, toalha);
        List<PedidoExtra> result = instance.listarDoCliente(RUI);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPendentes() {
        System.out.println("listarPendentes (os decididos saem da lista)");
        PedidoExtra jantar = instance.pedirServico(RUI, emCurso, JANTAR, 1);
        PedidoExtra toalha = instance.pedirMaterial(RUI, emCurso, TOALHA, 2);
        instance.aceitar(RECECAO, jantar);
        List<PedidoExtra> expResult = List.of(toalha);
        List<PedidoExtra> result = instance.listarPendentes(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testAceitarServico() {
        System.out.println("aceitar (o serviço entra na reserva)");
        PedidoExtra pedido = instance.pedirServico(RUI, emCurso, JANTAR, 2);
        instance.aceitar(RECECAO, pedido);
        List<String> esperado = List.of("serviço 1 por 7");
        List<Object> expResult = Arrays.asList(esperado, EstadoPedido.ACEITE);
        List<Object> result = Arrays.asList(pedidoDAL.decisoes, pedido.getEstado());
        assertEquals(expResult, result);
    }

    @Test
    public void testAceitarMaterialNaoConsumivel() {
        System.out.println("aceitar (a toalha sai como saída temporária, volta no check-out)");
        PedidoExtra pedido = toalhaPendente(false);
        instance.aceitar(RECECAO, pedido);
        List<String> expResult = List.of("material 1 por 7 TEMPORARIA");
        List<String> result = pedidoDAL.decisoes;
        assertEquals(expResult, result);
    }

    @Test
    public void testAceitarMaterialConsumivel() {
        System.out.println("aceitar (o gel sai como saída definitiva)");
        PedidoExtra pedido = instance.pedirMaterial(RUI, emCurso, GEL, 1);
        pedido.setConsumivel(true);
        instance.aceitar(RECECAO, pedido);
        List<String> expResult = List.of("material 1 por 7 DEFINITIVA");
        List<String> result = pedidoDAL.decisoes;
        assertEquals(expResult, result);
    }

    @Test
    public void testRecusar() {
        System.out.println("recusar (com o motivo, sem mexer em nada)");
        PedidoExtra pedido = toalhaPendente(false);
        instance.recusar(RECECAO, pedido, "  Já não há toalhas de banho limpas  ");
        PedidoExtra esperado = new PedidoExtra(1, 30, 0, 7, "Toalha", 2, EstadoPedido.RECUSADO);
        esperado.setMotivoRecusa("Já não há toalhas de banho limpas");
        List<Object> expResult = Arrays.asList(esperado,
                List.of("recusado 1 por 7: Já não há toalhas de banho limpas"));
        List<Object> result = Arrays.asList(pedido, pedidoDAL.decisoes);
        assertEquals(expResult, result);
    }

    @Test
    public void testPedirNumaReservaConcluida() {
        System.out.println("pedirServico (a reserva já acabou)");
        String expResult = "Só pode pedir extras para uma reserva sua em curso. / 0";
        try {
            instance.pedirServico(RUI, concluida, JANTAR, 1);
            fail("Devia ter recusado o pedido");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage() + " / " + pedidoDAL.pedidos.size();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testPedirNaReservaDeOutroCliente() {
        System.out.println("pedirServico (a reserva é de outro cliente)");
        String expResult = "Só pode pedir extras para uma reserva sua em curso. / 0";
        try {
            instance.pedirServico(EVA, emCurso, JANTAR, 1);
            fail("Devia ter recusado o pedido");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage() + " / " + pedidoDAL.pedidos.size();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testPedirSemServico() {
        System.out.println("pedirServico (sem serviço)");
        String expResult = "Escolha o serviço. / 0";
        try {
            instance.pedirServico(RUI, emCurso, null, 1);
            fail("Devia ter recusado o pedido");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage() + " / " + pedidoDAL.pedidos.size();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testPedirQuantidadeZero() {
        System.out.println("pedirServico (quantidade 0)");
        String expResult = "A quantidade tem de ser entre 1 e 99. / 0";
        try {
            instance.pedirServico(RUI, emCurso, JANTAR, 0);
            fail("Devia ter recusado o pedido");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage() + " / " + pedidoDAL.pedidos.size();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testPedirQuantidadeDemasiado() {
        System.out.println("pedirServico (quantidade 100)");
        String expResult = "A quantidade tem de ser entre 1 e 99. / 0";
        try {
            instance.pedirServico(RUI, emCurso, JANTAR, 100);
            fail("Devia ter recusado o pedido");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage() + " / " + pedidoDAL.pedidos.size();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testPedirSemReserva() {
        System.out.println("pedirServico (sem reserva)");
        String expResult = "Escolha a reserva em curso. / 0";
        try {
            instance.pedirServico(RUI, null, JANTAR, 1);
            fail("Devia ter recusado o pedido");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage() + " / " + pedidoDAL.pedidos.size();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testPedirQuandoAReservaAcabouEntretanto() {
        System.out.println("pedirServico (a reserva saiu de curso antes de gravar)");
        pedidoDAL.reservaEmCurso = false;
        String expResult = "A reserva 30 já não está em curso. / 0";
        try {
            instance.pedirServico(RUI, emCurso, JANTAR, 1);
            fail("Devia ter recusado o pedido");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage() + " / " + pedidoDAL.pedidos.size();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testPedirMaterialSemMaterial() {
        System.out.println("pedirMaterial (sem material)");
        String expResult = "Escolha o material.";
        try {
            instance.pedirMaterial(RUI, emCurso, null, 1);
            fail("Devia ter pedido o material");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testPedirSemConta() {
        System.out.println("pedirServico (sem conta)");
        String expResult = "Entre na sua conta para pedir extras.";
        try {
            instance.pedirServico(null, emCurso, JANTAR, 1);
            fail("Devia ter exigido a conta");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testListarPendentesSemSessao() {
        System.out.println("listarPendentes (só a receção e o gestor)");
        String expResult = "Só a receção e o gestor podem decidir os pedidos dos clientes.";
        try {
            instance.listarPendentes(null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAceitarSemStockSuficiente() {
        System.out.println("aceitar (sem stock suficiente não se aceita)");
        PedidoExtra pedido = instance.pedirMaterial(RUI, emCurso, GEL, 5);
        String expResult = "Não há stock suficiente de Gel de banho: há 3 e o cliente pediu 5. / 0 / "
                + EstadoPedido.PENDENTE;
        try {
            instance.aceitar(RECECAO, pedido);
            fail("Devia ter recusado por falta de stock");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage() + " / " + pedidoDAL.decisoes.size() + " / " + pedido.getEstado();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAceitarMaterialQueSaiuDoStock() {
        System.out.println("aceitar (o material já não está no stock)");
        PedidoExtra pedido = toalhaPendente(false);
        stockDAL.materiais.clear();
        String expResult = "O material Toalha não está no stock.";
        try {
            instance.aceitar(RECECAO, pedido);
            fail("Devia ter recusado");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAceitarJaDecidido() {
        System.out.println("aceitar (o pedido já foi decidido)");
        PedidoExtra pedido = instance.pedirServico(RUI, emCurso, JANTAR, 1);
        instance.aceitar(RECECAO, pedido);
        String expResult = "Este pedido já foi decidido. / 1";
        try {
            instance.aceitar(RECECAO, pedido);
            fail("Não devia aceitar duas vezes");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage() + " / " + pedidoDAL.decisoes.size();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAceitarQuandoABaseDeDadosRecusa() {
        System.out.println("aceitar (a reserva acabou ou outra pessoa decidiu primeiro)");
        PedidoExtra pedido = instance.pedirServico(RUI, emCurso, JANTAR, 1);
        pedidoDAL.decisaoFalha = true;
        String esperado = "Não foi possível aceitar: o pedido já foi decidido, a reserva já não está em curso "
                + "ou já não há stock suficiente.";
        try {
            instance.aceitar(RECECAO, pedido);
            fail("Devia ter avisado");
        } catch (ValidacaoException expected) {
            String expResult = esperado + " / " + EstadoPedido.PENDENTE;
            String result = expected.getMessage() + " / " + pedido.getEstado();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAceitarSemPedido() {
        System.out.println("aceitar (sem pedido escolhido)");
        String expResult = "Escolha um pedido na tabela.";
        try {
            instance.aceitar(RECECAO, null);
            fail("Devia ter pedido o pedido");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAceitarSemSessao() {
        System.out.println("aceitar (sem sessão de funcionário)");
        PedidoExtra pedido = instance.pedirServico(RUI, emCurso, JANTAR, 1);
        String expResult = "Só a receção e o gestor podem decidir os pedidos dos clientes. / 0";
        try {
            instance.aceitar(null, pedido);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            String result = expected.getMessage() + " / " + pedidoDAL.decisoes.size();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testRecusarSemMotivo() {
        System.out.println("recusar (sem motivo)");
        PedidoExtra pedido = toalhaPendente(false);
        String expResult = "Escreva o motivo da recusa. / " + EstadoPedido.PENDENTE;
        try {
            instance.recusar(RECECAO, pedido, "   ");
            fail("Devia ter pedido o motivo");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage() + " / " + pedido.getEstado();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testRecusarMotivoDemasiadoLongo() {
        System.out.println("recusar (motivo com mais de 200 caracteres)");
        PedidoExtra pedido = toalhaPendente(false);
        String expResult = "O motivo tem no máximo 200 caracteres.";
        try {
            instance.recusar(RECECAO, pedido, "a".repeat(201));
            fail("Devia ter recusado o motivo");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testRecusarJaDecididoNaBaseDeDados() {
        System.out.println("recusar (outra pessoa decidiu primeiro)");
        PedidoExtra pedido = toalhaPendente(false);
        pedidoDAL.decisaoFalha = true;
        String expResult = "Este pedido já foi decidido.";
        try {
            instance.recusar(RECECAO, pedido, "Sem stock");
            fail("Devia ter avisado");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }
}
