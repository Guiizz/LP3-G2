package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Passagem de um cartão num local do hotel (US39): que cartão, onde, de que reserva e quando.
 * Só ficam registadas as passagens permitidas.
 */
public class AcessoCartao {

    /** O cartão do gestor, que abre todos os locais e não pertence a nenhuma reserva. */
    public static final String CARTAO_MESTRE = "C000";

    private int id;
    private String cartao;
    private LocalHotel local;
    private int quartoNumero;
    private int reservaId;
    private LocalDateTime registadoEm;

    /**
     * Cria uma passagem de cartão.
     *
     * @param id           o id (0 se ainda não foi gravada)
     * @param cartao       o número do cartão
     * @param local        o local onde o cartão foi passado
     * @param quartoNumero o número do quarto, se o local for o quarto; 0 nos outros locais
     * @param reservaId    o id da reserva do cartão; 0 para o cartão mestre
     * @param registadoEm  quando foi registada ({@code null} antes de gravar: é a base de dados que a põe)
     */
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

    /**
     * @return o local para mostrar no ecrã, por exemplo {@code "Quarto 201"} ou {@code "Piscina"}
     */
    public String getLocalTexto() {
        if (local == LocalHotel.QUARTO) {
            return "Quarto " + quartoNumero;
        }
        return local == null ? "" : local.getDescricao();
    }

    public String getReservaTexto() {
        return reservaId > 0 ? String.valueOf(reservaId) : "-";
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
