package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resumo de uma entrega para o pessoal do hotel: além do que o fornecedor vê, mostra quem
 * decidiu, quando e o motivo da recusa (US49).
 */
public class ResumoEntregaHotel {

    private final int entregaId;
    private final String numeroEncomenda;
    private final String fornecedor;
    private final int tiposDeMaterial;
    private final int totalUnidades;
    private final LocalDateTime carregadaEm;
    private final BigDecimal total;
    private final EstadoEntrega estado;
    private final String decididaPor;
    private final LocalDateTime decididaEm;
    private final String motivoRecusa;

    /**
     * Cria o resumo de uma entrega para o hotel.
     *
     * @param entregaId       o id da entrega
     * @param numeroEncomenda o número da encomenda
     * @param fornecedor      o nome do fornecedor
     * @param tiposDeMaterial quantos materiais diferentes a entrega tem
     * @param totalUnidades   o total de unidades da entrega, que é o que entra no stock se for aceite
     * @param carregadaEm     quando a entrega foi carregada
     * @param total           o total da entrega, em euros
     * @param estado          o estado da entrega
     * @param decididaPor     o nome de quem aceitou ou recusou, ou {@code null} se ainda está pendente
     * @param decididaEm      quando foi decidida, ou {@code null} se ainda está pendente
     * @param motivoRecusa    o motivo da recusa, ou {@code null} se não houve
     */
    public ResumoEntregaHotel(int entregaId, String numeroEncomenda, String fornecedor, int tiposDeMaterial,
                              int totalUnidades, LocalDateTime carregadaEm, BigDecimal total, EstadoEntrega estado,
                              String decididaPor, LocalDateTime decididaEm, String motivoRecusa) {
        this.entregaId = entregaId;
        this.numeroEncomenda = numeroEncomenda;
        this.fornecedor = fornecedor;
        this.tiposDeMaterial = tiposDeMaterial;
        this.totalUnidades = totalUnidades;
        this.carregadaEm = carregadaEm;
        this.total = total;
        this.estado = estado;
        this.decididaPor = decididaPor;
        this.decididaEm = decididaEm;
        this.motivoRecusa = motivoRecusa;
    }

    public int getEntregaId() {
        return entregaId;
    }

    public String getNumeroEncomenda() {
        return numeroEncomenda;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public int getTiposDeMaterial() {
        return tiposDeMaterial;
    }

    public int getTotalUnidades() {
        return totalUnidades;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public EstadoEntrega getEstado() {
        return estado;
    }

    public String getDecididaPor() {
        return decididaPor;
    }

    public String getMotivoRecusa() {
        return motivoRecusa;
    }

    /**
     * @return a data e hora em que a entrega foi carregada, com o fuso horário
     */
    public String getCarregadaEmTexto() {
        return ResumoEntrega.escreverData(carregadaEm);
    }

    /**
     * @return a data e hora da decisão, com o fuso horário, ou texto vazio se ainda está pendente
     */
    public String getDecididaEmTexto() {
        if (decididaEm == null) {
            return "";
        }
        return ResumoEntrega.escreverData(decididaEm);
    }
}
