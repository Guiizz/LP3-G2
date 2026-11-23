package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoReserva;
import pt.isep.lp3.g2.hotel4youg2.model.ServicoReserva;

import java.time.LocalDate;
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
     * Grava a reserva e os seus serviços na mesma transação, só se o quarto estiver livre (US60).
     * Se gravar, atualiza o id da reserva; se algum serviço falhar, nada fica gravado.
     *
     * @param reserva  a reserva a gravar
     * @param servicos os serviços escolhidos ao criar a reserva
     * @return {@code true} se foi gravada; {@code false} se o quarto já estava reservado nessas datas
     * @throws IllegalStateException se um dos serviços foi desativado entretanto
     */
    boolean inserirComServicosSeLivre(Reserva reserva, List<ServicoReserva> servicos);

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

    int cancelarSemCheckin(LocalDate hoje);

    /**
     * Apaga a reserva e os seus serviços, só se ainda estiver por fazer: confirmada e sem check-in (US57 e US79).
     *
     * @param id o id da reserva
     * @return {@code true} se foi apagada; {@code false} se não existe, foi cancelada ou teve check-in
     */
    boolean apagarSemCheckin(int id);

    /**
     * Grava as datas e o preço novos da reserva, só se ainda estiver confirmada e sem check-in
     * e o quarto estiver livre nas datas novas (sem contar com a própria reserva) (US30).
     *
     * @param reserva a reserva, já com as datas e o preço novos
     * @return {@code true} se foi alterada; {@code false} se o quarto está ocupado ou a reserva já não pode ser alterada
     */
    boolean alterarDatasSeLivre(Reserva reserva);

    /**
     * Grava o early check-in, o late check-out e a taxa novos da reserva (US58), só se a reserva ainda não teve
     * check-out, se o early check-in não mudar depois do check-in e se o quarto permitir o pedido
     * (sem contar com a própria reserva).
     *
     * @param reserva a reserva, já com o early check-in, o late check-out e a taxa novos
     * @return {@code true} se foi alterada; {@code false} se o quarto não permite ou a reserva já não pode mudar
     */
    boolean alterarHorarioSeLivre(Reserva reserva);

    /**
     * Grava o check-in (estado, hora e cartão), só se a reserva ainda estiver confirmada e sem check-in (US35).
     *
     * @param reserva a reserva, já com o check-in feito no objeto
     * @return {@code true} se foi gravado; {@code false} se outro funcionário a cancelou ou fez o check-in entretanto
     */
    boolean registarCheckin(Reserva reserva);

    /**
     * Grava o check-in e tira do stock os materiais da reserva, numa só transação (US76). Por defeito só grava o
     * check-in, como {@link #registarCheckin(Reserva)}; a implementação da base de dados também dá baixa do stock.
     *
     * @param reserva       a reserva, já com o check-in feito no objeto
     * @param funcionarioId o id de quem faz o check-in
     * @return {@code true} se foi gravado; {@code false} se outro funcionário a cancelou ou fez o check-in entretanto
     * @throws IllegalStateException se faltar stock de algum material; nesse caso nada é gravado
     */
    default boolean registarCheckin(Reserva reserva, int funcionarioId) {
        return registarCheckin(reserva);
    }

    /**
     * Grava o check-out (estado e hora), só se a reserva ainda estiver em curso (US36).
     *
     * @param reserva a reserva, já com o check-out feito no objeto
     * @return {@code true} se foi gravado; {@code false} se a reserva já não estava em curso
     */
    boolean registarCheckout(Reserva reserva);

    /**
     * Grava o check-out e devolve ao stock as saídas temporárias da reserva, numa só transação (US76). Por defeito
     * só grava o check-out, como {@link #registarCheckout(Reserva)}; a implementação da base de dados também
     * devolve os materiais.
     *
     * @param reserva       a reserva, já com o check-out feito no objeto
     * @param funcionarioId o id de quem faz o check-out
     * @return {@code true} se foi gravado; {@code false} se a reserva já não estava em curso
     */
    default boolean registarCheckout(Reserva reserva, int funcionarioId) {
        return registarCheckout(reserva);
    }

    /**
     * Procura a reserva em curso de um cartão (US39 e US48).
     *
     * @param cartao o número do cartão, em maiúsculas
     * @return a reserva em curso, ou {@code null} se o cartão não for de nenhuma reserva em curso
     */
    Reserva procurarEmCursoPorCartao(String cartao);
}