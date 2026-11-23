package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSugestao;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Sugestão ou queixa de um cliente (US23 e US61), registada na receção ou enviada pelo próprio
 * cliente com o NIF.
 */
public class SugestaoQueixa {

    private int id;
    private int clienteId;
    private TipoSugestao tipo;
    private String descricao;
    private int registadaPor;
    private LocalDateTime registadaEm;
    private String nomeCliente;
    private int reservaId;
    private int quartoNumero;

    /**
     * Cria uma sugestão ou queixa.
     *
     * @param id           o id (0 se ainda não foi gravada)
     * @param clienteId    o id do cliente
     * @param tipo         sugestão ou queixa
     * @param descricao    o texto
     * @param registadaPor o id do funcionário que a registou; 0 se foi o próprio cliente, com o NIF
     * @param registadaEm  quando foi registada ({@code null} antes de gravar: é a base de dados que a põe)
     */
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

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public int getReservaId() {
        return reservaId;
    }

    public int getQuartoNumero() {
        return quartoNumero;
    }

    /**
     * Liga a sugestão ou queixa à reserva em curso do cliente quando foi registada (US61).
     *
     * @param reservaId    o id da reserva em curso (0 se não houver)
     * @param quartoNumero o número do quarto dessa reserva
     */
    public void setReserva(int reservaId, int quartoNumero) {
        this.reservaId = reservaId;
        this.quartoNumero = quartoNumero;
    }

    /**
     * @return o quarto e a reserva em curso, por exemplo {@code "Quarto 201 (reserva n.º 5)"},
     *         ou {@code "-"} se não havia reserva em curso; para a consulta do gestor (US61)
     */
    public String getReservaTexto() {
        if (reservaId == 0) {
            return "-";
        }
        return "Quarto " + quartoNumero + " (reserva n.º " + reservaId + ")";
    }

    /**
     * @return quando foi registada (ex.: 17/11/2026 09:05)
     */
    public String getRegistadaEmTexto() {
        return registadaEm == null ? "" : registadaEm.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
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
                && Objects.equals(registadaEm, outra.registadaEm) && reservaId == outra.reservaId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clienteId, tipo, descricao, registadaPor, registadaEm, reservaId);
    }

    @Override
    public String toString() {
        return tipo + " do cliente " + clienteId + ": " + descricao;
    }
}
