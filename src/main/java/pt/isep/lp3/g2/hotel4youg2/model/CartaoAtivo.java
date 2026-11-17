package pt.isep.lp3.g2.hotel4youg2.model;

import java.util.Objects;

/**
 * Cartão com o seu titular, para escolher de uma lista (US63 e US74). No ecrã "Registos de acesso"
 * aparecem os cartões que já foram usados, cada um com a pessoa que o usou.
 */
public class CartaoAtivo {

    private final String cartao;
    private final String titular;

    /**
     * Cria um cartão da lista.
     *
     * @param cartao  o número do cartão, por exemplo {@code "F12"} ou {@code "C201"}
     * @param titular quem o usa, por exemplo o nome do funcionário ou o nome do cliente
     */
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
