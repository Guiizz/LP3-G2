package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.AvaliacaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IAvaliacaoDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Avaliacao;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

/**
 * Regras das avaliações: o cliente avalia a estadia depois do check-out e o gestor vê as avaliações de cada mês.
 */
public class AvaliacaoBLL {

    /** Número máximo de caracteres do comentário. */
    public static final int MAXIMO_COMENTARIO = 500;

    private final IAvaliacaoDAL avaliacaoDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public AvaliacaoBLL() {
        this(new AvaliacaoDAL());
    }

    /**
     * Cria o BLL com outro DAL (usado nos testes).
     *
     * @param avaliacaoDAL onde ficam as avaliações
     */
    public AvaliacaoBLL(IAvaliacaoDAL avaliacaoDAL) {
        this.avaliacaoDAL = avaliacaoDAL;
    }

    /**
     * Guarda a avaliação de uma estadia. Só o dono da reserva avalia, só depois do check-out e só uma vez.
     *
     * @param conta      a conta do cliente
     * @param reserva    a reserva
     * @param estrelas   as estrelas (de 1 a 5)
     * @param comentario o comentário (opcional, até 500 caracteres)
     * @return a avaliação gravada
     * @throws IllegalStateException    se não houver sessão iniciada ou a reserva não for do cliente
     * @throws IllegalArgumentException se a reserva não estiver concluída, as estrelas ou o comentário não forem
     *                                  válidos, ou a estadia já tiver sido avaliada
     */
    public Avaliacao avaliar(ContaCliente conta, Reserva reserva, int estrelas, String comentario) {
        exigirDono(conta, reserva);
        if (reserva.getEstado() != EstadoReserva.CONCLUIDA) {
            throw new IllegalArgumentException("Só pode avaliar a estadia depois do check-out.");
        }
        if (estrelas < Avaliacao.MINIMO_ESTRELAS || estrelas > Avaliacao.MAXIMO_ESTRELAS) {
            throw new IllegalArgumentException("Escolha de " + Avaliacao.MINIMO_ESTRELAS + " a "
                    + Avaliacao.MAXIMO_ESTRELAS + " estrelas.");
        }
        String texto = comentario == null ? "" : comentario.trim();
        if (texto.length() > MAXIMO_COMENTARIO) {
            throw new IllegalArgumentException("O comentário pode ter até " + MAXIMO_COMENTARIO + " caracteres.");
        }
        Avaliacao avaliacao = new Avaliacao(0, reserva.getId(), estrelas, texto.isEmpty() ? null : texto, null);
        if (!avaliacaoDAL.inserir(avaliacao)) {
            throw new IllegalArgumentException("Esta estadia já foi avaliada.");
        }
        return avaliacao;
    }

    /**
     * @param conta   a conta do cliente
     * @param reserva a reserva
     * @return a avaliação da reserva, ou {@code null} se ainda não foi avaliada
     * @throws IllegalStateException    se não houver sessão iniciada ou a reserva não for do cliente
     * @throws IllegalArgumentException se não for escolhida nenhuma reserva
     */
    public Avaliacao procurar(ContaCliente conta, Reserva reserva) {
        exigirDono(conta, reserva);
        return avaliacaoDAL.procurarPorReserva(reserva.getId());
    }

    /**
     * Lista as avaliações feitas num mês.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param mes     o mês
     * @return as avaliações, da mais recente para a mais antiga
     * @throws IllegalStateException    se quem age não for gestor
     * @throws IllegalArgumentException se não for escolhido o mês
     */
    public List<Avaliacao> doMes(Utilizador quemAge, YearMonth mes) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode ver as avaliações das estadias.");
        }
        if (mes == null) {
            throw new IllegalArgumentException("Escolha o mês.");
        }
        return avaliacaoDAL.listar(mes.atDay(1), mes.atEndOfMonth());
    }

    /**
     * @param avaliacoes as avaliações
     * @return a média das estrelas, com uma casa decimal, ou {@code null} se não houver avaliações
     */
    public static BigDecimal media(List<Avaliacao> avaliacoes) {
        if (avaliacoes.isEmpty()) {
            return null;
        }
        int soma = 0;
        for (Avaliacao avaliacao : avaliacoes) {
            soma += avaliacao.getEstrelas();
        }
        return BigDecimal.valueOf(soma).divide(BigDecimal.valueOf(avaliacoes.size()), 1, RoundingMode.HALF_UP);
    }

    private static void exigirDono(ContaCliente conta, Reserva reserva) {
        if (conta == null) {
            throw new IllegalStateException("Entre na sua conta para avaliar a estadia.");
        }
        if (reserva == null) {
            throw new IllegalArgumentException("Escolha a estadia que quer avaliar.");
        }
        if (reserva.getClienteId() != conta.getClienteId()) {
            throw new IllegalStateException("Só pode avaliar as suas estadias.");
        }
    }
}
