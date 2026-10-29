package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class LinhaEntrega {

    private static final BigDecimal TOLERANCIA = new BigDecimal("0.01");

    private int numero;
    private String codigoProduto;
    private String descricao;
    private int quantidade;
    private Unidade unidade;
    private BigDecimal precoUnitario;
    private BigDecimal valorSemIva;
    private int taxaIva;
    private BigDecimal valorIva;
    private BigDecimal total;

    public LinhaEntrega(int numero, String codigoProduto, String descricao, int quantidade, Unidade unidade,
                        BigDecimal precoUnitario, BigDecimal valorSemIva, int taxaIva, BigDecimal valorIva,
                        BigDecimal total) {
        this.numero = numero;
        this.codigoProduto = codigoProduto;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.precoUnitario = precoUnitario;
        this.valorSemIva = valorSemIva;
        this.taxaIva = taxaIva;
        this.valorIva = valorIva;
        this.total = total;
    }

    public int getNumero() {
        return numero;
    }

    public String getCodigoProduto() {
        return codigoProduto;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getValorSemIva() {
        return valorSemIva;
    }

    public int getTaxaIva() {
        return taxaIva;
    }

    public BigDecimal getValorIva() {
        return valorIva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    // quantidade × preço = valor sem IVA
    public boolean isValorSemIvaCorreto() {
        return quaseIgual(precoUnitario.multiply(BigDecimal.valueOf(quantidade)), valorSemIva);
    }

    // valor sem IVA × taxa = IVA
    public boolean isIvaCorreto() {
        BigDecimal esperado = valorSemIva.multiply(BigDecimal.valueOf(taxaIva))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return quaseIgual(esperado, valorIva);
    }

    // valor sem IVA + IVA = total
    public boolean isTotalCorreto() {
        return quaseIgual(valorSemIva.add(valorIva), total);
    }

    public boolean isContasCertas() {
        return isValorSemIvaCorreto() && isIvaCorreto() && isTotalCorreto();
    }

    private static boolean quaseIgual(BigDecimal a, BigDecimal b) {
        return a.subtract(b).abs().compareTo(TOLERANCIA) <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LinhaEntrega outra)) {
            return false;
        }
        return numero == outra.numero && quantidade == outra.quantidade && taxaIva == outra.taxaIva
                && Objects.equals(codigoProduto, outra.codigoProduto) && Objects.equals(descricao, outra.descricao)
                && unidade == outra.unidade && iguais(precoUnitario, outra.precoUnitario)
                && iguais(valorSemIva, outra.valorSemIva) && iguais(valorIva, outra.valorIva)
                && iguais(total, outra.total);
    }

    private static boolean iguais(BigDecimal a, BigDecimal b) {
        return a == null ? b == null : b != null && a.compareTo(b) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero, codigoProduto, descricao, quantidade, unidade, taxaIva);
    }
}
