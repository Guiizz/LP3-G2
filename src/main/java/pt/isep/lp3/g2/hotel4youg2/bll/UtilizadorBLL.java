package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.UtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.util.List;

/**
 * Regras de negócio das contas de utilizador: listar, desativar e reativar (US22).
 */
public class UtilizadorBLL {

    private final IUtilizadorDAL utilizadorDAL;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public UtilizadorBLL() {
        this(new UtilizadorDAL());
    }

    /**
     * Cria o BLL com outro DAL.
     *
     * @param utilizadorDAL de onde vêm as contas
     */
    public UtilizadorBLL(IUtilizadorDAL utilizadorDAL) {
        this.utilizadorDAL = utilizadorDAL;
    }

    /**
     * @return todas as contas, ordenadas pelo nome de utilizador
     */
    public List<Utilizador> listar() {
        return utilizadorDAL.listar();
    }

    /**
     * Desativa uma conta: a pessoa deixa de conseguir entrar na aplicação.
     *
     * @param quemAge    o utilizador com a sessão iniciada (tem de ser gestor)
     * @param utilizador a conta a desativar (não pode ser a de quem age)
     * @throws IllegalStateException    se quem age não for gestor
     * @throws IllegalArgumentException se não houver conta escolhida, for a própria conta ou a conta não existir
     */
    public void desativar(Utilizador quemAge, Utilizador utilizador) {
        alterarEstado(quemAge, utilizador, false);
    }

    /**
     * Reativa uma conta desativada: a pessoa volta a conseguir entrar.
     *
     * @param quemAge    o utilizador com a sessão iniciada (tem de ser gestor)
     * @param utilizador a conta a reativar
     * @throws IllegalStateException    se quem age não for gestor
     * @throws IllegalArgumentException se não houver conta escolhida ou a conta não existir
     */
    public void reativar(Utilizador quemAge, Utilizador utilizador) {
        alterarEstado(quemAge, utilizador, true);
    }

    private void alterarEstado(Utilizador quemAge, Utilizador utilizador, boolean ativo) {
        if (quemAge == null || quemAge.getPerfil() != Perfil.GESTOR) {
            throw new IllegalStateException("Só o gestor pode gerir contas.");
        }
        if (utilizador == null) {
            throw new IllegalArgumentException("Escolha uma conta.");
        }
        if (!ativo && quemAge.getId() == utilizador.getId()) {
            throw new IllegalArgumentException("Não pode desativar a sua própria conta.");
        }
        if (!utilizadorDAL.atualizarAtivo(utilizador.getId(), ativo)) {
            throw new IllegalArgumentException("A conta não existe.");
        }
    }
}
