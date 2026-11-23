package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AcessoCartaoTest {

    private static final LocalDateTime HORA = LocalDateTime.of(2026, 11, 17, 9, 5);

    @Test
    public void testGetPessoaTextoDoFuncionario() {
        System.out.println("getPessoaTexto (funcionário)");
        AcessoCartao instance = new AcessoCartao(1, "F12", LocalHotel.BAR, 0, 0, HORA);
        instance.setFuncionario(12, "Ana Silva");
        String expResult = "Ana Silva";
        String result = instance.getPessoaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPessoaTextoDoCliente() {
        System.out.println("getPessoaTexto (cliente da reserva)");
        AcessoCartao instance = new AcessoCartao(1, "C201", LocalHotel.QUARTO, 201, 30, HORA);
        instance.setClienteNome("Rui Costa");
        String expResult = "Rui Costa";
        String result = instance.getPessoaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPessoaTextoDoCartaoMestre() {
        System.out.println("getPessoaTexto (cartão mestre)");
        AcessoCartao instance = new AcessoCartao(1, AcessoCartao.CARTAO_MESTRE, LocalHotel.PISCINA, 0, 0, HORA);
        String expResult = "Gestor (cartão mestre)";
        String result = instance.getPessoaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPessoaTextoSemPessoa() {
        System.out.println("getPessoaTexto (cartão sem pessoa)");
        AcessoCartao instance = new AcessoCartao(1, "C999", LocalHotel.PISCINA, 0, 0, HORA);
        String expResult = "-";
        String result = instance.getPessoaTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDataTexto() {
        System.out.println("getDataTexto");
        AcessoCartao instance = new AcessoCartao(1, "C201", LocalHotel.QUARTO, 201, 30, HORA);
        String expResult = "17/11/2026";
        String result = instance.getDataTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetHoraTexto() {
        System.out.println("getHoraTexto");
        AcessoCartao instance = new AcessoCartao(1, "C201", LocalHotel.QUARTO, 201, 30, HORA);
        String expResult = "09:05";
        String result = instance.getHoraTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDataTextoSemHora() {
        System.out.println("getDataTexto (ainda não gravado)");
        AcessoCartao instance = new AcessoCartao(0, "C201", LocalHotel.QUARTO, 201, 30, null);
        String expResult = "";
        String result = instance.getDataTexto();
        assertEquals(expResult, result);
    }
}
