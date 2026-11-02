package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;

/**
 * Quarto do hotel. O preço por noite vem da tarifa associada ao seu tipo e à sua vista.
 */
public class Quarto {

    private int numero;
    private int piso;
    private TipoQuarto tipo;
    private Vista vista;
    private int capacidade;
    private Tarifa tarifa;
    private boolean ativo = true;

    /**
     * Cria um quarto, ainda sem tarifa associada.
     *
     * @param numero     o número do quarto (ex.: 201)
     * @param piso       o piso onde fica
     * @param tipo       o tipo de quarto
     * @param vista      a vista do quarto
     * @param capacidade o número máximo de hóspedes
     */
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

    /**
     * Devolve o preço de uma noite neste quarto.
     *
     * @return o valor da tarifa, ou {@code null} se o quarto ainda não tiver tarifa
     */
    public BigDecimal getValorNoite() {
        return tarifa != null ? tarifa.getValorNoite() : null;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getEstado() {
        return ativo ? "Ativo" : "Desativado";
    }
}
