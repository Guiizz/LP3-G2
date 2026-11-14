package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;
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
    private boolean earlyCheckin;
    private boolean lateCheckout;
    private BigDecimal taxaHorario = BigDecimal.ZERO;
    private String nomeCliente;
    private String nifCliente;

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

    /**
     * @return {@code true} se a reserva estiver confirmada e ainda sem check-in
     */
    public boolean podeSerCancelada(){
       return estado == EstadoReserva.CONFIRMADA && checkinEm == null;
    }

    /**
     * Cancela a reserva.
     *
     * @throws IllegalStateException se a reserva já não estiver confirmada ou já tiver check-in
     */
    public void cancelar() {
        if (!podeSerCancelada()) {
            throw new IllegalStateException("Só é possível cancelar reservas confirmadas e sem check-in");
        }
        estado = EstadoReserva.CANCELADA;
    }

    /**
     * @return {@code true} se a reserva estiver confirmada e ainda sem check-in (US30)
     */
    public boolean podeSerAlterada() {
        return estado == EstadoReserva.CONFIRMADA && checkinEm == null;
    }

    /**
     * @return {@code true} se ainda se puderem juntar ou retirar serviços extra: reserva confirmada
     *         ou em curso (US37)
     */
    public boolean aceitaServicos() {
        return estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.EM_CURSO;
    }

    public BigDecimal getTotalServicos() {
        return totalServicos;
    }

    public void setTotalServicos(BigDecimal totalServicos) {
        this.totalServicos = totalServicos == null ? BigDecimal.ZERO : totalServicos;
    }

    public boolean isEarlyCheckin() {
        return earlyCheckin;
    }

    public void setEarlyCheckin(boolean earlyCheckin) {
        this.earlyCheckin = earlyCheckin;
    }

    public boolean isLateCheckout() {
        return lateCheckout;
    }

    public void setLateCheckout(boolean lateCheckout) {
        this.lateCheckout = lateCheckout;
    }

    public BigDecimal getTaxaHorario() {
        return taxaHorario;
    }

    public void setTaxaHorario(BigDecimal taxaHorario) {
        this.taxaHorario = taxaHorario == null ? BigDecimal.ZERO : taxaHorario;
    }

    /**
     * @return o total da reserva: o alojamento, os serviços extra e a taxa de early check-in
     *         e de late check-out
     */
    public BigDecimal getTotal() {
        return (precoTotal == null ? BigDecimal.ZERO : precoTotal).add(totalServicos).add(taxaHorario);
    }

    public String getCartao() {
        return cartao;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getNifCliente() {
        return nifCliente;
    }

    public void setNifCliente(String nifCliente) {
        this.nifCliente = nifCliente;
    }

    public String getCheckinEmTexto() {
        return checkinEm == null ? "" : checkinEm.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    public String getEstadoTexto() {
        return estado == null ? "" : estado.getDescricao();
    }

    public String getCheckoutEmTexto() {
        return checkoutEm == null ? "" : checkoutEm.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    public String getAvisoAlteracoes() {
        if (podeSerAlterada()) {
            return "";
        }
        if (aceitaServicos()) {
            return "Com o check-in feito, as datas já não se alteram.";
        }
        return "Reserva " + getEstadoTexto().toLowerCase() + ": já não se alteram as datas nem os serviços.";
    }

    public void setCartao(String cartao) {
        this.cartao = cartao;
    }

    /**
     * Faz o check-in (US35): a reserva passa a em curso, guarda a hora e o cliente recebe o cartão
     * {@code "C" + número do quarto}.
     *
     * @param agora      a data e hora do check-in
     * @param horaMinima a hora a partir da qual o check-in é permitido (a standard ou a de early check-in)
     * @throws IllegalStateException se a reserva estiver cancelada, já tiver check-in, não for o dia
     *                               de entrada ou ainda for cedo demais
     */
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

    /**
     * @return {@code true} se o cartão da reserva ainda abrir o quarto, ou seja, se a reserva estiver em curso
     */
    public boolean cartaoDaAcesso() {
        return estado == EstadoReserva.EM_CURSO;
    }

    /**
     * Garante que a reserva pode fazer check-out (US36).
     *
     * @throws IllegalStateException se a reserva não estiver em curso
     */
    public void exigirPodeFazerCheckout() {
        if (estado != EstadoReserva.EM_CURSO) {
            throw new IllegalStateException("Só é possível fazer check-out de uma reserva em curso (a reserva "
                    + id + " está " + estado + ")");
        }
    }

    /**
     * Faz o check-out (US36): a reserva passa a concluída e guarda a hora. O cartão deixa de dar acesso.
     *
     * @param agora a data e hora do check-out
     * @throws IllegalStateException se a reserva não estiver em curso
     */
    public void fazerCheckout(LocalDateTime agora) {
        exigirPodeFazerCheckout();
        estado = EstadoReserva.CONCLUIDA;
        checkoutEm = agora;
    }

    public long getNoites() {
        return ChronoUnit.DAYS.between(dataEntrada, dataSaida);
    }

    /**
     * Muda as datas da reserva e recalcula o preço com o mesmo valor por noite (US30).
     *
     * @param novaEntrada a data de entrada nova
     * @param novaSaida   a data de saída nova
     * @throws IllegalStateException se a reserva já não estiver confirmada ou já tiver check-in
     */
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
                && Objects.equals(cartao, outra.cartao)
                && earlyCheckin == outra.earlyCheckin
                && lateCheckout == outra.lateCheckout
                && mesmoValor(taxaHorario, outra.taxaHorario);
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
                checkinEm, checkoutEm, cartao, earlyCheckin, lateCheckout);
    }

    @Override
    public String toString() {
        return "Reserva{id=" + id + ", cliente=" + clienteId + ", quarto=" + quartoNumero
                + ", " + dataEntrada + " a " + dataSaida + ", hospedes=" + numHospedes
                + ", " + estado + ", preco=" + precoTotal + "}";
    }
}
