package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Resumo de uma entrega para o histórico do fornecedor: a encomenda, quantos tipos de material tem,
 * quando foi carregada, o total e o estado em que está.
 */
public class ResumoEntrega {

    private static final ZoneId FUSO = ZoneId.of("Europe/Lisbon");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss (VV)");

    private final String numeroEncomenda;
    private final int tiposDeMaterial;
    private final LocalDateTime carregadaEm;
    private final BigDecimal total;
    private final EstadoEntrega estado;
    private final String motivoRecusa;

    /**
     * Cria o resumo de uma entrega, sem motivo de recusa.
     *
     * @param numeroEncomenda o número da encomenda
     * @param tiposDeMaterial quantos materiais diferentes a entrega tem
     * @param carregadaEm     quando a entrega foi carregada
     * @param total           o total da entrega, em euros
     * @param estado          o estado da entrega (pendente, aceite ou recusada)
     */
    public ResumoEntrega(String numeroEncomenda, int tiposDeMaterial, LocalDateTime carregadaEm, BigDecimal total, EstadoEntrega estado) {
        this(numeroEncomenda, tiposDeMaterial, carregadaEm, total, estado, null);
    }

    /**
     * Cria o resumo de uma entrega, com o motivo da recusa.
     *
     * @param numeroEncomenda o número da encomenda
     * @param tiposDeMaterial quantos materiais diferentes a entrega tem
     * @param carregadaEm     quando a entrega foi carregada
     * @param total           o total da entrega, em euros
     * @param estado          o estado da entrega (pendente, aceite ou recusada)
     * @param motivoRecusa    o motivo escrito por quem recusou, ou {@code null} se não houve
     */
    public ResumoEntrega(String numeroEncomenda, int tiposDeMaterial, LocalDateTime carregadaEm, BigDecimal total,
                         EstadoEntrega estado, String motivoRecusa) {
        this.numeroEncomenda = numeroEncomenda;
        this.tiposDeMaterial = tiposDeMaterial;
        this.carregadaEm = carregadaEm;
        this.total = total;
        this.estado = estado;
        this.motivoRecusa = motivoRecusa;
    }

    public String getNumeroEncomenda() {
        return numeroEncomenda;
    }

    public int getTiposDeMaterial() {
        return tiposDeMaterial;
    }

    public LocalDateTime getCarregadaEm() {
        return carregadaEm;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public EstadoEntrega getEstado() {
        return estado;
    }

    /**
     * @return o motivo da recusa, ou {@code null} se a entrega não foi recusada ou não tem motivo
     */
    public String getMotivoRecusa() {
        return motivoRecusa;
    }

    /**
     * Escreve a data em que a entrega foi carregada, até ao segundo e com o fuso horário,
     * para mostrar no ecrã.
     *
     * @return a data e hora, por exemplo {@code 05/11/2026 21:24:38 (Europe/Lisbon)}
     */
    public String getCarregadaEmTexto() {
        return escreverData(carregadaEm);
    }

    // escreve uma data e hora no formato dos ecrãs, até ao segundo e com o fuso horário
    static String escreverData(LocalDateTime data) {
        return data.atZone(FUSO).format(FORMATO);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResumoEntrega outro)) {
            return false;
        }
        return tiposDeMaterial == outro.tiposDeMaterial
                && Objects.equals(numeroEncomenda, outro.numeroEncomenda)
                && Objects.equals(carregadaEm, outro.carregadaEm)
                && Objects.equals(total, outro.total)
                && estado == outro.estado;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroEncomenda, tiposDeMaterial, carregadaEm, total, estado);
    }
}
