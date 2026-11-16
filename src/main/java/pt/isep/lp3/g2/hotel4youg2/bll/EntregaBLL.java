package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.EntregaDAL;
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

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio das entregas de fornecedores: ler e validar o ficheiro XML,
 * conferir as contas e registar a entrega (US26).
 */
public class EntregaBLL {

    /** Quantas entregas pendentes cada fornecedor pode ter ao mesmo tempo (US38). */
    public static final int MAX_PENDENTES = 5;

    /** Tamanho máximo do motivo da recusa, igual ao da coluna na base de dados (US49). */
    public static final int TAMANHO_MAXIMO_MOTIVO = 200;

    /** Diferença máxima (0,05 €) aceite nas contas de uma linha por causa de arredondamentos (US53). */
    public static final BigDecimal TOLERANCIA = new BigDecimal("0.05");

    /** Opção do filtro do histórico que mostra as entregas em todos os estados (US54). */
    public static final String TODAS = "Todas";

    private static final String ENTREGA_JA_DECIDIDA = "Esta entrega já foi decidida.";

    private final LeitorXmlEntrega leitor;
    private final IEntregaDAL entregaDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public EntregaBLL() {
        this(new LeitorXmlEntrega(), new EntregaDAL());
    }

    /**
     * Cria o BLL com outro leitor de XML e outro DAL.
     *
     * @param leitor     quem lê e valida o ficheiro XML
     * @param entregaDAL onde as entregas são gravadas
     */
    public EntregaBLL(LeitorXmlEntrega leitor, IEntregaDAL entregaDAL) {
        this.leitor = leitor;
        this.entregaDAL = entregaDAL;
    }

    /**
     * Importa o ficheiro XSD que passa a validar as entregas enquanto a aplicação estiver aberta (US53).
     * Se o ficheiro não for um XSD válido, é recusado e continua o XSD anterior.
     *
     * @param ficheiro o ficheiro XSD
     * @throws IllegalArgumentException se não houver ficheiro
     * @throws ErrosFicheiroException   se o ficheiro não for um XSD válido
     */
    public void importarXsd(File ficheiro) {
        if (ficheiro == null) {
            throw new IllegalArgumentException("Escolha um ficheiro XSD.");
        }
        LeitorXmlEntrega.importarXsd(ficheiro);
    }

    /**
     * @return o nome do XSD importado, ou {@code null} se estiver a ser usado o XSD do projeto
     */
    public String nomeXsdEmUso() {
        return LeitorXmlEntrega.getNomeXsdImportado();
    }

    /**
     * Lê o ficheiro XML da entrega e valida-o com o XSD e com as regras do negócio (US53):
     * data que existe, unidades, uma só moeda, quantidade maior que zero, preço não negativo e as contas
     * de cada linha. Junta todos os erros e só depois recusa. Diferenças de até 0,05 € nas contas são
     * arredondamentos: não recusam, ficam como avisos (ver {@link #verificarContas(Entrega)}).
     *
     * @param ficheiro o ficheiro XML da entrega
     * @return a entrega lida, sem erros
     * @throws IllegalArgumentException se não houver ficheiro
     * @throws ErrosFicheiroException   com todos os erros, se o ficheiro não for válido
     */
    public Entrega lerFicheiro(File ficheiro) {
        if (ficheiro == null) {
            throw new IllegalArgumentException("Escolha um ficheiro XML.");
        }
        List<String> erros = new ArrayList<>();
        Entrega entrega = leitor.ler(ficheiro, erros);
        return validar(entrega, erros);
    }

    /**
     * Como {@link #lerFicheiro(File)}, mas lê de um fluxo de dados já aberto.
     *
     * @param entrada o fluxo com o conteúdo do XML
     * @return a entrega lida, sem erros
     * @throws ErrosFicheiroException com todos os erros, se o ficheiro não for válido
     */
    public Entrega lerFicheiro(InputStream entrada) {
        List<String> erros = new ArrayList<>();
        Entrega entrega = leitor.ler(entrada, erros);
        return validar(entrega, erros);
    }

    // junta os erros das linhas aos que o leitor já encontrou; se houver algum, recusa com todos
    private static Entrega validar(Entrega entrega, List<String> erros) {
        for (LinhaEntrega linha : entrega.getLinhas()) {
            validarLinha(linha, erros);
        }
        if (!erros.isEmpty()) {
            throw new ErrosFicheiroException(erros);
        }
        return entrega;
    }

    private static void validarLinha(LinhaEntrega linha, List<String> erros) {
        String inicio = "Linha " + linha.getNumero() + ", campo ";
        if (linha.getQuantidade() <= 0) {
            erros.add(inicio + "Quantity: a quantidade tem de ser maior que zero.");
        }
        if (linha.getPrecoUnitario().signum() < 0) {
            erros.add(inicio + "PricePerUnit: o preço não pode ser negativo.");
        }
        BigDecimal semIva = linha.getPrecoUnitario().multiply(BigDecimal.valueOf(linha.getQuantidade()));
        if (muitoDiferente(semIva, linha.getValorSemIva())) {
            erros.add(inicio + "MonetaryAdjustmentStartAmount: a quantidade vezes o preço dá " + euros(semIva)
                    + ", mas o ficheiro diz " + euros(linha.getValorSemIva()) + ".");
        }
        BigDecimal iva = ivaEsperado(linha);
        if (muitoDiferente(iva, linha.getValorIva())) {
            erros.add(inicio + "TaxAmount: o IVA de " + linha.getTaxaIva() + "% devia ser " + euros(iva)
                    + ", mas o ficheiro diz " + euros(linha.getValorIva()) + ".");
        }
        BigDecimal total = linha.getValorSemIva().add(linha.getValorIva());
        if (muitoDiferente(total, linha.getTotal())) {
            erros.add(inicio + "LineBaseAmount: o total devia ser " + euros(total)
                    + ", mas o ficheiro diz " + euros(linha.getTotal()) + ".");
        }
    }

    // diferença maior que a tolerância dos arredondamentos
    private static boolean muitoDiferente(BigDecimal esperado, BigDecimal lido) {
        return esperado.subtract(lido).abs().compareTo(TOLERANCIA) > 0;
    }

    private static BigDecimal ivaEsperado(LinhaEntrega linha) {
        return linha.getValorSemIva().multiply(BigDecimal.valueOf(linha.getTaxaIva()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Confere as contas de cada linha (quantidade × preço, IVA e total). Depois de
     * {@link #lerFicheiro(File)}, só sobram diferenças pequenas (arredondamentos), que não impedem o envio:
     * ficam como avisos.
     *
     * @param entrega a entrega lida do ficheiro
     * @return um aviso por cada linha com contas erradas; lista vazia se estiver tudo certo
     */
    public List<String> verificarContas(Entrega entrega) {
        List<String> avisos = new ArrayList<>();
        for (LinhaEntrega linha : entrega.getLinhas()) {
            String inicio = "Linha " + linha.getNumero() + " (" + linha.getDescricao() + "): ";
            if (!linha.isValorSemIvaCorreto()) {
                avisos.add(inicio + "a quantidade vezes o preço dá "
                        + euros(linha.getPrecoUnitario().multiply(BigDecimal.valueOf(linha.getQuantidade())))
                        + ", mas o ficheiro diz " + euros(linha.getValorSemIva()) + ".");
            } else if (!linha.isIvaCorreto()) {
                avisos.add(inicio + "o IVA de " + linha.getTaxaIva() + "% devia ser "
                        + euros(ivaEsperado(linha))
                        + ", mas o ficheiro diz " + euros(linha.getValorIva()) + ".");
            } else if (!linha.isTotalCorreto()) {
                avisos.add(inicio + "o total devia ser " + euros(linha.getValorSemIva().add(linha.getValorIva()))
                        + ", mas o ficheiro diz " + euros(linha.getTotal()) + ".");
            }
        }
        return avisos;
    }

    /**
     * Regista a entrega na base de dados, no nome de quem a está a registar.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param entrega a entrega lida e confirmada
     * @return o id da entrega gravada
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se não houver entrega ou se esta já tiver sido registada
     *                                  para o mesmo fornecedor
     */
    public int registar(Utilizador quemAge, Entrega entrega) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para registar entregas.");
        }
        if (entrega == null) {
            throw new IllegalArgumentException("Carregue primeiro um ficheiro XML válido.");
        }
        if (entregaDAL.existeEntrega(entrega.getFornecedor().getCodigo(), entrega.getNumeroEncomenda())) {
            throw new IllegalArgumentException("A entrega " + entrega.getNumeroEncomenda() + " do fornecedor "
                    + entrega.getFornecedor().getNome() + " já foi registada.");
        }
        return entregaDAL.registar(entrega, quemAge.getFuncionarioId());
    }

    /**
     * Grava a entrega carregada pelo fornecedor como pendente, à espera da decisão do hotel.
     * Não altera o stock. O ficheiro tem de ser do fornecedor que tem a sessão iniciada.
     *
     * @param fornecedor o fornecedor com a sessão iniciada
     * @param entrega    a entrega lida do ficheiro XML
     * @return o id da entrega gravada
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se não houver entrega, se o ficheiro for de outro fornecedor,
     *                                  se a entrega já foi carregada ou se o fornecedor já tem
     *                                  {@value #MAX_PENDENTES} entregas pendentes
     */
    public int carregar(Fornecedor fornecedor, Entrega entrega) {
        if (fornecedor == null) {
            throw new IllegalStateException("Entre na aplicação para carregar entregas.");
        }
        if (entrega == null) {
            throw new IllegalArgumentException("Carregue primeiro um ficheiro XML válido.");
        }
        String codigo = entrega.getFornecedor().getCodigo();
        if (!codigo.equals(fornecedor.getCodigo())) {
            throw new IllegalArgumentException("O ficheiro é do fornecedor " + codigo
                    + ". Só pode carregar entregas do seu fornecedor.");
        }
        if (entregaDAL.existeEntrega(codigo, entrega.getNumeroEncomenda())) {
            throw new IllegalArgumentException("A entrega " + entrega.getNumeroEncomenda() + " já foi carregada.");
        }
        if (entregaDAL.contarPendentes(codigo) >= MAX_PENDENTES) {
            throw new IllegalArgumentException("Tem " + MAX_PENDENTES
                    + " entregas por aprovar. Aguarde a decisão do hotel.");
        }
        return entregaDAL.carregar(entrega);
    }

    /**
     * Aceita uma entrega pendente: passa a aceite, fica registado quem a aceitou e quando, e a
     * quantidade de cada material entra no stock.
     *
     * @param quemAge   o utilizador com a sessão iniciada (gestor ou rececionista)
     * @param entregaId o id da entrega
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se a entrega não existir ou se já tiver sido decidida
     */
    public void aceitar(Utilizador quemAge, int entregaId) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para decidir entregas.");
        }
        exigirPendente(entregaId);
        if (!entregaDAL.aceitar(entregaId, quemAge.getFuncionarioId())) {
            throw new IllegalArgumentException(ENTREGA_JA_DECIDIDA);
        }
    }

    /**
     * Recusa uma entrega pendente. O motivo é opcional; o fornecedor pode voltar a enviar a entrega.
     * O stock não é alterado.
     *
     * @param quemAge   o utilizador com a sessão iniciada (gestor ou rececionista)
     * @param entregaId o id da entrega
     * @param motivo    o motivo da recusa (pode estar vazio ou ser {@code null})
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se a entrega não existir, se já tiver sido decidida ou se o motivo
     *                                  tiver mais de {@value #TAMANHO_MAXIMO_MOTIVO} caracteres
     */
    public void recusar(Utilizador quemAge, int entregaId, String motivo) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para decidir entregas.");
        }
        String motivoLimpo = limparMotivo(motivo);
        if (motivoLimpo != null && motivoLimpo.length() > TAMANHO_MAXIMO_MOTIVO) {
            throw new IllegalArgumentException(
                    "O motivo pode ter no máximo " + TAMANHO_MAXIMO_MOTIVO + " caracteres.");
        }
        exigirPendente(entregaId);
        if (!entregaDAL.recusar(entregaId, quemAge.getFuncionarioId(), motivoLimpo)) {
            throw new IllegalArgumentException(ENTREGA_JA_DECIDIDA);
        }
    }

    /**
     * Devolve todas as entregas para o pessoal do hotel, com as pendentes primeiro e, nas já decididas,
     * quem decidiu, quando e o motivo da recusa.
     *
     * @param quemAge o utilizador com a sessão iniciada (gestor ou rececionista)
     * @return o resumo das entregas de todos os fornecedores
     * @throws IllegalStateException se não houver sessão iniciada
     */
    public List<ResumoEntregaHotel> listarParaHotel(Utilizador quemAge) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para ver as entregas.");
        }
        return entregaDAL.listarParaHotel();
    }

    // sem texto, só com espaços ou só com símbolos (por exemplo "...") o motivo fica vazio
    private static String limparMotivo(String motivo) {
        if (motivo == null || !temLetraOuDigito(motivo)) {
            return null;
        }
        return motivo.trim();
    }

    private static boolean temLetraOuDigito(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (Character.isLetterOrDigit(texto.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    // confirma que a entrega existe e ainda está pendente, para dar uma mensagem clara antes de decidir
    private void exigirPendente(int entregaId) {
        Optional<Entrega> entrega = entregaDAL.procurarPorId(entregaId);
        if (entrega.isEmpty()) {
            throw new IllegalArgumentException("A entrega não existe.");
        }
        if (entrega.get().getEstado() != EstadoEntrega.PENDENTE) {
            throw new IllegalArgumentException(ENTREGA_JA_DECIDIDA);
        }
    }

    private static String euros(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " €";
    }

    /**
     * Devolve o histórico das entregas do fornecedor, com o estado de cada uma.
     * Cada fornecedor só vê as suas.
     *
     * @param fornecedor o fornecedor com a sessão iniciada
     * @return o resumo das entregas do fornecedor, da mais recente para a mais antiga
     * @throws IllegalStateException se não houver sessão iniciada
     */
    public List<ResumoEntrega> historico(Fornecedor fornecedor) {
        if (fornecedor == null) {
            throw new IllegalStateException("Entre na aplicação para ver as suas entregas.");
        }
        return entregaDAL.listarResumosDoFornecedor(fornecedor.getCodigo());
    }

    /**
     * Devolve o histórico das entregas do fornecedor, só com as do estado escolhido (US54).
     *
     * @param fornecedor o fornecedor com a sessão iniciada
     * @param estado     o estado a mostrar, ou {@code null} para mostrar todas
     * @return o resumo das entregas do fornecedor nesse estado, da mais recente para a mais antiga
     * @throws IllegalStateException se não houver sessão iniciada
     */
    public List<ResumoEntrega> historico(Fornecedor fornecedor, EstadoEntrega estado) {
        List<ResumoEntrega> todas = historico(fornecedor);
        if (estado == null) {
            return todas;
        }
        List<ResumoEntrega> escolhidas = new ArrayList<>();
        for (ResumoEntrega resumo : todas) {
            if (resumo.getEstado() == estado) {
                escolhidas.add(resumo);
            }
        }
        return escolhidas;
    }

    /**
     * Devolve as entregas para o pessoal do hotel, só com as do estado escolhido (US54).
     *
     * @param quemAge o utilizador com a sessão iniciada (gestor ou rececionista)
     * @param estado  o estado a mostrar, ou {@code null} para mostrar todas
     * @return o resumo das entregas nesse estado
     * @throws IllegalStateException se não houver sessão iniciada
     */
    public List<ResumoEntregaHotel> listarParaHotel(Utilizador quemAge, EstadoEntrega estado) {
        List<ResumoEntregaHotel> todas = listarParaHotel(quemAge);
        if (estado == null) {
            return todas;
        }
        List<ResumoEntregaHotel> escolhidas = new ArrayList<>();
        for (ResumoEntregaHotel resumo : todas) {
            if (resumo.getEstado() == estado) {
                escolhidas.add(resumo);
            }
        }
        return escolhidas;
    }

    /**
     * Passa a opção escolhida no filtro do histórico para o estado da entrega (US54).
     *
     * @param opcao {@value #TODAS} ou o nome de um estado (PENDENTE, ACEITE ou RECUSADA)
     * @return o estado, ou {@code null} se a opção for {@value #TODAS} ou não houver opção
     */
    public static EstadoEntrega estadoDoFiltro(String opcao) {
        if (opcao == null || opcao.equals(TODAS)) {
            return null;
        }
        return EstadoEntrega.valueOf(opcao);
    }

    /**
     * Devolve a entrega completa, com as linhas, para o fornecedor a ver como uma fatura (US54).
     * Cada fornecedor só abre as suas entregas.
     *
     * @param fornecedor o fornecedor com a sessão iniciada
     * @param entregaId  o id da entrega
     * @return a entrega com as linhas
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se a entrega não existir ou for de outro fornecedor
     */
    public Entrega faturaParaFornecedor(Fornecedor fornecedor, int entregaId) {
        if (fornecedor == null) {
            throw new IllegalStateException("Entre na aplicação para ver as suas entregas.");
        }
        Entrega entrega = procurarEntrega(entregaId);
        if (!entrega.getFornecedor().getCodigo().equals(fornecedor.getCodigo())) {
            throw new IllegalArgumentException("Só pode ver as entregas do seu fornecedor.");
        }
        return entrega;
    }

    /**
     * Devolve a entrega completa, com as linhas, para o pessoal do hotel a ver como uma fatura (US54).
     *
     * @param quemAge   o utilizador com a sessão iniciada (gestor ou rececionista)
     * @param entregaId o id da entrega
     * @return a entrega com as linhas
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se a entrega não existir
     */
    public Entrega faturaParaHotel(Utilizador quemAge, int entregaId) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para ver as entregas.");
        }
        return procurarEntrega(entregaId);
    }

    private Entrega procurarEntrega(int entregaId) {
        Optional<Entrega> entrega = entregaDAL.procurarPorId(entregaId);
        if (entrega.isEmpty()) {
            throw new IllegalArgumentException("A entrega não existe.");
        }
        return entrega.get();
    }
}
