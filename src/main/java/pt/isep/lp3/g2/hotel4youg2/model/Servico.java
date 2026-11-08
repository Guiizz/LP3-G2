package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Serviço extra que o hotel oferece (por exemplo, massagem ou pequeno-almoço),
 * com o seu preço fixo.
 */
public class Servico {

    private int id;
    private String designacao;
    private BigDecimal preco;
    private Categoria categoria;
    private boolean ativo;

    /**
     * Cria um serviço.
     *
     * @param id         o id do serviço (0 se ainda não foi gravado)
     * @param designacao o nome do serviço
     * @param preco      o preço fixo, em euros
     * @param categoria  a categoria do serviço
     * @param ativo      {@code true} se o serviço pode ser adicionado às reservas
     */
    public Servico(int id, String designacao, BigDecimal preco, Categoria categoria, boolean ativo) {
        this.id = id;
        this.designacao = designacao;
        this.preco = preco;
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

    public String getNomeCategoria() {
        return categoria.getNome();
    }

    public String getPrecoTexto() {
        return preco.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " €";
    }

    public String getEstadoTexto() {
        return ativo ? "Ativo" : "Desativado";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Servico outro)) {
            return false;
        }
        return id == outro.id && ativo == outro.ativo && Objects.equals(designacao, outro.designacao) && Objects.equals(categoria, outro.categoria) && (preco == null ? outro.preco == null : outro.preco != null && preco.compareTo(outro.preco) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, designacao, categoria, ativo, preco == null ? null : preco.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return designacao + " (" + preco + " €)";
    }
}
