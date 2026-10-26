package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDate;

public class Cliente extends Pessoa {

    public Cliente(int id, String nome, String cc, String nif, String morada, String telemovel, LocalDate dataNascimento, String email) {
        super(id, nome, cc, nif, morada, telemovel, dataNascimento, email);
    }

    @Override
    public String toString() {
        return getNome() + " (NIF " + getNif() + ")";
    }
}
