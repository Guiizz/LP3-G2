package pt.isep.lp3.g2.hotel4youg2.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Serviço extra junto a uma reserva (US37): que serviço, quantas vezes e a que preço.
 * O preço unitário fica guardado como estava na tabela de preços no momento em que foi junto,
 * por isso mudar a tabela depois não altera as reservas antigas.
 */
public class ServicoReserva {

    private int id;
    private int reservaId;
    private Servico servico;
    private int quantidade;
    private BigDecimal precoUnitario;
    private LocalDateTime adicionadoEm;

    /**
     * Cria um serviço da reserva.
     *
     * @param id            o id (0 se ainda não foi gravado)
     * @param reservaId     o id da reserva a que pertence
     * @param servico       o serviço extra
     * @param quantidade    quantas vezes foi pedido
     * @param precoUnitario o preço de cada unidade, tirado da tabela de preços
     * @param adicionadoEm  quando foi junto à reserva ({@code null} antes de gravar)
     */
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

    /**
     * @return a quantidade vezes o preço unitário
     */
    public BigDecimal getTotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public String getNomeServico() {
        return servico.getDesignacao();
    }

    public String getPrecoTexto() {
        return euros(precoUnitario);
    }

    public String getTotalTexto() {
        return euros(getTotal());
    }

    public String getAdicionadoEmTexto() {
        return adicionadoEm == null ? "" : adicionadoEm.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private static String euros(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " €";
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