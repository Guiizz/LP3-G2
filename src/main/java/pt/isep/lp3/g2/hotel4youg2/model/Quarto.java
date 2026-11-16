package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;
import java.math.BigDecimal;
import java.util.Objects;

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

    /**
     * Dois quartos são iguais se tiverem os mesmos dados (incluindo a tarifa e o estado).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Quarto)) {
            return false;
        }
        Quarto outro = (Quarto) o;
        return numero == outro.numero
                && piso == outro.piso
                && capacidade == outro.capacidade
                && ativo == outro.ativo
                && tipo == outro.tipo
                && vista == outro.vista
                && Objects.equals(tarifa, outro.tarifa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero, piso, tipo, vista, capacidade, ativo);
    }

    @Override
    public String toString() {
        return "Quarto{" + numero + ", piso " + piso + ", " + tipo + ", " + vista + ", capacidade " + capacidade
                + ", " + getEstado() + ", " + tarifa + "}";
    }
}
