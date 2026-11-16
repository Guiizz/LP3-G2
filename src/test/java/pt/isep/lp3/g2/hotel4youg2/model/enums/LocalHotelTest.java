package pt.isep.lp3.g2.hotel4youg2.model.enums;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalHotelTest {

    @Test
    public void testLocaisDoEnunciado() {
        System.out.println("values (os locais do enunciado e o piso de cada um)");
        List<String> expResult = List.of("Quarto 2", "Bar 0", "Salas de conferência 0", "Restaurante 1",
                "Piscina 1", "Cozinha 1", "Garagem -1", "Armazém -1");
        List<String> result = new ArrayList<>();
        for (LocalHotel local : LocalHotel.values()) {
            result.add(local.getDescricao() + " " + local.getPiso());
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPiso() {
        System.out.println("getPiso");
        int expResult = -1;
        int result = LocalHotel.GARAGEM.getPiso();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "Salas de conferência";
        String result = LocalHotel.SALA_CONFERENCIAS.toString();
        assertEquals(expResult, result);
    }
}
