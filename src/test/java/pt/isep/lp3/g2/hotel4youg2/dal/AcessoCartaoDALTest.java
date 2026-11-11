package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
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

    @Test
    public void testApagar() {
        System.out.println("apagar");
        AcessoCartao acesso = registar(LocalHotel.RESTAURANTE, 0);
        instance.apagar(acesso.getId());
        List<AcessoCartao> expResult = new ArrayList<>();
        List<AcessoCartao> result = instance.listarPorCartao(cartao);
        assertEquals(expResult, result);
    }
}
