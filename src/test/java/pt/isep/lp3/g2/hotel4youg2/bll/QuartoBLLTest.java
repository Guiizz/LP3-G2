package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe QuartoBLL.
 */
public class QuartoBLLTest {

    /** Repositório de teste: guarda os quartos em memória em vez de ir à base de dados. */
    private static class QuartoDALFalsa implements IQuartoDAL {
        private List<Quarto> quartos = new ArrayList<>();
        private List<Quarto> disponiveis = new ArrayList<>();
        private int quartoComReservasFuturas = -1;
        private int alteracoes = 0;

        @Override
        public List<Quarto> listarComTarifa() {
            return quartos;
        }

        @Override
        public List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida) {
            return disponiveis;
        }

        @Override
        public List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida, boolean earlyCheckin,
                                              boolean lateCheckout) {
            return disponiveis;
        }

        @Override
        public List<Quarto> listarTodos() {
            return quartos;
        }

        @Override
        public boolean temReservasFuturas(int numero) {
            return numero == quartoComReservasFuturas;
        }

        @Override
        public boolean atualizarAtivo(int numero, boolean ativo) {
            for (Quarto quarto : quartos) {
                if (quarto.getNumero() == numero) {
                    quarto.setAtivo(ativo);
                    alteracoes++;
                    return true;
                }
            }
            return false;
        }
    }

    private static final LocalDate HOJE = LocalDate.now();

    private QuartoDALFalsa dal;
    private QuartoBLL instance;

    @BeforeEach
    public void setUp() {
        dal = new QuartoDALFalsa();
        dal.quartos.add(criarQuarto(201, 2, TipoQuarto.INDIVIDUAL, Vista.NENHUMA, 1, "50.00"));
        dal.quartos.add(criarQuarto(208, 2, TipoQuarto.DUPLO, Vista.NENHUMA, 2, "80.00"));
        dal.quartos.add(criarQuarto(301, 3, TipoQuarto.DUPLO, Vista.NENHUMA, 2, "80.00"));
        dal.quartos.add(criarQuarto(308, 3, TipoQuarto.FAMILIA, Vista.NENHUMA, 4, "120.00"));
        dal.disponiveis.add(criarQuarto(201, 2, TipoQuarto.INDIVIDUAL, Vista.NENHUMA, 1, "50.00"));
        dal.disponiveis.add(criarQuarto(209, 2, TipoQuarto.DUPLO, Vista.MONTANHA, 2, "80.00"));
        dal.disponiveis.add(criarQuarto(305, 3, TipoQuarto.DUPLO, Vista.MAR, 2, "100.00"));
        dal.disponiveis.add(criarQuarto(309, 3, TipoQuarto.FAMILIA, Vista.MAR, 4, "150.00"));
        instance = new QuartoBLL(dal);
    }

    @AfterEach
    public void tearDown() {
        dal = null;
        instance = null;
    }

    // cria um quarto já com a tarifa do seu tipo e da sua vista
    private Quarto criarQuarto(int numero, int piso, TipoQuarto tipo, Vista vista, int capacidade, String preco) {
        Quarto quarto = new Quarto(numero, piso, tipo, vista, capacidade);
        quarto.setTarifa(new Tarifa(tipo, vista, new BigDecimal(preco)));
        return quarto;
    }

    // devolve a mensagem de erro ao pesquisar quartos livres, ou null se a pesquisa foi feita
    private String mensagemAoPesquisar(LocalDate entrada, LocalDate saida, int hospedes) {
        try {
            instance.pesquisarDisponiveis(entrada, saida, hospedes, null, null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    // devolve a mensagem de erro ao listar todos os quartos, ou null se a lista foi devolvida
    private String mensagemAoListarTodos(Perfil perfil) {
        try {
            instance.listarTodos(perfil);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    // devolve a mensagem de erro ao desativar o quarto, ou null se o quarto foi desativado
    private String mensagemAoDesativar(Perfil perfil, int numero) {
        try {
            instance.desativar(perfil, numero);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    // devolve a mensagem de erro ao reativar o quarto, ou null se o quarto foi reativado
    private String mensagemAoReativar(Perfil perfil, int numero) {
        try {
            instance.reativar(perfil, numero);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testListarSemFiltro() {
        System.out.println("listar (sem filtro)");
        List<Quarto> expResult = dal.quartos;
        List<Quarto> result = instance.listar(null, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorTipo() {
        System.out.println("listar (por tipo)");
        List<Quarto> expResult = new ArrayList<>();
        expResult.add(criarQuarto(208, 2, TipoQuarto.DUPLO, Vista.NENHUMA, 2, "80.00"));
        expResult.add(criarQuarto(301, 3, TipoQuarto.DUPLO, Vista.NENHUMA, 2, "80.00"));
        List<Quarto> result = instance.listar(TipoQuarto.DUPLO, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorPiso() {
        System.out.println("listar (por piso)");
        List<Quarto> expResult = new ArrayList<>();
        expResult.add(criarQuarto(301, 3, TipoQuarto.DUPLO, Vista.NENHUMA, 2, "80.00"));
        expResult.add(criarQuarto(308, 3, TipoQuarto.FAMILIA, Vista.NENHUMA, 4, "120.00"));
        List<Quarto> result = instance.listar(null, 3);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarPorTipoEPiso() {
        System.out.println("listar (por tipo e piso)");
        List<Quarto> expResult = new ArrayList<>();
        expResult.add(criarQuarto(208, 2, TipoQuarto.DUPLO, Vista.NENHUMA, 2, "80.00"));
        List<Quarto> result = instance.listar(TipoQuarto.DUPLO, 2);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarSemQuartosQueCumpram() {
        System.out.println("listar (nenhum quarto cumpre o filtro)");
        List<Quarto> expResult = new ArrayList<>();
        List<Quarto> result = instance.listar(TipoQuarto.INDIVIDUAL, 3);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarMostraOPrecoDaTarifa() {
        System.out.println("listar (cada quarto mostra o preço da sua tarifa)");
        BigDecimal expResult = new BigDecimal("120.00");
        BigDecimal result = instance.listar(TipoQuarto.FAMILIA, null).get(0).getValorNoite();
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarDisponiveisSoOndeCabemOsHospedes() {
        System.out.println("pesquisarDisponiveis (só onde cabem os hóspedes)");
        List<Quarto> expResult = new ArrayList<>();
        expResult.add(criarQuarto(209, 2, TipoQuarto.DUPLO, Vista.MONTANHA, 2, "80.00"));
        expResult.add(criarQuarto(305, 3, TipoQuarto.DUPLO, Vista.MAR, 2, "100.00"));
        expResult.add(criarQuarto(309, 3, TipoQuarto.FAMILIA, Vista.MAR, 4, "150.00"));
        List<Quarto> result = instance.pesquisarDisponiveis(HOJE, HOJE.plusDays(2), 2, null, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarDisponiveisPorTipoEVista() {
        System.out.println("pesquisarDisponiveis (por tipo e vista)");
        List<Quarto> expResult = new ArrayList<>();
        expResult.add(criarQuarto(305, 3, TipoQuarto.DUPLO, Vista.MAR, 2, "100.00"));
        List<Quarto> result = instance.pesquisarDisponiveis(HOJE, HOJE.plusDays(2), 2, TipoQuarto.DUPLO, Vista.MAR);
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarDisponiveisSemQuartosQueCumpram() {
        System.out.println("pesquisarDisponiveis (nenhum quarto cumpre)");
        List<Quarto> expResult = new ArrayList<>();
        List<Quarto> result = instance.pesquisarDisponiveis(HOJE, HOJE.plusDays(2), 3, TipoQuarto.DUPLO, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarDisponiveisEntradaHoje() {
        System.out.println("pesquisarDisponiveis (entrada hoje é permitida)");
        String expResult = null;
        String result = mensagemAoPesquisar(HOJE, HOJE.plusDays(1), 1);
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarDisponiveisEntradaNoPassado() {
        System.out.println("pesquisarDisponiveis (entrada no passado)");
        String expResult = "A data de entrada não pode ser no passado.";
        String result = mensagemAoPesquisar(HOJE.minusDays(1), HOJE.plusDays(2), 1);
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarDisponiveisSaidaIgualAEntrada() {
        System.out.println("pesquisarDisponiveis (saída igual à entrada)");
        String expResult = "A data de saída tem de ser posterior à de entrada.";
        String result = mensagemAoPesquisar(HOJE.plusDays(5), HOJE.plusDays(5), 1);
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarDisponiveisSaidaAntesDaEntrada() {
        System.out.println("pesquisarDisponiveis (saída antes da entrada)");
        String expResult = "A data de saída tem de ser posterior à de entrada.";
        String result = mensagemAoPesquisar(HOJE.plusDays(5), HOJE.plusDays(3), 1);
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarDisponiveisSemDatas() {
        System.out.println("pesquisarDisponiveis (sem datas)");
        String expResult = "Indique as datas de entrada e de saída.";
        String result = mensagemAoPesquisar(null, null, 1);
        assertEquals(expResult, result);
    }

    @Test
    public void testPesquisarDisponiveisZeroHospedes() {
        System.out.println("pesquisarDisponiveis (zero hóspedes)");
        String expResult = "O número de hóspedes tem de ser pelo menos 1.";
        String result = mensagemAoPesquisar(HOJE, HOJE.plusDays(1), 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarTodos() {
        System.out.println("listarTodos");
        List<Quarto> expResult = dal.quartos;
        List<Quarto> result = instance.listarTodos(Perfil.GESTOR);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarTodosComoRececionista() {
        System.out.println("listarTodos (rececionista não pode)");
        String expResult = "Só o gestor pode gerir quartos";
        String result = mensagemAoListarTodos(Perfil.RECECIONISTA);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarTodosSemPerfil() {
        System.out.println("listarTodos (sem perfil)");
        String expResult = "Só o gestor pode gerir quartos";
        String result = mensagemAoListarTodos(null);
        assertEquals(expResult, result);
    }

    @Test
    public void testDesativar() {
        System.out.println("desativar");
        Quarto expResult = criarQuarto(201, 2, TipoQuarto.INDIVIDUAL, Vista.NENHUMA, 1, "50.00");
        expResult.setAtivo(false);
        instance.desativar(Perfil.GESTOR, 201);
        Quarto result = dal.quartos.get(0);
        assertEquals(expResult, result);
        assertEquals(1, dal.alteracoes);
    }

    @Test
    public void testDesativarComoRececionista() {
        System.out.println("desativar (rececionista não pode)");
        String expResult = "Só o gestor pode gerir quartos";
        String result = mensagemAoDesativar(Perfil.RECECIONISTA, 201);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testDesativarComReservasFuturas() {
        System.out.println("desativar (quarto com reservas futuras)");
        dal.quartoComReservasFuturas = 201;
        String expResult = "Não é possível desativar o quarto 201: tem reservas futuras";
        String result = mensagemAoDesativar(Perfil.GESTOR, 201);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testDesativarQuartoInexistente() {
        System.out.println("desativar (o quarto não existe)");
        String expResult = "O quarto 999 não existe";
        String result = mensagemAoDesativar(Perfil.GESTOR, 999);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testReativar() {
        System.out.println("reativar");
        dal.quartos.get(0).setAtivo(false);
        Quarto expResult = criarQuarto(201, 2, TipoQuarto.INDIVIDUAL, Vista.NENHUMA, 1, "50.00");
        instance.reativar(Perfil.GESTOR, 201);
        Quarto result = dal.quartos.get(0);
        assertEquals(expResult, result);
        assertEquals(1, dal.alteracoes);
    }

    @Test
    public void testReativarComoRececionista() {
        System.out.println("reativar (rececionista não pode)");
        String expResult = "Só o gestor pode gerir quartos";
        String result = mensagemAoReativar(Perfil.RECECIONISTA, 201);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }

    @Test
    public void testReativarQuartoInexistente() {
        System.out.println("reativar (o quarto não existe)");
        String expResult = "O quarto 999 não existe";
        String result = mensagemAoReativar(Perfil.GESTOR, 999);
        assertEquals(expResult, result);
        assertEquals(0, dal.alteracoes);
    }
}
