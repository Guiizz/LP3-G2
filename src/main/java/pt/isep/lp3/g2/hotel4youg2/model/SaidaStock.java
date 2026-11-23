package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Saída temporária de material que ainda não voltou ao stock (US77), para a lista das saídas
 * por devolver: que material saiu, quantas unidades, porquê, quem a registou e quando.
 */
public class SaidaStock {

    private final int id;
    private final int materialId;
    private final String codigo;
    private final String material;
    private final int quantidade;
    private final String motivo;
    private final String funcionario;
    private final LocalDateTime registadaEm;

    /**
     * Cria uma saída de stock.
     *
     * @param id          o id da saída
     * @param materialId  o id do material
     * @param codigo      o código do material
     * @param material    a descrição do material
     * @param quantidade  quantas unidades saíram
     * @param motivo      o motivo, já em texto, ou {@code null}
     * @param funcionario o nome de quem registou a saída, ou {@code null}
     * @param registadaEm quando foi registada
     */
    public SaidaStock(int id, int materialId, String codigo, String material, int quantidade, String motivo,
                      String funcionario, LocalDateTime registadaEm) {
        this.id = id;
        this.materialId = materialId;
        this.codigo = codigo;
        this.material = material;
        this.quantidade = quantidade;
        this.motivo = motivo;
        this.funcionario = funcionario;
        this.registadaEm = registadaEm;
    }

    public int getId() {
        return id;
    }

    public int getMaterialId() {
        return materialId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMaterial() {
        return material;
    }

    public int getQuantidade() {
        return quantidade;
    }

    /**
     * @return o motivo da saída, ou "-" se não houver
     */
    public String getMotivo() {
        return motivo == null || motivo.isBlank() ? "-" : motivo;
    }

    /**
     * @return o nome de quem registou a saída, ou "-" se não houver
     */
    public String getFuncionario() {
        return funcionario == null || funcionario.isBlank() ? "-" : funcionario;
    }

    public LocalDateTime getRegistadaEm() {
        return registadaEm;
    }

    /**
     * @return a data e a hora da saída, por exemplo {@code "16/11/2026 09:05"}, para a tabela
     */
    public String getRegistadaEmTexto() {
        return registadaEm == null ? "" : registadaEm.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SaidaStock outra)) {
            return false;
        }
        return id == outra.id && materialId == outra.materialId && quantidade == outra.quantidade
                && Objects.equals(codigo, outra.codigo) && Objects.equals(material, outra.material)
                && Objects.equals(motivo, outra.motivo) && Objects.equals(funcionario, outra.funcionario)
                && Objects.equals(registadaEm, outra.registadaEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, materialId, quantidade, codigo, material, motivo, funcionario, registadaEm);
    }

    @Override
    public String toString() {
        return quantidade + " × " + material + " (" + getMotivo() + ")";
    }
}
