package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoPedido;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class PedidoExtra {

    private int id;
    private final int reservaId;
    private final int servicoId;
    private final int materialId;
    private final String descricao;
    private final int quantidade;
    private EstadoPedido estado;
    private String motivoRecusa;
    private LocalDateTime pedidoEm;
    private boolean consumivel;
    private int quartoNumero;
    private String nomeCliente;

    public PedidoExtra(int id, int reservaId, int servicoId, int materialId, String descricao, int quantidade,
                       EstadoPedido estado) {
        this.id = id;
        this.reservaId = reservaId;
        this.servicoId = servicoId;
        this.materialId = materialId;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.estado = estado;
    }

    public static PedidoExtra deServico(int reservaId, Servico servico, int quantidade) {
        return new PedidoExtra(0, reservaId, servico.getId(), 0, servico.getDesignacao(), quantidade,
                EstadoPedido.PENDENTE);
    }

    public static PedidoExtra deMaterial(int reservaId, MaterialStock material, int quantidade) {
        return new PedidoExtra(0, reservaId, 0, material.getMaterialId(), material.getDescricao(), quantidade,
                EstadoPedido.PENDENTE);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReservaId() {
        return reservaId;
    }

    public int getServicoId() {
        return servicoId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public boolean isServico() {
        return servicoId > 0;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public boolean isPendente() {
        return estado == EstadoPedido.PENDENTE;
    }

    public void aceitar() {
        estado = EstadoPedido.ACEITE;
    }

    public void recusar(String motivo) {
        estado = EstadoPedido.RECUSADO;
        motivoRecusa = motivo;
    }

    public String getMotivoRecusa() {
        return motivoRecusa;
    }

    public void setMotivoRecusa(String motivoRecusa) {
        this.motivoRecusa = motivoRecusa;
    }

    public LocalDateTime getPedidoEm() {
        return pedidoEm;
    }

    public void setPedidoEm(LocalDateTime pedidoEm) {
        this.pedidoEm = pedidoEm;
    }

    public boolean isConsumivel() {
        return consumivel;
    }

    public void setConsumivel(boolean consumivel) {
        this.consumivel = consumivel;
    }

    public int getQuartoNumero() {
        return quartoNumero;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setReserva(int quartoNumero, String nomeCliente) {
        this.quartoNumero = quartoNumero;
        this.nomeCliente = nomeCliente;
    }

    public String getTipoTexto() {
        if (isServico()) {
            return "Serviço";
        }
        return consumivel ? "Material consumível" : "Material (volta no check-out)";
    }

    public String getEstadoTexto() {
        return estado == null ? "" : estado.getDescricao();
    }

    public String getMotivoTexto() {
        return motivoRecusa == null || motivoRecusa.isBlank() ? "-" : motivoRecusa;
    }

    public String getPedidoEmTexto() {
        return pedidoEm == null ? "" : pedidoEm.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PedidoExtra outro = (PedidoExtra) o;
        return id == outro.id && reservaId == outro.reservaId && servicoId == outro.servicoId
                && materialId == outro.materialId && quantidade == outro.quantidade
                && consumivel == outro.consumivel && estado == outro.estado
                && Objects.equals(descricao, outro.descricao) && Objects.equals(motivoRecusa, outro.motivoRecusa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reservaId, servicoId, materialId, descricao, quantidade, estado, motivoRecusa,
                consumivel);
    }

    @Override
    public String toString() {
        return quantidade + " x " + descricao + " (" + getEstadoTexto() + ")";
    }
}
