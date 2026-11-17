package pt.isep.lp3.g2.hotel4youg2.model.enums;

/**
 * Motivo de uma saída definitiva de material (US77). No "Outro" é obrigatório escrever o motivo.
 */
public enum MotivoSaida {
    ESTRAGADO("Estragado"),
    ROUBADO("Roubado"),
    DEVOLVIDO_FORNECEDOR("Devolvido ao fornecedor"),
    OUTRO("Outro");

    private final String descricao;

    MotivoSaida(String descricao) {
        this.descricao = descricao;
    }

    /**
     * @return o nome do motivo para mostrar no ecrã, por exemplo "Devolvido ao fornecedor"
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Junta o motivo e o texto escrito pelo funcionário, para mostrar numa tabela.
     *
     * @param motivo    o motivo escolhido, ou {@code null} (saída temporária ou saída antiga)
     * @param descricao o texto escrito, ou {@code null}
     * @return por exemplo "Outro: caiu ao chão", "Estragado" ou "Reparação"; "-" se não houver nenhum
     */
    public static String descrever(MotivoSaida motivo, String descricao) {
        boolean semTexto = descricao == null || descricao.isBlank();
        if (motivo == null) {
            return semTexto ? "-" : descricao.trim();
        }
        return semTexto ? motivo.getDescricao() : motivo.getDescricao() + ": " + descricao.trim();
    }

    @Override
    public String toString() {
        return descricao;
    }
}
