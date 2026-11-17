package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import java.util.Objects;

/**
 * Um material na configuração de um tipo de quarto: o que esse tipo de quarto leva por defeito
 * e quantas unidades (US75). Por exemplo, um quarto duplo leva 2 toalhas de banho.
 */
public class MaterialQuarto {

    private final TipoQuarto tipoQuarto;
    private final Material material;
    private final int quantidade;

    /**
     * Cria uma linha da configuração de um tipo de quarto.
     *
     * @param tipoQuarto o tipo de quarto
     * @param material   o material
     * @param quantidade quantas unidades leva
     */
    public MaterialQuarto(TipoQuarto tipoQuarto, Material material, int quantidade) {
        this.tipoQuarto = tipoQuarto;
        this.material = material;
        this.quantidade = quantidade;
    }

    public TipoQuarto getTipoQuarto() {
        return tipoQuarto;
    }

    public Material getMaterial() {
        return material;
    }

    public int getQuantidade() {
        return quantidade;
    }

    /**
     * @return o id do material
     */
    public int getMaterialId() {
        return material.getId();
    }

    /**
     * @return o código do material, para as tabelas
     */
    public String getCodigo() {
        return material.getCodigo();
    }

    /**
     * @return a descrição do material, para as tabelas
     */
    public String getDescricao() {
        return material.getDescricao();
    }

    /**
     * @return o fornecedor do material, para as tabelas
     */
    public String getFornecedor() {
        return material.getFornecedor();
    }

    /**
     * @return "Consumível" ou "Não consumível", para as tabelas
     */
    public String getConsumivelTexto() {
        return material.getConsumivelTexto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MaterialQuarto outro)) {
            return false;
        }
        return quantidade == outro.quantidade && tipoQuarto == outro.tipoQuarto
                && Objects.equals(material, outro.material);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoQuarto, material, quantidade);
    }

    @Override
    public String toString() {
        return quantidade + " × " + material.getDescricao();
    }
}
