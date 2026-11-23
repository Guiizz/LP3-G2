package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.util.StringConverter;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

class CartaoDaReserva extends StringConverter<Reserva> {
    @Override
    public String toString(Reserva reserva) {
        return reserva == null ? "" : reserva.getCartao() + " . Quarto " + reserva.getQuartoNumero();
    }

    @Override
    public Reserva fromString(String texto) {
        return null;
    }
}
