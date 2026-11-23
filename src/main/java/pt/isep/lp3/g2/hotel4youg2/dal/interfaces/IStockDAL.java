package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.MaterialStock;
import pt.isep.lp3.g2.hotel4youg2.model.MovimentoStock;
import pt.isep.lp3.g2.hotel4youg2.model.SaidaStock;
import pt.isep.lp3.g2.hotel4youg2.model.enums.MotivoSaida;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoSaida;

import java.util.List;

/**
 * Acesso à base de dados do stock de materiais e das saídas (US42, US62 e US77).
 */
public interface IStockDAL {

    /**
     * @return o stock de cada material, com o fornecedor e quantas unidades entraram e saíram (sem contar as
     *         saídas temporárias que já voltaram), ordenado pela descrição do material
     */
    List<MaterialStock> listar();

    /**
     * Tira a quantidade do stock e regista o movimento de saída, com o tipo e o motivo (US77), numa só
     * transação. Só tira se houver quantidade suficiente.
     *
     * @param materialId      o id do material
     * @param quantidade      quantas unidades saem
     * @param funcionarioId   o id do funcionário que regista a saída
     * @param tipo            temporária ou definitiva
     * @param motivo          o motivo da saída definitiva, ou {@code null} numa temporária
     * @param descricaoMotivo o texto do motivo, ou {@code null}
     * @return {@code true} se a saída foi registada; {@code false} se não havia quantidade suficiente
     */
    boolean registarSaida(int materialId, int quantidade, int funcionarioId, TipoSaida tipo, MotivoSaida motivo,
                          String descricaoMotivo);

    /**
     * Lista as entradas (linhas de entregas aceites), as saídas temporárias e definitivas e as devoluções
     * de um material (US62 e US77).
     *
     * @param materialId o id do material
     * @return os movimentos, do mais recente para o mais antigo
     */
    List<MovimentoStock> listarMovimentos(int materialId);

    /**
     * @return as saídas temporárias que ainda não voltaram ao stock, da mais antiga para a mais recente (US77)
     */
    List<SaidaStock> listarPorDevolver();

    /**
     * Dá uma saída temporária como devolvida: guarda quando e quem a recebeu e soma a quantidade ao stock,
     * tudo numa só transação. Só funciona se a saída for temporária e ainda não tiver sido devolvida,
     * por isso duas pessoas não a conseguem devolver duas vezes (US77).
     *
     * @param saidaId       o id da saída
     * @param funcionarioId o id do funcionário que recebe o material
     * @return {@code true} se a saída foi devolvida; {@code false} se não existe, não é temporária ou já tinha sido devolvida
     */
    boolean devolver(int saidaId, int funcionarioId);
}
