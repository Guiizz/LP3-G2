package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IStockDAL;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class StockBLLTest {

    private static class StockDALFalsa implements IStockDAL {
        private final List<MaterialStock> materiais = new ArrayList<>();
        private int emStock = 10;
        private int movimentos;
        private int ultimoFuncionario;

        @Override
        public List<MaterialStock> listar() {
            return materiais;
        }

        @Override
        public boolean registarSaida(int materialId, int quantidade, int funcionarioId) {
            if (quantidade > emStock) {
                return false;
            }
            emStock -= quantidade;
            movimentos++;
            ultimoFuncionario = funcionarioId;
            return true;
        }
    }

    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);
    private static final MaterialStock TOALHAS = new MaterialStock(4, "M-1", "Toalhas", "Lavandaria Norte", 10);

    private StockDALFalsa stockDAL;
    private StockBLL instance;

    @BeforeEach
    public void setUp() {
        stockDAL = new StockDALFalsa();
        stockDAL.materiais.add(TOALHAS);
        instance = new StockBLL(stockDAL);
    }

    @AfterEach
    public void tearDown() {
        stockDAL.materiais.clear();
    }

    private void esperarErro(MaterialStock material, String quantidade, String expResult) {
        try {
            instance.registarSaida(RECECAO, material, quantidade);
            fail("Devia ter recusado a saída");
        } catch (IllegalArgumentException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(10, stockDAL.emStock);
            assertEquals(0, stockDAL.movimentos);
        }
    }

    @Test
    public void testListar() {
        System.out.println("listar");
        List<MaterialStock> expResult = List.of(TOALHAS);
        List<MaterialStock> result = instance.listar(RECECAO);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarSemSessao() {
        System.out.println("listar (sem sessão iniciada)");
        String expResult = "Entre na aplicação para registar saídas de stock.";
        try {
            instance.listar(null);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
        }
    }

    @Test
    public void testRegistarSaida() {
        System.out.println("registarSaida (tira do stock)");
        instance.registarSaida(RECECAO, TOALHAS, "3");
        int expResult = 7;
        int result = stockDAL.emStock;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaGuardaOFuncionario() {
        System.out.println("registarSaida (fica registado o funcionário)");
        instance.registarSaida(RECECAO, TOALHAS, "3");
        int expResult = 7;
        int result = stockDAL.ultimoFuncionario;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAceitaEspacos() {
        System.out.println("registarSaida (espaços na quantidade)");
        instance.registarSaida(RECECAO, TOALHAS, " 4 ");
        int expResult = 6;
        int result = stockDAL.emStock;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaDeTudo() {
        System.out.println("registarSaida (pode tirar tudo o que existe)");
        instance.registarSaida(RECECAO, TOALHAS, "10");
        int expResult = 0;
        int result = stockDAL.emStock;
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSaidaAMais() {
        System.out.println("registarSaida (mais do que existe)");
        esperarErro(TOALHAS, "11", "Não é possível retirar mais do que existe em stock.");
    }

    @Test
    public void testRegistarSaidaSemMaterial() {
        System.out.println("registarSaida (sem material)");
        esperarErro(null, "3", "Escolha o material na tabela.");
    }

    @Test
    public void testRegistarSaidaSemQuantidade() {
        System.out.println("registarSaida (sem quantidade)");
        esperarErro(TOALHAS, "  ", "Indique a quantidade.");
    }

    @Test
    public void testRegistarSaidaQuantidadeNula() {
        System.out.println("registarSaida (quantidade nula)");
        esperarErro(TOALHAS, null, "Indique a quantidade.");
    }

    @Test
    public void testRegistarSaidaQuantidadeZero() {
        System.out.println("registarSaida (quantidade zero)");
        esperarErro(TOALHAS, "0", "A quantidade tem de ser um número inteiro maior que zero.");
    }

    @Test
    public void testRegistarSaidaQuantidadeNegativa() {
        System.out.println("registarSaida (quantidade negativa)");
        esperarErro(TOALHAS, "-3", "A quantidade tem de ser um número inteiro maior que zero.");
    }

    @Test
    public void testRegistarSaidaQuantidadeComLetras() {
        System.out.println("registarSaida (quantidade com letras)");
        esperarErro(TOALHAS, "abc", "A quantidade tem de ser um número inteiro maior que zero.");
    }

    @Test
    public void testRegistarSaidaSemSessao() {
        System.out.println("registarSaida (sem sessão iniciada)");
        String expResult = "Entre na aplicação para registar saídas de stock.";
        try {
            instance.registarSaida(null, TOALHAS, "3");
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            assertEquals(expResult, expected.getMessage());
            assertEquals(0, stockDAL.movimentos);
        }
    }
}
