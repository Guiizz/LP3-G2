package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.ISugestaoQueixaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoSugestao;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SugestaoQueixaBLLTest {

    // Base de dados falsa (em memória): guarda as sugestões e queixas e põe a data como a BD faria
    private static class SugestaoQueixaDALFalsa implements ISugestaoQueixaDAL {
        final List<SugestaoQueixa> registadas = new ArrayList<>();

        @Override
        public int inserir(SugestaoQueixa sugestao) {
            registadas.add(sugestao);
            sugestao.setId(registadas.size());
            sugestao.setRegistadaEm(LocalDateTime.of(2026, 10, 29, 18, 30));
            return sugestao.getId();
        }

        @Override
        public Optional<SugestaoQueixa> procurarPorId(int id) {
            return registadas.stream().filter(s -> s.getId() == id).findFirst();
        }

        @Override
        public boolean apagar(int id) {
            return registadas.removeIf(s -> s.getId() == id);
        }
    }

    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);
    private static final Cliente CLIENTE = new Cliente(15, "Ana Silva", "12345678", "123456789", "Rua A",
            "912345678", null, "ana@exemplo.pt");

    private SugestaoQueixaDALFalsa sugestaoDAL;
    private SugestaoQueixaBLL instance;

    @BeforeEach
    public void setUp() {
        sugestaoDAL = new SugestaoQueixaDALFalsa();
        instance = new SugestaoQueixaBLL(sugestaoDAL);
    }

    @AfterEach
    public void tearDown() {
        sugestaoDAL.registadas.clear();
    }

    @Test
    public void testRegistar() {
        System.out.println("registar");
        SugestaoQueixa expResult = new SugestaoQueixa(1, 15, TipoSugestao.QUEIXA, "O ar condicionado não funciona.", 7,
                LocalDateTime.of(2026, 10, 29, 18, 30));
        SugestaoQueixa result = instance.registar(RECECAO, CLIENTE, TipoSugestao.QUEIXA, "O ar condicionado não funciona.");
        assertEquals(expResult, result);
        assertEquals(1, sugestaoDAL.registadas.size());
    }

    @Test
    public void testRegistarTiraEspacos() {
        System.out.println("registar (tira os espaços das pontas)");
        String expResult = "Mais opções vegetarianas.";
        String result = instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, "   Mais opções vegetarianas.  ")
                .getDescricao();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarNoLimite() {
        System.out.println("registar (descrição com 1000 caracteres)");
        int expResult = 1000;
        int result = instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, "a".repeat(1000)).getDescricao().length();
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarSemSessao() {
        System.out.println("registar (sem sessão)");
        try {
            instance.registar(null, CLIENTE, TipoSugestao.QUEIXA, "Barulho no quarto.");
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals(0, sugestaoDAL.registadas.size());
        }
    }

    @Test
    public void testRegistarSemCliente() {
        System.out.println("registar (sem cliente)");
        try {
            instance.registar(RECECAO, null, TipoSugestao.QUEIXA, "Barulho no quarto.");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Procure primeiro o cliente pelo NIF.", expected.getMessage());
        }
    }

    @Test
    public void testRegistarSemTipo() {
        System.out.println("registar (sem tipo)");
        try {
            instance.registar(RECECAO, CLIENTE, null, "Barulho no quarto.");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha o tipo: sugestão ou queixa.", expected.getMessage());
        }
    }

    @Test
    public void testRegistarSemDescricao() {
        System.out.println("registar (descrição só com espaços)");
        try {
            instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, "   ");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A descrição é obrigatória.", expected.getMessage());
            assertEquals(0, sugestaoDAL.registadas.size());
        }
    }

    @Test
    public void testRegistarDescricaoNula() {
        System.out.println("registar (descrição nula)");
        try {
            instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, null);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A descrição é obrigatória.", expected.getMessage());
        }
    }

    @Test
    public void testRegistarDescricaoLonga() {
        System.out.println("registar (descrição com 1001 caracteres)");
        try {
            instance.registar(RECECAO, CLIENTE, TipoSugestao.SUGESTAO, "a".repeat(1001));
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A descrição não pode ter mais de 1000 caracteres.", expected.getMessage());
        }
    }
}
