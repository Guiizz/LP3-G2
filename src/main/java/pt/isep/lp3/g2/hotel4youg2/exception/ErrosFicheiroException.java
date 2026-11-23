package pt.isep.lp3.g2.hotel4youg2.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Erro lançado quando um ficheiro (XML ou XSD) é recusado. Guarda todos os erros encontrados,
 * para o ecrã os mostrar juntos (US53).
 */
public class ErrosFicheiroException extends IllegalArgumentException {

    /** Os erros encontrados no ficheiro. */
    private final List<String> erros;

    /**
     * @param erros os erros encontrados, um por linha; a mensagem da exceção junta-os todos
     */
    public ErrosFicheiroException(List<String> erros) {
        super(String.join("\n", erros));
        this.erros = new ArrayList<>(erros);
    }

    /**
     * @param erro o único erro encontrado
     */
    public ErrosFicheiroException(String erro) {
        this(List.of(erro));
    }

    /**
     * @return os erros encontrados, pela ordem em que apareceram
     */
    public List<String> getErros() {
        return erros;
    }
}
