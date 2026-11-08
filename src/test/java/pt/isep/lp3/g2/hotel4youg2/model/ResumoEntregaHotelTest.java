package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResumoEntregaHotelTest {

    @Test
    public void testGetDecididaEmTextoPendente() {
        System.out.println("getDecididaEmTexto (pendente)");
        ResumoEntregaHotel instance = new ResumoEntregaHotel(1, "E-1", "Fornecedor Um", 2, 500,
                LocalDateTime.of(2026, 11, 5, 21, 24, 38), new BigDecimal("100.00"), EstadoEntrega.PENDENTE,
                null, null, null);
        String expResult = "";
        String result = instance.getDecididaEmTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDecididaEmTexto() {
        System.out.println("getDecididaEmTexto");
        ResumoEntregaHotel instance = new ResumoEntregaHotel(1, "E-1", "Fornecedor Um", 2, 500,
                LocalDateTime.of(2026, 11, 5, 21, 24, 38), new BigDecimal("100.00"), EstadoEntrega.RECUSADA,
                "Ana", LocalDateTime.of(2026, 11, 6, 10, 0, 5), "Faltam toalhas");
        String expResult = "06/11/2026 10:00:05 (Europe/Lisbon)";
        String result = instance.getDecididaEmTexto();
        assertEquals(expResult, result);
    }
}
