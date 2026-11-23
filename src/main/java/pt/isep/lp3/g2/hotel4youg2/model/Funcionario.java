package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Funcionário do hotel. Pode ter uma conta ({@link Utilizador}) para entrar na aplicação.
 */
public class Funcionario extends Pessoa {

    /** Letra com que começa o cartão de um funcionário, seguida do número de funcionário (US52). */
    public static final String PREFIXO_CARTAO = "F";

    /** Letra do cartão dos funcionários da restauração (ex.: R3). */
    public static final String PREFIXO_CARTAO_RESTAURACAO = "R";

    private int numeroFuncionario;
    private Perfil perfil;
    private String empresa;
    private LocalDate dataAdmissao;

    public int getNumeroFuncionario() {
        return numeroFuncionario;
    }

    /**
     * @return o número do cartão do funcionário, por exemplo {@code "F12"} (US52)
     */
    public String getCartao() {
        return (isRestauracao() ? PREFIXO_CARTAO_RESTAURACAO : PREFIXO_CARTAO) + numeroFuncionario;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    /**
     * @return {@code true} se o funcionário for da restauração
     */
    public boolean isRestauracao() {
        return perfil == Perfil.RESTAURACAO;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
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

    /**
     * Dois funcionários são iguais se tiverem os mesmos dados de pessoa, o mesmo número e a mesma data de admissão.
     */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        Funcionario outro = (Funcionario) o;
        return numeroFuncionario == outro.numeroFuncionario
                && Objects.equals(dataAdmissao, outro.dataAdmissao)
                && Objects.equals(empresa, outro.empresa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), numeroFuncionario, dataAdmissao, empresa);
    }

    @Override
    public String toString() {
        return getCartao() + " - " + getNome();
    }
}
