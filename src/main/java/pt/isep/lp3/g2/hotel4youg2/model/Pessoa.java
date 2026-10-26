package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDate;

/**
 * Dados comuns a todas as pessoas do hotel (clientes e funcionários).
 * O NIF é único: não pode haver duas pessoas com o mesmo NIF.
 */
public abstract class Pessoa {
    private int id;
    private String nome;
    private String cc;
    private String nif;
    private String morada;
    private String telemovel;
    private LocalDate dataNascimento;
    private String email;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public String getTelemovel() {
        return telemovel;
    }

    public void setTelemovel(String telemovel) {
        this.telemovel = telemovel;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @param id             o identificador (0 se ainda não estiver gravada)
     * @param nome           o nome completo
     * @param cc             o número do cartão de cidadão
     * @param nif            o NIF, com 9 dígitos
     * @param morada         a morada
     * @param telemovel      o telemóvel
     * @param dataNascimento a data de nascimento
     * @param email          o email
     */
    protected Pessoa(int id, String nome, String cc, String nif, String morada, String telemovel, LocalDate dataNascimento, String email) {
        this.id = id;
        this.nome = nome;
        this.cc = cc;
        this.nif = nif;
        this.morada = morada;
        this.telemovel = telemovel;
        this.dataNascimento = dataNascimento;
        this.email = email;
    }
}
