package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ServicoReserva {

    private int id;
    private int reservaId;
    private Servico servico;
    private int quantidade;
    // o preço fica guardado como estava na tabela de preços no momento em que o serviço foi junto
    private BigDecimal precoUnitario;
    private LocalDateTime adicionadoEm;

    public ServicoReserva(int id, int reservaId, Servico servico, int quantidade, BigDecimal precoUnitario,
                          LocalDateTime adicionadoEm) {
        this.id = id;
        this.reservaId = reservaId;
        this.servico = servico;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.adicionadoEm = adicionadoEm;
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

    public Servico getServico() {
        return servico;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public LocalDateTime getAdicionadoEm() {
        return adicionadoEm;
    }

    public void setAdicionadoEm(LocalDateTime adicionadoEm) {
        this.adicionadoEm = adicionadoEm;
    }

    public BigDecimal getTotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServicoReserva outro)) {
            return false;
        }
        return id == outro.id && reservaId == outro.reservaId && quantidade == outro.quantidade
                && (servico == null ? outro.servico == null : outro.servico != null && servico.getId() == outro.servico.getId())
                && (precoUnitario == null ? outro.precoUnitario == null
                    : outro.precoUnitario != null && precoUnitario.compareTo(outro.precoUnitario) == 0)
                && Objects.equals(adicionadoEm, outro.adicionadoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reservaId, servico == null ? 0 : servico.getId(), quantidade, adicionadoEm,
                precoUnitario == null ? null : precoUnitario.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return quantidade + " × " + (servico == null ? "?" : servico.getDesignacao()) + " (" + getTotal() + " €)";
    }
}
