package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AcessoCartaoDALTest {

    private AcessoCartaoDAL instance;
    private String cartao;
    private int quarto;
    private final List<AcessoCartao> criados = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabela = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('AcessoCartao') IS NOT NULL "
                    + "THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(tabela.get(0) == 1, "Falta correr o 15_acessos_cartao.sql");
            List<Integer> coluna = ConexaoBD.select("SELECT CASE WHEN COL_LENGTH('AcessoCartao', 'funcionarioId') "
                    + "IS NOT NULL THEN 1 ELSE 0 END", new InteiroMapper());
            Assumptions.assumeTrue(coluna.get(0) == 1, "Falta correr o 17_picagem_funcionarios.sql");
            List<Integer> quartos = ConexaoBD.select("SELECT TOP 1 numero FROM Quarto", new InteiroMapper());
            Assumptions.assumeTrue(!quartos.isEmpty(), "Faltam quartos na BD");
            quarto = quartos.get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new AcessoCartaoDAL();
        cartao = "T" + Math.floorMod(System.nanoTime(), 100_000_000L);
    }

    @AfterEach
    public void tearDown() {
        for (AcessoCartao acesso : criados) {
            if (acesso.getId() > 0) {
                instance.apagar(acesso.getId());
            }
        }
        criados.clear();
    }

    private List<AcessoCartao> soOsDoTeste(List<AcessoCartao> lista) {
        List<AcessoCartao> resultado = new ArrayList<>();
        for (AcessoCartao acesso : lista) {
            if (criados.contains(acesso)) {
                resultado.add(acesso);
            }
        }
        return resultado;
    }

    private AcessoCartao registar(LocalHotel local, int numeroQuarto) {
        return registar(cartao, local, numeroQuarto);
    }

    private AcessoCartao registar(String numero, LocalHotel local, int numeroQuarto) {
        AcessoCartao acesso = new AcessoCartao(0, numero, local, numeroQuarto, 0, null);
        instance.registar(acesso);
        criados.add(acesso);
        return acesso;
    }

    @Test
    public void testRegistar() {
        System.out.println("registar");
        AcessoCartao acesso = registar(LocalHotel.QUARTO, quarto);
        boolean expResult = true;
        boolean result = acesso.getId() > 0;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarHoraAutomatica() {
        System.out.println("registar (hora posta pela BD)");
        AcessoCartao acesso = registar(LocalHotel.PISCINA, 0);
        boolean expResult = true;
        boolean result = Duration.between(acesso.getRegistadoEm(), LocalDateTime.now()).abs().toMinutes() < 5;
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorCartao() {
        System.out.println("listarPorCartao");
        AcessoCartao noQuarto = registar(LocalHotel.QUARTO, quarto);
        List<AcessoCartao> expResult = List.of(noQuarto);
        List<AcessoCartao> result = instance.listarPorCartao(cartao);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorCartaoMaisRecentePrimeiro() {
        System.out.println("listarPorCartao (mais recente primeiro)");
        AcessoCartao primeiro = registar(LocalHotel.RESTAURANTE, 0);
        AcessoCartao segundo = registar(LocalHotel.PISCINA, 0);
        List<AcessoCartao> expResult = List.of(segundo, primeiro);
        List<AcessoCartao> result = instance.listarPorCartao(cartao);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorQuartoMostraOsCartoesEOMestre() {
        System.out.println("listarPorQuarto (cartões que entraram, com o mestre, mais recente primeiro)");
        AcessoCartao hospede = registar(LocalHotel.QUARTO, quarto);
        AcessoCartao mestre = registar(AcessoCartao.CARTAO_MESTRE, LocalHotel.QUARTO, quarto);
        List<AcessoCartao> expResult = List.of(mestre, hospede);
        List<AcessoCartao> result = soOsDoTeste(instance.listarPorQuarto(quarto));
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorQuartoIgnoraOutrosLocais() {
        System.out.println("listarPorQuarto (a piscina não conta como quarto)");
        registar(LocalHotel.PISCINA, 0);
        List<AcessoCartao> expResult = new ArrayList<>();
        List<AcessoCartao> result = soOsDoTeste(instance.listarPorQuarto(quarto));
        assertEquals(expResult, result);
    }

    private Funcionario umFuncionario() {
        List<Integer> ids = ConexaoBD.select("SELECT TOP 1 pessoaId FROM Funcionario ORDER BY numeroFuncionario",
                new InteiroMapper());
        Assumptions.assumeTrue(!ids.isEmpty(), "Faltam funcionários na BD");
        return new FuncionarioDAL().procurarPorId(ids.get(0));
    }

    private AcessoCartao registarPicagem(Funcionario funcionario, LocalHotel local) {
        AcessoCartao picagem = new AcessoCartao(0, cartao, local, 0, 0, null);
        picagem.setFuncionario(funcionario.getId(), funcionario.getNome());
        instance.registar(picagem);
        criados.add(picagem);
        return picagem;
    }

    @Test
    public void testRegistarEmTodosOsLocais() {
        System.out.println("registar (a BD aceita todos os locais do hotel)");
        for (LocalHotel local : LocalHotel.values()) {
            registar(local, local == LocalHotel.QUARTO ? quarto : 0);
        }
        int expResult = LocalHotel.values().length;
        int result = instance.listarPorCartao(cartao).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarPicagemDeFuncionario() {
        System.out.println("registar (a picagem fica com o funcionário)");
        AcessoCartao picagem = registarPicagem(umFuncionario(), LocalHotel.BAR);
        List<AcessoCartao> expResult = List.of(picagem);
        List<AcessoCartao> result = instance.listarPorCartao(cartao);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorCartaoTrazONomeDoFuncionario() {
        System.out.println("listarPorCartao (traz o nome do funcionário)");
        Funcionario funcionario = umFuncionario();
        registarPicagem(funcionario, LocalHotel.COZINHA);
        String expResult = funcionario.getNome();
        String result = instance.listarPorCartao(cartao).get(0).getFuncionarioTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorCartaoSemFuncionario() {
        System.out.println("listarPorCartao (o cartão de um cliente não tem funcionário)");
        registar(LocalHotel.PISCINA, 0);
        String expResult = "-";
        String result = instance.listarPorCartao(cartao).get(0).getFuncionarioTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarCartoesDeClientes() {
        System.out.println("listarCartoesDeClientes (um cartão por reserva em curso)");
        int expResult = ConexaoBD.select("SELECT COUNT(*) FROM Reserva WHERE estado = 'EM_CURSO' "
                + "AND cartao IS NOT NULL", new InteiroMapper()).get(0);
        int result = instance.listarCartoesDeClientes().size();
        assertEquals(expResult, result);
    }

    @Test
    public void testApagar() {
        System.out.println("apagar");
        AcessoCartao acesso = registar(LocalHotel.RESTAURANTE, 0);
        instance.apagar(acesso.getId());
        List<AcessoCartao> expResult = new ArrayList<>();
        List<AcessoCartao> result = instance.listarPorCartao(cartao);
        assertEquals(expResult, result);
    }

    private int[] umaReserva() {
        List<Integer> reservas = ConexaoBD.select("SELECT TOP 1 id FROM Reserva ORDER BY id", new InteiroMapper());
        Assumptions.assumeTrue(!reservas.isEmpty(), "Faltam reservas na BD");
        int reservaId = reservas.get(0);
        int clienteId = ConexaoBD.select("SELECT clienteId FROM Reserva WHERE id = ?", new InteiroMapper(),
                reservaId).get(0);
        return new int[] {reservaId, clienteId};
    }

    private AcessoCartao registarDaReserva(int reservaId, LocalHotel local) {
        AcessoCartao acesso = new AcessoCartao(0, cartao, local, 0, reservaId, null);
        instance.registar(acesso);
        criados.add(acesso);
        return acesso;
    }

    @Test
    public void testListarPorCliente() {
        System.out.println("listarPorCliente (as picagens das reservas do cliente, mais recente primeiro)");
        int[] reserva = umaReserva();
        AcessoCartao restaurante = registarDaReserva(reserva[0], LocalHotel.RESTAURANTE);
        AcessoCartao piscina = registarDaReserva(reserva[0], LocalHotel.PISCINA);
        List<AcessoCartao> expResult = List.of(piscina, restaurante);
        List<AcessoCartao> result = soOsDoTeste(instance.listarPorCliente(reserva[1]));
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorClienteSemPicagensDeFuncionarios() {
        System.out.println("listarPorCliente (as picagens sem reserva não aparecem)");
        int[] reserva = umaReserva();
        registar(LocalHotel.PISCINA, 0);
        List<AcessoCartao> expResult = new ArrayList<>();
        List<AcessoCartao> result = soOsDoTeste(instance.listarPorCliente(reserva[1]));
        assertEquals(expResult, result);
    }
}
