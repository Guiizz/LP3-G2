package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Preço por noite de um quarto, conforme o seu tipo e a sua vista.
 * Existe uma tarifa para cada combinação de tipo e vista.
 */
public class Tarifa {

    private TipoQuarto tipo;
    private Vista vista;
    private BigDecimal valorNoite;

    public TipoQuarto getTipo() {
        return tipo;
    }

    public void setTipo(TipoQuarto tipo) {
        this.tipo = tipo;
    }

    public Vista getVista() {
        return vista;
    }

    public void setVista(Vista vista) {
        this.vista = vista;
    }

    public BigDecimal getValorNoite() {
        return valorNoite;
    }

    public void setValorNoite(BigDecimal valorNoite) {
        this.valorNoite = valorNoite;
    }

    /**
     * Cria uma tarifa.
     *
     * @param tipo       o tipo de quarto
     * @param vista      a vista do quarto
     * @param valorNoite o preço de uma noite, em euros
     */
    public Tarifa(TipoQuarto tipo, Vista vista, BigDecimal valorNoite) {
        this.tipo = tipo;
        this.vista = vista;
        this.valorNoite = valorNoite;
    }

    /**
     * Duas tarifas são iguais se tiverem o mesmo tipo, a mesma vista e o mesmo preço.
     * O preço compara-se pelo valor (100.0 e 100.00 contam como iguais).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tarifa)) {
            return false;
        }
        Tarifa outra = (Tarifa) o;
        return tipo == outra.tipo
                && vista == outra.vista
                && mesmoValor(valorNoite, outra.valorNoite);
    }

    private static boolean mesmoValor(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.compareTo(b) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, vista);
    }

    @Override
    public String toString() {
        return "Tarifa{" + tipo + ", " + vista + ", " + valorNoite + "}";
    }
}
