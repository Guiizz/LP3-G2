package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.StockDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IStockDAL;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.MovimentoStock;
import pt.isep.lp3.g2.hotel4youg2.model.SaidaStock;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.MotivoSaida;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoMovimento;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio do stock de materiais (US42, US62 e US77): consultar o que existe, ver o histórico
 * de entradas e saídas de cada material, registar as saídas com o tipo e o motivo e devolver as temporárias.
 * O stock aumenta quando uma entrega é aceite (US49) ou uma saída temporária volta, e diminui com as saídas.
 */
public class StockBLL {

    /** Tamanho máximo do texto do motivo, igual ao da coluna na base de dados (US77). */
    public static final int TAMANHO_MAXIMO_MOTIVO = 200;

    private final IStockDAL stockDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public StockBLL() {
        this(new StockDAL());
    }

    /**
     * Cria o BLL com outro DAL (usado nos testes).
     *
     * @param stockDAL onde estão o stock e os movimentos
     */
    public StockBLL(IStockDAL stockDAL) {
        this.stockDAL = stockDAL;
    }

    /**
     * Lista o stock de todos os materiais.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @return cada material com o fornecedor e a quantidade em stock
     * @throws IllegalStateException se não houver sessão iniciada
     */
    public List<MaterialStock> listar(Utilizador quemAge) {
        exigirSessao(quemAge);
        return stockDAL.listar();
    }

    /**
     * Procura materiais pelo código, pela descrição ou pelo fornecedor, sem distinguir maiúsculas (US62).
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param termo   o texto a procurar; vazio ou {@code null} mostra todos
     * @return os materiais encontrados, com o que entrou, o que saiu e o stock efetivo
     * @throws IllegalStateException se não houver sessão iniciada
     */
    public List<MaterialStock> pesquisar(Utilizador quemAge, String termo) {
        exigirSessaoParaConsultar(quemAge);
        String procurado = termo == null ? "" : termo.trim().toLowerCase();
        List<MaterialStock> encontrados = new ArrayList<>();
        for (MaterialStock material : stockDAL.listar()) {
            String texto = (material.getCodigo() + " " + material.getDescricao() + " " + material.getFornecedor())
                    .toLowerCase();
            if (texto.contains(procurado)) {
                encontrados.add(material);
            }
        }
        return encontrados;
    }

    /**
     * Devolve o histórico de entradas e saídas de um material, do mais recente para o mais antigo (US62).
     *
     * @param quemAge  o utilizador com a sessão iniciada
     * @param material o material escolhido na tabela
     * @param tipo     só as entradas ou só as saídas, ou {@code null} para todos os movimentos
     * @return os movimentos do material, com a data, a quantidade e o funcionário
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se não for escolhido nenhum material
     */
    public List<MovimentoStock> historico(Utilizador quemAge, MaterialStock material, TipoMovimento tipo) {
        exigirSessaoParaConsultar(quemAge);
        if (material == null) {
            throw new IllegalArgumentException("Escolha o material na tabela.");
        }
        List<MovimentoStock> movimentos = new ArrayList<>();
        for (MovimentoStock movimento : stockDAL.listarMovimentos(material.getMaterialId())) {
            if (tipo == null || movimento.getTipo() == tipo) {
                movimentos.add(movimento);
            }
        }
        return movimentos;
    }

    /**
     * Regista a saída de um material do stock, com o tipo e o motivo (US42 e US77). Fica guardado o funcionário
     * que a registou. Não é possível tirar mais do que existe.
     * <ul>
     *   <li>Temporária: o material volta mais tarde (ver {@link #devolver(Utilizador, SaidaStock)}); o texto do
     *   motivo é opcional (por exemplo "Reparação") e não há motivo da lista.</li>
     *   <li>Definitiva: é obrigatório escolher o motivo; no "Outro" é obrigatório escrevê-lo.</li>
     * </ul>
     *
     * @param quemAge         o utilizador com a sessão iniciada
     * @param material        o material escolhido na tabela
     * @param quantidade      a quantidade, como foi escrita no ecrã (número inteiro maior que zero)
     * @param tipo            temporária ou definitiva
     * @param motivo          o motivo da saída definitiva (ignorado numa temporária)
     * @param descricaoMotivo o texto do motivo, como foi escrito no ecrã (pode estar vazio)
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se faltar o material, o tipo ou o motivo, se a quantidade não for válida,
     *                                  se o texto do motivo for comprido demais ou se a quantidade for maior do
     *                                  que o que existe em stock
     */
    public void registarSaida(Utilizador quemAge, MaterialStock material, String quantidade, TipoSaida tipo,
                              MotivoSaida motivo, String descricaoMotivo) {
        exigirSessao(quemAge);
        if (material == null) {
            throw new IllegalArgumentException("Escolha o material na tabela.");
        }
        int unidades = lerQuantidade(quantidade);
        if (tipo == null) {
            throw new IllegalArgumentException("Escolha o tipo de saída: temporária ou definitiva.");
        }
        String texto = limparTexto(descricaoMotivo);
        if (texto != null && texto.length() > TAMANHO_MAXIMO_MOTIVO) {
            throw new IllegalArgumentException("O motivo pode ter no máximo " + TAMANHO_MAXIMO_MOTIVO + " caracteres.");
        }
        MotivoSaida motivoGuardado = null;
        if (tipo == TipoSaida.DEFINITIVA) {
            if (motivo == null) {
                throw new IllegalArgumentException("Escolha o motivo da saída definitiva.");
            }
            if (motivo == MotivoSaida.OUTRO && texto == null) {
                throw new IllegalArgumentException("Escreva o motivo da saída.");
            }
            motivoGuardado = motivo;
        }
        if (!stockDAL.registarSaida(material.getMaterialId(), unidades, quemAge.getFuncionarioId(), tipo,
                motivoGuardado, texto)) {
            throw new IllegalArgumentException("Não é possível retirar mais do que existe em stock.");
        }
    }

    /**
     * Lista as saídas temporárias que ainda não voltaram ao stock (US77).
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @return as saídas por devolver, da mais antiga para a mais recente
     * @throws IllegalStateException se não houver sessão iniciada
     */
    public List<SaidaStock> listarPorDevolver(Utilizador quemAge) {
        exigirSessaoParaConsultar(quemAge);
        return stockDAL.listarPorDevolver();
    }

    /**
     * Dá uma saída temporária como devolvida: o material volta ao stock e fica guardado quem o recebeu (US77).
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param saida   a saída escolhida na tabela
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se não houver saída escolhida ou se já tiver sido devolvida
     */
    public void devolver(Utilizador quemAge, SaidaStock saida) {
        exigirSessao(quemAge);
        if (saida == null) {
            throw new IllegalArgumentException("Escolha a saída na tabela.");
        }
        if (!stockDAL.devolver(saida.getId(), quemAge.getFuncionarioId())) {
            throw new IllegalArgumentException("Esta saída já foi devolvida.");
        }
    }

    // sem texto ou só com espaços fica null
    private static String limparTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private int lerQuantidade(String quantidade) {
        String texto = quantidade == null ? "" : quantidade.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Indique a quantidade.");
        }
        if (!texto.matches("\\d{1,9}") || Integer.parseInt(texto) == 0) {
            throw new IllegalArgumentException("A quantidade tem de ser um número inteiro maior que zero.");
        }
        return Integer.parseInt(texto);
    }

    private void exigirSessaoParaConsultar(Utilizador quemAge) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para consultar o stock.");
        }
    }

    private void exigirSessao(Utilizador quemAge) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para registar saídas de stock.");
        }
    }
}
