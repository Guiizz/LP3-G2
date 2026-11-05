package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Reserva de um quarto por um cliente, entre duas datas.
 */
public class Reserva {

    private int id;
    private int clienteId;
    private int quartoNumero;
    private LocalDate dataEntrada;
    private LocalDate dataSaida;
    private int numHospedes;
    private EstadoReserva estado;
    private BigDecimal precoTotal;
    private LocalDateTime checkinEm;
    private LocalDateTime checkoutEm;
    // soma dos serviços extra juntos à reserva (US37); o precoTotal continua a ser só o quarto
    private BigDecimal totalServicos = BigDecimal.ZERO;

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

    public LocalDateTime getCheckinEm() {
        return checkinEm;
    }

    public void setCheckinEm(LocalDateTime checkinEm) {
        this.checkinEm = checkinEm;
    }

    public LocalDateTime getCheckoutEm() {
        return checkoutEm;
    }

    public void setCheckoutEm(LocalDateTime checkoutEm) {
        this.checkoutEm = checkoutEm;
    }

    /**
     * Cria uma reserva.
     *
     * @param id           o identificador da reserva
     * @param clienteId    o id do cliente que fez a reserva
     * @param quartoNumero o número do quarto reservado
     * @param dataEntrada  o dia de chegada
     * @param dataSaida    o dia de partida
     * @param numHospedes  o número de hóspedes
     * @param estado       o estado da reserva
     * @param precoTotal   o preço total da estadia
     * @param checkinEm    quando foi feito o check-in, ou {@code null} se ainda não foi
     * @param checkoutEm   quando foi feito o check-out, ou {@code null} se ainda não foi
     */
    public Reserva(int id, int clienteId, int quartoNumero, LocalDate dataEntrada, LocalDate dataSaida, int numHospedes, EstadoReserva estado, BigDecimal precoTotal, LocalDateTime checkinEm, LocalDateTime checkoutEm) {
        this.id = id;
        this.clienteId = clienteId;
        this.quartoNumero = quartoNumero;
        this.dataEntrada = dataEntrada;
        this.dataSaida = dataSaida;
        this.numHospedes = numHospedes;
        this.estado = estado;
        this.precoTotal = precoTotal;
        this.checkinEm = checkinEm;
        this.checkoutEm = checkoutEm;
    }

    public boolean podeSerCancelada(){
       return estado == EstadoReserva.CONFIRMADA && checkinEm == null;
    }

    public void cancelar() {
        if (!podeSerCancelada()) {
            throw new IllegalStateException("Só é possível cancelar reservas confirmadas e sem check-in");
        }
        estado = EstadoReserva.CANCELADA;
    }

    public boolean podeSerAlterada() {
        return estado == EstadoReserva.CONFIRMADA && checkinEm == null;
    }

    public boolean aceitaServicos() {
        return estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.EM_CURSO;
    }

    public BigDecimal getTotalServicos() {
        return totalServicos;
    }

    public void setTotalServicos(BigDecimal totalServicos) {
        this.totalServicos = totalServicos == null ? BigDecimal.ZERO : totalServicos;
    }

    public BigDecimal getTotal() {
        return (precoTotal == null ? BigDecimal.ZERO : precoTotal).add(totalServicos);
    }

    public long getNoites() {
        return ChronoUnit.DAYS.between(dataEntrada, dataSaida);
    }

    public void alterarDatas(LocalDate novaEntrada, LocalDate novaSaida) {
        if (!podeSerAlterada()) {
            throw new IllegalStateException("Só é possível alterar reservas confirmadas e sem check-in");
        }
        BigDecimal valorNoite = precoTotal.divide(BigDecimal.valueOf(getNoites()), 2, RoundingMode.HALF_UP);
        dataEntrada = novaEntrada;
        dataSaida = novaSaida;
        precoTotal = valorNoite.multiply(BigDecimal.valueOf(getNoites()));
    }

    /**
     * Duas reservas são iguais se tiverem os mesmos dados.
     * O preço compara-se pelo valor (100.0 e 100.00 contam como iguais).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reserva)) {
            return false;
        }
        Reserva outra = (Reserva) o;
        return id == outra.id
                && clienteId == outra.clienteId
                && quartoNumero == outra.quartoNumero
                && numHospedes == outra.numHospedes
                && Objects.equals(dataEntrada, outra.dataEntrada)
                && Objects.equals(dataSaida, outra.dataSaida)
                && estado == outra.estado
                && mesmoValor(precoTotal, outra.precoTotal)
                && Objects.equals(checkinEm, outra.checkinEm)
                && Objects.equals(checkoutEm, outra.checkoutEm);
    }

    private static boolean mesmoValor(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.compareTo(b) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clienteId, quartoNumero, numHospedes, dataEntrada, dataSaida, estado,
                checkinEm, checkoutEm);
    }

    @Override
    public String toString() {
        return "Reserva{id=" + id + ", cliente=" + clienteId + ", quarto=" + quartoNumero
                + ", " + dataEntrada + " a " + dataSaida + ", hospedes=" + numHospedes
                + ", " + estado + ", preco=" + precoTotal + "}";
    }
}
