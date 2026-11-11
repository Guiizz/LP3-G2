package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.StockDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IStockDAL;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.util.List;

/**
 * Regras de negócio do stock de materiais (US42): consultar o que existe e registar as saídas.
 * O stock só aumenta quando uma entrega é aceite (US49) e só diminui com as saídas registadas aqui.
 */
public class StockBLL {

    private final IStockDAL stockDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public StockBLL() {
        this(new StockDAL());
    }

    /**
     * Cria o BLL com outro DAL (usado nos testes).
     *
     * @param stockDAL onde estão o stock e os movimentos
     */
    public StockBLL(IStockDAL stockDAL) {
        this.stockDAL = stockDAL;
    }

    /**
     * Lista o stock de todos os materiais.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @return cada material com o fornecedor e a quantidade em stock
     * @throws IllegalStateException se não houver sessão iniciada
     */
    public List<MaterialStock> listar(Utilizador quemAge) {
        exigirSessao(quemAge);
        return stockDAL.listar();
    }

    /**
     * Regista a saída de um material do stock. Fica guardado o funcionário que a registou.
     * Não é possível tirar mais do que existe.
     *
     * @param quemAge    o utilizador com a sessão iniciada
     * @param material   o material escolhido na tabela
     * @param quantidade a quantidade, como foi escrita no ecrã (número inteiro maior que zero)
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se não houver material, se a quantidade não for válida
     *                                  ou se for maior do que o que existe em stock
     */
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
