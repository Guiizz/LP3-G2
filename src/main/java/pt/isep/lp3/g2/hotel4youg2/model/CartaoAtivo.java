package pt.isep.lp3.g2.hotel4youg2.model;

import java.util.Objects;

/**
 * Cartão que abre portas neste momento (US63): o de um funcionário com a conta ativa ou o de um cliente
 * com uma reserva em curso. Aparece na lista para escolher o cartão no ecrã dos acessos.
 */
public class CartaoAtivo {

    private final String cartao;
    private final String titular;

    /**
     * Cria um cartão da lista.
     *
     * @param cartao  o número do cartão, por exemplo {@code "F12"} ou {@code "C201"}
     * @param titular quem o usa, por exemplo o nome do funcionário ou o nome do cliente e o quarto
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
