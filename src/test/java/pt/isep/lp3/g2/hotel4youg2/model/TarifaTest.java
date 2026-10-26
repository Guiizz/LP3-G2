package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TarifaTest {

    @Test
    void construtorGuardaOsDados() {
        Tarifa tarifa = new Tarifa(TipoQuarto.DUPLO, Vista.MONTANHA, new BigDecimal("90.00"));
        assertEquals(TipoQuarto.DUPLO, tarifa.getTipo());
        assertEquals(Vista.MONTANHA, tarifa.getVista());
        assertEquals(new BigDecimal("90.00"), tarifa.getValorNoite());
    }

    @Test
    void alterarOValorNoite() {
        Tarifa tarifa = new Tarifa(TipoQuarto.INDIVIDUAL, Vista.MAR, new BigDecimal("65.00"));
        tarifa.setValorNoite(new BigDecimal("70.00"));
        assertEquals(new BigDecimal("70.00"), tarifa.getValorNoite());
    }
}
