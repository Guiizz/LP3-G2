package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;

import java.util.List;

/**
 * Acesso à base de dados das passagens dos cartões (US39 e US41).
 */
public interface IAcessoCartaoDAL {

    /**
     * Grava uma passagem de cartão. Preenche o id e a hora, que é posta pela base de dados.
     *
     * @param acesso a passagem a gravar
     */
    void registar(AcessoCartao acesso);

    /**
     * @param cartao o número do cartão
     * @return as passagens do cartão, da mais recente para a mais antiga; vazia se não houver nenhuma
     */
    List<AcessoCartao> listarPorCartao(String cartao);

    /**
     * @param quartoNumero o número do quarto
     * @return as passagens no quarto, de qualquer cartão (incluindo o mestre), da mais recente
     *         para a mais antiga; vazia se não houver nenhuma
     */
    List<AcessoCartao> listarPorQuarto(int quartoNumero);

    /**
     * Apaga uma passagem (usado nos testes para limpar o que gravaram).
     *
     * @param id o id da passagem
     * @return {@code true} se a passagem existia e foi apagada
     */
    boolean apagar(int id);
}
