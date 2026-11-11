package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.ReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.SugestaoQueixaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.ISugestaoQueixaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSugestao;

import java.util.List;

/**
 * Regras de negócio das sugestões e queixas dos clientes: a receção regista-as (US23),
 * o cliente pode enviá-las com o cartão do quarto (US48) e o gestor consulta-as.
 */
public class SugestaoQueixaBLL {

    /** O maior número de caracteres da descrição, igual ao da coluna na base de dados. */
    public static final int MAXIMO_DESCRICAO = 1000;

    private final ISugestaoQueixaDAL sugestaoDAL;
    private final IReservaDAL reservaDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public SugestaoQueixaBLL() {
        this(new SugestaoQueixaDAL(), new ReservaDAL());
    }

    /**
     * Cria o BLL com outro DAL das sugestões (usado nos testes); as reservas vêm da base de dados.
     *
     * @param sugestaoDAL onde ficam as sugestões e queixas
     */
    public SugestaoQueixaBLL(ISugestaoQueixaDAL sugestaoDAL) {
        this(sugestaoDAL, new ReservaDAL());
    }

    /**
     * Cria o BLL com outros DAL (usado nos testes).
     *
     * @param sugestaoDAL onde ficam as sugestões e queixas
     * @param reservaDAL  onde procurar a reserva em curso de um cartão
     */
    public SugestaoQueixaBLL(ISugestaoQueixaDAL sugestaoDAL, IReservaDAL reservaDAL) {
        this.sugestaoDAL = sugestaoDAL;
        this.reservaDAL = reservaDAL;
    }

    /**
     * Regista uma sugestão ou queixa de um cliente, feita na receção. Fica guardado o funcionário
     * que a registou; a data é posta pela base de dados.
     *
     * @param quemAge   o utilizador com a sessão iniciada
     * @param cliente   o cliente, procurado pelo NIF
     * @param tipo      sugestão ou queixa
     * @param descricao o texto (obrigatório, até {@value #MAXIMO_DESCRICAO} caracteres)
     * @return a sugestão ou queixa gravada, com o id e a data
     * @throws IllegalStateException    se não houver sessão iniciada
     * @throws IllegalArgumentException se faltar o cliente ou o tipo, ou se a descrição não for válida
     */
    public SugestaoQueixa registar(Utilizador quemAge, Cliente cliente, TipoSugestao tipo, String descricao) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para registar sugestões e queixas.");
        }
        if (cliente == null) {
            throw new IllegalArgumentException("Procure primeiro o cliente pelo NIF.");
        }
        String texto = textoValidado(tipo, descricao);
        // a data fica a null: é a BD que a regista no momento em que grava
        SugestaoQueixa sugestao = new SugestaoQueixa(0, cliente.getId(), tipo, texto, quemAge.getFuncionarioId(), null);
        sugestaoDAL.inserir(sugestao);
        return sugestao;
    }

    /**
     * Regista uma sugestão ou queixa enviada pelo próprio cliente, com o número do cartão do quarto
     * (US48). Fica no nome do cliente da reserva em curso desse cartão, sem funcionário.
     *
     * @param cartao    o número do cartão (maiúsculas ou minúsculas, com ou sem espaços)
     * @param tipo      sugestão ou queixa
     * @param descricao o texto (obrigatório, até {@value #MAXIMO_DESCRICAO} caracteres)
     * @return a sugestão ou queixa gravada, com o id e a data
     * @throws IllegalArgumentException se faltar o cartão, se o cartão não for de uma reserva em curso,
     *                                  se faltar o tipo ou se a descrição não for válida
     */
    public SugestaoQueixa registarPeloCartao(String cartao, TipoSugestao tipo, String descricao) {
        String numero = cartao == null ? "" : cartao.trim().toUpperCase();
        if (numero.isEmpty()) {
            throw new IllegalArgumentException("Escreva o número do cartão.");
        }
        Reserva reserva = reservaDAL.procurarEmCursoPorCartao(numero);
        if (reserva == null || !reserva.cartaoDaAcesso()) {
            throw new IllegalArgumentException("O cartão " + numero
                    + " não é válido ou é de uma reserva que já não está em curso.");
        }
        String texto = textoValidado(tipo, descricao);
        SugestaoQueixa sugestao = new SugestaoQueixa(0, reserva.getClienteId(), tipo, texto, 0, null);
        sugestaoDAL.inserir(sugestao);
        return sugestao;
    }

    private String textoValidado(TipoSugestao tipo, String descricao) {
        if (tipo == null) {
            throw new IllegalArgumentException("Escolha o tipo: sugestão ou queixa.");
        }
        String texto = descricao == null ? "" : descricao.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("A descrição é obrigatória.");
        }
        if (texto.length() > MAXIMO_DESCRICAO) {
            throw new IllegalArgumentException("A descrição não pode ter mais de " + MAXIMO_DESCRICAO + " caracteres.");
        }
        return texto;
    }

    /**
     * Lista as sugestões e queixas para o gestor. Só o gestor pode consultar.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param tipo    só sugestões ou só queixas, ou {@code null} para todas
     * @return as sugestões e queixas, da mais recente para a mais antiga, com o nome do cliente
     * @throws IllegalStateException se quem age não for o gestor
     */
    public List<SugestaoQueixa> listar(Utilizador quemAge, TipoSugestao tipo) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode consultar as sugestões e queixas.");
        }
        return sugestaoDAL.listar(tipo);
    }
}
