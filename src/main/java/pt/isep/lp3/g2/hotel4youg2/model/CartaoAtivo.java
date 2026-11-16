package pt.isep.lp3.g2.hotel4youg2.model;

import java.util.Objects;

public class CartaoAtivo {

    private final String cartao;
    private final String titular;

    public CartaoAtivo(String cartao, String titular) {
        this.cartao = cartao;
        this.titular = titular;
    }

    public String getCartao() {
        return cartao;
    }

    public String getTitular() {
        return titular;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CartaoAtivo outro = (CartaoAtivo) o;
        return Objects.equals(cartao, outro.cartao) && Objects.equals(titular, outro.titular);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartao, titular);
    }

    @Override
    public String toString() {
        return cartao + " - " + titular;
    }
}
