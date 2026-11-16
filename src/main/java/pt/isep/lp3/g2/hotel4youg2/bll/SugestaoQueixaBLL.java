package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.ClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.SugestaoQueixaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.ISugestaoQueixaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSugestao;

import java.util.List;

/**
 * Regras de negócio das sugestões e queixas dos clientes: a receção regista-as (US23),
 * o cliente pode enviá-las com o NIF (US61) e o gestor consulta-as.
 */
public class SugestaoQueixaBLL {

    /** O maior número de caracteres da descrição, igual ao da coluna na base de dados. */
    public static final int MAXIMO_DESCRICAO = 1000;

    private final ISugestaoQueixaDAL sugestaoQueixaDAL;
    private final IReservaDAL reservaDAL;
    private final ClienteBLL clienteBLL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public SugestaoQueixaBLL() {
        this(new SugestaoQueixaDAL(), new ReservaDAL(), new ClienteDAL());
    }

    /**
     * Cria o BLL com outros DAL (usado nos testes).
     *
     * @param sugestaoQueixaDAL onde ficam as sugestões e queixas
     * @param reservaDAL        onde procurar a reserva em curso do cliente
     * @param clienteDAL        onde procurar o cliente pelo NIF
     */
    public SugestaoQueixaBLL(ISugestaoQueixaDAL sugestaoQueixaDAL, IReservaDAL reservaDAL, IClienteDAL clienteDAL) {
        this.sugestaoQueixaDAL = sugestaoQueixaDAL;
        this.reservaDAL = reservaDAL;
        this.clienteBLL = new ClienteBLL(clienteDAL);
    }

    /**
     * Regista uma sugestão ou queixa de um cliente, feita na receção. Fica guardado o funcionário
     * que a registou e a reserva em curso do cliente, se houver; a data é posta pela base de dados.
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
        return gravarComReserva(sugestao);
    }

    /**
     * Regista uma sugestão ou queixa enviada pelo próprio cliente, que se identifica com o NIF (US61).
     * Fica sem funcionário e com a reserva em curso do cliente, se houver.
     *
     * @param nif       o NIF do cliente (com ou sem espaços à volta)
     * @param tipo      sugestão ou queixa
     * @param descricao o texto (obrigatório, até {@value #MAXIMO_DESCRICAO} caracteres)
     * @return a sugestão ou queixa gravada, com o id e a data
     * @throws pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException se o NIF estiver vazio ou não tiver 9 dígitos
     * @throws IllegalArgumentException se o NIF não for de um cliente registado, se faltar o tipo
     *                                  ou se a descrição não for válida
     */
    public SugestaoQueixa registarPeloNif(String nif, TipoSugestao tipo, String descricao) {
        Cliente cliente = clienteBLL.pesquisarPorNif(nif);
        if (cliente == null) {
            throw new IllegalArgumentException("O NIF " + nif.trim() + " não é de um cliente registado no hotel.");
        }
        String texto = textoValidado(tipo, descricao);
        SugestaoQueixa sugestao = new SugestaoQueixa(0, cliente.getId(), tipo, texto, 0, null);
        return gravarComReserva(sugestao);
    }

    private SugestaoQueixa gravarComReserva(SugestaoQueixa sugestao) {
        for (Reserva reserva : reservaDAL.listarPorCliente(sugestao.getClienteId())) {
            if (reserva.getEstado() == EstadoReserva.EM_CURSO) {
                sugestao.setReserva(reserva.getId(), reserva.getQuartoNumero());
            }
        }
        sugestaoQueixaDAL.inserir(sugestao);
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
     *         e o quarto da reserva em curso quando foram registadas
     * @throws IllegalStateException se quem age não for o gestor
     */
    public List<SugestaoQueixa> listar(Utilizador quemAge, TipoSugestao tipo) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode consultar as sugestões e queixas.");
        }
        return sugestaoQueixaDAL.listar(tipo);
    }
}
