package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoEntrega;
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
    private String moeda = "EUR";
    private BigDecimal totalRegistado;
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

    /**
     * @return a moeda dos valores da entrega (código de 3 letras, por exemplo "EUR")
     */
    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }

    /**
     * @return o total guardado na base de dados quando a entrega foi gravada, ou {@code null} se a entrega
     * ainda não foi gravada (acabou de ser lida do ficheiro)
     */
    public BigDecimal getTotalRegistado() {
        return totalRegistado;
    }

    public void setTotalRegistado(BigDecimal totalRegistado) {
        this.totalRegistado = totalRegistado;
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

    /**
     * Soma o valor sem IVA de todas as linhas (US54).
     *
     * @return o total sem IVA; zero se não tiver linhas
     */
    public BigDecimal getTotalSemIva() {
        BigDecimal soma = BigDecimal.ZERO;
        for (LinhaEntrega linha : linhas) {
            soma = soma.add(linha.getValorSemIva());
        }
        return soma;
    }

    /**
     * Soma o valor do IVA de todas as linhas (US54).
     *
     * @return o total do IVA; zero se não tiver linhas
     */
    public BigDecimal getTotalIva() {
        BigDecimal soma = BigDecimal.ZERO;
        for (LinhaEntrega linha : linhas) {
            soma = soma.add(linha.getValorIva());
        }
        return soma;
    }

    /**
     * Confere o total guardado com a soma das linhas (US54). Uma entrega acabada de ler do ficheiro
     * ainda não tem total guardado, por isso está certa.
     *
     * @return {@code true} se não houver total guardado ou se este bater certo (até 1 cêntimo) com a soma das linhas
     */
    public boolean isTotalCerto() {
        if (totalRegistado == null) {
            return true;
        }
        return getTotal().subtract(totalRegistado).abs().compareTo(new BigDecimal("0.01")) <= 0;
    }

    /**
     * @return {@code true} se o total bater certo e todas as linhas tiverem as contas certas (US54)
     */
    public boolean isContasCertas() {
        if (!isTotalCerto()) {
            return false;
        }
        for (LinhaEntrega linha : linhas) {
            if (!linha.isContasCertas()) {
                return false;
            }
        }
        return true;
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
