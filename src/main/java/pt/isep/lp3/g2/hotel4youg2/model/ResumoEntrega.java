package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ResumoEntrega {

    private static final ZoneId FUSO = ZoneId.of("Europe/Lisbon");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss (VV)");

    private final String numeroEncomenda;
    private final int tiposDeMaterial;
    private final LocalDateTime carregadaEm;
    private final BigDecimal total;
    private final EstadoEntrega estado;

    public ResumoEntrega(String numeroEncomenda, int tiposDeMaterial, LocalDateTime carregadaEm, BigDecimal total, EstadoEntrega estado) {
        this.numeroEncomenda = numeroEncomenda;
        this.tiposDeMaterial = tiposDeMaterial;
        this.carregadaEm = carregadaEm;
        this.total = total;
        this.estado = estado;
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

    public String getCarregadaEmTexto() {
        return carregadaEm.atZone(FUSO).format(FORMATO);
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
