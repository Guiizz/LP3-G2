package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.CartaoAtivo;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;

import java.time.LocalDate;
import java.util.List;

/**
 * Acesso à base de dados das passagens dos cartões (US39, US41 e US74).
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
     * Lista os acessos com os filtros escolhidos. Um filtro vazio, 0 ou {@code null} não conta.
     *
     * @param cartao       o número do cartão
     * @param quartoNumero o número do quarto
     * @param local        o local do hotel
     * @param dia          o dia
     * @return os acessos, do mais recente para o mais antigo
     */
    List<AcessoCartao> listar(String cartao, int quartoNumero, LocalHotel local, LocalDate dia);

    /**
     * @param clienteId o id do cliente
     * @return as picagens do cliente, da mais recente para a mais antiga
     */
    List<AcessoCartao> listarPorCliente(int clienteId);

    /**
     * Apaga uma passagem (usado nos testes para limpar o que gravaram).
     *
     * @param id o id da passagem
     * @return {@code true} se a passagem existia e foi apagada
     */
    boolean apagar(int id);

    /**
     * @return os cartões que já foram usados, cada um com a pessoa que o usou
     */
    List<CartaoAtivo> listarCartoesUsados();
}
