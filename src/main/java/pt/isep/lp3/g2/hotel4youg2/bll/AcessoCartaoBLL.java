package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.AcessoCartaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IAcessoCartaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.LocalHotel;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.util.List;

public class AcessoCartaoBLL {

    public static final String CATEGORIA_RESTAURANTE = "Refeição";
    public static final String SERVICO_PISCINA = "Piscina";

    private final IAcessoCartaoDAL acessoDAL;
    private final IReservaDAL reservaDAL;
    private final IServicoReservaDAL servicoReservaDAL;

    public AcessoCartaoBLL() {
        this(new AcessoCartaoDAL(), new ReservaDAL(), new ServicoReservaDAL());
    }

    public AcessoCartaoBLL(IAcessoCartaoDAL acessoDAL, IReservaDAL reservaDAL, IServicoReservaDAL servicoReservaDAL) {
        this.acessoDAL = acessoDAL;
        this.reservaDAL = reservaDAL;
        this.servicoReservaDAL = servicoReservaDAL;
    }

    public AcessoCartao registarPassagem(Utilizador quemAge, String cartao, LocalHotel local, int quartoNumero) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para registar a passagem de cartões.");
        }
        String numero = normalizar(cartao);
        if (numero.isEmpty()) {
            throw new IllegalArgumentException("Escreva o número do cartão.");
        }
        if (local == null) {
            throw new IllegalArgumentException("Escolha o local: quarto, restaurante ou piscina.");
        }
        if (local == LocalHotel.QUARTO && quartoNumero <= 0) {
            throw new IllegalArgumentException("Indique o número do quarto.");
        }
        int quarto = local == LocalHotel.QUARTO ? quartoNumero : 0;
        int reservaId = 0;
        if (!numero.equals(AcessoCartao.CARTAO_MESTRE)) {
            Reserva reserva = reservaDAL.procurarEmCursoPorCartao(numero);
            if (reserva == null) {
                throw new ValidacaoException("Acesso recusado: o cartão " + numero
                        + " não é válido ou é de uma reserva que já não está em curso.");
            }
            exigirAcesso(numero, reserva, local, quarto);
            reservaId = reserva.getId();
        }
        AcessoCartao acesso = new AcessoCartao(0, numero, local, quarto, reservaId, null);
        acessoDAL.registar(acesso);
        return acesso;
    }

    public List<AcessoCartao> listarPorCartao(String cartao) {
        return acessoDAL.listarPorCartao(normalizar(cartao));
    }

    private void exigirAcesso(String cartao, Reserva reserva, LocalHotel local, int quarto) {
        if (local == LocalHotel.QUARTO) {
            if (quarto != reserva.getQuartoNumero()) {
                throw new ValidacaoException("Acesso recusado: o cartão " + cartao + " só abre o quarto "
                        + reserva.getQuartoNumero() + ".");
            }
            return;
        }
        if (!reservaIncluiLocal(reserva, local)) {
            throw new ValidacaoException("Acesso recusado: a reserva do cartão " + cartao + " não inclui "
                    + (local == LocalHotel.RESTAURANTE ? "refeições no restaurante." : "a piscina."));
        }
    }

    private boolean reservaIncluiLocal(Reserva reserva, LocalHotel local) {
        for (ServicoReserva extra : servicoReservaDAL.listarPorReserva(reserva.getId())) {
            Servico servico = extra.getServico();
            if (local == LocalHotel.RESTAURANTE && servico.getCategoria() != null
                    && CATEGORIA_RESTAURANTE.equalsIgnoreCase(servico.getCategoria().getNome())) {
                return true;
            }
            if (local == LocalHotel.PISCINA && servico.getDesignacao() != null
                    && servico.getDesignacao().toLowerCase().startsWith(SERVICO_PISCINA.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String normalizar(String cartao) {
        return cartao == null ? "" : cartao.trim().toUpperCase();
    }
}
