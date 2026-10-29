package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.ReservaRepositorio;
import pt.isep.lp3.g2.hotel4youg2.dal.ReservaRepositorioBD;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Regras de negócio das reservas (US27).
 */
public class ReservaService {

    private final ReservaRepositorio repositorio;

    public ReservaService() {
        this(new ReservaRepositorioBD());
    }

    public ReservaService(ReservaRepositorio repositorio) {
        this.repositorio = repositorio;
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