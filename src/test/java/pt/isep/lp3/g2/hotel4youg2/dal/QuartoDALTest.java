package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.RowMapper;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuartoDALTest {

    private QuartoDAL instance;
    private int numero;

    @BeforeEach
    public void setUp() {
        try {
            ConexaoBD.select("SELECT 1", new InteiroMapper());
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new QuartoDAL();
        numero = 901 + (int) Math.floorMod(System.nanoTime(), 99L);
        Assumptions.assumeTrue(procurar(numero) == null, "O quarto " + numero + " já existe na BD");
    }

    @AfterEach
    public void tearDown() {
        if (numero > 0) {
            ConexaoBD.executar("DELETE FROM Quarto WHERE numero = ?", numero);
        }
    }

    private Quarto procurar(int numeroQuarto) {
        for (Quarto quarto : new QuartoDAL().listarTodos()) {
            if (quarto.getNumero() == numeroQuarto) {
                return quarto;
            }
        }
        return null;
    }

    private BigDecimal precoDaTarifa(TipoQuarto tipo, Vista vista) {
        return ConexaoBD.select("SELECT valorNoite FROM Tarifa WHERE tipo = ? AND vista = ?",
                new PrecoMapper(), tipo.name(), vista.name()).get(0);
    }

    private static class PrecoMapper implements RowMapper<BigDecimal> {
        @Override
        public BigDecimal map(ResultSet rs) throws SQLException {
            return rs.getBigDecimal(1);
        }
    }

    @Test
    public void testInserir() {
        System.out.println("inserir (fica gravado, ativo e com a tarifa do tipo e da vista)");
        Quarto expResult = new Quarto(numero, 9, TipoQuarto.DUPLO, Vista.MAR, 2);
        expResult.setTarifa(new Tarifa(TipoQuarto.DUPLO, Vista.MAR, precoDaTarifa(TipoQuarto.DUPLO, Vista.MAR)));
        instance.inserir(new Quarto(numero, 9, TipoQuarto.DUPLO, Vista.MAR, 2));
        Quarto result = procurar(numero);
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirDevolveTrue() {
        System.out.println("inserir (devolve true quando grava)");
        boolean expResult = true;
        boolean result = instance.inserir(new Quarto(numero, 9, TipoQuarto.INDIVIDUAL, Vista.NENHUMA, 1));
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirApareceNaDisponibilidade() {
        System.out.println("inserir (o quarto novo aparece nos quartos livres para reservar)");
        instance.inserir(new Quarto(numero, 9, TipoQuarto.FAMILIA, Vista.MONTANHA, 4));
        LocalDate entrada = LocalDate.now().plusYears(5);
        boolean expResult = true;
        boolean result = false;
        for (Quarto quarto : instance.listarDisponiveis(entrada, entrada.plusDays(2))) {
            if (quarto.getNumero() == numero) {
                result = true;
            }
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testInserirRepetido() {
        System.out.println("inserir (número repetido não grava e devolve false)");
        instance.inserir(new Quarto(numero, 9, TipoQuarto.DUPLO, Vista.MAR, 2));
        Quarto expResult = procurar(numero);
        boolean result = instance.inserir(new Quarto(numero, 9, TipoQuarto.INDIVIDUAL, Vista.NENHUMA, 1));
        assertEquals(false, result);
        assertEquals(expResult, procurar(numero));
    }
}