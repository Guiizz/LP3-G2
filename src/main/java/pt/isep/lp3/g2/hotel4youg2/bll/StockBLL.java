package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.IStockDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.StockDAL;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.util.List;

public class StockBLL {

    private final IStockDAL stockDAL;

    public StockBLL() {
        this(new StockDAL());
    }

    public StockBLL(IStockDAL stockDAL) {
        this.stockDAL = stockDAL;
    }

    public List<MaterialStock> listar(Utilizador quemAge) {
        exigirSessao(quemAge);
        return stockDAL.listar();
    }

    public void registarSaida(Utilizador quemAge, MaterialStock material, String quantidade) {
        exigirSessao(quemAge);
        if (material == null) {
            throw new IllegalArgumentException("Escolha o material na tabela.");
        }
        int unidades = lerQuantidade(quantidade);
        if (!stockDAL.registarSaida(material.getMaterialId(), unidades, quemAge.getFuncionarioId())) {
            throw new IllegalArgumentException("Não é possível retirar mais do que existe em stock.");
        }
    }

    private int lerQuantidade(String quantidade) {
        String texto = quantidade == null ? "" : quantidade.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Indique a quantidade.");
        }
        if (!texto.matches("\\d{1,9}") || Integer.parseInt(texto) == 0) {
            throw new IllegalArgumentException("A quantidade tem de ser um número inteiro maior que zero.");
        }
        return Integer.parseInt(texto);
    }

    private void exigirSessao(Utilizador quemAge) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para registar saídas de stock.");
        }
    }
}
