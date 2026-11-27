package pt.isep.lp3.g2.hotel4youg2.model.enums;

public enum CampoCliente {
    NOME("Nome"),
    NIF("NIF"),
    TELEFONE("Telefone"),
    EMAIL("Email");

    private final String texto;

    CampoCliente(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
