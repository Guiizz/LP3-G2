package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.ConfiguracaoQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IConfiguracaoQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Material;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;

import java.util.List;

/**
 * Regras de negócio dos materiais dos quartos (US75): cada material é consumível ou não, e cada tipo de quarto
 * (individual, duplo e família) tem os materiais e as quantidades que leva por defeito. Só o gestor altera;
 * o rececionista só consulta.
 */
public class ConfiguracaoQuartoBLL {

    /** Quantidade máxima de um material num tipo de quarto. */
    public static final int QUANTIDADE_MAXIMA = 99;

    private static final String SO_GESTOR = "Só o gestor pode alterar os materiais dos quartos.";

    private final IConfiguracaoQuartoDAL configuracaoDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public ConfiguracaoQuartoBLL() {
        this(new ConfiguracaoQuartoDAL());
    }

    /**
     * Cria o BLL com outro DAL (usado nos testes).
     *
     * @param configuracaoDAL onde estão os materiais e a configuração dos quartos
     */
    public ConfiguracaoQuartoBLL(IConfiguracaoQuartoDAL configuracaoDAL) {
        this.configuracaoDAL = configuracaoDAL;
    }

    /**
     * Lista todos os materiais, com o fornecedor e se são consumíveis.
     *
     * @param quemAge o utilizador com a sessão iniciada (gestor ou rececionista)
     * @return os materiais, ordenados pela descrição
     * @throws IllegalStateException se não for gestor nem rececionista
     */
    public List<Material> listarMateriais(Utilizador quemAge) {
        exigirConsulta(quemAge);
        return configuracaoDAL.listarMateriais();
    }

    /**
     * Muda um material para consumível ou não consumível. Só o gestor pode.
     *
     * @param quemAge    o utilizador com a sessão iniciada
     * @param material   o material escolhido na tabela
     * @param consumivel {@code true} para consumível
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se não houver material escolhido ou se já não existir
     */
    public void alterarConsumivel(Utilizador quemAge, Material material, boolean consumivel) {
        exigirGestor(quemAge);
        if (material == null) {
            throw new IllegalArgumentException("Escolha o material na tabela.");
        }
        if (!configuracaoDAL.alterarConsumivel(material.getId(), consumivel)) {
            throw new IllegalArgumentException("O material já não existe.");
        }
        material.setConsumivel(consumivel);
    }

    /**
     * Mostra os materiais que um tipo de quarto leva por defeito.
     *
     * @param quemAge o utilizador com a sessão iniciada (gestor ou rececionista)
     * @param tipo    o tipo de quarto
     * @return os materiais e as quantidades; vazio se o tipo ainda não estiver configurado
     * @throws IllegalStateException    se não for gestor nem rececionista
     * @throws IllegalArgumentException se não houver tipo de quarto escolhido
     */
    public List<MaterialQuarto> configuracao(Utilizador quemAge, TipoQuarto tipo) {
        exigirConsulta(quemAge);
        if (tipo == null) {
            throw new IllegalArgumentException("Escolha o tipo de quarto.");
        }
        return configuracaoDAL.listarConfiguracao(tipo);
    }

    /**
     * Junta um material à configuração de um tipo de quarto ou, se já lá estiver, muda a quantidade.
     * Só o gestor pode.
     *
     * @param quemAge    o utilizador com a sessão iniciada
     * @param tipo       o tipo de quarto
     * @param material   o material escolhido
     * @param quantidade a quantidade, como foi escrita no ecrã (número inteiro de 1 a {@value #QUANTIDADE_MAXIMA})
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se faltar o tipo, o material ou a quantidade, ou se a quantidade não for válida
     */
    public void guardar(Utilizador quemAge, TipoQuarto tipo, Material material, String quantidade) {
        exigirGestor(quemAge);
        if (tipo == null) {
            throw new IllegalArgumentException("Escolha o tipo de quarto.");
        }
        if (material == null) {
            throw new IllegalArgumentException("Escolha o material na tabela.");
        }
        configuracaoDAL.guardar(tipo, material.getId(), lerQuantidade(quantidade));
    }

    /**
     * Retira um material da configuração do seu tipo de quarto. Só o gestor pode.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param linha   a linha da configuração escolhida na tabela
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se não houver linha escolhida ou se o material já tiver sido retirado
     */
    public void remover(Utilizador quemAge, MaterialQuarto linha) {
        exigirGestor(quemAge);
        if (linha == null) {
            throw new IllegalArgumentException("Escolha o material na configuração.");
        }
        if (!configuracaoDAL.remover(linha.getTipoQuarto(), linha.getMaterialId())) {
            throw new IllegalArgumentException("Este material já não está na configuração.");
        }
    }

    private static int lerQuantidade(String quantidade) {
        String texto = quantidade == null ? "" : quantidade.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Indique a quantidade.");
        }
        if (!texto.matches("\\d{1,2}") || Integer.parseInt(texto) == 0) {
            throw new IllegalArgumentException("A quantidade tem de ser um número inteiro de 1 a "
                    + QUANTIDADE_MAXIMA + ".");
        }
        return Integer.parseInt(texto);
    }

    private static void exigirConsulta(Utilizador quemAge) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Entre na aplicação como gestor ou rececionista para ver os materiais "
                    + "dos quartos.");
        }
    }

    private static void exigirGestor(Utilizador quemAge) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException(SO_GESTOR);
        }
    }
}
