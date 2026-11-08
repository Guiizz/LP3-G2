package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ResumoEntregaTest {

    private ResumoEntrega resumo(LocalDateTime data) {
        return new ResumoEntrega("OC-1", 2, data, new BigDecimal("1012.91"), EstadoEntrega.PENDENTE);
    }

    @Test
    public void testGetCarregadaEmTexto() {
        System.out.println("getCarregadaEmTexto");
        String expResult = "05/11/2026 21:24:38 (Europe/Lisbon)";
        String result = resumo(LocalDateTime.of(2026, 11, 5, 21, 24, 38)).getCarregadaEmTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMotivoRecusa() {
        System.out.println("getMotivoRecusa");
        ResumoEntrega instance = new ResumoEntrega("OC-1", 2, LocalDateTime.of(2026, 11, 5, 21, 24, 38),
                new BigDecimal("1012.91"), EstadoEntrega.RECUSADA, "Faltam toalhas");
        String expResult = "Faltam toalhas";
        String result = instance.getMotivoRecusa();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMotivoRecusaSemMotivo() {
        System.out.println("getMotivoRecusa (sem motivo)");
        assertNull(resumo(LocalDateTime.of(2026, 11, 5, 21, 24, 38)).getMotivoRecusa());
    }

    @Test
    public void testGetCarregadaEmTextoMostraOsSegundosNoVerao() {
        System.out.println("getCarregadaEmTexto (verão)");
        String expResult = "05/07/2026 09:05:07 (Europe/Lisbon)";
        String result = resumo(LocalDateTime.of(2026, 7, 5, 9, 5, 7)).getCarregadaEmTexto();
        assertEquals(expResult, result);
    }
}