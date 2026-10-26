package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;

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
}
