package pt.isep.lp3.g2.hotel4youg2.model;

import java.util.Objects;

/**
 * Material de um fornecedor, que pode ser consumível (champô, gel de banho), e sai do stock de vez,
 * ou não consumível (televisão, secador, cofre), e volta ao stock (US75).
 */
public class Material {

    private final int id;
    private final String codigo;
    private final String descricao;
    private final String fornecedor;
    private boolean consumivel;

    /**
     * Cria um material.
     *
     * @param id         o id do material
     * @param codigo     o código dado pelo fornecedor
     * @param descricao  a descrição do material
     * @param fornecedor o nome do fornecedor
     * @param consumivel {@code true} se for consumível
     */
    public Material(int id, String codigo, String descricao, String fornecedor, boolean consumivel) {
        this.id = id;
        this.codigo = codigo;
        this.descricao = descricao;
        this.fornecedor = fornecedor;
        this.consumivel = consumivel;
    }

    public int getId() {
        return id;
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

    public boolean isConsumivel() {
        return consumivel;
    }

    public void setConsumivel(boolean consumivel) {
        this.consumivel = consumivel;
    }

    /**
     * @return "Consumível" ou "Não consumível", para as tabelas
     */
    public String getConsumivelTexto() {
        return consumivel ? "Consumível" : "Não consumível";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Material outro)) {
            return false;
        }
        return id == outro.id && consumivel == outro.consumivel && Objects.equals(codigo, outro.codigo)
                && Objects.equals(descricao, outro.descricao) && Objects.equals(fornecedor, outro.fornecedor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codigo, descricao, fornecedor, consumivel);
    }

    @Override
    public String toString() {
        return descricao + " (" + fornecedor + ")";
    }
}
