package pt.isep.lp3.g2.hotel4youg2.model;

import java.util.Objects;

/**
 * Categoria a que pertence um serviço extra do hotel (por exemplo, Spa ou Refeição).
 */
public class Categoria {

    private int id;
    private String nome;

    /**
     * Cria uma categoria.
     *
     * @param id   o id da categoria (0 se ainda não foi gravada)
     * @param nome o nome da categoria
     */
    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Categoria outra)) {
            return false;
        }
        return id == outra.id && Objects.equals(nome, outra.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome);
    }

    @Override
    public String toString() {
        return nome;
    }
}
