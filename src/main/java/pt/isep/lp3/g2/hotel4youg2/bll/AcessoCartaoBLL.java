package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.AcessoCartaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.FuncionarioDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.QuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IAcessoCartaoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFuncionarioDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IServicoReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.AcessoCartao;
import pt.isep.lp3.g2.hotel4youg2.model.CartaoAtivo;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Servico;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio dos acessos com cartão (US39, US41 e US52): decide se um cartão abre um local
 * do hotel, regista as passagens permitidas e deixa o gestor consultá-las.
 * O cartão de um cliente só funciona enquanto a sua reserva estiver em curso; o cartão de um
 * funcionário ({@value Funcionario#PREFIXO_CARTAO} e o número de funcionário) só abre os locais
 * dos pisos 0 e 1, e só enquanto a conta dele estiver ativa; o cartão mestre
 * ({@value AcessoCartao#CARTAO_MESTRE}) abre todos os locais.
 */
public class AcessoCartaoBLL {

    /** Categoria dos serviços extra que dão acesso ao restaurante. */
    public static final String CATEGORIA_RESTAURANTE = "Refeição";

    /** Início da designação do serviço extra que dá acesso à piscina. */
    public static final String SERVICO_PISCINA = "Piscina";

    private static final String CARTAO_DE_FUNCIONARIO = Funcionario.PREFIXO_CARTAO + "[1-9]\\d{0,8}";

    private final IAcessoCartaoDAL acessoCartaoDAL;
    private final IReservaDAL reservaDAL;
    private final IServicoReservaDAL servicoReservaDAL;
    private final IQuartoDAL quartoDAL;
    private final IFuncionarioDAL funcionarioDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public AcessoCartaoBLL() {
        this(new AcessoCartaoDAL(), new ReservaDAL(), new ServicoReservaDAL(), new QuartoDAL(), new FuncionarioDAL());
    }

    /**
     * Cria o BLL com outros DAL (usado nos testes).
     *
     * @param acessoCartaoDAL   onde ficam as passagens dos cartões
     * @param reservaDAL        onde procurar a reserva em curso de cada cartão
     * @param servicoReservaDAL onde estão os serviços extra de cada reserva
     * @param quartoDAL         onde confirmar que o quarto existe
     * @param funcionarioDAL    onde procurar o funcionário de cada cartão
     */
    public AcessoCartaoBLL(IAcessoCartaoDAL acessoCartaoDAL, IReservaDAL reservaDAL, IServicoReservaDAL servicoReservaDAL,
                           IQuartoDAL quartoDAL, IFuncionarioDAL funcionarioDAL) {
        this.acessoCartaoDAL = acessoCartaoDAL;
        this.reservaDAL = reservaDAL;
        this.servicoReservaDAL = servicoReservaDAL;
        this.quartoDAL = quartoDAL;
        this.funcionarioDAL = funcionarioDAL;
    }

    /**
     * Regista a passagem de um cartão num local do hotel, se o cartão tiver acesso a esse local.
     * O cartão de um cliente só abre o seu quarto, e o restaurante ou a piscina só se a reserva
     * incluir esse serviço extra. O cartão de um funcionário só abre os locais dos pisos 0 e 1,
     * se a conta dele estiver ativa, e a passagem fica registada com o funcionário.
     * Os acessos recusados não ficam registados.
     *
     * @param quemAge      o utilizador com a sessão iniciada
     * @param cartao       o número do cartão (maiúsculas ou minúsculas, com ou sem espaços)
     * @param local        o local onde o cartão foi passado
     * @param quartoNumero o número do quarto, quando o local é o quarto (ignorado nos outros locais)
     * @return a passagem registada, com o id e a hora postos pela base de dados
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se faltar o cartão, o local ou o quarto, ou se o quarto não existir
     * @throws ValidacaoException       se o acesso for recusado: cartão sem reserva em curso, outro quarto,
     *                                  local que a reserva não inclui, cartão que não é de nenhum
     *                                  funcionário, funcionário com a conta desativada ou fora dos
     *                                  pisos 0 e 1
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
            throw new IllegalArgumentException("Escolha o local.");
        }
        if (numero.matches(CARTAO_DE_FUNCIONARIO)) {
            return registarPicagem(numero, local);
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
            if (reserva == null || !reserva.cartaoDaAcesso()) {
                throw new ValidacaoException("Acesso recusado: o cartão " + numero
                        + " não é válido ou é de uma reserva que já não está em curso.");
            }
            exigirAcesso(numero, reserva, local, quarto);
            reservaId = reserva.getId();
        }
        AcessoCartao acesso = new AcessoCartao(0, numero, local, quarto, reservaId, null);
        acessoCartaoDAL.registar(acesso);
        return acesso;
    }

    public List<Funcionario> listarFuncionariosComCartao(Utilizador quemAge) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para ver os cartões dos funcionários.");
        }
        return funcionarioDAL.listarComContaAtiva();
    }

    public List<CartaoAtivo> listarCartoes(Utilizador quemAge) {
        List<CartaoAtivo> cartoes = new ArrayList<>();
        for (Funcionario funcionario : listarFuncionariosComCartao(quemAge)) {
            cartoes.add(new CartaoAtivo(funcionario.getCartao(), funcionario.getNome()));
        }
        cartoes.addAll(acessoCartaoDAL.listarCartoesDeClientes());
        return cartoes;
    }

    public String cartaoDoFuncionario(Utilizador quemAge) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para usar o seu cartão.");
        }
        Funcionario funcionario = funcionarioDAL.procurarPorId(quemAge.getFuncionarioId());
        if (funcionario == null) {
            throw new IllegalStateException("A sua conta não tem um cartão de funcionário.");
        }
        return funcionario.getCartao();
    }

    /**
     * Lista as passagens de um cartão, para mostrar no ecrã onde se registam os acessos.
     *
     * @param cartao o número do cartão
     * @return as passagens do cartão, da mais recente para a mais antiga
     */
    public List<AcessoCartao> listarPorCartao(String cartao) {
        return acessoCartaoDAL.listarPorCartao(normalizar(cartao));
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
        return acessoCartaoDAL.listarPorCartao(numero);
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
        return acessoCartaoDAL.listarPorQuarto(Integer.parseInt(numero));
    }

    private AcessoCartao registarPicagem(String cartao, LocalHotel local) {
        int numeroFuncionario = Integer.parseInt(cartao.substring(Funcionario.PREFIXO_CARTAO.length()));
        Funcionario funcionario = funcionarioDAL.procurarPorNumero(numeroFuncionario);
        if (funcionario == null) {
            throw new ValidacaoException("Acesso recusado: o cartão " + cartao + " não é de nenhum funcionário.");
        }
        if (!funcionarioDAL.temContaAtiva(funcionario.getId())) {
            throw new ValidacaoException("Acesso recusado: o cartão " + cartao
                    + " é de um funcionário com a conta desativada.");
        }
        if (local.getPiso() != 0 && local.getPiso() != 1) {
            throw new ValidacaoException("Acesso recusado: o cartão " + cartao
                    + " é de um funcionário e só abre os locais dos pisos 0 e 1.");
        }
        AcessoCartao acesso = new AcessoCartao(0, cartao, local, 0, 0, null);
        acesso.setFuncionario(funcionario.getId(), funcionario.getNome());
        acessoCartaoDAL.registar(acesso);
        return acesso;
    }

    private void exigirAcesso(String cartao, Reserva reserva, LocalHotel local, int quarto) {
        if (local == LocalHotel.QUARTO) {
            if (quarto != reserva.getQuartoNumero()) {
                throw new ValidacaoException("Acesso recusado: o cartão " + cartao + " só abre o quarto "
                        + reserva.getQuartoNumero() + ".");
            }
            return;
        }
        if (local != LocalHotel.RESTAURANTE && local != LocalHotel.PISCINA) {
            throw new ValidacaoException("Acesso recusado: o cartão " + cartao
                    + " é de um cliente e só abre o seu quarto, o restaurante e a piscina.");
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
