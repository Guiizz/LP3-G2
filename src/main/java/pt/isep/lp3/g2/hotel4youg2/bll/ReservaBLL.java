package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.HorarioReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.QuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IHorarioReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;
import pt.isep.lp3.g2.hotel4youg2.model.enums.SeparadorReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Regras de negócio das reservas (US27).
 */
public class ReservaBLL {

    private final IReservaDAL reservaDAL;
    private final IQuartoDAL quartoDAL;
    private final IHorarioReservaDAL horarioReservaDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public ReservaBLL() {
        this(new ReservaDAL(), new QuartoDAL(), new HorarioReservaDAL());
    }

    /**
     * Cria o BLL com outros DAL (usado nos testes).
     *
     * @param reservaDAL        onde ficam as reservas
     * @param quartoDAL         onde procurar os quartos disponíveis
     * @param horarioReservaDAL onde estão as horas e as taxas das reservas
     */
    public ReservaBLL(IReservaDAL reservaDAL, IQuartoDAL quartoDAL, IHorarioReservaDAL horarioReservaDAL) {
        this.reservaDAL = reservaDAL;
        this.quartoDAL = quartoDAL;
        this.horarioReservaDAL = horarioReservaDAL;
    }

    /**
     * Devolve as horas e as taxas em vigor, para o ecrã "Criar reserva" as mostrar (US47).
     *
     * @return as horas e as taxas das reservas
     */
    public HorarioReserva horario() {
        return horarioReservaDAL.obter();
    }

    /**
     * Calcula a taxa a somar à reserva pelo early check-in e pelo late check-out (US47).
     *
     * @param earlyCheckin  se o cliente quer early check-in
     * @param lateCheckout  se o cliente quer late check-out
     * @return a soma das taxas pedidas; zero se não pedir nenhum
     */
    public BigDecimal calcularTaxa(boolean earlyCheckin, boolean lateCheckout) {
        return horarioReservaDAL.obter().calcularTaxa(earlyCheckin, lateCheckout);
    }

    /**
     * Devolve as opções de quarto disponíveis para o pedido do cliente: um quarto de cada
     * combinação de tipo e vista que esteja livre nas datas e onde caibam os hóspedes.
     * Com early check-in ou late check-out, o quarto também tem de estar livre nessas horas.
     *
     * @param entrada      a data de entrada
     * @param saida        a data de saída
     * @param hospedes     o número de hóspedes (pelo menos 1)
     * @param earlyCheckin se o cliente quer early check-in
     * @param lateCheckout se o cliente quer late check-out
     * @return uma opção por tipo e vista; vazia se não houver nenhuma
     * @throws ValidacaoException se as datas ou o número de hóspedes não forem válidos
     */
    public List<Quarto> opcoesDisponiveis(LocalDate entrada, LocalDate saida, int hospedes,
                                          boolean earlyCheckin, boolean lateCheckout) {
        validarDatas(entrada, saida);
        validarHospedes(hospedes);
        List<Quarto> opcoes = new ArrayList<>();
        List<String> tiposJaVistos = new ArrayList<>();
        for (Quarto quarto : quartoDAL.listarDisponiveis(entrada, saida, earlyCheckin, lateCheckout)) {
            String tipoEVista = quarto.getTipo() + "/" + quarto.getVista();
            if (quarto.getCapacidade() >= hospedes && !tiposJaVistos.contains(tipoEVista)) {
                tiposJaVistos.add(tipoEVista);
                opcoes.add(quarto);
            }
        }
        return opcoes;
    }

    /**
     * Cria uma reserva confirmada para o tipo e a vista escolhidos. É a aplicação que atribui
     * o quarto: o primeiro que ainda estiver livre quando a reserva é gravada. O preço é o das
     * noites mais a taxa de early check-in e de late check-out.
     *
     * @param cliente      o cliente, procurado pelo NIF
     * @param tipo         o tipo de quarto escolhido
     * @param vista        a vista escolhida
     * @param entrada      a data de entrada (hoje ou depois)
     * @param saida        a data de saída (depois da entrada)
     * @param hospedes     o número de hóspedes (pelo menos 1)
     * @param earlyCheckin se o cliente quer early check-in
     * @param lateCheckout se o cliente quer late check-out
     * @return a reserva gravada, com o quarto atribuído
     * @throws ValidacaoException se faltar o cliente, o tipo ou a vista, se as datas ou os hóspedes
     *                            não forem válidos, ou se já não houver nenhum quarto livre
     */
    public Reserva criar(Cliente cliente, TipoQuarto tipo, Vista vista, LocalDate entrada, LocalDate saida,
                         int hospedes, boolean earlyCheckin, boolean lateCheckout) {
        if (cliente == null) {
            throw new ValidacaoException("Escolha o cliente pelo NIF");
        }
        if (tipo == null || vista == null) {
            throw new ValidacaoException("Escolha o tipo de quarto e a vista");
        }
        validarDatas(entrada, saida);
        validarHospedes(hospedes);
        BigDecimal taxa = calcularTaxa(earlyCheckin, lateCheckout);
        for (Quarto quarto : quartoDAL.listarDisponiveis(entrada, saida, earlyCheckin, lateCheckout)) {
            if (quarto.getTipo() == tipo && quarto.getVista() == vista && quarto.getCapacidade() >= hospedes) {
                Reserva reserva = new Reserva(0, cliente.getId(), quarto.getNumero(), entrada, saida, hospedes,
                        EstadoReserva.CONFIRMADA, calcularPreco(quarto, entrada, saida), null, null);
                reserva.setEarlyCheckin(earlyCheckin);
                reserva.setLateCheckout(lateCheckout);
                reserva.setTaxaHorario(taxa);
                if (reservaDAL.inserirSeLivre(reserva)) {
                    return reserva;
                }
            }
        }
        if (earlyCheckin || lateCheckout) {
            throw new ValidacaoException("Nenhum quarto " + tipo + " com vista " + vista
                    + " permite o early check-in ou o late check-out nestas datas. "
                    + "Pode fazer a reserva nas horas standard.");
        }
        throw new ValidacaoException("Já não há quartos " + tipo + " com vista " + vista
                + " livres nestas datas. Pesquise outra vez os quartos disponíveis.");
    }

    private void validarHospedes(int hospedes) {
        if (hospedes < 1) {
            throw new ValidacaoException("O número de hóspedes tem de ser pelo menos 1");
        }
    }

    /**
     * Calcula o preço do alojamento: o valor por noite do quarto vezes o número de noites.
     *
     * @param quarto  o quarto, com a tarifa
     * @param entrada a data de entrada
     * @param saida   a data de saída
     * @return o preço das noites, sem serviços nem taxas
     * @throws ValidacaoException se faltar o quarto ou a tarifa, ou se as datas não forem válidas
     */
    public BigDecimal calcularPreco(Quarto quarto, LocalDate entrada, LocalDate saida) {
        if (quarto == null || quarto.getValorNoite() == null) {
            throw new ValidacaoException("Escolha um quarto");
        }
        validarDatas(entrada, saida);
        long noites = ChronoUnit.DAYS.between(entrada, saida);
        return quarto.getValorNoite().multiply(BigDecimal.valueOf(noites));
    }

    /**
     * Cria uma reserva confirmada num quarto escolhido, nas horas standard.
     *
     * @param cliente  o cliente, procurado pelo NIF
     * @param quarto   o quarto escolhido
     * @param entrada  a data de entrada (hoje ou depois)
     * @param saida    a data de saída (depois da entrada)
     * @param hospedes o número de hóspedes (de 1 até à capacidade do quarto)
     * @return a reserva gravada
     * @throws ValidacaoException se faltar o cliente ou o quarto, se as datas ou os hóspedes
     *                            não forem válidos, ou se o quarto já estiver reservado nessas datas
     */
    public Reserva criar(Cliente cliente, Quarto quarto, LocalDate entrada, LocalDate saida, int hospedes) {
        if (cliente == null) {
            throw new ValidacaoException("Escolha o cliente pelo NIF");
        }
        if (quarto == null) {
            throw new ValidacaoException("Escolha um quarto");
        }
        validarDatas(entrada, saida);
        if (hospedes < 1) {
            throw new ValidacaoException("O número de hóspedes tem de ser pelo menos 1");
        }
        if (hospedes > quarto.getCapacidade()) {
            throw new ValidacaoException("O quarto " + quarto.getNumero() + " só tem capacidade para "
                    + quarto.getCapacidade() + " pessoa(s)");
        }

        BigDecimal preco = calcularPreco(quarto, entrada, saida);
        Reserva reserva = new Reserva(0, cliente.getId(), quarto.getNumero(), entrada, saida, hospedes,
                EstadoReserva.CONFIRMADA, preco, null, null);
        if (!reservaDAL.inserirSeLivre(reserva)) {
            throw new ValidacaoException("O quarto " + quarto.getNumero()
                    + " já está reservado nessas datas. Pesquise outra vez os quartos disponíveis.");
        }
        return reserva;
    }

    /**
     * Lista todas as reservas, para o ecrã "Gerir reservas".
     *
     * @return todas as reservas, da entrada mais recente para a mais antiga
     */
    public List<Reserva> listar() {
        return reservaDAL.listar();
    }

    public List<Reserva> listar(SeparadorReserva separador, String pesquisa) {
        return filtrar(reservaDAL.listar(), separador, pesquisa);
    }

    public static List<Reserva> filtrar(List<Reserva> reservas, SeparadorReserva separador, String pesquisa) {
        if (separador == null) {
            throw new ValidacaoException("Escolha o separador das reservas");
        }
        String termo = pesquisa == null ? "" : pesquisa.trim().toLowerCase();
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva reserva : reservas) {
            if (separador.inclui(reserva.getEstado()) && corresponde(reserva, termo)) {
                resultado.add(reserva);
            }
        }
        if (separador == SeparadorReserva.POR_FAZER) {
            resultado.sort(new EntradaMaisProximaPrimeiro());
        }
        return resultado;
    }

    private static boolean corresponde(Reserva reserva, String termo) {
        if (termo.isEmpty()) {
            return true;
        }
        String nome = reserva.getNomeCliente() == null ? "" : reserva.getNomeCliente().toLowerCase();
        String nif = reserva.getNifCliente() == null ? "" : reserva.getNifCliente();
        String quarto = String.valueOf(reserva.getQuartoNumero());
        return nome.contains(termo) || nif.startsWith(termo) || quarto.equals(termo);
    }

    private static class EntradaMaisProximaPrimeiro implements Comparator<Reserva> {
        @Override
        public int compare(Reserva a, Reserva b) {
            int porData = a.getDataEntrada().compareTo(b.getDataEntrada());
            if (porData != 0) {
                return porData;
            }
            return Integer.compare(a.getId(), b.getId());
        }
    }

    /**
     * Lista as reservas de um cliente (US33).
     *
     * @param cliente o cliente, procurado pelo NIF
     * @return as reservas do cliente, da entrada mais recente para a mais antiga
     * @throws ValidacaoException se não for escolhido nenhum cliente
     */
    public List<Reserva> consultarDoCliente(Cliente cliente) {
        if (cliente == null) {
            throw new ValidacaoException("Escolha o cliente pelo NIF");
        }
        List<Reserva> reservas = reservaDAL.listarPorCliente(cliente.getId());
        reservas.sort(new MaisRecentePrimeiro());
        return reservas;
    }

    private static class MaisRecentePrimeiro implements Comparator<Reserva> {
        @Override
        public int compare(Reserva a, Reserva b) {
            int porData = b.getDataEntrada().compareTo(a.getDataEntrada());
            if (porData != 0) {
                return porData;
            }
            return Integer.compare(b.getId(), a.getId());
        }
    }

    /**
     * Cancela uma reserva que ainda não teve check-in. Só o gestor o pode fazer.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param reserva a reserva a cancelar
     * @throws IllegalStateException se quem age não for o gestor, ou se a reserva já estiver
     *                               cancelada ou tiver check-in
     * @throws ValidacaoException    se não for escolhida nenhuma reserva, ou se outro funcionário
     *                               a cancelou ou fez o check-in entretanto
     */
    public void cancelar(Utilizador quemAge, Reserva reserva) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode cancelar reservas");
        }
        if (reserva == null) {
            throw new ValidacaoException("Escolha uma reserva");
        }
        reserva.cancelar();
        if (!reservaDAL.cancelar(reserva.getId())) {
            throw new ValidacaoException("A reserva " + reserva.getId()
                    + " já não pode ser cancelada: não existe ou já teve check-in");
        }
    }

    /**
     * Altera as datas de uma reserva confirmada, se o quarto estiver livre nas datas novas (US30).
     *
     * @param quemAge     o utilizador com a sessão iniciada (rececionista ou gestor)
     * @param reserva     a reserva a alterar
     * @param novaEntrada a data de entrada nova (hoje ou depois)
     * @param novaSaida   a data de saída nova (depois da entrada)
     * @throws IllegalStateException se quem age não for rececionista nem gestor, ou se a reserva
     *                               já não puder ser alterada
     * @throws ValidacaoException    se não for escolhida nenhuma reserva, se as datas não forem válidas
     *                               ou se o quarto não estiver livre nessas datas
     */
    public void alterarDatas(Utilizador quemAge, Reserva reserva, LocalDate novaEntrada, LocalDate novaSaida) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Entre na aplicação como rececionista ou gestor para alterar reservas");
        }
        if (reserva == null) {
            throw new ValidacaoException("Escolha uma reserva");
        }
        validarDatas(novaEntrada, novaSaida);
        LocalDate entradaAntes = reserva.getDataEntrada();
        LocalDate saidaAntes = reserva.getDataSaida();
        BigDecimal precoAntes = reserva.getPrecoTotal();
        reserva.alterarDatas(novaEntrada, novaSaida);
        if (!reservaDAL.alterarDatasSeLivre(reserva)) {
            reserva.setDataEntrada(entradaAntes);
            reserva.setDataSaida(saidaAntes);
            reserva.setPrecoTotal(precoAntes);
            throw new ValidacaoException("O quarto " + reserva.getQuartoNumero()
                    + " não está livre nessas datas, ou a reserva já não pode ser alterada");
        }
    }

    /**
     * Faz o check-in de uma reserva agora (US35). Ver {@link #fazerCheckin(Utilizador, Reserva, LocalDateTime)}.
     *
     * @param quemAge o utilizador com a sessão iniciada (rececionista ou gestor)
     * @param reserva a reserva
     * @return o número do cartão a entregar ao cliente
     */
    public String fazerCheckin(Utilizador quemAge, Reserva reserva) {
        return fazerCheckin(quemAge, reserva, LocalDateTime.now());
    }

    /**
     * Faz o check-in de uma reserva confirmada (US35): passa a em curso e o cliente recebe o cartão
     * do quarto. Só no dia de entrada e a partir da hora de check-in (ou da de early check-in,
     * se a reserva o tiver).
     *
     * @param quemAge o utilizador com a sessão iniciada (rececionista ou gestor)
     * @param reserva a reserva
     * @param agora   a data e hora do check-in (nos testes, uma hora fixa)
     * @return o número do cartão a entregar ao cliente
     * @throws IllegalStateException se quem age não for rececionista nem gestor, ou se a reserva
     *                               não puder fazer check-in agora (cancelada, repetido, outro dia ou cedo demais)
     * @throws ValidacaoException    se não for escolhida nenhuma reserva, ou se outro funcionário
     *                               a cancelou ou fez o check-in entretanto
     */
    public String fazerCheckin(Utilizador quemAge, Reserva reserva, LocalDateTime agora) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Entre na aplicação como rececionista ou gestor para fazer check-in");
        }
        if (reserva == null) {
            throw new ValidacaoException("Escolha uma reserva");
        }
        reserva.fazerCheckin(agora, horarioReservaDAL.obter().horaMinimaCheckin(reserva.isEarlyCheckin()));
        if (!reservaDAL.registarCheckin(reserva)) {
            throw new ValidacaoException("A reserva " + reserva.getId()
                    + " já não pode fazer check-in: foi cancelada ou já tem o check-in feito");
        }
        return reserva.getCartao();
    }

    /**
     * Calcula o total a pagar no check-out (US36): o quarto, os serviços extra e a taxa de early
     * check-in e de late check-out. Não altera a reserva.
     *
     * @param quemAge o utilizador com a sessão iniciada (rececionista ou gestor)
     * @param reserva a reserva em curso
     * @return o total a pagar
     * @throws IllegalStateException se quem age não for rececionista nem gestor, ou se a reserva
     *                               não estiver em curso
     * @throws ValidacaoException    se não for escolhida nenhuma reserva
     */
    public BigDecimal totalAPagar(Utilizador quemAge, Reserva reserva) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Entre na aplicação como rececionista ou gestor para fazer check-out");
        }
        if (reserva == null) {
            throw new ValidacaoException("Escolha uma reserva");
        }
        reserva.exigirPodeFazerCheckout();
        return reserva.getTotal();
    }

    /**
     * Faz o check-out de uma reserva agora (US36). Ver {@link #fazerCheckout(Utilizador, Reserva, LocalDateTime)}.
     *
     * @param quemAge o utilizador com a sessão iniciada (rececionista ou gestor)
     * @param reserva a reserva em curso
     * @return o total pago
     */
    public BigDecimal fazerCheckout(Utilizador quemAge, Reserva reserva) {
        return fazerCheckout(quemAge, reserva, LocalDateTime.now());
    }

    /**
     * Faz o check-out de uma reserva em curso (US36): passa a concluída e o cartão deixa de dar acesso.
     *
     * @param quemAge o utilizador com a sessão iniciada (rececionista ou gestor)
     * @param reserva a reserva em curso
     * @param agora   a data e hora do check-out (nos testes, uma hora fixa)
     * @return o total pago: quarto, serviços extra e taxas
     * @throws IllegalStateException se quem age não for rececionista nem gestor, ou se a reserva
     *                               não estiver em curso
     * @throws ValidacaoException    se não for escolhida nenhuma reserva, ou se outro funcionário
     *                               já fez o check-out entretanto
     */
    public BigDecimal fazerCheckout(Utilizador quemAge, Reserva reserva, LocalDateTime agora) {
        BigDecimal total = totalAPagar(quemAge, reserva);
        reserva.fazerCheckout(agora);
        if (!reservaDAL.registarCheckout(reserva)) {
            throw new ValidacaoException("A reserva " + reserva.getId()
                    + " já não pode fazer check-out: já não está em curso");
        }
        return total;
    }

    private void validarDatas(LocalDate entrada, LocalDate saida) {
        if (entrada == null || saida == null) {
            throw new ValidacaoException("Indique as datas de entrada e de saída");
        }
        if (entrada.isBefore(LocalDate.now())) {
            throw new ValidacaoException("A data de entrada não pode ser no passado");
        }
        if (!saida.isAfter(entrada)) {
            throw new ValidacaoException("A data de saída tem de ser posterior à de entrada");
        }
    }
}