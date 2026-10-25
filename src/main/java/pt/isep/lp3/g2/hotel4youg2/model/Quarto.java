package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;

public class Quarto {

    private int numero;
    private int piso;
    private TipoQuarto tipo;
    private Vista vista;
    private int capacidade;
    private Tarifa tarifa;

    public Quarto(int numero, int piso, TipoQuarto tipo, Vista vista, int capacidade) {
        this.numero = numero;
        this.piso = piso;
        this.tipo = tipo;
        this.vista = vista;
        this.capacidade = capacidade;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getPiso() {
        return piso;
    }

    public void setPiso(int piso) {
        this.piso = piso;
    }

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

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public void setTarifa(Tarifa tarifa) {
        this.tarifa = tarifa;
    }

    public BigDecimal getValorNoite() {
        return tarifa != null ? tarifa.getValorNoite() : null;
    }
}
