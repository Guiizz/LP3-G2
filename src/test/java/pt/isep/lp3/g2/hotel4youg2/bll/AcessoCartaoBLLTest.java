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
        private final List<CartaoAtivo> cartoesDeClientes = new ArrayList<>();

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
        public List<CartaoAtivo> listarCartoesDeClientes() {
            return cartoesDeClientes;
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
            if (emCurso != null && emCurso.getClienteId() == clienteId) {
                resultado.add(emCurso);
            }
            return resultado;
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
    public void testConsultarPorCartaoDoFuncionario() {
        System.out.println("consultarPorCartao (o gestor vê as picagens de um funcionário)");
        AcessoCartao bar = instance.registarPassagem(RECECAO, "F12", LocalHotel.BAR, 0);
        AcessoCartao cozinha = instance.registarPassagem(RECECAO, "F12", LocalHotel.COZINHA, 0);
        List<AcessoCartao> expResult = List.of(bar, cozinha);
        List<AcessoCartao> result = instance.consultarPorCartao(GESTOR, " f12 ");
        assertEquals(expResult, result);
    }

    @Test
    public void testListarFuncionariosComCartao() {
        System.out.println("listarFuncionariosComCartao");
        List<Funcionario> expResult = List.of(funcionarioDAL.ana);
        List<Funcionario> result = instance.listarFuncionariosComCartao(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarFuncionariosComCartaoSemContasAtivas() {
        System.out.println("listarFuncionariosComCartao (as contas desativadas não aparecem)");
        funcionarioDAL.contaAtiva = false;
        List<Funcionario> expResult = new ArrayList<>();
        List<Funcionario> result = instance.listarFuncionariosComCartao(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarFuncionariosComCartaoSemSessao() {
        System.out.println("listarFuncionariosComCartao (sem sessão iniciada)");
        String expResult = "Entre na aplicação para ver os cartões dos funcionários.";
        try {
            instance.listarFuncionariosComCartao(null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testListarCartoes() {
        System.out.println("listarCartoes (funcionários ativos e clientes com reserva em curso)");
        acessoDAL.cartoesDeClientes.add(new CartaoAtivo("C201", "Rui Costa (quarto 201)"));
        List<CartaoAtivo> expResult = List.of(new CartaoAtivo("F12", "Ana Silva"),
                new CartaoAtivo("C201", "Rui Costa (quarto 201)"));
        List<CartaoAtivo> result = instance.listarCartoes(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarCartoesSemContasAtivas() {
        System.out.println("listarCartoes (só pessoas ativas)");
        funcionarioDAL.contaAtiva = false;
        List<CartaoAtivo> expResult = new ArrayList<>();
        List<CartaoAtivo> result = instance.listarCartoes(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarCartoesSemSessao() {
        System.out.println("listarCartoes (sem sessão iniciada)");
        String expResult = "Entre na aplicação para ver os cartões dos funcionários.";
        try {
            instance.listarCartoes(null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    private static final Utilizador GESTORA_ANA = new Utilizador(3, 7, "ana", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO_SEM_FUNCIONARIO =
            new Utilizador(4, 99, "outra", "h", "s", Perfil.RECECIONISTA, true);
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

    private void esperarRecusaDoFuncionario(LocalPicagem local, String expResult) {
        try {
            instance.picar(RECECAO, local);
            fail("Devia ter recusado o acesso");
        } catch (ValidacaoException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(0, acessoDAL.registados.size());
        }
    }

    @Test
    public void testCartaoDeFuncionario() {
        System.out.println("cartaoDe (o funcionário tem o seu cartão F)");
        String expResult = "F12";
        String result = instance.cartaoDe(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testCartaoDeGestor() {
        System.out.println("cartaoDe (o gestor só tem o cartão mestre)");
        String expResult = AcessoCartao.CARTAO_MESTRE;
        String result = instance.cartaoDe(GESTOR);
        assertEquals(expResult, result);
    }

    @Test
    public void testCartaoDeSemSessao() {
        System.out.println("cartaoDe (sem sessão iniciada)");
        String expResult = "Entre na aplicação para usar o seu cartão.";
        try {
            instance.cartaoDe(null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testCartaoDeSemFuncionario() {
        System.out.println("cartaoDe (conta sem funcionário)");
        String expResult = "A sua conta não tem um cartão de funcionário.";
        try {
            instance.cartaoDe(RECECAO_SEM_FUNCIONARIO);
            fail("Devia ter avisado que não há cartão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testPicarFuncionarioComOProprioCartao() {
        System.out.println("picar (o funcionário pica com o seu cartão)");
        AcessoCartao expResult = picagemDaAna(LocalHotel.BAR);
        AcessoCartao result = instance.picar(RECECAO, new LocalPicagem(LocalHotel.BAR, 0));
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarFuncionarioNaCozinha() {
        System.out.println("picar (o funcionário entra na cozinha, no piso 1)");
        AcessoCartao expResult = picagemDaAna(LocalHotel.COZINHA);
        AcessoCartao result = instance.picar(RECECAO, new LocalPicagem(LocalHotel.COZINHA, 0));
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarFuncionarioNoQuartoDeUmCliente() {
        System.out.println("picar (o funcionário não entra nos quartos)");
        esperarRecusaDoFuncionario(LocalPicagem.quarto(201), SO_PISOS_0_E_1);
    }

    @Test
    public void testPicarFuncionarioNaGaragem() {
        System.out.println("picar (o funcionário não entra na garagem)");
        esperarRecusaDoFuncionario(new LocalPicagem(LocalHotel.GARAGEM, 0), SO_PISOS_0_E_1);
    }

    @Test
    public void testPicarFuncionarioComContaDesativada() {
        System.out.println("picar (conta desativada)");
        funcionarioDAL.contaAtiva = false;
        esperarRecusaDoFuncionario(new LocalPicagem(LocalHotel.BAR, 0),
                "Acesso recusado: o cartão F12 é de um funcionário com a conta desativada.");
    }

    @Test
    public void testPicarGestorComOCartaoMestre() {
        System.out.println("picar (o gestor pica com o cartão mestre e fica o seu nome)");
        AcessoCartao expResult = new AcessoCartao(1, AcessoCartao.CARTAO_MESTRE, LocalHotel.QUARTO, 305, 0,
                acessoDAL.agora);
        expResult.setFuncionario(7, "Ana Silva");
        AcessoCartao result = instance.picar(GESTORA_ANA, LocalPicagem.quarto(305));
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarGestorEmTodosOsLocais() {
        System.out.println("picar (o cartão mestre abre todos os locais do hotel)");
        for (LocalPicagem local : instance.listarLocais()) {
            instance.picar(GESTORA_ANA, local);
        }
        int expResult = instance.listarLocais().size();
        int result = acessoDAL.registados.size();
        assertEquals(expResult, result);
    }

    @Test
    public void testPicarGestorQuartoQueNaoExiste() {
        System.out.println("picar (quarto que não existe)");
        String expResult = "O quarto 999 não existe.";
        try {
            instance.picar(GESTOR, LocalPicagem.quarto(999));
            fail("Devia ter recusado o quarto");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testPicarSemLocal() {
        System.out.println("picar (sem local)");
        String expResult = "Escolha o local.";
        try {
            instance.picar(RECECAO, null);
            fail("Devia ter pedido o local");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testPicarSemSessao() {
        System.out.println("picar (sem sessão iniciada)");
        String expResult = "Entre na aplicação para usar o seu cartão.";
        try {
            instance.picar(null, new LocalPicagem(LocalHotel.BAR, 0));
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testReservaComCartao() {
        System.out.println("reservaComCartao (a reserva em curso do cliente)");
        Reserva expResult = reservaDAL.emCurso;
        Reserva result = instance.reservaComCartao(RUI);
        assertEquals(expResult, result);
    }

    @Test
    public void testReservaComCartaoSemReservaEmCurso() {
        System.out.println("reservaComCartao (sem reserva em curso não há cartão)");
        Reserva expResult = null;
        Reserva result = instance.reservaComCartao(SEM_RESERVAS);
        assertEquals(expResult, result);
    }

    @Test
    public void testReservaComCartaoDepoisDoCheckout() {
        System.out.println("reservaComCartao (depois do check-out o cartão deixa de existir)");
        reservaDAL.emCurso.setEstado(EstadoReserva.CONCLUIDA);
        Reserva expResult = null;
        Reserva result = instance.reservaComCartao(RUI);
        assertEquals(expResult, result);
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
        instance.picar(RECECAO, new LocalPicagem(LocalHotel.BAR, 0));
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
