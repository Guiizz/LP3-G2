package bll;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.bll.QuartoService;
import pt.isep.lp3.g2.hotel4youg2.dal.QuartoRepositorio;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    }

    private static Quarto quarto(int numero, int piso, TipoQuarto tipo, String preco) {
        Quarto q = new Quarto(numero, piso, tipo, Vista.NENHUMA, 1);
        q.setTarifa(new Tarifa(tipo, Vista.NENHUMA, new BigDecimal(preco)));
        return q;
    }

    private final QuartoService service = new QuartoService(new RepositorioFalso());

    @Test
    void semFiltoMostraTodos() {
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
    void filtraPorTipoPiso() {
        assertEquals(2, service.listar(null, 3).size());
    }

    @Test
    void filtroPorTipoEPiso() {
        List<Quarto> r = service.listar(TipoQuarto.DUPLO, 2);
        assertEquals(1, r.size());
        assertEquals(208, r.get(0).getNumero());
    }

    @Test
    void cadaQuartoMostraOPrecoDaSuaTarifa() {
        Quarto familia = service.listar(TipoQuarto.FAMILIA, null).get(0);
        assertEquals(new BigDecimal("120.00"), familia.getValorNoite());
    }
}
