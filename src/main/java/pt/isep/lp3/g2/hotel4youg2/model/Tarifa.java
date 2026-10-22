package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;

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

    public Tarifa(TipoQuarto tipo, Vista vista, BigDecimal valorNoite) {
        this.tipo = tipo;
        this.vista = vista;
        this.valorNoite = valorNoite;
    }
}
