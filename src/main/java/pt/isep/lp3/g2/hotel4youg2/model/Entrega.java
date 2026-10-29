package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Entrega {

    private int id;
    private String numeroEncomenda;
    private LocalDate dataEncomenda;
    private Fornecedor fornecedor;
    private final List<LinhaEntrega> linhas = new ArrayList<>();

    public Entrega(int id, String numeroEncomenda, LocalDate dataEncomenda, Fornecedor fornecedor) {
        this.id = id;
        this.numeroEncomenda = numeroEncomenda;
        this.dataEncomenda = dataEncomenda;
        this.fornecedor = fornecedor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroEncomenda() {
        return numeroEncomenda;
    }

    public LocalDate getDataEncomenda() {
        return dataEncomenda;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public List<LinhaEntrega> getLinhas() {
        return linhas;
    }

    public void adicionarLinha(LinhaEntrega linha) {
        linhas.add(linha);
    }

    public BigDecimal getTotal() {
        BigDecimal soma = BigDecimal.ZERO;
        for (LinhaEntrega linha : linhas) {
            soma = soma.add(linha.getTotal());
        }
        return soma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Entrega outra)) {
            return false;
        }
        return Objects.equals(numeroEncomenda, outra.numeroEncomenda)
                && Objects.equals(dataEncomenda, outra.dataEncomenda)
                && Objects.equals(fornecedor, outra.fornecedor)
                && Objects.equals(linhas, outra.linhas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroEncomenda, dataEncomenda, fornecedor, linhas);
    }
}
