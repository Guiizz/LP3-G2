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

public class AvaliacaoBLL {

    public static final int MAXIMO_COMENTARIO = 500;

    private final IAvaliacaoDAL avaliacaoDAL;

    public AvaliacaoBLL() {
        this(new AvaliacaoDAL());
    }

    public AvaliacaoBLL(IAvaliacaoDAL avaliacaoDAL) {
        this.avaliacaoDAL = avaliacaoDAL;
    }

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

    public Avaliacao procurar(ContaCliente conta, Reserva reserva) {
        exigirDono(conta, reserva);
        return avaliacaoDAL.procurarPorReserva(reserva.getId());
    }

    public List<Avaliacao> doMes(Utilizador quemAge, YearMonth mes) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode ver as avaliações das estadias.");
        }
        if (mes == null) {
            throw new IllegalArgumentException("Escolha o mês.");
        }
        return avaliacaoDAL.listar(mes.atDay(1), mes.atEndOfMonth());
    }

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
