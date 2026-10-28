package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuartoTest {

    @Test
    void construtorGuardaOsDados() {
        Quarto quarto = new Quarto(305, 3, TipoQuarto.DUPLO, Vista.MAR, 2);
        assertEquals(305, quarto.getNumero());
        assertEquals(3, quarto.getPiso());
        assertEquals(TipoQuarto.DUPLO, quarto.getTipo());
        assertEquals(Vista.MAR, quarto.getVista());
        assertEquals(2, quarto.getCapacidade());
    }

    @Test
    void semTarifaNaoTemPreco() {
        Quarto quarto = new Quarto(201, 2, TipoQuarto.INDIVIDUAL, Vista.NENHUMA, 1);
        assertNull(quarto.getValorNoite());
    }

    @Test
    void precoVemDaTarifa() {
        Quarto quarto = new Quarto(309, 3, TipoQuarto.FAMILIA, Vista.MAR, 4);
        quarto.setTarifa(new Tarifa(TipoQuarto.FAMILIA, Vista.MAR, new BigDecimal("150.00")));
        assertEquals(new BigDecimal("150.00"), quarto.getValorNoite());
    }

    @Test
    void mudarATarifaMudaOPreco() {
        Quarto quarto = new Quarto(208, 2, TipoQuarto.DUPLO, Vista.NENHUMA, 2);
        quarto.setTarifa(new Tarifa(TipoQuarto.DUPLO, Vista.NENHUMA, new BigDecimal("80.00")));
        quarto.setTarifa(new Tarifa(TipoQuarto.DUPLO, Vista.NENHUMA, new BigDecimal("85.00")));
        assertEquals(new BigDecimal("85.00"), quarto.getValorNoite());
    }

    @Test
    void quartoNovoEstaAtivo() {
        Quarto quarto = new Quarto(201, 2, TipoQuarto.INDIVIDUAL, Vista.NENHUMA, 1);
        assertTrue(quarto.isAtivo());
    }

    @Test
    void quartoPodeSerDesativadoEReativado() {
        Quarto quarto = new Quarto(305, 3, TipoQuarto.DUPLO, Vista.MAR, 2);
        quarto.setAtivo(false);
        assertFalse(quarto.isAtivo());
        quarto.setAtivo(true);
        assertTrue(quarto.isAtivo());
    }
}
