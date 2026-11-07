package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.HorarioReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IHorarioReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.QuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.HorarioReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

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

    private final IReservaDAL repositorio;
    private final IQuartoDAL quartos;
    private final IHorarioReservaDAL horarios;

    public ReservaBLL() {
        this(new ReservaDAL(), new QuartoDAL(), new HorarioReservaDAL());
    }

    public ReservaBLL(IReservaDAL repositorio, IQuartoDAL quartos, IHorarioReservaDAL horarios) {
        this.repositorio = repositorio;
        this.quartos = quartos;
        this.horarios = horarios;
    }

    public HorarioReserva horario() {
        return horarios.obter();
    }

    public BigDecimal calcularTaxa(boolean earlyCheckin, boolean lateCheckout) {
        return horarios.obter().calcularTaxa(earlyCheckin, lateCheckout);
    }

    public List<Quarto> opcoesDisponiveis(LocalDate entrada, LocalDate saida, int hospedes,
                                          boolean earlyCheckin, boolean lateCheckout) {
        validarDatas(entrada, saida);
        validarHospedes(hospedes);
        List<Quarto> opcoes = new ArrayList<>();
        List<String> tiposJaVistos = new ArrayList<>();
        for (Quarto quarto : quartos.listarDisponiveis(entrada, saida, earlyCheckin, lateCheckout)) {
            String tipoEVista = quarto.getTipo() + "/" + quarto.getVista();
            if (quarto.getCapacidade() >= hospedes && !tiposJaVistos.contains(tipoEVista)) {
                tiposJaVistos.add(tipoEVista);
                opcoes.add(quarto);
            }
        }
        return opcoes;
    }

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
        for (Quarto quarto : quartos.listarDisponiveis(entrada, saida, earlyCheckin, lateCheckout)) {
            if (quarto.getTipo() == tipo && quarto.getVista() == vista && quarto.getCapacidade() >= hospedes) {
                Reserva reserva = new Reserva(0, cliente.getId(), quarto.getNumero(), entrada, saida, hospedes,
                        EstadoReserva.CONFIRMADA, calcularPreco(quarto, entrada, saida), null, null);
                reserva.setEarlyCheckin(earlyCheckin);
                reserva.setLateCheckout(lateCheckout);
                reserva.setTaxaHorario(taxa);
                if (repositorio.inserirSeLivre(reserva)) {
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

    public BigDecimal calcularPreco(Quarto quarto, LocalDate entrada, LocalDate saida) {
        if (quarto == null || quarto.getValorNoite() == null) {
            throw new ValidacaoException("Escolha um quarto");
        }
        validarDatas(entrada, saida);
        long noites = ChronoUnit.DAYS.between(entrada, saida);
        return quarto.getValorNoite().multiply(BigDecimal.valueOf(noites));
    }

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
        if (!repositorio.inserirSeLivre(reserva)) {
            throw new ValidacaoException("O quarto " + quarto.getNumero()
                    + " já está reservado nessas datas. Pesquise outra vez os quartos disponíveis.");
        }
        return reserva;
    }

    public List<Reserva> listar() {
        return repositorio.listar();
    }

    public List<Reserva> consultarDoCliente(Cliente cliente) {
        if (cliente == null) {
            throw new ValidacaoException("Escolha o cliente pelo NIF");
        }
        List<Reserva> reservas = repositorio.listarPorCliente(cliente.getId());
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

    public void cancelar(Utilizador quemAge, Reserva reserva) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode cancelar reservas");
        }
        if (reserva == null) {
            throw new ValidacaoException("Escolha uma reserva");
        }
        reserva.cancelar();
        if (!repositorio.cancelar(reserva.getId())) {
            throw new ValidacaoException("A reserva " + reserva.getId()
                    + " já não pode ser cancelada: não existe ou já teve check-in");
        }
    }

    public void alterarDatas(Utilizador quemAge, Reserva reserva, LocalDate novaEntrada, LocalDate novaSaida) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Entre na aplicação como rececionista ou gestor para alterar reservas");
        }
        if (reserva == null) {
            throw new ValidacaoException("Escolha uma reserva");
        }
        validarDatas(novaEntrada, novaSaida);
        reserva.alterarDatas(novaEntrada, novaSaida);
        if (!repositorio.alterarDatasSeLivre(reserva)) {
            throw new ValidacaoException("O quarto " + reserva.getQuartoNumero()
                    + " não está livre nessas datas, ou a reserva já não pode ser alterada");
        }
    }

    public String fazerCheckin(Utilizador quemAge, Reserva reserva) {
        return fazerCheckin(quemAge, reserva, LocalDateTime.now());
    }

    public String fazerCheckin(Utilizador quemAge, Reserva reserva, LocalDateTime agora) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Entre na aplicação como rececionista ou gestor para fazer check-in");
        }
        if (reserva == null) {
            throw new ValidacaoException("Escolha uma reserva");
        }
        reserva.fazerCheckin(agora, horarios.obter().horaMinimaCheckin(reserva.isEarlyCheckin()));
        if (!repositorio.registarCheckin(reserva)) {
            throw new ValidacaoException("A reserva " + reserva.getId()
                    + " já não pode fazer check-in: foi cancelada ou já tem o check-in feito");
        }
        return reserva.getCartao();
    }

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

    public BigDecimal fazerCheckout(Utilizador quemAge, Reserva reserva) {
        return fazerCheckout(quemAge, reserva, LocalDateTime.now());
    }

    public BigDecimal fazerCheckout(Utilizador quemAge, Reserva reserva, LocalDateTime agora) {
        BigDecimal total = totalAPagar(quemAge, reserva);
        reserva.fazerCheckout(agora);
        if (!repositorio.registarCheckout(reserva)) {
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