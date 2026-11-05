package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Linha de uma entrega: um material, a quantidade e os valores com e sem IVA,
 * tal como vêm no ficheiro XML do fornecedor.
 */
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

    /**
     * Cria uma linha de entrega.
     *
     * @param numero         o número da linha na entrega
     * @param codigoProduto  o código do material, dado pelo fornecedor
     * @param descricao      a descrição do material
     * @param quantidade     a quantidade entregue
     * @param unidade        a unidade em que vem
     * @param precoUnitario  o preço de cada unidade, sem IVA
     * @param valorSemIva    o valor da linha sem IVA
     * @param taxaIva        a taxa de IVA, em percentagem (por exemplo, 23)
     * @param valorIva       o valor do IVA
     * @param total          o valor da linha com IVA
     */
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

    public String getQuantidadeTexto() {
        return quantidade + " " + unidade.getDescricao();
    }

    public String getPrecoTexto() {
        return euros(precoUnitario);
    }

    public String getIvaTexto() {
        return taxaIva + "% (" + euros(valorIva) + ")";
    }

    public String getTotalTexto() {
        return euros(total);
    }

    private static String euros(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " €";
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

    /**
     * Confere se a quantidade vezes o preço unitário dá o valor sem IVA
     * (aceita uma diferença até 0,01 €).
     *
     * @return {@code true} se a conta estiver certa
     */
    public boolean isValorSemIvaCorreto() {
        return quaseIgual(precoUnitario.multiply(BigDecimal.valueOf(quantidade)), valorSemIva);
    }

    /**
     * Confere se o valor sem IVA vezes a taxa dá o valor do IVA
     * (aceita uma diferença até 0,01 €).
     *
     * @return {@code true} se a conta estiver certa
     */
    public boolean isIvaCorreto() {
        BigDecimal esperado = valorSemIva.multiply(BigDecimal.valueOf(taxaIva))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return quaseIgual(esperado, valorIva);
    }

    /**
     * Confere se o valor sem IVA mais o IVA dá o total
     * (aceita uma diferença até 0,01 €).
     *
     * @return {@code true} se a conta estiver certa
     */
    public boolean isTotalCorreto() {
        return quaseIgual(valorSemIva.add(valorIva), total);
    }

    /**
     * Confere as três contas da linha de uma só vez.
     *
     * @return {@code true} só se as três contas estiverem certas
     */
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
