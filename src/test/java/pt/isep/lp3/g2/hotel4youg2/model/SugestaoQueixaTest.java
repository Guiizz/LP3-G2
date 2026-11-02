package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SugestaoQueixaTest {

    @Test
    void construtorGuardaOsDados() {
        SugestaoQueixa s = new SugestaoQueixa(3, 7, TipoSugestao.QUEIXA, "Ar condicionado avariado", 2,
                LocalDateTime.of(2026, 10, 29, 18, 30));
        assertEquals(3, s.getId());
        assertEquals(7, s.getClienteId());
        assertEquals(TipoSugestao.QUEIXA, s.getTipo());
        assertEquals("Ar condicionado avariado", s.getDescricao());
        assertEquals(2, s.getRegistadaPor());
    }

    @Test
    void dataApareceFormatada() {
        SugestaoQueixa s = new SugestaoQueixa(1, 7, TipoSugestao.SUGESTAO, "Mais toalhas", 2,
                LocalDateTime.of(2026, 10, 29, 18, 30));
        assertEquals("29/10/2026 18:30", s.getRegistadaEmTexto());
    }

    @Test
    void semDataMostraTextoVazio() {
        SugestaoQueixa s = new SugestaoQueixa(0, 7, TipoSugestao.SUGESTAO, "Mais toalhas", 2, null);
        assertEquals("", s.getRegistadaEmTexto());
    }

    @Test
    void nomeDoClienteFicaGuardado() {
        SugestaoQueixa s = new SugestaoQueixa(1, 7, TipoSugestao.QUEIXA, "Barulho", 2, null);
        s.setNomeCliente("Ana Silva");
        assertEquals("Ana Silva", s.getNomeCliente());
    }
}