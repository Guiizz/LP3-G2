package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;

import java.util.List;

/**
 * Acesso aos dados das reservas.
 */
public interface IReservaDAL {

    /**
     * Grava a reserva, mas só se o quarto estiver livre nessas datas.
     * Se gravar, atualiza o id da reserva.
     *
     * @param reserva a reserva a gravar
     * @return {@code true} se foi gravada; {@code false} se o quarto já estava reservado
     *         em datas que se sobrepõem
     */
    boolean inserirSeLivre(Reserva reserva);

    /**
     * @param id o id da reserva
     * @return a reserva, ou {@code null} se não existir
     */
    Reserva procurarPorId(int id);

    /**
     * @return todas as reservas, da entrada mais recente para a mais antiga
     */
    List<Reserva> listar();

    /**
     * @param clienteId o id do cliente
     * @return as reservas do cliente, da entrada mais recente para a mais antiga; vazia se não tiver nenhuma
     */
    List<Reserva> listarPorCliente(int clienteId);

    /**
     * Muda o estado de uma reserva, sem verificar o estado anterior.
     *
     * @param id     o id da reserva
     * @param estado o estado novo
     * @return {@code true} se a reserva existia e foi alterada
     */
    boolean atualizarEstado(int id, EstadoReserva estado);

    /**
     * Apaga uma reserva (usado nos testes para limpar o que gravaram).
     *
     * @param id o id da reserva
     * @return {@code true} se a reserva existia e foi apagada
     */
    boolean apagar(int id);

    /**
     * Cancela a reserva, só se ainda estiver confirmada e sem check-in.
     *
     * @param id o id da reserva
     * @return {@code true} se foi cancelada; {@code false} se não existe, já foi cancelada ou já teve check-in
     */
    boolean cancelar(int id);

    /**
     * Grava as datas e o preço novos da reserva, só se ainda estiver confirmada e sem check-in
     * e o quarto estiver livre nas datas novas (sem contar com a própria reserva) (US30).
     *
     * @param reserva a reserva, já com as datas e o preço novos
     * @return {@code true} se foi alterada; {@code false} se o quarto está ocupado ou a reserva já não pode ser alterada
     */
    boolean alterarDatasSeLivre(Reserva reserva);

    /**
     * Grava o check-in (estado, hora e cartão), só se a reserva ainda estiver confirmada e sem check-in (US35).
     *
     * @param reserva a reserva, já com o check-in feito no objeto
     * @return {@code true} se foi gravado; {@code false} se outro funcionário a cancelou ou fez o check-in entretanto
     */
    boolean registarCheckin(Reserva reserva);

    /**
     * Grava o check-out (estado e hora), só se a reserva ainda estiver em curso (US36).
     *
     * @param reserva a reserva, já com o check-out feito no objeto
     * @return {@code true} se foi gravado; {@code false} se a reserva já não estava em curso
     */
    boolean registarCheckout(Reserva reserva);

    /**
     * Procura a reserva em curso de um cartão (US39 e US48).
     *
     * @param cartao o número do cartão, em maiúsculas
     * @return a reserva em curso, ou {@code null} se o cartão não for de nenhuma reserva em curso
     */
    Reserva procurarEmCursoPorCartao(String cartao);
}