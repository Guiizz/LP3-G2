package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IEntregaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.LeitorXmlEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class EntregaBLLTest {

    // Base de dados falsa (em memória): guarda as entregas registadas e quem as registou
    private static class EntregaDALFalsa implements IEntregaDAL {
        final List<Entrega> registadas = new ArrayList<>();
        int ultimoFuncionario;

        @Override
        public boolean existeEntrega(String codigoFornecedor, String numeroEncomenda) {
            return registadas.stream().anyMatch(e -> e.getFornecedor().getCodigo().equals(codigoFornecedor)
                    && e.getNumeroEncomenda().equals(numeroEncomenda));
        }

        @Override
        public int registar(Entrega entrega, int funcionarioId) {
            registadas.add(entrega);
            ultimoFuncionario = funcionarioId;
            entrega.setId(registadas.size());
            return entrega.getId();
        }

        @Override
        public Optional<Entrega> procurarPorId(int id) {
            return registadas.stream().filter(e -> e.getId() == id).findFirst();
        }

        @Override
        public boolean apagar(int id) {
            return registadas.removeIf(e -> e.getId() == id);
        }
    }

    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);

    private EntregaDALFalsa entregaDAL;
    private EntregaBLL instance;

    @BeforeEach
    public void setUp() {
        entregaDAL = new EntregaDALFalsa();
        instance = new EntregaBLL(new LeitorXmlEntrega(), entregaDAL);
    }

    @AfterEach
    public void tearDown() {
        entregaDAL.registadas.clear();
    }

    private InputStream ficheiro(String nome) {
        return EntregaBLLTest.class.getResourceAsStream("/pt/isep/lp3/g2/hotel4youg2/xml/" + nome);
    }

    @Test
    public void testLerFicheiro() {
        System.out.println("lerFicheiro");
        String expResult = "OC56974";
        Entrega result = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        assertEquals(expResult, result.getNumeroEncomenda());
    }

    @Test
    public void testVerificarContas() {
        System.out.println("verificarContas (ficheiro do professor)");
        Entrega entrega = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        List<String> expResult = List.of("Linha 2 (Rituals Condicionador/Champô de banho 200ml): o IVA de 23% "
                + "devia ser 160,54 €, mas o ficheiro diz 160,51 €.");
        List<String> result = instance.verificarContas(entrega);
        assertEquals(expResult, result);
    }

    @Test
    public void testVerificarContasTotalErrado() {
        System.out.println("verificarContas (total errado)");
        Entrega entrega = instance.lerFicheiro(ficheiro("entrega-total-errado.xml"));
        String expResult = "Linha 1 (Toalha de banho 400g cinza): o total devia ser 154,37 €, mas o ficheiro diz 999,99 €.";
        String result = instance.verificarContas(entrega).get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarComContasErradas() {
        System.out.println("registar (contas erradas, só avisa)");
        Entrega entrega = instance.lerFicheiro(ficheiro("entrega-total-errado.xml"));
        int expResult = 1;
        int result = instance.registar(RECECAO, entrega);
        assertEquals(expResult, result);
    }

    @Test
    public void testLerFicheiroNulo() {
        System.out.println("lerFicheiro (sem ficheiro)");
        try {
            instance.lerFicheiro((java.io.File) null);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha um ficheiro XML.", expected.getMessage());
        }
    }

    @Test
    public void testRegistar() {
        System.out.println("registar");
        Entrega entrega = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        int expResult = 1;
        int result = instance.registar(RECECAO, entrega);
        assertEquals(expResult, result);
        assertEquals(7, entregaDAL.ultimoFuncionario);
    }

    @Test
    public void testRegistarRepetida() {
        System.out.println("registar (repetida)");
        instance.registar(RECECAO, instance.lerFicheiro(ficheiro("entrega-valida.xml")));
        try {
            instance.registar(RECECAO, instance.lerFicheiro(ficheiro("entrega-valida.xml")));
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A entrega OC56974 do fornecedor ProductHero já foi registada.", expected.getMessage());
            assertEquals(1, entregaDAL.registadas.size());
        }
    }

    @Test
    public void testRegistarSemSessao() {
        System.out.println("registar (sem sessão)");
        Entrega entrega = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        try {
            instance.registar(null, entrega);
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals(0, entregaDAL.registadas.size());
        }
    }

    @Test
    public void testRegistarSemEntrega() {
        System.out.println("registar (sem entrega)");
        try {
            instance.registar(RECECAO, null);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Carregue primeiro um ficheiro XML válido.", expected.getMessage());
        }
    }
}
