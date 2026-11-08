package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class AcessoCartao {

    public static final String CARTAO_MESTRE = "C000";

    private int id;
    private String cartao;
    private LocalHotel local;
    private int quartoNumero;
    private int reservaId;
    private LocalDateTime registadoEm;

    public AcessoCartao(int id, String cartao, LocalHotel local, int quartoNumero, int reservaId,
                        LocalDateTime registadoEm) {
        this.id = id;
        this.cartao = cartao;
        this.local = local;
        this.quartoNumero = quartoNumero;
        this.reservaId = reservaId;
        this.registadoEm = registadoEm;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCartao() {
        return cartao;
    }

    public LocalHotel getLocal() {
        return local;
    }

    public int getQuartoNumero() {
        return quartoNumero;
    }

    public int getReservaId() {
        return reservaId;
    }

    public LocalDateTime getRegistadoEm() {
        return registadoEm;
    }

    public void setRegistadoEm(LocalDateTime registadoEm) {
        this.registadoEm = registadoEm;
    }

    public String getLocalTexto() {
        if (local == LocalHotel.QUARTO) {
            return "Quarto " + quartoNumero;
        }
        return local == null ? "" : local.getDescricao();
    }

    public String getRegistadoEmTexto() {
        return registadoEm == null ? "" : registadoEm.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AcessoCartao outro = (AcessoCartao) o;
        return id == outro.id && quartoNumero == outro.quartoNumero && reservaId == outro.reservaId
                && Objects.equals(cartao, outro.cartao) && local == outro.local
                && Objects.equals(registadoEm, outro.registadoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cartao, local, quartoNumero, reservaId, registadoEm);
    }

    @Override
    public String toString() {
        return cartao + " - " + getLocalTexto() + " - " + getRegistadoEmTexto();
    }
}
