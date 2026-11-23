package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Um valor das estatísticas do hotel, com o seu rótulo (ex.: o mês ou o tipo de quarto).
 */
public class Estatistica {
    private final String rotulo;
    private final BigDecimal valor;

    /**
     * Cria um valor das estatísticas.
     *
     * @param rotulo o rótulo (ex.: 11/2026)
     * @param valor  o valor, ou {@code null} para 0
     */
    public Estatistica(String rotulo, BigDecimal valor) {
        this.rotulo = rotulo;
        this.valor = valor == null ? BigDecimal.ZERO : valor;
    }

    public String getRotulo() {
        return rotulo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Estatistica outra)) {
            return false;
        }
        return Objects.equals(rotulo, outra.rotulo) && valor.compareTo(outra.valor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rotulo, valor.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return rotulo + "=" + valor;
    }
}
