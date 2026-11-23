package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;

import java.util.Objects;

/**
 * Local onde o cliente pica o cartão: um local do hotel ou um quarto.
 */
public class LocalPicagem {

    private final LocalHotel local;
    private final int quartoNumero;

    /**
     * Cria um local de picagem.
     *
     * @param local        o local do hotel
     * @param quartoNumero o número do quarto (só conta se o local for o quarto)
     */
    public LocalPicagem(LocalHotel local, int quartoNumero) {
        this.local = local;
        this.quartoNumero = local == LocalHotel.QUARTO ? quartoNumero : 0;
    }

    /**
     * @param numero o número do quarto
     * @return o local de picagem desse quarto
     */
    public static LocalPicagem quarto(int numero) {
        return new LocalPicagem(LocalHotel.QUARTO, numero);
    }

    public LocalHotel getLocal() {
        return local;
    }

    public int getQuartoNumero() {
        return quartoNumero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalPicagem outro = (LocalPicagem) o;
        return quartoNumero == outro.quartoNumero && local == outro.local;
    }

    @Override
    public int hashCode() {
        return Objects.hash(local, quartoNumero);
    }

    @Override
    public String toString() {
        if (local == LocalHotel.QUARTO) {
            return "Quarto " + quartoNumero;
        }
        return local == null ? "" : local.getDescricao();
    }
}
