package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDate;

/**
 * Funcionário do hotel. Pode ter uma conta ({@link Utilizador}) para entrar na aplicação.
 */
public class Funcionario extends Pessoa {

    private int numeroFuncionario;
    private LocalDate dataAdmissao;

    public int getNumeroFuncionario() {
        return numeroFuncionario;
    }

    public void setNumeroFuncionario(int numeroFuncionario) {
        this.numeroFuncionario = numeroFuncionario;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    /**
     * Cria um funcionário.
     *
     * @param id                identificador da pessoa na base de dados (0 se ainda não foi gravada)
     * @param nome              nome completo
     * @param cc                número do cartão de cidadão (opcional)
     * @param nif               número de identificação fiscal
     * @param morada            morada (opcional)
     * @param telemovel         número de telemóvel
     * @param dataNascimento    data de nascimento
     * @param email             endereço de email
     * @param numeroFuncionario número interno do funcionário
     * @param dataAdmissao      data de admissão no hotel
     */
    public Funcionario(int id, String nome, String cc, String nif, String morada, String telemovel,
                       LocalDate dataNascimento, String email, int numeroFuncionario, LocalDate dataAdmissao) {
        super(id, nome, cc, nif, morada, telemovel, dataNascimento, email);
        this.numeroFuncionario = numeroFuncionario;
        this.dataAdmissao = dataAdmissao;
    }
}
