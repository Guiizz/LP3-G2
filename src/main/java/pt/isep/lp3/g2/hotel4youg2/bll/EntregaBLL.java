package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.EntregaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IEntregaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.LeitorXmlEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio das entregas de fornecedores: ler e validar o ficheiro XML,
 * conferir as contas e registar a entrega (US26).
 */
public class EntregaBLL {

    /** Quantas entregas pendentes cada fornecedor pode ter ao mesmo tempo (US38). */
    public static final int MAX_PENDENTES = 5;

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
     * Lê o ficheiro XML da entrega. Recusa linhas com quantidade zero ou preço negativo,
     * que o XSD aceita mas a base de dados não.
     *
     * @param ficheiro o ficheiro XML da entrega
     * @return a entrega lida
     * @throws IllegalArgumentException se não houver ficheiro, o XML não for válido
     *                                  ou alguma linha tiver quantidade ou preço inválidos
     */
    public Entrega lerFicheiro(File ficheiro) {
        if (ficheiro == null) {
            throw new IllegalArgumentException("Escolha um ficheiro XML.");
        }
        return validarLinhas(leitor.ler(ficheiro));
    }

    /**
     * Como {@link #lerFicheiro(File)}, mas lê de um fluxo de dados já aberto.
     *
     * @param entrada o fluxo com o conteúdo do XML
     * @return a entrega lida
     * @throws IllegalArgumentException se o XML não for válido
     *                                  ou alguma linha tiver quantidade ou preço inválidos
     */
    public Entrega lerFicheiro(InputStream entrada) {
        return validarLinhas(leitor.ler(entrada));
    }

    // o XSD aceita quantidade 0 e preços negativos, mas a BD não: recusa logo ao ler, com uma mensagem clara
    private static Entrega validarLinhas(Entrega entrega) {
        for (LinhaEntrega linha : entrega.getLinhas()) {
            String inicio = "Linha " + linha.getNumero() + " (" + linha.getDescricao() + "): ";
            if (linha.getQuantidade() <= 0) {
                throw new IllegalArgumentException(inicio + "a quantidade tem de ser maior que zero.");
            }
            if (linha.getPrecoUnitario().signum() < 0) {
                throw new IllegalArgumentException(inicio + "o preço não pode ser negativo.");
            }
        }
        return entrega;
    }

    /**
     * Confere as contas de cada linha (quantidade × preço, IVA e total).
     * As contas erradas não impedem o registo: ficam como avisos para quem regista decidir.
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
                        + euros(linha.getValorSemIva().multiply(BigDecimal.valueOf(linha.getTaxaIva()))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
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

    private static String euros(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " €";
    }

    public List<ResumoEntrega> historico(Fornecedor fornecedor) {
        if (fornecedor == null) {
            throw new IllegalStateException("Entre na aplicação para ver as suas entregas.");
        }
        return entregaDAL.listarResumosDoFornecedor(fornecedor.getCodigo());
    }
}
