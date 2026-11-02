package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Serviço extra que o hotel oferece (por exemplo, massagem ou pequeno-almoço),
 * com o seu preço e a forma como é cobrado.
 */
public class Servico {

    private int id;
    private String designacao;
    private BigDecimal preco;
    private TipoCobranca tipoCobranca;
    private Categoria categoria;
    private boolean ativo;

    /**
     * Cria um serviço.
     *
     * @param id           o id do serviço (0 se ainda não foi gravado)
     * @param designacao   o nome do serviço
     * @param preco        o preço, em euros
     * @param tipoCobranca como o serviço é cobrado
     * @param categoria    a categoria do serviço
     * @param ativo        {@code true} se o serviço pode ser adicionado às reservas
     */
    public Servico(int id, String designacao, BigDecimal preco, TipoCobranca tipoCobranca, Categoria categoria, boolean ativo) {
        this.id = id;
        this.designacao = designacao;
        this.preco = preco;
        this.tipoCobranca = tipoCobranca;
        this.categoria = categoria;
        this.ativo = ativo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesignacao() {
        return designacao;
    }

    public void setDesignacao(String designacao) {
        this.designacao = designacao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public TipoCobranca getTipoCobranca() {
        return tipoCobranca;
    }

    public void setTipoCobranca(TipoCobranca tipoCobranca) {
        this.tipoCobranca = tipoCobranca;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Servico outro)) {
            return false;
        }
        return id == outro.id && ativo == outro.ativo && Objects.equals(designacao, outro.designacao) && tipoCobranca == outro.tipoCobranca && Objects.equals(categoria, outro.categoria) && (preco == null ? outro.preco == null : outro.preco != null && preco.compareTo(outro.preco) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, designacao, tipoCobranca, categoria, ativo, preco == null ? null : preco.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return designacao + " (" + preco + " €, " + tipoCobranca + ")";
    }
}
