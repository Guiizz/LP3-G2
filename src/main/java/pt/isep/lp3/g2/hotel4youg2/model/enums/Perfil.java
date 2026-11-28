package pt.isep.lp3.g2.hotel4youg2.model.enums;

public enum Perfil {
    GESTOR("Gestor"),
    RECECIONISTA("Rececionista"),
    RESTAURACAO("Restauração");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}