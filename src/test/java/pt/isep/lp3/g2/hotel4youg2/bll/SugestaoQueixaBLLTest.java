package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ISugestaoQueixaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoSugestao;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SugestaoQueixaBLLTest {

    // Base de dados falsa (em memória): guarda as sugestões e queixas e põe a data como a BD faria
    private static class SugestaoQueixaDALFalsa implements ISugestaoQueixaDAL {
        final List<SugestaoQueixa> registadas = new ArrayList<>();

        @Override
        public int inserir(SugestaoQueixa sugestao) {
            registadas.add(sugestao);
            sugestao.setId(registadas.size());
            sugestao.setRegistadaEm(LocalDateTime.of(2026, 10, 29, 18, 30));
            return sugestao.getId();
        }

        @Override
        public Optional<SugestaoQueixa> procurarPorId(int id) {
            for (SugestaoQueixa s : registadas) {
                if (s.getId() == id) {
                    return Optional.of(s);
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean apagar(int id) {
            Iterator<SugestaoQueixa> iterador = registadas.iterator();
            while (iterador.hasNext()) {
                if (iterador.next().getId() == id) {
                    iterador.remove();
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<SugestaoQueixa> listar(TipoSugestao tipo) {
            List<SugestaoQueixa> resultado = new ArrayList<>();
            for (SugestaoQueixa s : registadas) {
                if (tipo == null || s.getTipo() == tipo) {
                    resultado.add(s);
                }
            }
            return resultado;
        }
    }

    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);
    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Cliente CLIENTE = new Cliente(15, "Ana Silva", "12345678", "123456789", "Rua A",
            "912345678", null, "ana@exemplo.pt");

    private SugestaoQueixaDALFalsa sugestaoDAL;
    private SugestaoQueixaBLL instance;

    @BeforeEach
    public void setUp() {
        sugestaoDAL = new SugestaoQueixaDALFalsa();
        instance = new SugestaoQueixaBLL(sugestaoDAL);
    }

    @AfterEach
    public void tearDown() {
        sugestaoDAL.registadas.clear();
    }

    @Test
    public void testRegistar() {
        System.out.println("registar");
        SugestaoQueixa expResult = new SugestaoQueixa(1, 15, TipoSugestao.QUEIXA, "O ar condicionado não funciona.", 7,
                LocalDateTime.of(2026, 10, 29, 18, 30));
        SugestaoQueixa result = instance.registar(RECECAO, CLIENTE, TipoSugestao.QUEIXA, "O ar condicionado não funciona.");
        assertEquals(expResult, result);
        assertEquals(1, sugestaoDAL.registadas.size());
    }

    @Test
    public void testRegistarTiraEspacos() {
        System.out.println("registar (tira os espaços das pontas)");
        String expResult = "Mais opções vegetarianas.";
        String result = instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, "   Mais opções vegetarianas.  ")
                .getDescricao();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarNoLimite() {
        System.out.println("registar (descrição com 1000 caracteres)");
        int expResult = 1000;
        int result = instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, "a".repeat(1000)).getDescricao().length();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSemSessao() {
        System.out.println("registar (sem sessão)");
        try {
            instance.registar(null, CLIENTE, TipoSugestao.QUEIXA, "Barulho no quarto.");
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals(0, sugestaoDAL.registadas.size());
        }
    }

    @Test
    public void testRegistarSemCliente() {
        System.out.println("registar (sem cliente)");
        try {
            instance.registar(RECECAO, null, TipoSugestao.QUEIXA, "Barulho no quarto.");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Procure primeiro o cliente pelo NIF.", expected.getMessage());
        }
    }

    @Test
    public void testRegistarSemTipo() {
        System.out.println("registar (sem tipo)");
        try {
            instance.registar(RECECAO, CLIENTE, null, "Barulho no quarto.");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha o tipo: sugestão ou queixa.", expected.getMessage());
        }
    }

    @Test
    public void testRegistarSemDescricao() {
        System.out.println("registar (descrição só com espaços)");
        try {
            instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, "   ");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A descrição é obrigatória.", expected.getMessage());
            assertEquals(0, sugestaoDAL.registadas.size());
        }
    }

    @Test
    public void testRegistarDescricaoNula() {
        System.out.println("registar (descrição nula)");
        try {
            instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, null);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A descrição é obrigatória.", expected.getMessage());
        }
    }

    @Test
    public void testRegistarDescricaoLonga() {
        System.out.println("registar (descrição com 1001 caracteres)");
        try {
            instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, "a".repeat(1001));
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A descrição não pode ter mais de 1000 caracteres.", expected.getMessage());
        }
    }

    @Test
    public void testListar() {
        System.out.println("listar");
        instance.registar(RECECAO, CLIENTE, TipoSugestao.QUEIXA, "Barulho no corredor.");
        instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, "Mais toalhas.");
        int expResult = 2;
        int result = instance.listar(GESTOR, null).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorTipo() {
        System.out.println("listar (só queixas)");
        instance.registar(RECECAO, CLIENTE, TipoSugestao.QUEIXA, "Barulho no corredor.");
        instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, "Mais toalhas.");
        int expResult = 1;
        int result = instance.listar(GESTOR, TipoSugestao.QUEIXA).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarRececionista() {
        System.out.println("listar (rececionista)");
        try {
            instance.listar(RECECAO, null);
            fail("Devia ter recusado o rececionista");
        } catch (IllegalStateException expected) {
            assertEquals("Só o gestor pode consultar as sugestões e queixas.", expected.getMessage());
        }
    }

    @Test
    public void testListarSemSessao() {
        System.out.println("listar (sem sessão)");
        try {
            instance.listar(null, null);
            fail("Devia ter recusado sem sessão");
        } catch (IllegalStateException expected) {
            assertEquals("Só o gestor pode consultar as sugestões e queixas.", expected.getMessage());
        }
    }

    private static class ReservaDALFalsa implements IReservaDAL {
        private Reserva emCurso;
        private String cartaoPedido;

        @Override
        public Reserva procurarEmCursoPorCartao(String cartao) {
            cartaoPedido = cartao;
            if (emCurso != null && cartao.equals(emCurso.getCartao())) {
                return emCurso;
            }
            return null;
        }

        @Override
        public boolean inserirSeLivre(Reserva reserva) {
            return false;
        }

        @Override
        public Reserva procurarPorId(int id) {
            return null;
        }

        @Override
        public List<Reserva> listar() {
            return new ArrayList<>();
        }

        @Override
        public List<Reserva> listarPorCliente(int clienteId) {
            return new ArrayList<>();
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
        public boolean alterarDatasSeLivre(Reserva reserva) {
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
    }

    private ReservaDALFalsa reservasComCartao() {
        ReservaDALFalsa reservas = new ReservaDALFalsa();
        LocalDate hoje = LocalDate.now();
        reservas.emCurso = new Reserva(30, CLIENTE.getId(), 201, hoje.minusDays(1), hoje.plusDays(2), 1,
                EstadoReserva.EM_CURSO, new BigDecimal("300.00"), hoje.minusDays(1).atTime(15, 30), null);
        reservas.emCurso.setCartao("C201");
        return reservas;
    }

    @Test
    public void testRegistarPeloCartao() {
        System.out.println("registarPeloCartao");
        ReservaDALFalsa reservas = reservasComCartao();
        instance = new SugestaoQueixaBLL(sugestaoDAL, reservas);
        SugestaoQueixa result = instance.registarPeloCartao(reservas.emCurso.getCartao(), TipoSugestao.SUGESTAO,
                "Mais almofadas no quarto");
        SugestaoQueixa expResult = new SugestaoQueixa(result.getId(), reservas.emCurso.getClienteId(),
                TipoSugestao.SUGESTAO, "Mais almofadas no quarto", 0, result.getRegistadaEm());
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPeloCartaoFicaGuardada() {
        System.out.println("registarPeloCartao (fica guardada para o gestor)");
        instance = new SugestaoQueixaBLL(sugestaoDAL, reservasComCartao());
        SugestaoQueixa expResult = instance.registarPeloCartao("C201", TipoSugestao.QUEIXA, "Ar condicionado avariado");
        List<SugestaoQueixa> result = instance.listar(GESTOR, TipoSugestao.QUEIXA);
        assertEquals(List.of(expResult), result);
    }

    @Test
    public void testRegistarPeloCartaoAceitaMinusculasEEspacos() {
        System.out.println("registarPeloCartao (minúsculas e espaços)");
        ReservaDALFalsa reservas = reservasComCartao();
        instance = new SugestaoQueixaBLL(sugestaoDAL, reservas);
        instance.registarPeloCartao("  c201 ", TipoSugestao.SUGESTAO, "Mais toalhas");
        String expResult = "C201";
        String result = reservas.cartaoPedido;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPeloCartaoInvalido() {
        System.out.println("registarPeloCartao (cartão inválido ou reserva terminada)");
        instance = new SugestaoQueixaBLL(sugestaoDAL, reservasComCartao());
        String expResult = "O cartão C999 não é válido ou é de uma reserva que já não está em curso.";
        try {
            instance.registarPeloCartao("C999", TipoSugestao.QUEIXA, "Barulho");
            fail("Devia ter recusado um cartão sem reserva em curso");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(0, sugestaoDAL.registadas.size());
        }
    }

    @Test
    public void testRegistarPeloCartaoSemCartao() {
        System.out.println("registarPeloCartao (sem cartão)");
        instance = new SugestaoQueixaBLL(sugestaoDAL, reservasComCartao());
        try {
            instance.registarPeloCartao("   ", TipoSugestao.QUEIXA, "Barulho");
            fail("Devia ter recusado sem cartão");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escreva o número do cartão.", expected.getMessage());
        }
    }

    @Test
    public void testRegistarPeloCartaoSemTipo() {
        System.out.println("registarPeloCartao (sem tipo)");
        instance = new SugestaoQueixaBLL(sugestaoDAL, reservasComCartao());
        try {
            instance.registarPeloCartao("C201", null, "Barulho");
            fail("Devia ter recusado sem tipo");
        } catch (IllegalArgumentException expected) {
            assertEquals(0, sugestaoDAL.registadas.size());
        }
    }

    @Test
    public void testRegistarPeloCartaoSemDescricao() {
        System.out.println("registarPeloCartao (sem descrição)");
        instance = new SugestaoQueixaBLL(sugestaoDAL, reservasComCartao());
        try {
            instance.registarPeloCartao("C201", TipoSugestao.SUGESTAO, "   ");
            fail("Devia ter recusado sem descrição");
        } catch (IllegalArgumentException expected) {
            assertEquals("A descrição é obrigatória.", expected.getMessage());
            assertEquals(0, sugestaoDAL.registadas.size());
        }
    }
}
