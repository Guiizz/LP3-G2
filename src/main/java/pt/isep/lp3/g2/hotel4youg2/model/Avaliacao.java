package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Avaliacao {

    public static final int MINIMO_ESTRELAS = 1;
    public static final int MAXIMO_ESTRELAS = 5;

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int id;
    private final int reservaId;
    private final int estrelas;
    private final String comentario;
    private LocalDateTime avaliadaEm;
    private String nomeCliente;
    private int quartoNumero;

    public Avaliacao(int id, int reservaId, int estrelas, String comentario, LocalDateTime avaliadaEm) {
        this.id = id;
        this.reservaId = reservaId;
        this.estrelas = estrelas;
        this.comentario = comentario;
        this.avaliadaEm = avaliadaEm;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReservaId() {
        return reservaId;
    }

    public int getEstrelas() {
        return estrelas;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getAvaliadaEm() {
        return avaliadaEm;
    }

    public void setAvaliadaEm(LocalDateTime avaliadaEm) {
        this.avaliadaEm = avaliadaEm;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public int getQuartoNumero() {
        return quartoNumero;
    }

    public void setQuartoNumero(int quartoNumero) {
        this.quartoNumero = quartoNumero;
    }

    public String getEstrelasTexto() {
        return desenharEstrelas(estrelas);
    }

    public String getComentarioTexto() {
        return comentario == null ? "-" : comentario;
    }

    public String getDataTexto() {
        return avaliadaEm == null ? "" : avaliadaEm.format(DATA);
    }

    public static String desenharEstrelas(int quantas) {
        StringBuilder texto = new StringBuilder();
        for (int i = MINIMO_ESTRELAS; i <= MAXIMO_ESTRELAS; i++) {
            texto.append(i <= quantas ? '★' : '☆');
        }
        return texto.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Avaliacao outra)) {
            return false;
        }
        return id == outra.id && reservaId == outra.reservaId && estrelas == outra.estrelas
                && Objects.equals(comentario, outra.comentario) && Objects.equals(avaliadaEm, outra.avaliadaEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reservaId, estrelas, comentario, avaliadaEm);
    }

    @Override
    public String toString() {
        return "Avaliacao{reserva=" + reservaId + ", " + estrelas + " estrelas}";
    }
}
