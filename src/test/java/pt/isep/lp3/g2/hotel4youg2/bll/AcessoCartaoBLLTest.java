package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IAcessoCartaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class AcessoCartaoBLLTest {

    private static class AcessoCartaoDALFalsa implements IAcessoCartaoDAL {
        private final List<AcessoCartao> registados = new ArrayList<>();
        private final LocalDateTime agora = LocalDateTime.of(2026, 11, 8, 14, 30);
        private int consultas;

        @Override
        public void registar(AcessoCartao acesso) {
            acesso.setId(registados.size() + 1);
            acesso.setRegistadoEm(agora);
            registados.add(acesso);
        }

        @Override
        public List<AcessoCartao> listarPorQuarto(int quartoNumero) {
            consultas++;
            List<AcessoCartao> resultado = new ArrayList<>();
            for (AcessoCartao acesso : registados) {
                if (acesso.getQuartoNumero() == quartoNumero) {
                    resultado.add(acesso);
                }
            }
            return resultado;
        }

        @Override
        public List<AcessoCartao> listarPorCartao(String cartao) {
            consultas++;
            List<AcessoCartao> resultado = new ArrayList<>();
            for (AcessoCartao acesso : registados) {
                if (acesso.getCartao().equals(cartao)) {
                    resultado.add(acesso);
                }
            }
            return resultado;
        }

        @Override
        public boolean apagar(int id) {
            return false;
        }
    }

    private static class ReservaDALFalsa implements IReservaDAL {
        private Reserva emCurso;

        @Override
        public Reserva procurarEmCursoPorCartao(String cartao) {
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

    private static class ServicoReservaDALFalsa implements IServicoReservaDAL {
        private final List<ServicoReserva> extras = new ArrayList<>();

        @Override
        public boolean inserirSeAberta(ServicoReserva servicoReserva) {
            extras.add(servicoReserva);
            return true;
        }

        @Override
        public List<ServicoReserva> listarPorReserva(int reservaId) {
            List<ServicoReserva> resultado = new ArrayList<>();
            for (ServicoReserva extra : extras) {
                if (extra.getReservaId() == reservaId) {
                    resultado.add(extra);
                }
            }
            return resultado;
        }

        @Override
        public boolean apagarSeAberta(int id, int reservaId) {
            return false;
        }
    }

    private static class QuartoDALFalsa implements IQuartoDAL {

        @Override
        public List<Quarto> listarComTarifa() {
            return listarTodos();
        }

        @Override
        public List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida) {
            return new ArrayList<>();
        }

        @Override
        public List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida, boolean earlyCheckin,
                                              boolean lateCheckout) {
            return new ArrayList<>();
        }

        @Override
        public List<Quarto> listarTodos() {
            return List.of(new Quarto(201, 2, TipoQuarto.INDIVIDUAL, Vista.NENHUMA, 1),
                    new Quarto(202, 2, TipoQuarto.DUPLO, Vista.MAR, 2),
                    new Quarto(305, 3, TipoQuarto.FAMILIA, Vista.MONTANHA, 4));
        }

        @Override
        public boolean temReservasFuturas(int numero) {
            return false;
        }

        @Override
        public boolean atualizarAtivo(int numero, boolean ativo) {
            return false;
        }
    }

    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);
    private static final Servico JANTAR = new Servico(3, "Jantar", new BigDecimal("22.00"),
            new Categoria(1, "Refeição"), true);
    private static final Servico PISCINA = new Servico(11, "Piscina", new BigDecimal("5.00"),
            new Categoria(4, "Lazer"), true);
    private static final Servico GINASIO = new Servico(12, "Ginásio", new BigDecimal("5.00"),
            new Categoria(4, "Lazer"), true);

    private AcessoCartaoDALFalsa acessoDAL;
    private ReservaDALFalsa reservaDAL;
    private ServicoReservaDALFalsa servicoReservaDAL;
    private AcessoCartaoBLL instance;

    @BeforeEach
    public void setUp() {
        acessoDAL = new AcessoCartaoDALFalsa();
        reservaDAL = new ReservaDALFalsa();
        servicoReservaDAL = new ServicoReservaDALFalsa();
        LocalDate hoje = LocalDate.now();
        reservaDAL.emCurso = new Reserva(30, 15, 201, hoje.minusDays(1), hoje.plusDays(2), 1,
                EstadoReserva.EM_CURSO, new BigDecimal("300.00"), hoje.minusDays(1).atTime(15, 30), null);
        reservaDAL.emCurso.setCartao("C201");
        instance = new AcessoCartaoBLL(acessoDAL, reservaDAL, servicoReservaDAL, new QuartoDALFalsa());
    }

    @AfterEach
    public void tearDown() {
        acessoDAL.registados.clear();
        servicoReservaDAL.extras.clear();
    }

    private void juntarExtra(Servico servico) {
        servicoReservaDAL.inserirSeAberta(new ServicoReserva(0, reservaDAL.emCurso.getId(), servico, 1,
                servico.getPreco(), null));
    }

    private void esperarRecusa(String cartao, LocalHotel local, int quarto, String expResult) {
        try {
            instance.registarPassagem(RECECAO, cartao, local, quarto);
            fail("Devia ter recusado o acesso");
        } catch (ValidacaoException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(0, acessoDAL.registados.size());
        }
    }

    @Test
    public void testRegistarPassagemNoProprioQuarto() {
        System.out.println("registarPassagem (o cartão abre o seu quarto)");
        AcessoCartao expResult = new AcessoCartao(1, "C201", LocalHotel.QUARTO, 201, 30, acessoDAL.agora);
        AcessoCartao result = instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemFicaRegistada() {
        System.out.println("registarPassagem (fica registado o local e a hora)");
        AcessoCartao registado = instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        List<AcessoCartao> expResult = List.of(registado);
        List<AcessoCartao> result = instance.listarPorCartao("C201");
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemAceitaMinusculasEEspacos() {
        System.out.println("registarPassagem (minúsculas e espaços no cartão)");
        AcessoCartao expResult = new AcessoCartao(1, "C201", LocalHotel.QUARTO, 201, 30, acessoDAL.agora);
        AcessoCartao result = instance.registarPassagem(RECECAO, "  c201 ", LocalHotel.QUARTO, 201);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemNoutroQuarto() {
        System.out.println("registarPassagem (o cartão não abre outro quarto)");
        esperarRecusa("C201", LocalHotel.QUARTO, 202, "Acesso recusado: o cartão C201 só abre o quarto 201.");
    }

    @Test
    public void testRegistarPassagemRestauranteComRefeicao() {
        System.out.println("registarPassagem (restaurante com refeição na reserva)");
        juntarExtra(JANTAR);
        AcessoCartao expResult = new AcessoCartao(1, "C201", LocalHotel.RESTAURANTE, 0, 30, acessoDAL.agora);
        AcessoCartao result = instance.registarPassagem(RECECAO, "C201", LocalHotel.RESTAURANTE, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemRestauranteSemRefeicao() {
        System.out.println("registarPassagem (restaurante sem refeição na reserva)");
        juntarExtra(PISCINA);
        esperarRecusa("C201", LocalHotel.RESTAURANTE, 0,
                "Acesso recusado: a reserva do cartão C201 não inclui refeições no restaurante.");
    }

    @Test
    public void testRegistarPassagemPiscinaComExtra() {
        System.out.println("registarPassagem (piscina incluída na reserva)");
        juntarExtra(PISCINA);
        AcessoCartao expResult = new AcessoCartao(1, "C201", LocalHotel.PISCINA, 0, 30, acessoDAL.agora);
        AcessoCartao result = instance.registarPassagem(RECECAO, "C201", LocalHotel.PISCINA, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemPiscinaSoComGinasio() {
        System.out.println("registarPassagem (o ginásio não dá acesso à piscina)");
        juntarExtra(GINASIO);
        esperarRecusa("C201", LocalHotel.PISCINA, 0, "Acesso recusado: a reserva do cartão C201 não inclui a piscina.");
    }

    @Test
    public void testRegistarPassagemPiscinaSemExtras() {
        System.out.println("registarPassagem (piscina sem extras na reserva)");
        esperarRecusa("C201", LocalHotel.PISCINA, 0, "Acesso recusado: a reserva do cartão C201 não inclui a piscina.");
    }

    @Test
    public void testRegistarPassagemCartaoMestreQuarto() {
        System.out.println("registarPassagem (o cartão mestre abre qualquer quarto)");
        AcessoCartao expResult = new AcessoCartao(1, AcessoCartao.CARTAO_MESTRE, LocalHotel.QUARTO, 305, 0,
                acessoDAL.agora);
        AcessoCartao result = instance.registarPassagem(RECECAO, "C000", LocalHotel.QUARTO, 305);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemCartaoMestreRestaurante() {
        System.out.println("registarPassagem (o cartão mestre abre o restaurante)");
        AcessoCartao expResult = new AcessoCartao(1, AcessoCartao.CARTAO_MESTRE, LocalHotel.RESTAURANTE, 0, 0,
                acessoDAL.agora);
        AcessoCartao result = instance.registarPassagem(RECECAO, "C000", LocalHotel.RESTAURANTE, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemCartaoMestrePiscina() {
        System.out.println("registarPassagem (o cartão mestre abre a piscina)");
        AcessoCartao expResult = new AcessoCartao(1, AcessoCartao.CARTAO_MESTRE, LocalHotel.PISCINA, 0, 0,
                acessoDAL.agora);
        AcessoCartao result = instance.registarPassagem(RECECAO, "C000", LocalHotel.PISCINA, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemCartaoMestreQuartoQueNaoExiste() {
        System.out.println("registarPassagem (cartão mestre num quarto que não existe)");
        String expResult = "O quarto 999 não existe.";
        try {
            instance.registarPassagem(RECECAO, "C000", LocalHotel.QUARTO, 999);
            fail("Devia ter recusado um quarto que não existe");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(0, acessoDAL.registados.size());
        }
    }

    @Test
    public void testRegistarPassagemCartaoInvalido() {
        System.out.println("registarPassagem (cartão sem reserva em curso)");
        esperarRecusa("C999", LocalHotel.QUARTO, 201,
                "Acesso recusado: o cartão C999 não é válido ou é de uma reserva que já não está em curso.");
    }

    @Test
    public void testRegistarPassagemReservaTerminada() {
        System.out.println("registarPassagem (reserva já fez check-out)");
        reservaDAL.emCurso = null;
        esperarRecusa("C201", LocalHotel.QUARTO, 201,
                "Acesso recusado: o cartão C201 não é válido ou é de uma reserva que já não está em curso.");
    }

    @Test
    public void testRegistarPassagemSemCartao() {
        System.out.println("registarPassagem (sem cartão)");
        String expResult = "Escreva o número do cartão.";
        try {
            instance.registarPassagem(RECECAO, "  ", LocalHotel.QUARTO, 201);
            fail("Devia ter pedido o cartão");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testRegistarPassagemSemLocal() {
        System.out.println("registarPassagem (sem local)");
        String expResult = "Escolha o local: quarto, restaurante ou piscina.";
        try {
            instance.registarPassagem(RECECAO, "C201", null, 0);
            fail("Devia ter pedido o local");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testRegistarPassagemQuartoSemNumero() {
        System.out.println("registarPassagem (quarto sem número)");
        String expResult = "Indique o número do quarto.";
        try {
            instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 0);
            fail("Devia ter pedido o número do quarto");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testConsultarPorCartao() {
        System.out.println("consultarPorCartao (locais e horas do cartão)");
        juntarExtra(PISCINA);
        AcessoCartao quarto = instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        AcessoCartao piscina = instance.registarPassagem(RECECAO, "C201", LocalHotel.PISCINA, 0);
        List<AcessoCartao> expResult = List.of(quarto, piscina);
        List<AcessoCartao> result = instance.consultarPorCartao(GESTOR, " c201 ");
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarPorCartaoVazio() {
        System.out.println("consultarPorCartao (sem cartão)");
        String expResult = "Escreva o número do cartão.";
        try {
            instance.consultarPorCartao(GESTOR, "  ");
            fail("Devia ter pedido o cartão");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testConsultarPorCartaoRececionista() {
        System.out.println("consultarPorCartao (só o gestor)");
        int expResult = 0;
        try {
            instance.consultarPorCartao(RECECAO, "C201");
            fail("Devia ter recusado a rececionista");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, acessoDAL.consultas);
        }
    }

    @Test
    public void testConsultarPorCartaoSemSessao() {
        System.out.println("consultarPorCartao (sem sessão iniciada)");
        int expResult = 0;
        try {
            instance.consultarPorCartao(null, "C201");
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, acessoDAL.consultas);
        }
    }

    @Test
    public void testConsultarPorQuartoIncluiCartaoMestre() {
        System.out.println("consultarPorQuarto (cartões que entraram, com o mestre)");
        AcessoCartao hospede = instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        AcessoCartao mestre = instance.registarPassagem(RECECAO, "C000", LocalHotel.QUARTO, 201);
        List<AcessoCartao> expResult = List.of(hospede, mestre);
        List<AcessoCartao> result = instance.consultarPorQuarto(GESTOR, " 201 ");
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarPorQuartoSoDesseQuarto() {
        System.out.println("consultarPorQuarto (não mostra outros quartos)");
        instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        AcessoCartao mestre = instance.registarPassagem(RECECAO, "C000", LocalHotel.QUARTO, 305);
        List<AcessoCartao> expResult = List.of(mestre);
        List<AcessoCartao> result = instance.consultarPorQuarto(GESTOR, "305");
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarPorQuartoVazio() {
        System.out.println("consultarPorQuarto (sem quarto)");
        String expResult = "Indique o número do quarto.";
        try {
            instance.consultarPorQuarto(GESTOR, "");
            fail("Devia ter pedido o quarto");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testConsultarPorQuartoComLetras() {
        System.out.println("consultarPorQuarto (com letras)");
        String expResult = "O número do quarto tem de ser um número inteiro.";
        try {
            instance.consultarPorQuarto(GESTOR, "abc");
            fail("Devia ter recusado letras");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testConsultarPorQuartoRececionista() {
        System.out.println("consultarPorQuarto (só o gestor)");
        int expResult = 0;
        try {
            instance.consultarPorQuarto(RECECAO, "201");
            fail("Devia ter recusado a rececionista");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, acessoDAL.consultas);
        }
    }

    @Test
    public void testGetReservaTextoDoHospede() {
        System.out.println("getReservaTexto (hóspede)");
        AcessoCartao acesso = instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        String expResult = "30";
        String result = acesso.getReservaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetReservaTextoDoMestre() {
        System.out.println("getReservaTexto (cartão mestre não tem reserva)");
        AcessoCartao acesso = instance.registarPassagem(RECECAO, "C000", LocalHotel.QUARTO, 201);
        String expResult = "-";
        String result = acesso.getReservaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemSemSessao() {
        System.out.println("registarPassagem (sem sessão iniciada)");
        String expResult = "Entre na aplicação para registar a passagem de cartões.";
        try {
            instance.registarPassagem(null, "C201", LocalHotel.QUARTO, 201);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }
}
