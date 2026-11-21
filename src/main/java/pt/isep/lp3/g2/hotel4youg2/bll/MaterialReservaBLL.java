package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.MaterialReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IMaterialReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.MaterialQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.util.List;

/**
 * Regras dos materiais de cada reserva (US76): a reserva leva os materiais da configuração do tipo do quarto
 * (US75) e o rececionista pode ajustar as quantidades antes do check-in, só para essa reserva.
 */
public class MaterialReservaBLL {

    /** Quantidade máxima de um material numa reserva. */
    public static final int QUANTIDADE_MAXIMA = 99;

    private final IMaterialReservaDAL materialReservaDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public MaterialReservaBLL() {
        this(new MaterialReservaDAL());
    }

    /**
     * Cria o BLL com outro DAL (usado nos testes).
     *
     * @param materialReservaDAL onde estão os materiais das reservas
     */
    public MaterialReservaBLL(IMaterialReservaDAL materialReservaDAL) {
        this.materialReservaDAL = materialReservaDAL;
    }

    /**
     * Mostra os materiais que a reserva leva: os ajustados, se houver, ou os da configuração do tipo do quarto.
     *
     * @param quemAge o utilizador com a sessão iniciada (rececionista ou gestor)
     * @param reserva a reserva
     * @return os materiais e as quantidades
     * @throws IllegalStateException    se quem age não for rececionista nem gestor
     * @throws IllegalArgumentException se não houver reserva escolhida
     */
    public List<MaterialQuarto> listar(Utilizador quemAge, Reserva reserva) {
        exigirRececao(quemAge);
        if (reserva == null) {
            throw new IllegalArgumentException("Escolha uma reserva.");
        }
        return materialReservaDAL.listar(reserva.getId());
    }

    /**
     * Muda a quantidade de um material só nesta reserva, antes do check-in. A configuração do tipo de quarto
     * não muda.
     *
     * @param quemAge    o utilizador com a sessão iniciada (rececionista ou gestor)
     * @param reserva    a reserva
     * @param linha      o material escolhido na lista da reserva
     * @param quantidade a nova quantidade, como foi escrita no ecrã (0 a {@value #QUANTIDADE_MAXIMA}; 0 quer dizer
     *                   que a reserva não leva este material)
     * @throws IllegalStateException    se quem age não for rececionista nem gestor, ou se a reserva já não estiver
     *                                  por fazer (confirmada e sem check-in)
     * @throws IllegalArgumentException se faltar a reserva, o material ou a quantidade, ou se a quantidade não for
     *                                  válida
     */
    public void ajustar(Utilizador quemAge, Reserva reserva, MaterialQuarto linha, String quantidade) {
        exigirRececao(quemAge);
        if (reserva == null) {
            throw new IllegalArgumentException("Escolha uma reserva.");
        }
        if (!reserva.podeSerAlterada()) {
            throw new IllegalStateException("Os materiais só se ajustam antes do check-in, numa reserva confirmada.");
        }
        if (linha == null) {
            throw new IllegalArgumentException("Escolha o material na tabela.");
        }
        materialReservaDAL.ajustar(reserva.getId(), linha.getMaterialId(), lerQuantidade(quantidade));
    }

    private static int lerQuantidade(String quantidade) {
        String texto = quantidade == null ? "" : quantidade.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Indique a quantidade.");
        }
        if (!texto.matches("\\d{1,2}")) {
            throw new IllegalArgumentException("A quantidade tem de ser um número inteiro de 0 a "
                    + QUANTIDADE_MAXIMA + ".");
        }
        return Integer.parseInt(texto);
    }

    private static void exigirRececao(Utilizador quemAge) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Entre na aplicação como rececionista ou gestor para ver os materiais "
                    + "da reserva.");
        }
    }
}
