package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IHorarioReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class HorarioReservaBLLTest {

    private static class HorarioReservaDALFalso implements IHorarioReservaDAL {
        private HorarioReserva guardado = horarioStandard();
        private HorarioReserva atualizado;

        @Override
        public HorarioReserva obter() {
            return guardado;
        }

        @Override
        public boolean atualizar(HorarioReserva horario) {
            atualizado = horario;
            guardado = horario;
            return true;
        }
    }

    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO = new Utilizador(2, 2, "rececao", "h", "s", Perfil.RECECIONISTA, true);

    private HorarioReservaDALFalso repositorio;
    private HorarioReservaBLL instance;

    private static HorarioReserva horarioStandard() {
        return new HorarioReserva(LocalTime.of(15, 0), LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalTime.of(14, 0), new BigDecimal("20.00"), new BigDecimal("20.00"));
    }

    @BeforeEach
    public void setUp() {
        repositorio = new HorarioReservaDALFalso();
        instance = new HorarioReservaBLL(repositorio);
    }

    @AfterEach
    public void tearDown() {
        repositorio = null;
        instance = null;
    }

    @Test
    public void testObter() {
        System.out.println("obter");
        HorarioReserva expResult = horarioStandard();
        HorarioReserva result = instance.obter();
        assertEquals(expResult, result);
    }

    @Test
    public void testAlterar() {
        System.out.println("alterar");
        HorarioReserva expResult = new HorarioReserva(LocalTime.of(16, 0), LocalTime.of(11, 0),
                LocalTime.of(14, 0), LocalTime.of(13, 0), new BigDecimal("15.00"), new BigDecimal("25.00"));
        instance.alterar(GESTOR, expResult);
        HorarioReserva result = repositorio.atualizado;
        assertEquals(expResult, result);
    }

    @Test
    public void testAlterarRececionista() {
        System.out.println("alterar (rececionista)");
        try {
            instance.alterar(RECECAO, horarioStandard());
            fail("Devia ter recusado o rececionista");
        } catch (IllegalStateException expected) {
            assertEquals(null, repositorio.atualizado);
        }
    }

    @Test
    public void testAlterarTaxaNegativa() {
        System.out.println("alterar (taxa negativa)");
        HorarioReserva novo = new HorarioReserva(LocalTime.of(15, 0), LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalTime.of(14, 0), new BigDecimal("-1.00"), new BigDecimal("20.00"));
        try {
            instance.alterar(GESTOR, novo);
            fail("Devia ter recusado uma taxa negativa");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.atualizado);
        }
    }

    @Test
    public void testAlterarEarlyDepoisDoCheckin() {
        System.out.println("alterar (early check-in depois do check-in standard)");
        HorarioReserva novo = new HorarioReserva(LocalTime.of(15, 0), LocalTime.of(12, 0), LocalTime.of(16, 0),
                LocalTime.of(14, 0), new BigDecimal("20.00"), new BigDecimal("20.00"));
        String expResult = "O early check-in tem de ser antes da hora de check-in (15:00)";
        try {
            instance.alterar(GESTOR, novo);
            fail("Devia ter recusado");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAlterarLateAntesDoCheckout() {
        System.out.println("alterar (late check-out antes do check-out standard)");
        HorarioReserva novo = new HorarioReserva(LocalTime.of(15, 0), LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalTime.of(11, 0), new BigDecimal("20.00"), new BigDecimal("20.00"));
        String expResult = "O late check-out tem de ser depois da hora de check-out (12:00)";
        try {
            instance.alterar(GESTOR, novo);
            fail("Devia ter recusado");
        } catch (ValidacaoException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAlterarSemHoras() {
        System.out.println("alterar (sem horas)");
        HorarioReserva novo = new HorarioReserva(null, LocalTime.of(12, 0), LocalTime.of(13, 0),
                LocalTime.of(14, 0), new BigDecimal("20.00"), new BigDecimal("20.00"));
        try {
            instance.alterar(GESTOR, novo);
            fail("Devia ter recusado sem a hora de check-in");
        } catch (ValidacaoException expected) {
            assertEquals(null, repositorio.atualizado);
        }
    }

    @Test
    public void testHoraMinimaCheckin() {
        System.out.println("horaMinimaCheckin (com e sem early check-in)");
        HorarioReserva horario = instance.obter();
        String expResult = "13:00 / 15:00";
        String result = horario.horaMinimaCheckin(true) + " / " + horario.horaMinimaCheckin(false);
        assertEquals(expResult, result);
    }
}
