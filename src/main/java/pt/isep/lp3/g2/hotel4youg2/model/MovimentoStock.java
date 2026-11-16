package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoMovimento;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Movimento de um material no stock (US62): uma entrada (linha de uma entrega aceite) ou uma saída,
 * com a quantidade, o funcionário e a data.
 */
public class MovimentoStock {

    private final TipoMovimento tipo;
    private final int materialId;
    private final int quantidade;
    private final String funcionario;
    private final LocalDateTime registadoEm;

    /**
     * Cria um movimento de stock.
     *
     * @param tipo        entrada ou saída
     * @param materialId  o id do material
     * @param quantidade  quantas unidades entraram ou saíram
     * @param funcionario o nome do funcionário que aceitou a entrega ou registou a saída ({@code null} se não houver)
     * @param registadoEm quando aconteceu
     */
    public MovimentoStock(TipoMovimento tipo, int materialId, int quantidade, String funcionario,
                          LocalDateTime registadoEm) {
        this.tipo = tipo;
        this.materialId = materialId;
        this.quantidade = quantidade;
        this.funcionario = funcionario;
        this.registadoEm = registadoEm;
    }

    public TipoMovimento getTipo() {
        return tipo;
    }

    public int getMaterialId() {
        return materialId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getFuncionario() {
        return funcionario == null || funcionario.isBlank() ? "-" : funcionario;
    }

    public LocalDateTime getRegistadoEm() {
        return registadoEm;
    }

    /**
     * @return a data e a hora do movimento, por exemplo {@code "15/11/2026 10:30"}, para a tabela do histórico
     */
    public String getRegistadoEmTexto() {
        return registadoEm == null ? "" : registadoEm.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovimentoStock outro)) {
            return false;
        }
        return tipo == outro.tipo && materialId == outro.materialId && quantidade == outro.quantidade
                && Objects.equals(funcionario, outro.funcionario) && Objects.equals(registadoEm, outro.registadoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, materialId, quantidade, funcionario, registadoEm);
    }

    @Override
    public String toString() {
        return tipo + " de " + quantidade + " em " + getRegistadoEmTexto() + " (" + getFuncionario() + ")";
    }
}
