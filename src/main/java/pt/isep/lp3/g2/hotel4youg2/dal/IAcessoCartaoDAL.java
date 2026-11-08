package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;

import java.util.List;

public interface IAcessoCartaoDAL {

    void registar(AcessoCartao acesso);

    List<AcessoCartao> listarPorCartao(String cartao);

    List<AcessoCartao> listarPorQuarto(int quartoNumero);

    boolean apagar(int id);
}
