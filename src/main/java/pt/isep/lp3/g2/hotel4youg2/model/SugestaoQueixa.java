package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class SugestaoQueixa {

    private int id;
    private int clienteId;
    private TipoSugestao tipo;
    private String descricao;
    private int registadaPor;
    private LocalDateTime registadaEm;

    public SugestaoQueixa(int id, int clienteId, TipoSugestao tipo, String descricao, int registadaPor,
                          LocalDateTime registadaEm) {
        this.id = id;
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.descricao = descricao;
        this.registadaPor = registadaPor;
        this.registadaEm = registadaEm;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public TipoSugestao getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getRegistadaPor() {
        return registadaPor;
    }

    public LocalDateTime getRegistadaEm() {
        return registadaEm;
    }

    public void setRegistadaEm(LocalDateTime registadaEm) {
        this.registadaEm = registadaEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SugestaoQueixa outra)) {
            return false;
        }
        return id == outra.id && clienteId == outra.clienteId && tipo == outra.tipo
                && Objects.equals(descricao, outra.descricao) && registadaPor == outra.registadaPor
                && Objects.equals(registadaEm, outra.registadaEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clienteId, tipo, descricao, registadaPor, registadaEm);
    }

    @Override
    public String toString() {
        return tipo + " do cliente " + clienteId + ": " + descricao;
    }
}
