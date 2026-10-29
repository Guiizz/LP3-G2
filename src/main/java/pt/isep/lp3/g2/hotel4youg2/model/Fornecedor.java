package pt.isep.lp3.g2.hotel4youg2.model;

import java.util.Objects;

public class Fornecedor {

    private int id;
    private String codigo;
    private String nome;
    private String morada1;
    private String morada2;
    private String cidade;
    private String codigoPostal;
    private String pais;
    private String codigoPais;

    public Fornecedor(int id, String codigo, String nome, String morada1, String morada2, String cidade,
                      String codigoPostal, String pais, String codigoPais) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.morada1 = morada1;
        this.morada2 = morada2;
        this.cidade = cidade;
        this.codigoPostal = codigoPostal;
        this.pais = pais;
        this.codigoPais = codigoPais;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getMorada1() {
        return morada1;
    }

    public String getMorada2() {
        return morada2;
    }

    public String getCidade() {
        return cidade;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public String getPais() {
        return pais;
    }

    public String getCodigoPais() {
        return codigoPais;
    }

    public String getMoradaCompleta() {
        return morada1 + " " + morada2 + ", " + codigoPostal + " " + cidade + " (" + pais + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Fornecedor outro)) {
            return false;
        }
        return Objects.equals(codigo, outro.codigo) && Objects.equals(nome, outro.nome)
                && Objects.equals(morada1, outro.morada1) && Objects.equals(morada2, outro.morada2)
                && Objects.equals(cidade, outro.cidade) && Objects.equals(codigoPostal, outro.codigoPostal)
                && Objects.equals(pais, outro.pais) && Objects.equals(codigoPais, outro.codigoPais);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo, nome, morada1, morada2, cidade, codigoPostal, pais, codigoPais);
    }

    @Override
    public String toString() {
        return nome + " (" + codigo + ")";
    }
}
