package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IAcessoCartaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFuncionarioDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.CartaoAtivo;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.LocalPicagem;
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
        public List<AcessoCartao> listar(String cartao, int quartoNumero, LocalHotel local, LocalDate dia) {
            consultas++;
            List<AcessoCartao> resultado = new ArrayList<>();
            for (AcessoCartao acesso : registados) {
                boolean cartaoCerto = cartao.isEmpty() || acesso.getCartao().equals(cartao);
                boolean quartoCerto = quartoNumero == 0 || acesso.getQuartoNumero() == quartoNumero;
                boolean localCerto = local == null || acesso.getLocal() == local;
                boolean diaCerto = dia == null || acesso.getRegistadoEm().toLocalDate().equals(dia);
                if (cartaoCerto && quartoCerto && localCerto && diaCerto) {
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

        @Override
        public List<CartaoAtivo> listarCartoesUsados() {
            consultas++;
            return List.of(new CartaoAtivo("C201", "Rui Costa"), new CartaoAtivo("F12", "Ana Silva"));
        }

        private ReservaDALFalsa reservas;

        @Override
        public List<AcessoCartao> listarPorCliente(int clienteId) {
            List<Integer> reservasDoCliente = new ArrayList<>();
            for (Reserva reserva : reservas.listarPorCliente(clienteId)) {
                reservasDoCliente.add(reserva.getId());
            }
            List<AcessoCartao> resultado = new ArrayList<>();
            for (AcessoCartao acesso : registados) {
                if (reservasDoCliente.contains(acesso.getReservaId())) {
                    resultado.add(acesso);
                }
            }
            return resultado;
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
        public boolean inserirComServicosSeLivre(Reserva reserva, List<ServicoReserva> servicos) {
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
            List<Reserva> resultado = new ArrayList<>();
            if (outraEmCurso != null && outraEmCurso.getClienteId() == clienteId) {
                resultado.add(outraEmCurso);
            }
            if (emCurso != null && emCurso.getClienteId() == clienteId) {
                resultado.add(emCurso);
            }
            return resultado;
        }

        private Reserva outraEmCurso;

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

        @Override
        public boolean inserir(Quarto quarto) {
            return false;
        }

        @Override
        public boolean foiUsado(int numero) {
            return false;
        }

        @Override
        public boolean apagarSeNuncaUsado(int numero) {
            return false;
        }
    }

    private static class FuncionarioDALFalsa implements IFuncionarioDAL {
        private final Funcionario ana = new Funcionario(7, "Ana Silva", null, "123456789", null, null, null, null,
                12, LocalDate.of(2026, 10, 1));
        private boolean contaAtiva = true;

        @Override
        public boolean temContaAtiva(int pessoaId) {
            return contaAtiva && pessoaId == ana.getId();
        }

        @Override
        public List<Funcionario> listarComContaAtiva() {
            List<Funcionario> ativos = new ArrayList<>();
            if (contaAtiva) {
                ativos.add(ana);
            }
            return ativos;
        }

        @Override
        public Funcionario procurarPorNumero(int numeroFuncionario) {
            return numeroFuncionario == ana.getNumeroFuncionario() ? ana : null;
        }

        @Override
        public Funcionario procurarPorId(int pessoaId) {
            return pessoaId == ana.getId() ? ana : null;
        }

        @Override
        public boolean existeNif(String nif) {
            return false;
        }

        @Override
        public boolean existeNomeUtilizador(String nomeUtilizador) {
            return false;
        }

        @Override
        public void criarConta(Funcionario funcionario, Utilizador utilizador) {
        }
    }

    private static final String SO_PISOS_0_E_1 =
            "Acesso recusado: o cartão F12 é de um funcionário e só abre os locais dos pisos 0 e 1.";
    private static final String SO_QUARTO_RESTAURANTE_E_PISCINA =
            "Acesso recusado: o cartão C201 é de um cliente e só abre o seu quarto, o restaurante e a piscina.";

    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);
    private static final Utilizador SEM_FUNCIONARIO = new Utilizador(3, 99, "outra", "h", "s", Perfil.RECECIONISTA,
            true);
    private static final Servico JANTAR = new Servico(3, "Jantar", new BigDecimal("22.00"),
            new Categoria(1, "Refeição"), true);
    private static final Servico PISCINA = new Servico(11, "Piscina", new BigDecimal("5.00"),
            new Categoria(4, "Lazer"), true);
    private static final Servico GINASIO = new Servico(12, "Ginásio", new BigDecimal("5.00"),
            new Categoria(4, "Lazer"), true);

    private AcessoCartaoDALFalsa acessoDAL;
    private ReservaDALFalsa reservaDAL;
    private ServicoReservaDALFalsa servicoReservaDAL;
    private FuncionarioDALFalsa funcionarioDAL;
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
        acessoDAL.reservas = reservaDAL;
        funcionarioDAL = new FuncionarioDALFalsa();
        instance = new AcessoCartaoBLL(acessoDAL, reservaDAL, servicoReservaDAL, new QuartoDALFalsa(),
                funcionarioDAL);
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
        List<AcessoCartao> result = acessoDAL.listarPorCartao("C201");
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
        String expResult = "Escolha o local.";
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
    public void testConsultarSemFiltrosMostraTodos() {
        System.out.println("consultar (sem filtros mostra todos os acessos)");
        AcessoCartao hospede = instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        AcessoCartao mestre = instance.registarPassagem(RECECAO, "C000", LocalHotel.QUARTO, 305);
        AcessoCartao funcionario = instance.registarPassagem(RECECAO, "F12", LocalHotel.BAR, 0);
        List<AcessoCartao> expResult = List.of(hospede, mestre, funcionario);
        List<AcessoCartao> result = instance.consultar(GESTOR, "", "", null, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarPorCartao() {
        System.out.println("consultar (por cartão)");
        juntarExtra(PISCINA);
        AcessoCartao quarto = instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        AcessoCartao piscina = instance.registarPassagem(RECECAO, "C201", LocalHotel.PISCINA, 0);
        instance.registarPassagem(RECECAO, "C000", LocalHotel.QUARTO, 201);
        List<AcessoCartao> expResult = List.of(quarto, piscina);
        List<AcessoCartao> result = instance.consultar(GESTOR, " c201 ", "", null, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarPorCartaoDoFuncionario() {
        System.out.println("consultar (o gestor vê as picagens de um funcionário)");
        AcessoCartao bar = instance.registarPassagem(RECECAO, "F12", LocalHotel.BAR, 0);
        AcessoCartao cozinha = instance.registarPassagem(RECECAO, "F12", LocalHotel.COZINHA, 0);
        List<AcessoCartao> expResult = List.of(bar, cozinha);
        List<AcessoCartao> result = instance.consultar(GESTOR, " f12 ", null, null, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarPorQuartoIncluiCartaoMestre() {
        System.out.println("consultar (por quarto, com o cartão mestre)");
        AcessoCartao hospede = instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        AcessoCartao mestre = instance.registarPassagem(RECECAO, "C000", LocalHotel.QUARTO, 201);
        List<AcessoCartao> expResult = List.of(hospede, mestre);
        List<AcessoCartao> result = instance.consultar(GESTOR, "", " 201 ", null, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarPorQuartoSoDesseQuarto() {
        System.out.println("consultar (por quarto, não mostra outros quartos)");
        instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        AcessoCartao mestre = instance.registarPassagem(RECECAO, "C000", LocalHotel.QUARTO, 305);
        List<AcessoCartao> expResult = List.of(mestre);
        List<AcessoCartao> result = instance.consultar(GESTOR, "", "305", null, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarPorLocal() {
        System.out.println("consultar (por local: todos os acessos da piscina)");
        juntarExtra(PISCINA);
        instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        AcessoCartao hospede = instance.registarPassagem(RECECAO, "C201", LocalHotel.PISCINA, 0);
        AcessoCartao mestre = instance.registarPassagem(RECECAO, "C000", LocalHotel.PISCINA, 0);
        List<AcessoCartao> expResult = List.of(hospede, mestre);
        List<AcessoCartao> result = instance.consultar(GESTOR, "", "", LocalHotel.PISCINA, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarPorDia() {
        System.out.println("consultar (por dia)");
        AcessoCartao primeiro = instance.registarPassagem(RECECAO, "C000", LocalHotel.BAR, 0);
        AcessoCartao segundo = instance.registarPassagem(RECECAO, "C000", LocalHotel.BAR, 0);
        segundo.setRegistadoEm(LocalDateTime.of(2026, 11, 9, 10, 0));
        List<AcessoCartao> expResult = List.of(primeiro);
        List<AcessoCartao> result = instance.consultar(GESTOR, "", "", null, LocalDate.of(2026, 11, 8));
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarFiltrosJuntos() {
        System.out.println("consultar (cartão e local ao mesmo tempo)");
        AcessoCartao mestreNoQuarto = instance.registarPassagem(RECECAO, "C000", LocalHotel.QUARTO, 201);
        instance.registarPassagem(RECECAO, "C000", LocalHotel.PISCINA, 0);
        instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        List<AcessoCartao> expResult = List.of(mestreNoQuarto);
        List<AcessoCartao> result = instance.consultar(GESTOR, "C000", "", LocalHotel.QUARTO, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarSemResultados() {
        System.out.println("consultar (filtros sem acessos)");
        instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        List<AcessoCartao> expResult = new ArrayList<>();
        List<AcessoCartao> result = instance.consultar(GESTOR, "", "", LocalHotel.GARAGEM, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testConsultarQuartoComLetras() {
        System.out.println("consultar (quarto com letras)");
        String expResult = "O quarto tem de ser um número inteiro, por exemplo 201.";
        try {
            instance.consultar(GESTOR, "", "abc", null, null);
            fail("Devia ter recusado letras");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testConsultarQuartoZero() {
        System.out.println("consultar (quarto 0)");
        String expResult = "O quarto tem de ser um número inteiro, por exemplo 201.";
        try {
            instance.consultar(GESTOR, "", "0", null, null);
            fail("Devia ter recusado o quarto 0");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testConsultarRececionista() {
        System.out.println("consultar (só o gestor)");
        int expResult = 0;
        try {
            instance.consultar(RECECAO, "", "", null, null);
            fail("Devia ter recusado a rececionista");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, acessoDAL.consultas);
        }
    }

    @Test
    public void testConsultarSemSessao() {
        System.out.println("consultar (sem sessão iniciada)");
        int expResult = 0;
        try {
            instance.consultar(null, "", "", null, null);
            fail("Devia ter exigido a sessão");
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

    private AcessoCartao picagemDaAna(LocalHotel local) {
        AcessoCartao picagem = new AcessoCartao(1, "F12", local, 0, 0, acessoDAL.agora);
        picagem.setFuncionario(7, "Ana Silva");
        return picagem;
    }

    @Test
    public void testRegistarPassagemFuncionarioNoBar() {
        System.out.println("registarPassagem (o cartão do funcionário abre o bar, no piso 0)");
        AcessoCartao expResult = picagemDaAna(LocalHotel.BAR);
        AcessoCartao result = instance.registarPassagem(RECECAO, "F12", LocalHotel.BAR, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemFuncionarioNasSalasDeConferencia() {
        System.out.println("registarPassagem (o cartão do funcionário abre as salas de conferência, no piso 0)");
        AcessoCartao expResult = picagemDaAna(LocalHotel.SALA_CONFERENCIAS);
        AcessoCartao result = instance.registarPassagem(RECECAO, "F12", LocalHotel.SALA_CONFERENCIAS, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemFuncionarioNoRestaurante() {
        System.out.println("registarPassagem (o cartão do funcionário abre o restaurante, no piso 1)");
        AcessoCartao expResult = picagemDaAna(LocalHotel.RESTAURANTE);
        AcessoCartao result = instance.registarPassagem(RECECAO, "F12", LocalHotel.RESTAURANTE, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemFuncionarioNaPiscina() {
        System.out.println("registarPassagem (o cartão do funcionário abre a piscina, no piso 1)");
        AcessoCartao expResult = picagemDaAna(LocalHotel.PISCINA);
        AcessoCartao result = instance.registarPassagem(RECECAO, "F12", LocalHotel.PISCINA, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemFuncionarioNaCozinha() {
        System.out.println("registarPassagem (o cartão do funcionário abre a cozinha, no piso 1)");
        AcessoCartao expResult = picagemDaAna(LocalHotel.COZINHA);
        AcessoCartao result = instance.registarPassagem(RECECAO, "F12", LocalHotel.COZINHA, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemFuncionarioNaGaragem() {
        System.out.println("registarPassagem (o cartão do funcionário não abre a garagem, no piso -1)");
        esperarRecusa("F12", LocalHotel.GARAGEM, 0, SO_PISOS_0_E_1);
    }

    @Test
    public void testRegistarPassagemFuncionarioNoArmazem() {
        System.out.println("registarPassagem (o cartão do funcionário não abre o armazém, no piso -1)");
        esperarRecusa("F12", LocalHotel.ARMAZEM, 0, SO_PISOS_0_E_1);
    }

    @Test
    public void testRegistarPassagemFuncionarioNoQuarto() {
        System.out.println("registarPassagem (o cartão do funcionário não abre os quartos)");
        esperarRecusa("F12", LocalHotel.QUARTO, 201, SO_PISOS_0_E_1);
    }

    @Test
    public void testRegistarPassagemFuncionarioNoQuartoSemNumero() {
        System.out.println("registarPassagem (o funcionário é recusado no quarto mesmo sem o número)");
        esperarRecusa("F12", LocalHotel.QUARTO, 0, SO_PISOS_0_E_1);
    }

    @Test
    public void testRegistarPassagemFuncionarioAceitaMinusculasEEspacos() {
        System.out.println("registarPassagem (minúsculas e espaços no cartão do funcionário)");
        AcessoCartao expResult = picagemDaAna(LocalHotel.BAR);
        AcessoCartao result = instance.registarPassagem(RECECAO, "  f12 ", LocalHotel.BAR, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemFuncionarioQueNaoExiste() {
        System.out.println("registarPassagem (cartão de um funcionário que não existe)");
        esperarRecusa("F99", LocalHotel.BAR, 0, "Acesso recusado: o cartão F99 não é de nenhum funcionário.");
    }

    @Test
    public void testRegistarPassagemFuncionarioComContaDesativada() {
        System.out.println("registarPassagem (o cartão de uma conta desativada não abre nada)");
        funcionarioDAL.contaAtiva = false;
        esperarRecusa("F12", LocalHotel.BAR, 0,
                "Acesso recusado: o cartão F12 é de um funcionário com a conta desativada.");
    }

    @Test
    public void testRegistarPassagemFuncionarioComContaDesativadaNaGaragem() {
        System.out.println("registarPassagem (com a conta desativada, o motivo é a conta e não o piso)");
        funcionarioDAL.contaAtiva = false;
        esperarRecusa("F12", LocalHotel.GARAGEM, 0,
                "Acesso recusado: o cartão F12 é de um funcionário com a conta desativada.");
    }

    @Test
    public void testRegistarPassagemCartaoComZeroAEsquerda() {
        System.out.println("registarPassagem (F012 não é o cartão do funcionário 12)");
        esperarRecusa("F012", LocalHotel.BAR, 0,
                "Acesso recusado: o cartão F012 não é válido ou é de uma reserva que já não está em curso.");
    }

    @Test
    public void testRegistarPassagemFuncionarioFicaComONome() {
        System.out.println("registarPassagem (a picagem fica com o nome do funcionário)");
        AcessoCartao picagem = instance.registarPassagem(RECECAO, "F12", LocalHotel.COZINHA, 0);
        String expResult = "Ana Silva";
        String result = picagem.getFuncionarioTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFuncionarioTextoDoHospede() {
        System.out.println("getFuncionarioTexto (o cartão de um cliente não tem funcionário)");
        AcessoCartao acesso = instance.registarPassagem(RECECAO, "C201", LocalHotel.QUARTO, 201);
        String expResult = "-";
        String result = acesso.getFuncionarioTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetReservaTextoDoFuncionario() {
        System.out.println("getReservaTexto (o cartão de um funcionário não tem reserva)");
        AcessoCartao picagem = instance.registarPassagem(RECECAO, "F12", LocalHotel.BAR, 0);
        String expResult = "-";
        String result = picagem.getReservaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemCartaoMestreTodosOsLocais() {
        System.out.println("registarPassagem (o cartão mestre abre todos os locais)");
        for (LocalHotel local : LocalHotel.values()) {
            instance.registarPassagem(RECECAO, "C000", local, local == LocalHotel.QUARTO ? 201 : 0);
        }
        int expResult = LocalHotel.values().length;
        int result = acessoDAL.registados.size();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemCartaoMestreGaragem() {
        System.out.println("registarPassagem (o cartão mestre abre a garagem)");
        AcessoCartao expResult = new AcessoCartao(1, AcessoCartao.CARTAO_MESTRE, LocalHotel.GARAGEM, 0, 0,
                acessoDAL.agora);
        AcessoCartao result = instance.registarPassagem(RECECAO, "C000", LocalHotel.GARAGEM, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPassagemClienteNoBar() {
        System.out.println("registarPassagem (o cartão de um cliente não abre o bar)");
        esperarRecusa("C201", LocalHotel.BAR, 0, SO_QUARTO_RESTAURANTE_E_PISCINA);
    }

    @Test
    public void testRegistarPassagemClienteNaGaragem() {
        System.out.println("registarPassagem (o cartão de um cliente não abre a garagem)");
        esperarRecusa("C201", LocalHotel.GARAGEM, 0, SO_QUARTO_RESTAURANTE_E_PISCINA);
    }

    @Test
    public void testRegistarPassagemClienteNaCozinhaComRefeicao() {
        System.out.println("registarPassagem (a refeição na reserva não abre a cozinha)");
        juntarExtra(JANTAR);
        esperarRecusa("C201", LocalHotel.COZINHA, 0, SO_QUARTO_RESTAURANTE_E_PISCINA);
    }

    @Test
    public void testListarCartoesUsados() {
        System.out.println("listarCartoesUsados (cartões com acessos, com o titular)");
        List<CartaoAtivo> expResult = List.of(new CartaoAtivo("C201", "Rui Costa"), new CartaoAtivo("F12", "Ana Silva"));
        List<CartaoAtivo> result = instance.listarCartoesUsados(GESTOR);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarCartoesUsadosRececionista() {
        System.out.println("listarCartoesUsados (só o gestor)");
        int expResult = 0;
        try {
            instance.listarCartoesUsados(RECECAO);
            fail("Devia ter recusado a rececionista");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, acessoDAL.consultas);
        }
    }

    @Test
    public void testCartaoDoUtilizadorFuncionario() {
        System.out.println("cartaoDoUtilizador (funcionário usa o seu cartão)");
        String expResult = "F12";
        String result = instance.cartaoDoUtilizador(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testCartaoDoUtilizadorGestor() {
        System.out.println("cartaoDoUtilizador (o gestor usa o cartão mestre)");
        String expResult = "C000";
        String result = instance.cartaoDoUtilizador(GESTOR);
        assertEquals(expResult, result);
    }

    @Test
    public void testCartaoDoUtilizadorSemSessao() {
        System.out.println("cartaoDoUtilizador (sem sessão iniciada)");
        String expResult = "Entre na aplicação para usar o seu cartão.";
        try {
            instance.cartaoDoUtilizador(null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testCartaoDoUtilizadorSemFuncionario() {
        System.out.println("cartaoDoUtilizador (conta sem funcionário)");
        String expResult = "A sua conta não tem um cartão de funcionário.";
        try {
            instance.cartaoDoUtilizador(SEM_FUNCIONARIO);
            fail("Devia ter avisado que não há cartão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testLocaisDoCartaoFuncionario() {
        System.out.println("locaisDoCartao (o funcionário só vê os locais dos pisos 0 e 1)");
        List<LocalHotel> expResult = List.of(LocalHotel.BAR, LocalHotel.SALA_CONFERENCIAS, LocalHotel.RESTAURANTE,
                LocalHotel.PISCINA, LocalHotel.COZINHA);
        List<LocalHotel> result = instance.locaisDoCartao(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testLocaisDoCartaoGestor() {
        System.out.println("locaisDoCartao (o cartão mestre abre todos os locais)");
        List<LocalHotel> expResult = List.of(LocalHotel.values());
        List<LocalHotel> result = instance.locaisDoCartao(GESTOR);
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarFuncionario() {
        System.out.println("picar (o funcionário pica com o seu cartão)");
        AcessoCartao expResult = new AcessoCartao(1, "F12", LocalHotel.BAR, 0, 0, acessoDAL.agora);
        expResult.setFuncionario(7, "Ana Silva");
        AcessoCartao result = instance.picar(RECECAO, LocalHotel.BAR, 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarGestor() {
        System.out.println("picar (o gestor pica com o cartão mestre)");
        AcessoCartao expResult = new AcessoCartao(1, "C000", LocalHotel.QUARTO, 305, 0, acessoDAL.agora);
        AcessoCartao result = instance.picar(GESTOR, LocalHotel.QUARTO, 305);
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarFuncionarioForaDosPisos() {
        System.out.println("picar (o funcionário não abre a garagem)");
        String expResult = SO_PISOS_0_E_1;
        try {
            instance.picar(RECECAO, LocalHotel.GARAGEM, 0);
            fail("Devia ter recusado o acesso");
        } catch (ValidacaoException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testPicarSemSessao() {
        System.out.println("picar (sem sessão iniciada)");
        int expResult = 0;
        try {
            instance.picar(null, LocalHotel.BAR, 0);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, acessoDAL.registados.size());
        }
    }

    @Test
    public void testListarAsMinhasPassagens() {
        System.out.println("listarAsMinhasPassagens (só as passagens de quem tem a sessão)");
        AcessoCartao minha = instance.picar(RECECAO, LocalHotel.BAR, 0);
        instance.picar(GESTOR, LocalHotel.QUARTO, 305);
        List<AcessoCartao> expResult = List.of(minha);
        List<AcessoCartao> result = instance.listarAsMinhasPassagens(RECECAO);
        assertEquals(expResult, result);
    }

    private static final ContaCliente RUI = new ContaCliente(5, 15, "123456789", "Rui Costa", "h", "s", false);
    private static final ContaCliente SEM_RESERVAS = new ContaCliente(6, 16, "987654321", "Eva Lima", "h", "s", false);

    private AcessoCartao doRui(LocalHotel local, int quarto) {
        return new AcessoCartao(1, "C201", local, quarto, 30, acessoDAL.agora);
    }

    private void esperarRecusaDoCliente(ContaCliente conta, LocalPicagem local, String expResult) {
        try {
            instance.picarComoCliente(conta, local);
            fail("Devia ter recusado o acesso");
        } catch (ValidacaoException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(0, acessoDAL.registados.size());
        }
    }

    private Reserva segundaReservaDoRui() {
        LocalDate hoje = LocalDate.now();
        Reserva segunda = new Reserva(31, 15, 202, hoje, hoje.plusDays(3), 2, EstadoReserva.EM_CURSO,
                new BigDecimal("450.00"), hoje.atTime(15, 0), null);
        segunda.setCartao("C202");
        reservaDAL.outraEmCurso = segunda;
        return segunda;
    }

    @Test
    public void testReservasComCartao() {
        System.out.println("reservasComCartao (a reserva em curso do cliente)");
        List<Reserva> expResult = List.of(reservaDAL.emCurso);
        List<Reserva> result = instance.reservasComCartao(RUI);
        assertEquals(expResult, result);
    }

    @Test
    public void testReservasComCartaoDuasEmCurso() {
        System.out.println("reservasComCartao (duas reservas em curso, pela data de entrada)");
        Reserva segunda = segundaReservaDoRui();
        List<Reserva> expResult = List.of(reservaDAL.emCurso, segunda);
        List<Reserva> result = instance.reservasComCartao(RUI);
        assertEquals(expResult, result);
    }

    @Test
    public void testReservasComCartaoSemReservaEmCurso() {
        System.out.println("reservasComCartao (sem reserva em curso não há cartão)");
        List<Reserva> expResult = new ArrayList<>();
        List<Reserva> result = instance.reservasComCartao(SEM_RESERVAS);
        assertEquals(expResult, result);
    }

    @Test
    public void testReservasComCartaoDepoisDoCheckout() {
        System.out.println("reservasComCartao (depois do check-out o cartão deixa de existir)");
        reservaDAL.emCurso.setEstado(EstadoReserva.CONCLUIDA);
        List<Reserva> expResult = new ArrayList<>();
        List<Reserva> result = instance.reservasComCartao(RUI);
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarComoClienteComOCartaoEscolhido() {
        System.out.println("picarComoCliente (com duas reservas, pica com o cartão escolhido)");
        Reserva segunda = segundaReservaDoRui();
        AcessoCartao expResult = new AcessoCartao(1, "C202", LocalHotel.QUARTO, 202, 31, acessoDAL.agora);
        AcessoCartao result = instance.picarComoCliente(RUI, segunda, LocalPicagem.quarto(202));
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarComoClienteComOPrimeiroCartao() {
        System.out.println("picarComoCliente (com duas reservas, o C201 continua a abrir o 201)");
        segundaReservaDoRui();
        AcessoCartao expResult = doRui(LocalHotel.QUARTO, 201);
        AcessoCartao result = instance.picarComoCliente(RUI, reservaDAL.emCurso, LocalPicagem.quarto(201));
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarComoClienteCartaoEscolhidoNoutroQuarto() {
        System.out.println("picarComoCliente (o cartão escolhido usa as regras da sua reserva)");
        Reserva segunda = segundaReservaDoRui();
        String expResult = "Acesso recusado: o cartão C202 só abre o quarto 202.";
        try {
            instance.picarComoCliente(RUI, segunda, LocalPicagem.quarto(201));
            fail("Devia ter recusado o acesso");
        } catch (ValidacaoException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(0, acessoDAL.registados.size());
        }
    }

    @Test
    public void testPicarComoClienteCartaoEscolhidoSemPiscina() {
        System.out.println("picarComoCliente (a piscina da primeira reserva não serve ao segundo cartão)");
        juntarExtra(PISCINA);
        Reserva segunda = segundaReservaDoRui();
        String expResult = "Acesso recusado: a reserva do cartão C202 não inclui a piscina.";
        try {
            instance.picarComoCliente(RUI, segunda, new LocalPicagem(LocalHotel.PISCINA, 0));
            fail("Devia ter recusado o acesso");
        } catch (ValidacaoException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testPicarComoClienteDuasReservasSemEscolher() {
        System.out.println("picarComoCliente (com duas reservas tem de escolher o cartão)");
        segundaReservaDoRui();
        String expResult = "Tem mais do que uma reserva em curso: escolha o cartão.";
        try {
            instance.picarComoCliente(RUI, LocalPicagem.quarto(201));
            fail("Devia ter pedido o cartão");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testPicarComoClienteCartaoDeOutraPessoa() {
        System.out.println("picarComoCliente (não pode usar o cartão de outra pessoa)");
        LocalDate hoje = LocalDate.now();
        Reserva deOutro = new Reserva(40, 99, 305, hoje, hoje.plusDays(1), 1, EstadoReserva.EM_CURSO,
                new BigDecimal("120.00"), hoje.atTime(15, 0), null);
        deOutro.setCartao("C305");
        String expResult = "Acesso recusado: o cartão C305 não é de uma reserva sua em curso.";
        try {
            instance.picarComoCliente(RUI, deOutro, LocalPicagem.quarto(305));
            fail("Devia ter recusado o cartão");
        } catch (ValidacaoException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(0, acessoDAL.registados.size());
        }
    }

    @Test
    public void testPicarComoClienteNoProprioQuarto() {
        System.out.println("picarComoCliente (o C201 entra no quarto 201, com a reserva)");
        AcessoCartao expResult = doRui(LocalHotel.QUARTO, 201);
        AcessoCartao result = instance.picarComoCliente(RUI, LocalPicagem.quarto(201));
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarComoClienteNoutroQuarto() {
        System.out.println("picarComoCliente (o C201 é recusado no quarto 202)");
        esperarRecusaDoCliente(RUI, LocalPicagem.quarto(202), "Acesso recusado: o cartão C201 só abre o quarto 201.");
    }

    @Test
    public void testPicarComoClienteNoQuarto305() {
        System.out.println("picarComoCliente (o C201 é recusado no quarto 305)");
        esperarRecusaDoCliente(RUI, LocalPicagem.quarto(305), "Acesso recusado: o cartão C201 só abre o quarto 201.");
    }

    @Test
    public void testPicarComoClienteQuartoQueNaoExiste() {
        System.out.println("picarComoCliente (quarto que não existe)");
        String expResult = "O quarto 999 não existe.";
        try {
            instance.picarComoCliente(RUI, LocalPicagem.quarto(999));
            fail("Devia ter recusado o quarto");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testPicarComoClientePiscinaNaReserva() {
        System.out.println("picarComoCliente (piscina incluída na reserva)");
        juntarExtra(PISCINA);
        AcessoCartao expResult = doRui(LocalHotel.PISCINA, 0);
        AcessoCartao result = instance.picarComoCliente(RUI, new LocalPicagem(LocalHotel.PISCINA, 0));
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarComoClientePiscinaForaDaReserva() {
        System.out.println("picarComoCliente (sem a piscina na reserva)");
        esperarRecusaDoCliente(RUI, new LocalPicagem(LocalHotel.PISCINA, 0),
                "Acesso recusado: a reserva do cartão C201 não inclui a piscina.");
    }

    @Test
    public void testPicarComoClienteRestauranteComRefeicao() {
        System.out.println("picarComoCliente (restaurante com refeição na reserva)");
        juntarExtra(JANTAR);
        AcessoCartao expResult = doRui(LocalHotel.RESTAURANTE, 0);
        AcessoCartao result = instance.picarComoCliente(RUI, new LocalPicagem(LocalHotel.RESTAURANTE, 0));
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarComoClienteRestauranteSemRefeicao() {
        System.out.println("picarComoCliente (restaurante sem refeições na reserva)");
        esperarRecusaDoCliente(RUI, new LocalPicagem(LocalHotel.RESTAURANTE, 0),
                "Acesso recusado: a reserva do cartão C201 não inclui refeições no restaurante.");
    }

    @Test
    public void testPicarComoClienteNoBar() {
        System.out.println("picarComoCliente (o cliente não entra no bar)");
        esperarRecusaDoCliente(RUI, new LocalPicagem(LocalHotel.BAR, 0), SO_QUARTO_RESTAURANTE_E_PISCINA);
    }

    @Test
    public void testPicarComoClienteSemReservaEmCurso() {
        System.out.println("picarComoCliente (sem reserva em curso não há cartão)");
        esperarRecusaDoCliente(SEM_RESERVAS, LocalPicagem.quarto(201), AcessoCartaoBLL.SEM_RESERVA_EM_CURSO);
    }

    @Test
    public void testPicarComoClienteDepoisDoCheckout() {
        System.out.println("picarComoCliente (depois do check-out)");
        reservaDAL.emCurso.setEstado(EstadoReserva.CONCLUIDA);
        esperarRecusaDoCliente(RUI, LocalPicagem.quarto(201), AcessoCartaoBLL.SEM_RESERVA_EM_CURSO);
    }

    @Test
    public void testPicarComoClienteSemLocal() {
        System.out.println("picarComoCliente (sem local)");
        String expResult = "Escolha o local.";
        try {
            instance.picarComoCliente(RUI, null);
            fail("Devia ter pedido o local");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testPicarComoClienteSemConta() {
        System.out.println("picarComoCliente (sem conta)");
        String expResult = "Entre na sua conta para usar o cartão.";
        try {
            instance.picarComoCliente(null, LocalPicagem.quarto(201));
            fail("Devia ter exigido a conta");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testListarPicagensDoCliente() {
        System.out.println("listarPicagensDoCliente (só as picagens das reservas do cliente)");
        juntarExtra(PISCINA);
        AcessoCartao quarto = instance.picarComoCliente(RUI, LocalPicagem.quarto(201));
        AcessoCartao piscina = instance.picarComoCliente(RUI, new LocalPicagem(LocalHotel.PISCINA, 0));
        instance.picar(RECECAO, LocalHotel.BAR, 0);
        List<AcessoCartao> expResult = List.of(quarto, piscina);
        List<AcessoCartao> result = instance.listarPicagensDoCliente(RUI);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPicagensDeOutroCliente() {
        System.out.println("listarPicagensDoCliente (outro cliente não vê as picagens do Rui)");
        instance.picarComoCliente(RUI, LocalPicagem.quarto(201));
        List<AcessoCartao> expResult = new ArrayList<>();
        List<AcessoCartao> result = instance.listarPicagensDoCliente(SEM_RESERVAS);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarLocais() {
        System.out.println("listarLocais (os quartos aparecem um a um e depois os outros locais)");
        List<LocalPicagem> expResult = List.of(LocalPicagem.quarto(201), LocalPicagem.quarto(202),
                LocalPicagem.quarto(305), new LocalPicagem(LocalHotel.BAR, 0),
                new LocalPicagem(LocalHotel.SALA_CONFERENCIAS, 0), new LocalPicagem(LocalHotel.RESTAURANTE, 0),
                new LocalPicagem(LocalHotel.PISCINA, 0), new LocalPicagem(LocalHotel.COZINHA, 0),
                new LocalPicagem(LocalHotel.GARAGEM, 0), new LocalPicagem(LocalHotel.ARMAZEM, 0));
        List<LocalPicagem> result = instance.listarLocais();
        assertEquals(expResult, result);
    }
}
