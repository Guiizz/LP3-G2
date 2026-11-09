package pt.isep.lp3.g2.hotel4youg2.model;

import java.util.Objects;

/**
 * Fornecedor do hotel, identificado pelo código que vem no ficheiro XML das entregas.
 */
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
    private String passwordHash;
    private String passwordSalt;

    /**
     * Cria um fornecedor.
     *
     * @param id           o id do fornecedor (0 se ainda não foi gravado)
     * @param codigo       o código do fornecedor no ficheiro XML
     * @param nome         o nome do fornecedor
     * @param morada1      a primeira linha da morada
     * @param morada2      a segunda linha da morada
     * @param cidade       a cidade
     * @param codigoPostal o código postal
     * @param pais         o nome do país
     * @param codigoPais   o código ISO do país
     */
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

    /**
     * @return o hash da password do fornecedor, ou {@code null} se ainda não tem conta
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * @return o salt da password do fornecedor, ou {@code null} se ainda não tem conta
     */
    public String getPasswordSalt() {
        return passwordSalt;
    }

    /**
     * Guarda a password do fornecedor (nunca em claro: só o hash e o salt).
     *
     * @param passwordHash o hash da password, em hexadecimal
     * @param passwordSalt o salt usado, em hexadecimal
     */
    public void definirPassword(String passwordHash, String passwordSalt) {
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    /**
     * Junta a morada num só texto, para mostrar no ecrã.
     *
     * @return a morada completa (ruas, código postal, cidade e país)
     */
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
