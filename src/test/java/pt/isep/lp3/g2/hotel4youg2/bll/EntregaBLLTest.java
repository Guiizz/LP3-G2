package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.LeitorXmlEntrega;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IEntregaDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.ErrosFicheiroException;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoEntregaHotel;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class EntregaBLLTest {

    // Base de dados falsa (em memória): guarda as entregas registadas e quem as registou
    private static class EntregaDALFalsa implements IEntregaDAL {
        final List<Entrega> registadas = new ArrayList<>();
        int ultimoFuncionario;
        int ultimoDecisor;
        String ultimoMotivo;

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
                    resumos.add(new ResumoEntrega(e.getId(), e.getNumeroEncomenda(), contarMateriais(e),
                            LocalDateTime.of(2026, 11, 5, 21, 24, 38), e.getTotal(), e.getEstado(), null));
                }
            }
            return resumos;
        }

        @Override
        public boolean aceitar(int entregaId, int funcionarioId) {
            for (Entrega e : registadas) {
                if (e.getId() == entregaId && e.getEstado() == EstadoEntrega.PENDENTE) {
                    e.setEstado(EstadoEntrega.ACEITE);
                    ultimoDecisor = funcionarioId;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean recusar(int entregaId, int funcionarioId, String motivo) {
            for (Entrega e : registadas) {
                if (e.getId() == entregaId && e.getEstado() == EstadoEntrega.PENDENTE) {
                    e.setEstado(EstadoEntrega.RECUSADA);
                    ultimoDecisor = funcionarioId;
                    ultimoMotivo = motivo;
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<ResumoEntregaHotel> listarParaHotel() {
            List<ResumoEntregaHotel> resumos = new ArrayList<>();
            for (Entrega e : registadas) {
                resumos.add(new ResumoEntregaHotel(e.getId(), e.getNumeroEncomenda(), e.getFornecedor().getNome(),
                        contarMateriais(e), 0, LocalDateTime.of(2026, 11, 5, 21, 24, 38), e.getTotal(),
                        e.getEstado(), null, null, null));
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
        LeitorXmlEntrega.usarXsdDoProjeto();
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

    // lê um ficheiro que devia ser recusado e devolve a lista de erros
    private List<String> errosAoLer(String nome) {
        try {
            instance.lerFicheiro(ficheiro(nome));
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
            return null;
        } catch (ErrosFicheiroException expected) {
            return expected.getErros();
        }
    }

    @Test
    public void testLerFicheiroDiferencaDeCentimos() {
        System.out.println("lerFicheiro (IVA com 3 cêntimos de diferença passa, com aviso)");
        // ficheiro do professor: linha 2 com IVA 160,51 € em vez de 160,54 €
        Entrega entrega = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        int expResult = 1;
        int result = instance.verificarContas(entrega).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testLerFicheiroTotalErrado() {
        System.out.println("lerFicheiro (total errado é recusado)");
        List<String> expResult = List.of("Linha 1, campo LineBaseAmount: o total devia ser 154,37 €, "
                + "mas o ficheiro diz 999,99 €.");
        List<String> result = errosAoLer("entrega-total-errado.xml");
        assertEquals(expResult, result);
    }

    @Test
    public void testLerFicheiroVariosErros() {
        System.out.println("lerFicheiro (vários erros aparecem todos juntos)");
        List<String> expResult = List.of(
                "Cabeçalho, campo OrderDate: a data da encomenda não existe (5/13/2026).",
                "Linha 4, campo PricePerUnit: o valor está em USD, mas a encomenda está em EUR.",
                "Linha 5, campo LineBaseAmount: o total devia ser 3431,70 €, mas o ficheiro diz 9999,99 €.");
        List<String> result = errosAoLer("entrega-varios-erros.xml");
        assertEquals(expResult, result);
    }

    @Test
    public void testLerFicheiroRecusadoNaoGuardaNada() {
        System.out.println("lerFicheiro (ficheiro recusado: nada é guardado)");
        errosAoLer("entrega-varios-erros.xml");
        assertEquals(0, entregaDAL.registadas.size());
    }

    @Test
    public void testImportarXsdSemFicheiro() {
        System.out.println("importarXsd (sem ficheiro)");
        try {
            instance.importarXsd(null);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escolha um ficheiro XSD.", expected.getMessage());
        }
    }

    @Test
    public void testNomeXsdEmUsoSemImportar() {
        System.out.println("nomeXsdEmUso (sem importar usa o do projeto)");
        String expResult = null;
        String result = instance.nomeXsdEmUso();
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
            fail("Devia ter lançado ErrosFicheiroException, mas não lançou");
        } catch (ErrosFicheiroException expected) {
            List<String> expResult = List.of(
                    "Linha 1, campo Quantity: a quantidade tem de ser maior que zero.",
                    "Linha 1, campo MonetaryAdjustmentStartAmount: a quantidade vezes o preço dá 0,00 €, "
                            + "mas o ficheiro diz 125,50 €.");
            assertEquals(expResult, expected.getErros());
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

    // tenta recusar e devolve a mensagem do erro; devolve null se não houve erro
    private String mensagemAoRecusar(Utilizador quemAge, int entregaId, String motivo) {
        try {
            instance.recusar(quemAge, entregaId, motivo);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    // tenta aceitar e devolve a mensagem do erro; devolve null se não houve erro
    private String mensagemAoAceitar(Utilizador quemAge, int entregaId) {
        try {
            instance.aceitar(quemAge, entregaId);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testAceitar() {
        System.out.println("aceitar");
        Entrega entrega = entregaPendente("PENDENTE-1");
        instance.aceitar(RECECAO, entrega.getId());
        assertEquals(EstadoEntrega.ACEITE, entrega.getEstado());
        assertEquals(7, entregaDAL.ultimoDecisor);
    }

    @Test
    public void testAceitarSemSessao() {
        System.out.println("aceitar (sem sessão)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        String expResult = "Entre na aplicação para decidir entregas.";
        String result = mensagemAoAceitar(null, entrega.getId());
        assertEquals(expResult, result);
        assertEquals(EstadoEntrega.PENDENTE, entrega.getEstado());
    }

    @Test
    public void testAceitarQueNaoExiste() {
        System.out.println("aceitar (não existe)");
        String expResult = "A entrega não existe.";
        String result = mensagemAoAceitar(RECECAO, 99);
        assertEquals(expResult, result);
    }

    @Test
    public void testAceitarJaDecidida() {
        System.out.println("aceitar (já foi decidida)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        instance.aceitar(RECECAO, entrega.getId());
        String expResult = "Esta entrega já foi decidida.";
        String result = mensagemAoAceitar(RECECAO, entrega.getId());
        assertEquals(expResult, result);
    }

    @Test
    public void testRecusar() {
        System.out.println("recusar");
        Entrega entrega = entregaPendente("PENDENTE-1");
        instance.recusar(RECECAO, entrega.getId(), "Faltam toalhas");
        assertEquals(EstadoEntrega.RECUSADA, entrega.getEstado());
        assertEquals("Faltam toalhas", entregaDAL.ultimoMotivo);
        assertEquals(7, entregaDAL.ultimoDecisor);
    }

    @Test
    public void testRecusarSemMotivo() {
        System.out.println("recusar (sem motivo)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        instance.recusar(RECECAO, entrega.getId(), "   ");
        assertEquals(EstadoEntrega.RECUSADA, entrega.getEstado());
        assertNull(entregaDAL.ultimoMotivo);
    }

    @Test
    public void testRecusarMotivoSoComSimbolos() {
        System.out.println("recusar (motivo só com símbolos)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        instance.recusar(RECECAO, entrega.getId(), "....................");
        assertEquals(EstadoEntrega.RECUSADA, entrega.getEstado());
        assertNull(entregaDAL.ultimoMotivo);
    }

    @Test
    public void testRecusarMotivoComSimbolosEUmaPalavra() {
        System.out.println("recusar (motivo com símbolos e uma palavra)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        instance.recusar(RECECAO, entrega.getId(), "... Atrasada!");
        assertEquals("... Atrasada!", entregaDAL.ultimoMotivo);
    }

    @Test
    public void testRecusarMotivoComMuitosSimbolos() {
        System.out.println("recusar (201 pontos não dá erro, não há motivo)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        assertNull(mensagemAoRecusar(RECECAO, entrega.getId(), ".".repeat(201)));
        assertNull(entregaDAL.ultimoMotivo);
    }

    @Test
    public void testRecusarTiraOsEspacosDoMotivo() {
        System.out.println("recusar (tira os espaços do motivo)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        instance.recusar(RECECAO, entrega.getId(), "  Faltam toalhas  ");
        assertEquals("Faltam toalhas", entregaDAL.ultimoMotivo);
    }

    @Test
    public void testRecusarMotivoComMuitosCaracteres() {
        System.out.println("recusar (motivo com 201 caracteres)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        String expResult = "O motivo pode ter no máximo 200 caracteres.";
        String result = mensagemAoRecusar(RECECAO, entrega.getId(), "x".repeat(201));
        assertEquals(expResult, result);
        assertEquals(EstadoEntrega.PENDENTE, entrega.getEstado());
    }

    @Test
    public void testRecusarMotivoComOLimiteDeCaracteres() {
        System.out.println("recusar (motivo com 200 caracteres)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        assertNull(mensagemAoRecusar(RECECAO, entrega.getId(), "x".repeat(200)));
        assertEquals(EstadoEntrega.RECUSADA, entrega.getEstado());
    }

    @Test
    public void testRecusarSemSessao() {
        System.out.println("recusar (sem sessão)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        String expResult = "Entre na aplicação para decidir entregas.";
        String result = mensagemAoRecusar(null, entrega.getId(), "Motivo");
        assertEquals(expResult, result);
    }

    @Test
    public void testRecusarJaAceite() {
        System.out.println("recusar (já foi aceite)");
        Entrega entrega = entregaPendente("PENDENTE-1");
        instance.aceitar(RECECAO, entrega.getId());
        String expResult = "Esta entrega já foi decidida.";
        String result = mensagemAoRecusar(RECECAO, entrega.getId(), "Tarde de mais");
        assertEquals(expResult, result);
        assertEquals(EstadoEntrega.ACEITE, entrega.getEstado());
    }

    @Test
    public void testListarParaHotel() {
        System.out.println("listarParaHotel");
        entregaPendente("PENDENTE-1");
        List<ResumoEntregaHotel> result = instance.listarParaHotel(RECECAO);
        assertEquals(1, result.size());
        assertEquals("PENDENTE-1", result.get(0).getNumeroEncomenda());
    }

    @Test
    public void testListarParaHotelSemSessao() {
        System.out.println("listarParaHotel (sem sessão)");
        try {
            instance.listarParaHotel(null);
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals("Entre na aplicação para ver as entregas.", expected.getMessage());
        }
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

    // três entregas do fornecedor do ficheiro: uma aceite, uma recusada e uma pendente
    private void entregasEmTresEstados() {
        entregaPendente("OC-ACEITE");
        entregaPendente("OC-RECUSADA");
        entregaPendente("OC-PENDENTE");
        instance.aceitar(RECECAO, 1);
        instance.recusar(RECECAO, 2, "Faltam toalhas");
    }

    @Test
    public void testHistoricoFiltradoPorEstado() {
        System.out.println("historico (só as aceites)");
        entregasEmTresEstados();
        List<ResumoEntrega> result = instance.historico(fornecedorDoFicheiro(), EstadoEntrega.ACEITE);
        assertEquals(1, result.size());
        assertEquals("OC-ACEITE", result.get(0).getNumeroEncomenda());
    }

    @Test
    public void testHistoricoSemFiltroMostraTodas() {
        System.out.println("historico (sem filtro mostra as pendentes, aceites e recusadas)");
        entregasEmTresEstados();
        int expResult = 3;
        int result = instance.historico(fornecedorDoFicheiro(), null).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testHistoricoFiltradoSemEntregasNesseEstado() {
        System.out.println("historico (nenhuma entrega nesse estado)");
        entregaPendente("OC-PENDENTE");
        int expResult = 0;
        int result = instance.historico(fornecedorDoFicheiro(), EstadoEntrega.RECUSADA).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testListarParaHotelFiltradoPorEstado() {
        System.out.println("listarParaHotel (só as recusadas)");
        entregasEmTresEstados();
        List<ResumoEntregaHotel> result = instance.listarParaHotel(RECECAO, EstadoEntrega.RECUSADA);
        assertEquals(1, result.size());
        assertEquals("OC-RECUSADA", result.get(0).getNumeroEncomenda());
    }

    @Test
    public void testListarParaHotelSemFiltroMostraTodas() {
        System.out.println("listarParaHotel (sem filtro mostra todas)");
        entregasEmTresEstados();
        int expResult = 3;
        int result = instance.listarParaHotel(RECECAO, null).size();
        assertEquals(expResult, result);
    }

    @Test
    public void testEstadoDoFiltroTodas() {
        System.out.println("estadoDoFiltro (Todas)");
        EstadoEntrega expResult = null;
        EstadoEntrega result = EntregaBLL.estadoDoFiltro(EntregaBLL.TODAS);
        assertEquals(expResult, result);
    }

    @Test
    public void testEstadoDoFiltroSemOpcao() {
        System.out.println("estadoDoFiltro (sem opção escolhida)");
        EstadoEntrega expResult = null;
        EstadoEntrega result = EntregaBLL.estadoDoFiltro(null);
        assertEquals(expResult, result);
    }

    @Test
    public void testEstadoDoFiltro() {
        System.out.println("estadoDoFiltro (ACEITE)");
        EstadoEntrega expResult = EstadoEntrega.ACEITE;
        EstadoEntrega result = EntregaBLL.estadoDoFiltro("ACEITE");
        assertEquals(expResult, result);
    }

    @Test
    public void testFaturaParaFornecedor() {
        System.out.println("faturaParaFornecedor");
        Entrega expResult = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        int id = instance.carregar(fornecedorDoFicheiro(), expResult);
        Entrega result = instance.faturaParaFornecedor(fornecedorDoFicheiro(), id);
        assertEquals(expResult, result);
        assertEquals(5, result.getLinhas().size());
    }

    @Test
    public void testFaturaParaFornecedorDeOutroFornecedor() {
        System.out.println("faturaParaFornecedor (entrega de outro fornecedor)");
        int id = instance.carregar(fornecedorDoFicheiro(), instance.lerFicheiro(ficheiro("entrega-valida.xml")));
        Fornecedor outro = new Fornecedor(2, "99PT000", "Outro", null, null, null, null, null, null);
        try {
            instance.faturaParaFornecedor(outro, id);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Só pode ver as entregas do seu fornecedor.", expected.getMessage());
        }
    }

    @Test
    public void testFaturaParaFornecedorQueNaoExiste() {
        System.out.println("faturaParaFornecedor (entrega que não existe)");
        try {
            instance.faturaParaFornecedor(fornecedorDoFicheiro(), 99);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A entrega não existe.", expected.getMessage());
        }
    }

    @Test
    public void testFaturaParaFornecedorSemSessao() {
        System.out.println("faturaParaFornecedor (sem sessão)");
        try {
            instance.faturaParaFornecedor(null, 1);
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals("Entre na aplicação para ver as suas entregas.", expected.getMessage());
        }
    }

    @Test
    public void testFaturaParaHotel() {
        System.out.println("faturaParaHotel");
        Entrega expResult = instance.lerFicheiro(ficheiro("entrega-valida.xml"));
        int id = instance.carregar(fornecedorDoFicheiro(), expResult);
        Entrega result = instance.faturaParaHotel(RECECAO, id);
        assertEquals(expResult, result);
    }

    @Test
    public void testFaturaParaHotelQueNaoExiste() {
        System.out.println("faturaParaHotel (entrega que não existe)");
        try {
            instance.faturaParaHotel(RECECAO, 99);
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A entrega não existe.", expected.getMessage());
        }
    }

    @Test
    public void testFaturaParaHotelSemSessao() {
        System.out.println("faturaParaHotel (sem sessão)");
        try {
            instance.faturaParaHotel(null, 1);
            fail("Devia ter lançado IllegalStateException, mas não lançou");
        } catch (IllegalStateException expected) {
            assertEquals("Entre na aplicação para ver as entregas.", expected.getMessage());
        }
    }
}
