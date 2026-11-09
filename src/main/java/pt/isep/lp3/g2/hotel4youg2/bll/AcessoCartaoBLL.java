package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.AcessoCartaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IAcessoCartaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.QuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.LocalHotel;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.util.List;

/**
 * Regras de negócio dos acessos com cartão (US39 e US41): decide se um cartão abre um local
 * do hotel, regista as passagens permitidas e deixa o gestor consultá-las.
 * O cartão de um cliente só funciona enquanto a sua reserva estiver em curso; o cartão mestre
 * ({@value AcessoCartao#CARTAO_MESTRE}) abre todos os locais.
 */
public class AcessoCartaoBLL {

    /** Categoria dos serviços extra que dão acesso ao restaurante. */
    public static final String CATEGORIA_RESTAURANTE = "Refeição";

    /** Início da designação do serviço extra que dá acesso à piscina. */
    public static final String SERVICO_PISCINA = "Piscina";

    private final IAcessoCartaoDAL acessoDAL;
    private final IReservaDAL reservaDAL;
    private final IServicoReservaDAL servicoReservaDAL;
    private final IQuartoDAL quartoDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public AcessoCartaoBLL() {
        this(new AcessoCartaoDAL(), new ReservaDAL(), new ServicoReservaDAL(), new QuartoDAL());
    }

    /**
     * Cria o BLL com outros DAL (usado nos testes).
     *
     * @param acessoDAL         onde ficam as passagens dos cartões
     * @param reservaDAL        onde procurar a reserva em curso de cada cartão
     * @param servicoReservaDAL onde estão os serviços extra de cada reserva
     * @param quartoDAL         onde confirmar que o quarto existe
     */
    public AcessoCartaoBLL(IAcessoCartaoDAL acessoDAL, IReservaDAL reservaDAL, IServicoReservaDAL servicoReservaDAL,
                           IQuartoDAL quartoDAL) {
        this.acessoDAL = acessoDAL;
        this.reservaDAL = reservaDAL;
        this.servicoReservaDAL = servicoReservaDAL;
        this.quartoDAL = quartoDAL;
    }

    /**
     * Regista a passagem de um cartão num local do hotel, se o cartão tiver acesso a esse local.
     * O cartão de um cliente só abre o seu quarto, e o restaurante ou a piscina só se a reserva
     * incluir esse serviço extra. Os acessos recusados não ficam registados.
     *
     * @param quemAge      o utilizador com a sessão iniciada
     * @param cartao       o número do cartão (maiúsculas ou minúsculas, com ou sem espaços)
     * @param local        o local onde o cartão foi passado
     * @param quartoNumero o número do quarto, quando o local é o quarto (ignorado nos outros locais)
     * @return a passagem registada, com o id e a hora postos pela base de dados
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se faltar o cartão, o local ou o quarto, ou se o quarto não existir
     * @throws ValidacaoException       se o acesso for recusado: cartão sem reserva em curso, outro quarto,
     *                                  ou local que a reserva não inclui
     */
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
        if (local == LocalHotel.QUARTO && !quartoExiste(quartoNumero)) {
            throw new IllegalArgumentException("O quarto " + quartoNumero + " não existe.");
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

    /**
     * Lista as passagens de um cartão, para mostrar no ecrã onde se registam os acessos.
     *
     * @param cartao o número do cartão
     * @return as passagens do cartão, da mais recente para a mais antiga
     */
    public List<AcessoCartao> listarPorCartao(String cartao) {
        return acessoDAL.listarPorCartao(normalizar(cartao));
    }

    /**
     * Consulta onde e quando um cartão foi usado (US41). Só o gestor pode consultar.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param cartao  o número do cartão
     * @return as passagens do cartão, da mais recente para a mais antiga
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se não for escrito nenhum cartão
     */
    public List<AcessoCartao> consultarPorCartao(Utilizador quemAge, String cartao) {
        Permissoes.exigirGestor(quemAge);
        String numero = normalizar(cartao);
        if (numero.isEmpty()) {
            throw new IllegalArgumentException("Escreva o número do cartão.");
        }
        return acessoDAL.listarPorCartao(numero);
    }

    /**
     * Consulta que cartões entraram num quarto e quando (US41). Só o gestor pode consultar.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param quarto  o número do quarto, como foi escrito no ecrã
     * @return as passagens no quarto, incluindo as do cartão mestre, da mais recente para a mais antiga
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se o quarto estiver vazio ou não for um número inteiro
     */
    public List<AcessoCartao> consultarPorQuarto(Utilizador quemAge, String quarto) {
        Permissoes.exigirGestor(quemAge);
        String numero = quarto == null ? "" : quarto.trim();
        if (numero.isEmpty()) {
            throw new IllegalArgumentException("Indique o número do quarto.");
        }
        if (!numero.matches("\\d{1,9}")) {
            throw new IllegalArgumentException("O número do quarto tem de ser um número inteiro.");
        }
        return acessoDAL.listarPorQuarto(Integer.parseInt(numero));
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

    private boolean quartoExiste(int numero) {
        for (Quarto quarto : quartoDAL.listarTodos()) {
            if (quarto.getNumero() == numero) {
                return true;
            }
        }
        return false;
    }

    private String normalizar(String cartao) {
        return cartao == null ? "" : cartao.trim().toUpperCase();
    }
}
