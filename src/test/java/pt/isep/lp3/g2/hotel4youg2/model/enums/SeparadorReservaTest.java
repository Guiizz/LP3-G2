package pt.isep.lp3.g2.hotel4youg2.model.enums;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeparadorReservaTest {

    private List<EstadoReserva> estadosDe(SeparadorReserva separador) {
        List<EstadoReserva> estados = new ArrayList<>();
        for (EstadoReserva estado : EstadoReserva.values()) {
            if (separador.inclui(estado)) {
                estados.add(estado);
            }
        }
        return estados;
    }

    @Test
    public void testIncluiPorFazer() {
        System.out.println("inclui (por fazer)");
        List<EstadoReserva> expResult = new ArrayList<>();
        expResult.add(EstadoReserva.CONFIRMADA);
        List<EstadoReserva> result = estadosDe(SeparadorReserva.POR_FAZER);
        assertEquals(expResult, result);
    }

    @Test
    public void testIncluiEmCurso() {
        System.out.println("inclui (em curso)");
        List<EstadoReserva> expResult = new ArrayList<>();
        expResult.add(EstadoReserva.EM_CURSO);
        List<EstadoReserva> result = estadosDe(SeparadorReserva.EM_CURSO);
        assertEquals(expResult, result);
    }

    @Test
    public void testIncluiHistorico() {
        System.out.println("inclui (histórico)");
        List<EstadoReserva> expResult = new ArrayList<>();
        expResult.add(EstadoReserva.CONCLUIDA);
        expResult.add(EstadoReserva.CANCELADA);
        List<EstadoReserva> result = estadosDe(SeparadorReserva.HISTORICO);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDescricao() {
        System.out.println("getDescricao");
        String expResult = "Histórico";
        String result = SeparadorReserva.HISTORICO.getDescricao();
        assertEquals(expResult, result);
    }
}
