package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDate;

/**
 * Cliente do hotel: a pessoa que faz reservas e fica hospedada.
 */
public class Cliente extends Pessoa {

    /**
     * Cria um cliente.
     *
     * @param id             o identificador (0 se ainda não estiver gravado)
     * @param nome           o nome completo
     * @param cc             o número do cartão de cidadão (opcional)
     * @param nif            o NIF, com 9 dígitos
     * @param morada         a morada (opcional)
     * @param telemovel      o telemóvel
     * @param dataNascimento a data de nascimento
     * @param email          o email
     */
    public Cliente(int id, String nome, String cc, String nif, String morada, String telemovel, LocalDate dataNascimento, String email) {
        super(id, nome, cc, nif, morada, telemovel, dataNascimento, email);
    }

    /**
     * @return o nome e o NIF, como aparece nas listas
     */
    @Override
    public String toString() {
        return getNome() + " (NIF " + getNif() + ")";
    }
}
