package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entrega de material enviada por um fornecedor, com as linhas que dão entrada em stock.
 * Vem de um ficheiro XML (US26).
 */
public class Entrega {

    private int id;
    private String numeroEncomenda;
    private LocalDate dataEncomenda;
    private Fornecedor fornecedor;
    private EstadoEntrega estado = EstadoEntrega.PENDENTE;
    private final List<LinhaEntrega> linhas = new ArrayList<>();

    /**
     * Cria uma entrega, ainda sem linhas.
     *
     * @param id              o id da entrega (0 se ainda não foi gravada)
     * @param numeroEncomenda o número da encomenda, dado pelo fornecedor
     * @param dataEncomenda   a data da encomenda
     * @param fornecedor      o fornecedor que a enviou
     */
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

    /**
     * @return o estado da entrega; uma entrega nova está pendente
     */
    public EstadoEntrega getEstado() {
        return estado;
    }

    public void setEstado(EstadoEntrega estado) {
        this.estado = estado;
    }

    public List<LinhaEntrega> getLinhas() {
        return linhas;
    }

    /**
     * Acrescenta uma linha à entrega.
     *
     * @param linha a linha a acrescentar
     */
    public void adicionarLinha(LinhaEntrega linha) {
        linhas.add(linha);
    }

    /**
     * Soma o total (com IVA) de todas as linhas.
     *
     * @return o total da entrega; zero se não tiver linhas
     */
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
