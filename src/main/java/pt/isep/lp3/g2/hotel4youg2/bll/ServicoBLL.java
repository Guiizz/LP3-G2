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

public class ServicoBLL {

    private static final int TAMANHO_MAXIMO_DESIGNACAO = 100;
    private static final int TAMANHO_MAXIMO_CATEGORIA = 50;
    private static final BigDecimal PRECO_MAXIMO = new BigDecimal("999999.99");

    private final IServicoDAL servicoDAL;
    private final ICategoriaDAL categoriaDAL;

    public ServicoBLL() {
        this(new ServicoDAL(), new CategoriaDAL());
    }

    public ServicoBLL(IServicoDAL servicoDAL, ICategoriaDAL categoriaDAL) {
        this.servicoDAL = servicoDAL;
        this.categoriaDAL = categoriaDAL;
    }

    public List<Servico> listar() {
        return servicoDAL.listar();
    }

    public List<Categoria> listarCategorias() {
        return categoriaDAL.listar();
    }

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

    public Servico criar(Utilizador quemAge, String designacao, BigDecimal preco, TipoCobranca tipoCobranca, Categoria categoria) {
        exigirGestor(quemAge);
        String nome = validarDados(designacao, preco, tipoCobranca, categoria, 0);
        Servico servico = new Servico(0, nome, preco, tipoCobranca, categoria, true);
        servicoDAL.inserir(servico);
        return servico;
    }

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

    public void desativar(Utilizador quemAge, int id) {
        alterarEstado(quemAge, id, false);
    }

    public void reativar(Utilizador quemAge, int id) {
        alterarEstado(quemAge, id, true);
    }

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
