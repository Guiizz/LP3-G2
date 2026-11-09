package pt.isep.lp3.g2.hotel4youg2.model;

/**
 * Estado de uma entrega de fornecedor: o fornecedor carrega-a como pendente e,
 * depois, um funcionário aceita-a (dá entrada no stock) ou recusa-a (US38 e US49).
 */
public enum EstadoEntrega {
    /** Carregada pelo fornecedor, à espera da decisão do hotel. */
    PENDENTE,
    /** Aceite por um funcionário. */
    ACEITE,
    /** Recusada por um funcionário; o fornecedor pode voltar a enviá-la. */
    RECUSADA
}
