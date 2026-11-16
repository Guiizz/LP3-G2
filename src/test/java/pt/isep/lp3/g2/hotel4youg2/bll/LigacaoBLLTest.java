package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.ILigacaoDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LigacaoBLLTest {

    private static class LigacaoDALFalsa implements ILigacaoDAL {
        private DataAccessException erro;
        private int preparacoes;

        @Override
        public void testar() {
            if (erro != null) {
                throw erro;
            }
        }

        @Override
        public void preparar() {
            preparacoes++;
        }
    }

    private LigacaoDALFalsa ligacaoDAL;
    private LigacaoBLL instance;

    @BeforeEach
    public void setUp() {
        ligacaoDAL = new LigacaoDALFalsa();
        instance = new LigacaoBLL(ligacaoDAL);
    }

    @AfterEach
    public void tearDown() {
        ligacaoDAL.erro = null;
    }

    @Test
    public void testTestarLigacao() {
        System.out.println("testarLigacao (a base de dados responde)");
        String expResult = "Ligação à base de dados estabelecida";
        String result = instance.testarLigacao();
        assertEquals(expResult, result);
    }

    @Test
    public void testTestarLigacaoSemBaseDeDados() {
        System.out.println("testarLigacao (a base de dados não responde)");
        ligacaoDAL.erro = new DataAccessException("Erro ao ligar", new SQLException("timeout"));
        String expResult = "Não foi possível ligar: Erro ao ligar (timeout)";
        String result = instance.testarLigacao();
        assertEquals(expResult, result);
    }

    @Test
    public void testTestarLigacaoSemCausa() {
        System.out.println("testarLigacao (erro sem causa)");
        ligacaoDAL.erro = new DataAccessException("Erro ao ligar");
        String expResult = "Não foi possível ligar: Erro ao ligar (sem detalhe)";
        String result = instance.testarLigacao();
        assertEquals(expResult, result);
    }

    @Test
    public void testPrepararLigacao() {
        System.out.println("prepararLigacao");
        instance.prepararLigacao();
        int expResult = 1;
        int result = ligacaoDAL.preparacoes;
        assertEquals(expResult, result);
    }
}
