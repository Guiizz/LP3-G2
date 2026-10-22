package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Reserva {

    private int id;
    private int clienteId;
    private int quartoNumero;
    private LocalDate dataEntrada;
    private LocalDate dataSaida;
    private int numHospedes;
    private EstadoReserva estado;
    private BigDecimal precoTotal;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public int getQuartoNumero() {
        return quartoNumero;
    }

    public void setQuartoNumero(int quartoNumero) {
        this.quartoNumero = quartoNumero;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDate dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public LocalDate getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDate dataSaida) {
        this.dataSaida = dataSaida;
    }

    public int getNumHospedes() {
        return numHospedes;
    }

    public void setNumHospedes(int numHospedes) {
        this.numHospedes = numHospedes;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public BigDecimal getPrecoTotal() {
        return precoTotal;
    }

    public void setPrecoTotal(BigDecimal precoTotal) {
        this.precoTotal = precoTotal;
    }

    public Reserva(int id, int clienteId, int quartoNumero, LocalDate dataEntrada, LocalDate dataSaida, int numHospedes, EstadoReserva estado, BigDecimal precoTotal) {
        this.id = id;
        this.clienteId = clienteId;
        this.quartoNumero = quartoNumero;
        this.dataEntrada = dataEntrada;
        this.dataSaida = dataSaida;
        this.numHospedes = numHospedes;
        this.estado = estado;
        this.precoTotal = precoTotal;
    }
}
