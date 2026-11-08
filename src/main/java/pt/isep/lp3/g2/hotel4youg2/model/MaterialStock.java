package pt.isep.lp3.g2.hotel4youg2.model;

import java.util.Objects;

public class MaterialStock {

    private final int materialId;
    private final String codigo;
    private final String descricao;
    private final String fornecedor;
    private final int quantidade;

    public MaterialStock(int materialId, String codigo, String descricao, String fornecedor, int quantidade) {
        this.materialId = materialId;
        this.codigo = codigo;
        this.descricao = descricao;
        this.fornecedor = fornecedor;
        this.quantidade = quantidade;
    }

    public int getMaterialId() {
        return materialId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public int getQuantidade() {
        return quantidade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaterialStock outro = (MaterialStock) o;
        return materialId == outro.materialId && quantidade == outro.quantidade
                && Objects.equals(codigo, outro.codigo) && Objects.equals(descricao, outro.descricao)
                && Objects.equals(fornecedor, outro.fornecedor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(materialId, codigo, descricao, fornecedor, quantidade);
    }

    @Override
    public String toString() {
        return descricao + " (" + fornecedor + "): " + quantidade;
    }
}
