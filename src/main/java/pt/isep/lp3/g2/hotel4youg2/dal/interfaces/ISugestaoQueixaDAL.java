package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSugestao;

import java.util.List;
import java.util.Optional;

/**
 * Acesso à base de dados das sugestões e queixas dos clientes (US23 e US48).
 */
public interface ISugestaoQueixaDAL {

    /**
     * Grava uma sugestão ou queixa. Se {@code registadaPor} for 0, fica sem funcionário (foi o próprio
     * cliente, com o cartão). Preenche o id e a data, que é posta pela base de dados.
     *
     * @param sugestao a sugestão ou queixa a gravar
     * @return o id gravado
     */
    int inserir(SugestaoQueixa sugestao);

    /**
     * @param id o id da sugestão ou queixa
     * @return a sugestão ou queixa, ou {@link Optional#empty()} se não existir
     */
    Optional<SugestaoQueixa> procurarPorId(int id);

    /**
     * Apaga uma sugestão ou queixa (usado nos testes para limpar o que gravaram).
     *
     * @param id o id da sugestão ou queixa
     * @return {@code true} se existia e foi apagada
     */
    boolean apagar(int id);

    /**
     * @param tipo só sugestões ou só queixas, ou {@code null} para todas
     * @return as sugestões e queixas com o nome do cliente, da mais recente para a mais antiga
     */
    List<SugestaoQueixa> listar(TipoSugestao tipo);
}
