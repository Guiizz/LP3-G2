package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Avaliacao;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AvaliacaoDALTest {

    private AvaliacaoDAL instance;

    @BeforeEach
    public void setUp() {
        try {
            List<Integer> tabela = ConexaoBD.select("SELECT CASE WHEN OBJECT_ID('AvaliacaoEstadia') IS NULL "
                    + "THEN 0 ELSE 1 END", new InteiroMapper());
            Assumptions.assumeTrue(tabela.get(0) == 1, "Falta correr o 29_avaliacoes_estadia.sql");
        } catch (DataAccessException e) {
            Assumptions.assumeTrue(false, "Sem ligação à base de dados");
        }
        instance = new AvaliacaoDAL();
    }

    @Test
    public void testListarMesSemAvaliacoes() {
        System.out.println("listar (mês sem avaliações)");
        int expResult = 0;
        int result = instance.listar(LocalDate.of(1990, 1, 1), LocalDate.of(1990, 1, 31)).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarTodas() {
        System.out.println("listar (a lista tem todas as avaliações guardadas)");
        int expResult = ConexaoBD.select("SELECT COUNT(*) FROM AvaliacaoEstadia", new InteiroMapper()).get(0);
        int result = instance.listar(LocalDate.of(1900, 1, 1), LocalDate.of(2999, 12, 31)).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorReservaQueNaoExiste() {
        System.out.println("procurarPorReserva (reserva sem avaliação)");
        Avaliacao expResult = null;
        Avaliacao result = instance.procurarPorReserva(-1);
        assertEquals(expResult, result);
    }
}
