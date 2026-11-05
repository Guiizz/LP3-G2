package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private String cartao;
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

    public String getCartao() {
        return cartao;
    }

    public void setCartao(String cartao) {
        this.cartao = cartao;
    }

    public void fazerCheckin(LocalDateTime agora, LocalTime horaMinima) {
        if (estado == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("A reserva " + id + " foi cancelada e não pode fazer check-in");
        }
        if (checkinEm != null || estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("A reserva " + id + " já tem o check-in feito");
        }
        if (!agora.toLocalDate().equals(dataEntrada)) {
            throw new IllegalStateException("O check-in só pode ser feito no dia de entrada ("
                    + dataEntrada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
        }
        if (agora.toLocalTime().isBefore(horaMinima)) {
            throw new IllegalStateException("O check-in só é possível a partir das " + horaMinima);
        }
        estado = EstadoReserva.EM_CURSO;
        checkinEm = agora;
        cartao = "C" + quartoNumero;
    }

    public boolean cartaoDaAcesso() {
        return estado == EstadoReserva.EM_CURSO;
    }

    public void exigirPodeFazerCheckout() {
        if (estado != EstadoReserva.EM_CURSO) {
            throw new IllegalStateException("Só é possível fazer check-out de uma reserva em curso (a reserva "
                    + id + " está " + estado + ")");
        }
    }

    public void fazerCheckout(LocalDateTime agora) {
        exigirPodeFazerCheckout();
        estado = EstadoReserva.CONCLUIDA;
        checkoutEm = agora;
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
                && Objects.equals(checkoutEm, outra.checkoutEm)
                && Objects.equals(cartao, outra.cartao);
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
                checkinEm, checkoutEm, cartao);
    }

    @Override
    public String toString() {
        return "Reserva{id=" + id + ", cliente=" + clienteId + ", quarto=" + quartoNumero
                + ", " + dataEntrada + " a " + dataSaida + ", hospedes=" + numHospedes
                + ", " + estado + ", preco=" + precoTotal + "}";
    }
}
