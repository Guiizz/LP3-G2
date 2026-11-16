package pt.isep.lp3.g2.hotel4youg2.controller;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.CartaoAtivo;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EscolherPessoaControllerTest {

    private static final CartaoAtivo ANA = new CartaoAtivo("F12", "Ana Silva");
    private static final CartaoAtivo RUI = new CartaoAtivo("C201", "Rui Costa (quarto 201)");
    private static final List<CartaoAtivo> PESSOAS = List.of(ANA, RUI);

    @Test
    public void testFiltrarPeloNome() {
        System.out.println("filtrar (parte do nome)");
        List<Object> expResult = List.of(ANA);
        List<Object> result = EscolherPessoaController.filtrar(PESSOAS, "silva");
        assertEquals(expResult, result);
    }

    @Test
    public void testFiltrarPeloNumero() {
        System.out.println("filtrar (pelo número)");
        List<Object> expResult = List.of(RUI);
        List<Object> result = EscolherPessoaController.filtrar(PESSOAS, "201");
        assertEquals(expResult, result);
    }

    @Test
    public void testFiltrarSemTexto() {
        System.out.println("filtrar (sem texto mostra toda a gente)");
        List<Object> expResult = List.of(ANA, RUI);
        List<Object> result = EscolherPessoaController.filtrar(PESSOAS, "  ");
        assertEquals(expResult, result);
    }

    @Test
    public void testFiltrarComNull() {
        System.out.println("filtrar (texto null mostra toda a gente)");
        List<Object> expResult = List.of(ANA, RUI);
        List<Object> result = EscolherPessoaController.filtrar(PESSOAS, null);
        assertEquals(expResult, result);
    }

    @Test
    public void testFiltrarMaiusculasEEspacos() {
        System.out.println("filtrar (maiúsculas e espaços)");
        List<Object> expResult = List.of(RUI);
        List<Object> result = EscolherPessoaController.filtrar(PESSOAS, "  RUI ");
        assertEquals(expResult, result);
    }

    @Test
    public void testFiltrarSemResultados() {
        System.out.println("filtrar (ninguém com esse nome)");
        List<Object> expResult = new ArrayList<>();
        List<Object> result = EscolherPessoaController.filtrar(PESSOAS, "zacarias");
        assertEquals(expResult, result);
    }
}
