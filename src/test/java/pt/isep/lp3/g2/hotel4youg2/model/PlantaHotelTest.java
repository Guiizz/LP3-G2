package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlantaHotelTest {

    private List<Quarto> quartos() {
        List<Quarto> quartos = new ArrayList<>();
        quartos.add(new Quarto(201, 2, TipoQuarto.DUPLO, Vista.MAR, 2));
        quartos.add(new Quarto(301, 3, TipoQuarto.INDIVIDUAL, Vista.MONTANHA, 1));
        quartos.add(new Quarto(501, 5, TipoQuarto.FAMILIA, Vista.NENHUMA, 4));
        return quartos;
    }

    @Test
    public void testGetPisosPorOrdemComOPisoDeUmQuartoNovo() {
        System.out.println("getPisos (por ordem, com o piso 5 de um quarto criado)");
        PlantaHotel instance = new PlantaHotel(quartos());
        List<Integer> expResult = List.of(-1, 0, 1, 2, 3, 5);
        List<Integer> result = instance.getPisos();
        assertEquals(expResult, result);
    }

    @Test
    public void testLocaisDoPisoUmSemQuartos() {
        System.out.println("locaisDoPiso (piso 1: restaurante, piscina e cozinha)");
        PlantaHotel instance = new PlantaHotel(quartos());
        List<LocalPicagem> expResult = List.of(new LocalPicagem(LocalHotel.RESTAURANTE, 0),
                new LocalPicagem(LocalHotel.PISCINA, 0), new LocalPicagem(LocalHotel.COZINHA, 0));
        List<LocalPicagem> result = instance.locaisDoPiso(1);
        assertEquals(expResult, result);
    }

    @Test
    public void testLocaisDoPisoDoisSoComOsSeusQuartos() {
        System.out.println("locaisDoPiso (piso 2: só o quarto 201)");
        PlantaHotel instance = new PlantaHotel(quartos());
        List<LocalPicagem> expResult = List.of(LocalPicagem.quarto(201));
        List<LocalPicagem> result = instance.locaisDoPiso(2);
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarQuartoQueExiste() {
        System.out.println("procurarQuarto (201 existe)");
        PlantaHotel instance = new PlantaHotel(quartos());
        int expResult = 201;
        int result = instance.procurarQuarto(201).getNumero();
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarQuartoQueNaoExiste() {
        System.out.println("procurarQuarto (999 não existe)");
        PlantaHotel instance = new PlantaHotel(quartos());
        assertNull(instance.procurarQuarto(999));
    }

    @Test
    public void testPlantaSemQuartos() {
        System.out.println("getPisos (sem quartos: só os pisos -1, 0 e 1)");
        PlantaHotel instance = new PlantaHotel(null);
        List<Integer> expResult = List.of(-1, 0, 1);
        List<Integer> result = instance.getPisos();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDetalhesDoQuarto() {
        System.out.println("getDetalhes (quarto individual sem tarifa)");
        Quarto instance = new Quarto(301, 3, TipoQuarto.INDIVIDUAL, Vista.MONTANHA, 1);
        String result = instance.getDetalhes();
        assertTrue(result.contains("Capacidade: 1 pessoa"));
        assertFalse(result.contains("Preço por noite"));
    }

    private List<Quarto> tresQuartosNoPiso2() {
        List<Quarto> quartos = new ArrayList<>();
        quartos.add(new Quarto(201, 2, TipoQuarto.DUPLO, Vista.MAR, 2));
        quartos.add(new Quarto(202, 2, TipoQuarto.DUPLO, Vista.MAR, 2));
        quartos.add(new Quarto(203, 2, TipoQuarto.DUPLO, Vista.MAR, 2));
        return quartos;
    }

    @Test
    public void testFilaDeCimaFicaComAMetadeMaior() {
        System.out.println("filaDeCima (3 quartos: 201 e 202 em cima)");
        PlantaHotel instance = new PlantaHotel(tresQuartosNoPiso2());
        List<LocalPicagem> expResult = List.of(LocalPicagem.quarto(201), LocalPicagem.quarto(202));
        List<LocalPicagem> result = instance.filaDeCima(2);
        assertEquals(expResult, result);
    }

    @Test
    public void testFilaDeBaixoFicaComORestante() {
        System.out.println("filaDeBaixo (3 quartos: 203 em baixo)");
        PlantaHotel instance = new PlantaHotel(tresQuartosNoPiso2());
        List<LocalPicagem> expResult = List.of(LocalPicagem.quarto(203));
        List<LocalPicagem> result = instance.filaDeBaixo(2);
        assertEquals(expResult, result);
    }

    @Test
    public void testPisoDeUmQuartoEDeUmLocal() {
        System.out.println("pisoDe (quarto 301 no piso 3, piscina no piso 1)");
        PlantaHotel instance = new PlantaHotel(quartos());
        assertEquals(3, instance.pisoDe(LocalPicagem.quarto(301)));
        assertEquals(1, instance.pisoDe(new LocalPicagem(LocalHotel.PISCINA, 0)));
    }

    @Test
    public void testTemQuartos() {
        System.out.println("temQuartos (piso 2 sim, piso 0 não)");
        PlantaHotel instance = new PlantaHotel(quartos());
        assertTrue(instance.temQuartos(2));
        assertFalse(instance.temQuartos(0));
    }

    @Test
    public void testGetResumoDoQuarto() {
        System.out.println("getResumo (Duplo · Mar)");
        Quarto instance = new Quarto(201, 2, TipoQuarto.DUPLO, Vista.MAR, 2);
        String expResult = "Duplo · Mar";
        String result = instance.getResumo();
        assertEquals(expResult, result);
    }
}