package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IMaterialReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Material;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testes unitários da classe MaterialReservaBLL (US76).
 */
public class MaterialReservaBLLTest {

    private static class MaterialReservaDALFalsa implements IMaterialReservaDAL {
        private final List<MaterialQuarto> configuracao = new ArrayList<>();
        private final List<MaterialQuarto> ajustados = new ArrayList<>();
        private int ajustes;

        @Override
        public List<MaterialQuarto> listar(int reservaId) {
            if (!ajustados.isEmpty()) {
                return ajustados;
            }
            return configuracao;
        }

        @Override
        public void ajustar(int reservaId, int materialId, int quantidade) {
            if (ajustados.isEmpty()) {
                ajustados.addAll(configuracao);
            }
            for (int i = 0; i < ajustados.size(); i++) {
                MaterialQuarto linha = ajustados.get(i);
                if (linha.getMaterialId() == materialId) {
                    ajustados.set(i, new MaterialQuarto(linha.getTipoQuarto(), linha.getMaterial(), quantidade));
                }
            }
            ajustes++;
        }
    }

    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO = new Utilizador(2, 7, "rececao", "h", "s", Perfil.RECECIONISTA, true);

    private MaterialReservaDALFalsa materialReservaDAL;
    private MaterialReservaBLL instance;
    private Material toalha;
    private Material champo;
    private Reserva reserva;

    @BeforeEach
    public void setUp() {
        materialReservaDAL = new MaterialReservaDALFalsa();
        toalha = new Material(4, "PZ-1989", "Toalha de banho 400g cinza", "ProductHero", false);
        champo = new Material(5, "XQF6324", "Rituals Champô 200ml", "ProductHero", true);
        materialReservaDAL.configuracao.add(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2));
        materialReservaDAL.configuracao.add(new MaterialQuarto(TipoQuarto.DUPLO, champo, 2));
        LocalDate entrada = LocalDate.now().plusDays(3);
        reserva = new Reserva(12, 7, 201, entrada, entrada.plusDays(2), 1, EstadoReserva.CONFIRMADA,
                new BigDecimal("200.00"), null, null);
        instance = new MaterialReservaBLL(materialReservaDAL);
    }

    @AfterEach
    public void tearDown() {
        materialReservaDAL.configuracao.clear();
        materialReservaDAL.ajustados.clear();
        instance = null;
    }

    @Test
    public void testListarCarregaAConfiguracaoDoTipoDeQuarto() {
        System.out.println("listar (sem ajustes, a reserva leva a configuração do quarto duplo)");
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2),
                new MaterialQuarto(TipoQuarto.DUPLO, champo, 2));
        List<MaterialQuarto> result = instance.listar(RECECAO, reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testListarGestor() {
        System.out.println("listar (o gestor também pode ver)");
        int expResult = 2;
        int result = instance.listar(GESTOR, reserva).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testAjustar() {
        System.out.println("ajustar (uma só toalha para uma pessoa num quarto duplo)");
        instance.ajustar(RECECAO, reserva, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2), "1");
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 1),
                new MaterialQuarto(TipoQuarto.DUPLO, champo, 2));
        List<MaterialQuarto> result = instance.listar(RECECAO, reserva);
        assertEquals(expResult, result);
    }

    @Test
    public void testAjustarNaoMudaAConfiguracaoDoQuarto() {
        System.out.println("ajustar (o padrão do tipo de quarto continua igual)");
        instance.ajustar(RECECAO, reserva, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2), "1");
        List<MaterialQuarto> expResult = List.of(new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2),
                new MaterialQuarto(TipoQuarto.DUPLO, champo, 2));
        List<MaterialQuarto> result = materialReservaDAL.configuracao;
        assertEquals(expResult, result);
    }

    @Test
    public void testAjustarParaZero() {
        System.out.println("ajustar (0: a reserva não leva este material)");
        instance.ajustar(RECECAO, reserva, new MaterialQuarto(TipoQuarto.DUPLO, champo, 2), "0");
        int expResult = 0;
        int result = instance.listar(RECECAO, reserva).get(1).getQuantidade();
        assertEquals(expResult, result);
    }

    @Test
    public void testAjustarQuantidadeComEspacos() {
        System.out.println("ajustar (espaços à volta da quantidade)");
        instance.ajustar(GESTOR, reserva, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2), " 3 ");
        int expResult = 3;
        int result = instance.listar(GESTOR, reserva).get(0).getQuantidade();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarSemSessao() {
        System.out.println("listar (sem sessão iniciada)");
        try {
            instance.listar(null, reserva);
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            String expResult = "Entre na aplicação como rececionista ou gestor para ver os materiais da reserva.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testListarSemReserva() {
        System.out.println("listar (sem reserva)");
        try {
            instance.listar(RECECAO, null);
            fail("Devia ter pedido a reserva");
        } catch (IllegalArgumentException expected) {
            String expResult = "Escolha uma reserva.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAjustarDepoisDoCheckin() {
        System.out.println("ajustar (reserva com check-in feito já não muda)");
        Reserva emCurso = new Reserva(13, 7, 201, LocalDate.now(), LocalDate.now().plusDays(2), 1,
                EstadoReserva.EM_CURSO, new BigDecimal("200.00"), LocalDateTime.now(), null);
        try {
            instance.ajustar(RECECAO, emCurso, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2), "1");
            fail("Devia ter recusado o ajuste");
        } catch (IllegalStateException expected) {
            String expResult = "Os materiais só se ajustam antes do check-in, numa reserva confirmada.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(0, materialReservaDAL.ajustes);
        }
    }

    @Test
    public void testAjustarSemSessao() {
        System.out.println("ajustar (sem sessão iniciada)");
        try {
            instance.ajustar(null, reserva, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2), "1");
            fail("Devia ter exigido a sessão");
        } catch (IllegalStateException expected) {
            int expResult = 0;
            int result = materialReservaDAL.ajustes;
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAjustarSemReserva() {
        System.out.println("ajustar (sem reserva)");
        try {
            instance.ajustar(RECECAO, null, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2), "1");
            fail("Devia ter pedido a reserva");
        } catch (IllegalArgumentException expected) {
            String expResult = "Escolha uma reserva.";
            String result = expected.getMessage();
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testAjustarSemMaterial() {
        System.out.println("ajustar (sem material escolhido)");
        String expResult = "Escolha o material na tabela.";
        try {
            instance.ajustar(RECECAO, reserva, null, "1");
            fail("Devia ter recusado o ajuste");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(0, materialReservaDAL.ajustes);
        }
    }

    @Test
    public void testAjustarSemQuantidade() {
        System.out.println("ajustar (sem quantidade)");
        String expResult = "Indique a quantidade.";
        try {
            instance.ajustar(RECECAO, reserva, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2), " ");
            fail("Devia ter recusado o ajuste");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(0, materialReservaDAL.ajustes);
        }
    }

    @Test
    public void testAjustarQuantidadeAcimaDoMaximo() {
        System.out.println("ajustar (quantidade acima de 99)");
        String expResult = "A quantidade tem de ser um número inteiro de 0 a 99.";
        try {
            instance.ajustar(RECECAO, reserva, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2), "100");
            fail("Devia ter recusado o ajuste");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(0, materialReservaDAL.ajustes);
        }
    }

    @Test
    public void testAjustarQuantidadeNegativa() {
        System.out.println("ajustar (quantidade negativa)");
        String expResult = "A quantidade tem de ser um número inteiro de 0 a 99.";
        try {
            instance.ajustar(RECECAO, reserva, new MaterialQuarto(TipoQuarto.DUPLO, toalha, 2), "-1");
            fail("Devia ter recusado o ajuste");
        } catch (IllegalArgumentException expected) {
            String result = expected.getMessage();
            assertEquals(expResult, result);
            assertEquals(0, materialReservaDAL.ajustes);
        }
    }
}
