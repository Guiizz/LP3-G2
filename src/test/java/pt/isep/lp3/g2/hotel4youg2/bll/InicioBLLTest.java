package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InicioBLLTest {

    @Test
    public void testSaudacaoManha() {
        System.out.println("saudacao (9h)");
        assertEquals("Bom dia", InicioBLL.saudacao(LocalTime.of(9, 0)));
    }

    @Test
    public void testSaudacaoMeioDia() {
        System.out.println("saudacao (12h já é tarde)");
        assertEquals("Boa tarde", InicioBLL.saudacao(LocalTime.of(12, 0)));
    }

    @Test
    public void testSaudacaoNoite() {
        System.out.println("saudacao (22h)");
        assertEquals("Boa noite", InicioBLL.saudacao(LocalTime.of(22, 0)));
    }

    @Test
    public void testSaudacaoMadrugada() {
        System.out.println("saudacao (3h)");
        assertEquals("Boa noite", InicioBLL.saudacao(LocalTime.of(3, 0)));
    }
}