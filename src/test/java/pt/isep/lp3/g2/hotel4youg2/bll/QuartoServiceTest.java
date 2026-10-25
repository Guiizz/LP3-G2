package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.QuartoRepositorio;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuartoServiceTest {

    private static class RepositorioFalso implements QuartoRepositorio {

        @Override
        public List<Quarto> listarComTarifa() {
            return List.of(
                    quarto(201, 2, TipoQuarto.INDIVIDUAL, "50.00"),
                    quarto(208, 2, TipoQuarto.DUPLO, "80.00"),
                    quarto(301, 3, TipoQuarto.DUPLO, "80.00"),
                    quarto(308, 3, TipoQuarto.FAMILIA, "120.00"));
        }

        @Override
        public List<Quarto> listarDisponiveis(LocalDate entrada, LocalDate saida) {
            return List.of(
                    quartoDisponivel(201, TipoQuarto.INDIVIDUAL, Vista.NENHUMA, 1),
                    quartoDisponivel(209, TipoQuarto.DUPLO, Vista.MONTANHA, 2),
                    quartoDisponivel(305, TipoQuarto.DUPLO, Vista.MAR, 2),
                    quartoDisponivel(309, TipoQuarto.FAMILIA, Vista.MAR, 4));
        }
    }

    private static final LocalDate HOJE = LocalDate.now();

    private static Quarto quarto(int numero, int piso, TipoQuarto tipo, String preco) {
        Quarto q = new Quarto(numero, piso, tipo, Vista.NENHUMA, 1);
        q.setTarifa(new Tarifa(tipo, Vista.NENHUMA, new BigDecimal(preco)));
        return q;
    }

    private static Quarto quartoDisponivel(int numero, TipoQuarto tipo, Vista vista, int capacidade) {
        Quarto q = new Quarto(numero, numero / 100, tipo, vista, capacidade);
        q.setTarifa(new Tarifa(tipo, vista, new BigDecimal("80.00")));
        return q;
    }

    private final QuartoService service = new QuartoService(new RepositorioFalso());

    private String erroDaPesquisa(LocalDate entrada, LocalDate saida, int hospedes) {
        try {
            service.pesquisarDisponiveis(entrada, saida, hospedes, null, null);
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }


    @Test
    void semFiltroMostraTodos() {
        assertEquals(4, service.listar(null, null).size());
    }

    @Test
    void filtraPorTipo() {
        List<Quarto> duplos = service.listar(TipoQuarto.DUPLO, null);
        assertEquals(2, duplos.size());
        assertEquals(208, duplos.get(0).getNumero());
        assertEquals(301, duplos.get(1).getNumero());
    }

    @Test
    void filtraPorPiso() {
        assertEquals(2, service.listar(null, 3).size());
    }

    @Test
    void filtraPorTipoEPiso() {
        List<Quarto> r = service.listar(TipoQuarto.DUPLO, 2);
        assertEquals(1, r.size());
        assertEquals(208, r.get(0).getNumero());
    }

    @Test
    void cadaQuartoMostraOPrecoDaSuaTarifa() {
        Quarto familia = service.listar(TipoQuarto.FAMILIA, null).get(0);
        assertEquals(new BigDecimal("120.00"), familia.getValorNoite());
    }


    @Test
    void entradaNoPassadoFalha() {
        assertEquals("A data de entrada não pode ser no passado.",
                erroDaPesquisa(HOJE.minusDays(1), HOJE.plusDays(2), 1));
    }

    @Test
    void saidaIgualAEntradaFalha() {
        assertEquals("A data de saída tem de ser posterior à de entrada.",
                erroDaPesquisa(HOJE.plusDays(5), HOJE.plusDays(5), 1));
    }

    @Test
    void saidaAntesDaEntradaFalha() {
        assertEquals("A data de saída tem de ser posterior à de entrada.",
                erroDaPesquisa(HOJE.plusDays(5), HOJE.plusDays(3), 1));
    }

    @Test
    void semDatasFalha() {
        assertEquals("Indique as datas de entrada e de saída.", erroDaPesquisa(null, null, 1));
    }

    @Test
    void zeroHospedesFalha() {
        assertEquals("O número de hóspedes tem de ser pelo menos 1.",
                erroDaPesquisa(HOJE, HOJE.plusDays(1), 0));
    }

    @Test
    void entradaHojeEPermitida() {
        assertNull(erroDaPesquisa(HOJE, HOJE.plusDays(1), 1));
    }

    @Test
    void soMostraQuartosOndeCabemOsHospedes() {
        List<Quarto> r = service.pesquisarDisponiveis(HOJE, HOJE.plusDays(2), 2, null, null);
        assertEquals(3, r.size());
        assertEquals(209, r.get(0).getNumero());
        assertEquals(305, r.get(1).getNumero());
        assertEquals(309, r.get(2).getNumero());
    }

    @Test
    void filtraPorTipoEVista() {
        List<Quarto> r = service.pesquisarDisponiveis(HOJE, HOJE.plusDays(2), 2, TipoQuarto.DUPLO, Vista.MAR);
        assertEquals(1, r.size());
        assertEquals(305, r.get(0).getNumero());
    }

    @Test
    void semQuartosQueCumpramDevolveListaVazia() {
        List<Quarto> r = service.pesquisarDisponiveis(HOJE, HOJE.plusDays(2), 3, TipoQuarto.DUPLO, null);
        assertTrue(r.isEmpty());
    }
}