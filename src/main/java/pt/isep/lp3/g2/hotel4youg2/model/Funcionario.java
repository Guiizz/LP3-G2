package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDate;
import java.util.Enumeration;

public class Funcionario extends Pessoa{

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

    public Funcionario(int id, String nome, String cc, String nif, String morada, String telemovel, LocalDate dataNascimento, String email) {
        super(id, nome, cc, nif, morada, telemovel, dataNascimento, email);
        this.numeroFuncionario = numeroFuncionario;
        this.dataAdmissao = dataAdmissao;
    }
}
