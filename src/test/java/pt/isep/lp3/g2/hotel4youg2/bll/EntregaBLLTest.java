package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IEntregaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.LeitorXmlEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
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
            for (Entrega e : registadas) {
                if (e.getFornecedor().getCodigo().equals(codigoFornecedor)
                        && e.getNumeroEncomenda().equals(numeroEncomenda)
                        && e.getEstado() != EstadoEntrega.RECUSADA) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int contarPendentes(String codigoFornecedor) {
            int pendentes = 0;
            for (Entrega e : registadas) {
                if (e.getFornecedor().getCodigo().equals(codigoFornecedor)
                        && e.getEstado() == EstadoEntrega.PENDENTE) {
                    pendentes++;
                }
            }
            return pendentes;
        }

        @Override
        public int carregar(Entrega entrega) {
            entrega.setEstado(EstadoEntrega.PENDENTE);
            registadas.add(entrega);
            entrega.setId(registadas.size());
            return entrega.getId();
        }

        @Override
        public int registar(Entrega entrega, int funcionarioId) {
            entrega.setEstado(EstadoEntrega.ACEITE);
            registadas.add(entrega);
            ultimoFuncionario = funcionarioId;
            entrega.setId(registadas.size());
            return entrega.getId();
        }

        @Override
        public Optional<Entrega> procurarPorId(int id) {
            for (Entrega e : registadas) {
                if (e.getId() == id) {
                    return Optional.of(e);
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean apagar(int id) {
            Iterator<Entrega> iterador = registadas.iterator();
            while (iterador.hasNext()) {
                if (iterador.next().getId() == id) {
                    iterador.remove();
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<ResumoEntrega> listarResumosDoFornecedor(String codigoFornecedor) {
            List<ResumoEntrega> resumos = new ArrayList<>();
            for (Entrega e : registadas) {
                if (e.getFornecedor().getCodigo().equals(codigoFornecedor)) {
                    resumos.add(new ResumoEntrega(e.getNumeroEncomenda(), contarMateriais(e),
                            LocalDateTime.of(2026, 11, 5, 21, 24, 38), e.getTotal(), e.getEstado()));
                }
            }
            return resumos;
        }

        private int contarMateriais(Entrega entrega) {
            List<String> codigos = new ArrayList<>();
            for (LinhaEntrega linha : entrega.getLinhas()) {
                if (!codigos.contains(linha.getCodigoProduto())) {
                    codigos.add(linha.getCodigoProduto());
                }
            }
            return codigos.size();
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
    public void testLerFicheiroQuantidadeZero() {
        System.out.println("lerFicheiro (quantidade zero)");
        try {
            instance.lerFicheiro(ficheiro("entrega-quantidade-zero.xml"));
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Linha 1 (Toalha de banho 400g cinza): a quantidade tem de ser maior que zero.",
                    expected.getMessage());
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

    private Fornecedor fornecedorDoFicheiro() {
        return instance.lerFicheiro(ficheiro("entrega-valida.xml")).getFornecedor();
    }

    // cria uma entrega pendente do mesmo fornecedor do ficheiro, com outro número de encomenda
    private Entrega entregaPendente(String numero) {
        Entrega entrega = new Entrega(0, numero, java.time.LocalDate.of(2026, 11, 4), fornecedorDoFicheiro());
        entregaDAL.carregar(entrega);
        return entrega;
    }

    @Test
    public void testCarregar() {
        System.out.println("carregar");
        Entrega entrega = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        int expResult = 1;
        int result = instance.carregar(fornecedorDoFicheiro(), entrega);
        assertEquals(expResult, result);
        assertEquals(EstadoEntrega.PENDENTE, entrega.getEstado());
    }

    @Test
    public void testCarregarDeOutroFornecedor() {
        System.out.println("carregar (ficheiro de outro fornecedor)");
        Entrega entrega = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        Fornecedor outro = new Fornecedor(9, "OUTRO", "Outro", "", "", "", "", "", "PT");
        try {
            instance.carregar(outro, entrega);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("O ficheiro é do fornecedor " + entrega.getFornecedor().getCodigo()
                    + ". Só pode carregar entregas do seu fornecedor.", expected.getMessage());
            assertEquals(0, entregaDAL.registadas.size());
        }
    }

    @Test
    public void testCarregarRepetida() {
        System.out.println("carregar (repetida)");
        Fornecedor fornecedor = fornecedorDoFicheiro();
        instance.carregar(fornecedor, instance.lerFicheiro(ficheiro("entrega-valida.xml")));
        try {
            instance.carregar(fornecedor, instance.lerFicheiro(ficheiro("entrega-valida.xml")));
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A entrega OC56974 já foi carregada.", expected.getMessage());
            assertEquals(1, entregaDAL.registadas.size());
        }
    }

    @Test
    public void testCarregarDepoisDeRecusada() {
        System.out.println("carregar (depois de recusada)");
        Fornecedor fornecedor = fornecedorDoFicheiro();
        instance.carregar(fornecedor, instance.lerFicheiro(ficheiro("entrega-valida.xml")));
        entregaDAL.registadas.get(0).setEstado(EstadoEntrega.RECUSADA);
        int expResult = 2;
        int result = instance.carregar(fornecedor, instance.lerFicheiro(ficheiro("entrega-valida.xml")));
        assertEquals(expResult, result);
    }

    @Test
    public void testCarregarComCincoPendentes() {
        System.out.println("carregar (já tem 5 pendentes)");
        for (int i = 1; i <= EntregaBLL.MAX_PENDENTES; i++) {
            entregaPendente("PENDENTE-" + i);
        }
        Entrega entrega = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        try {
            instance.carregar(fornecedorDoFicheiro(), entrega);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Tem 5 entregas por aprovar. Aguarde a decisão do hotel.", expected.getMessage());
            assertEquals(EntregaBLL.MAX_PENDENTES, entregaDAL.registadas.size());
        }
    }

    @Test
    public void testCarregarComQuatroPendentes() {
        System.out.println("carregar (4 pendentes, ainda pode)");
        for (int i = 1; i < EntregaBLL.MAX_PENDENTES; i++) {
            entregaPendente("PENDENTE-" + i);
        }
        int expResult = EntregaBLL.MAX_PENDENTES;
        int result = instance.carregar(fornecedorDoFicheiro(), instance.lerFicheiro(ficheiro("entrega-valida.xml")));
        assertEquals(expResult, result);
    }

    @Test
    public void testCarregarSemSessao() {
        System.out.println("carregar (sem sessão)");
        Entrega entrega = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        try {
            instance.carregar(null, entrega);
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals(0, entregaDAL.registadas.size());
        }
    }

    @Test
    public void testCarregarSemEntrega() {
        System.out.println("carregar (sem entrega)");
        try {
            instance.carregar(fornecedorDoFicheiro(), null);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Carregue primeiro um ficheiro XML válido.", expected.getMessage());
        }
    }

    @Test
    public void testHistoricoSoMostraAsEntregasDoFornecedor() {
        System.out.println("historico (só as do fornecedor)");
        entregaPendente("MINHA-1");
        Fornecedor outro = new Fornecedor(9, "OUTRO", "Outro", "", "", "", "", "", "PT");
        entregaDAL.carregar(new Entrega(0, "DELE-1", java.time.LocalDate.of(2026, 11, 4), outro));
        List<ResumoEntrega> result = instance.historico(fornecedorDoFicheiro());
        assertEquals(1, result.size());
        assertEquals("MINHA-1", result.get(0).getNumeroEncomenda());
    }

    @Test
    public void testHistoricoMostraOEstadoDeCadaEntrega() {
        System.out.println("historico (estado de cada entrega)");
        entregaPendente("PENDENTE-1");
        entregaPendente("RECUSADA-1").setEstado(EstadoEntrega.RECUSADA);
        List<EstadoEntrega> expResult = List.of(EstadoEntrega.PENDENTE, EstadoEntrega.RECUSADA);
        List<EstadoEntrega> result = new ArrayList<>();
        for (ResumoEntrega resumo : instance.historico(fornecedorDoFicheiro())) {
            result.add(resumo.getEstado());
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testHistoricoSemSessao() {
        System.out.println("historico (sem sessão)");
        try {
            instance.historico(null);
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals("Entre na aplicação para ver as suas entregas.", expected.getMessage());
        }
    }
}
