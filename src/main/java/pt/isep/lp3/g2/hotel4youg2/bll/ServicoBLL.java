package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.CategoriaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ICategoriaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ServicoDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Categoria;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.TipoCobranca;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.math.BigDecimal;
import java.util.List;

/**
 * Regras de negócio dos serviços extra e das suas categorias.
 * Só o gestor os cria, edita, desativa e reativa (US25).
 */
public class ServicoBLL {

    private static final int TAMANHO_MAXIMO_DESIGNACAO = 100;
    private static final int TAMANHO_MAXIMO_CATEGORIA = 50;
    private static final BigDecimal PRECO_MAXIMO = new BigDecimal("999999.99");

    private final IServicoDAL servicoDAL;
    private final ICategoriaDAL categoriaDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public ServicoBLL() {
        this(new ServicoDAL(), new CategoriaDAL());
    }

    /**
     * Cria o BLL com outros DAL.
     *
     * @param servicoDAL   onde os serviços são gravados
     * @param categoriaDAL onde as categorias são gravadas
     */
    public ServicoBLL(IServicoDAL servicoDAL, ICategoriaDAL categoriaDAL) {
        this.servicoDAL = servicoDAL;
        this.categoriaDAL = categoriaDAL;
    }

    /**
     * @return todos os serviços, ativos e desativados
     */
    public List<Servico> listar() {
        return servicoDAL.listar();
    }

    /**
     * @return todas as categorias, ordenadas pelo nome
     */
    public List<Categoria> listarCategorias() {
        return categoriaDAL.listar();
    }

    /**
     * Cria uma categoria nova. Tira os espaços a mais ao nome.
     *
     * @param quemAge o utilizador com a sessão iniciada (tem de ser gestor)
     * @param nome    o nome da categoria (até 50 caracteres, sem repetir)
     * @return a categoria criada
     * @throws IllegalStateException    se quem age não for gestor
     * @throws IllegalArgumentException se o nome estiver vazio, for longo demais ou já existir
     */
    public Categoria criarCategoria(Utilizador quemAge, String nome) {
        exigirGestor(quemAge);
        String limpo = limpar(nome);
        if (limpo.isEmpty()) {
            throw new IllegalArgumentException("Escreva o nome da categoria.");
        }
        if (limpo.length() > TAMANHO_MAXIMO_CATEGORIA) {
            throw new IllegalArgumentException("O nome da categoria não pode ter mais de " + TAMANHO_MAXIMO_CATEGORIA + " caracteres.");
        }
        if (categoriaDAL.existeNome(limpo)) {
            throw new IllegalArgumentException("Já existe a categoria \"" + limpo + "\".");
        }
        Categoria categoria = new Categoria(0, limpo);
        categoriaDAL.inserir(categoria);
        return categoria;
    }

    /**
     * Cria um serviço novo, ativo.
     *
     * @param quemAge      o utilizador com a sessão iniciada (tem de ser gestor)
     * @param designacao   o nome do serviço (até 100 caracteres)
     * @param preco        o preço (maior que zero, com no máximo 2 casas decimais, até 999999,99 €)
     * @param tipoCobranca como o serviço é cobrado
     * @param categoria    a categoria do serviço
     * @return o serviço criado
     * @throws IllegalStateException    se quem age não for gestor
     * @throws IllegalArgumentException se algum dado não for válido ou se já existir um serviço
     *                                  com esta designação e esta forma de cobrança
     */
    public Servico criar(Utilizador quemAge, String designacao, BigDecimal preco, TipoCobranca tipoCobranca, Categoria categoria) {
        exigirGestor(quemAge);
        String nome = validarDados(designacao, preco, tipoCobranca, categoria, 0);
        Servico servico = new Servico(0, nome, preco, tipoCobranca, categoria, true);
        servicoDAL.inserir(servico);
        return servico;
    }

    /**
     * Altera os dados de um serviço. O serviço continua ativo ou desativado, como estava.
     *
     * @param quemAge      o utilizador com a sessão iniciada (tem de ser gestor)
     * @param id           o id do serviço a alterar
     * @param designacao   o novo nome do serviço
     * @param preco        o novo preço
     * @param tipoCobranca a nova forma de cobrança
     * @param categoria    a nova categoria
     * @return o serviço com os dados novos
     * @throws IllegalStateException    se quem age não for gestor
     * @throws IllegalArgumentException se o serviço não existir, algum dado não for válido
     *                                  ou já existir outro serviço com esta designação e esta cobrança
     */
    public Servico editar(Utilizador quemAge, int id, String designacao, BigDecimal preco, TipoCobranca tipoCobranca, Categoria categoria) {
        exigirGestor(quemAge);
        Servico atual = procurar(id);
        String nome = validarDados(designacao, preco, tipoCobranca, categoria, id);
        Servico alterado = new Servico(id, nome, preco, tipoCobranca, categoria, atual.isAtivo());
        if (!servicoDAL.atualizar(alterado)) {
            throw new IllegalArgumentException("O serviço já não existe.");
        }
        return alterado;
    }

    /**
     * Desativa um serviço: deixa de poder ser adicionado às reservas, mas não é apagado.
     *
     * @param quemAge o utilizador com a sessão iniciada (tem de ser gestor)
     * @param id      o id do serviço
     * @throws IllegalStateException    se quem age não for gestor
     * @throws IllegalArgumentException se o serviço não existir
     */
    public void desativar(Utilizador quemAge, int id) {
        alterarEstado(quemAge, id, false);
    }

    /**
     * Volta a ativar um serviço desativado.
     *
     * @param quemAge o utilizador com a sessão iniciada (tem de ser gestor)
     * @param id      o id do serviço
     * @throws IllegalStateException    se quem age não for gestor
     * @throws IllegalArgumentException se o serviço não existir
     */
    public void reativar(Utilizador quemAge, int id) {
        alterarEstado(quemAge, id, true);
    }

    /**
     * Converte o texto escrito no ecrã num preço. Aceita vírgula ou ponto como separador decimal.
     *
     * @param texto o texto do preço (por exemplo, {@code "12,50"})
     * @return o preço
     * @throws IllegalArgumentException se o texto estiver vazio ou não for um número
     */
    public static BigDecimal lerPreco(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Escreva o preço.");
        }
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Escreva um preço válido (por exemplo, 12,50).");
        }
    }

    private void alterarEstado(Utilizador quemAge, int id, boolean ativo) {
        exigirGestor(quemAge);
        procurar(id);
        if (!servicoDAL.atualizarAtivo(id, ativo)) {
            throw new IllegalArgumentException("O serviço já não existe.");
        }
    }

    private String validarDados(String designacao, BigDecimal preco, TipoCobranca tipoCobranca, Categoria categoria, int idIgnorar) {
        String nome = limpar(designacao);
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("Escreva a designação do serviço.");
        }
        if (nome.length() > TAMANHO_MAXIMO_DESIGNACAO) {
            throw new IllegalArgumentException("A designação não pode ter mais de " + TAMANHO_MAXIMO_DESIGNACAO + " caracteres.");
        }
        if (preco == null) {
            throw new IllegalArgumentException("Escreva o preço.");
        }
        if (preco.signum() <= 0) {
            throw new IllegalArgumentException("O preço tem de ser maior que zero.");
        }
        if (preco.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("O preço só pode ter 2 casas decimais.");
        }
        if (preco.compareTo(PRECO_MAXIMO) > 0) {
            throw new IllegalArgumentException("O preço não pode ser maior que 999999,99 €.");
        }
        if (tipoCobranca == null) {
            throw new IllegalArgumentException("Escolha o tipo de cobrança.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("Escolha a categoria.");
        }
        if (servicoDAL.existeDesignacao(nome, tipoCobranca, idIgnorar)) {
            throw new IllegalArgumentException("Já existe o serviço \"" + nome + "\" com a cobrança \"" + tipoCobranca.getDescricao().toLowerCase() + "\". Escolha outra forma de cobrança ou " + "edite o serviço que já existe.");
        }
        return nome;
    }

    private static String limpar(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }

    private Servico procurar(int id) {
        return servicoDAL.procurarPorId(id).orElseThrow(() -> new IllegalArgumentException("Escolha um serviço da lista."));
    }

    private static void exigirGestor(Utilizador quemAge) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode gerir os serviços.");
        }
    }
}
