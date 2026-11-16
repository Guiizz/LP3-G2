package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.FornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.UtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Conta;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio das contas de utilizador: listar, desativar e reativar (US22).
 */
public class UtilizadorBLL {

    private final IUtilizadorDAL utilizadorDAL;
    private final IFornecedorDAL fornecedorDAL;

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
        this(utilizadorDAL, new FornecedorDAL());
    }

    /**
     * Cria o BLL com outros DAL (usado nos testes).
     *
     * @param utilizadorDAL de onde vêm as contas dos funcionários
     * @param fornecedorDAL de onde vêm as contas dos fornecedores
     */
    public UtilizadorBLL(IUtilizadorDAL utilizadorDAL, IFornecedorDAL fornecedorDAL) {
        this.utilizadorDAL = utilizadorDAL;
        this.fornecedorDAL = fornecedorDAL;
    }

    /**
     * @return todas as contas, ordenadas pelo nome de utilizador
     */
    public List<Utilizador> listar() {
        return utilizadorDAL.listar();
    }

    /**
     * Lista as contas para o ecrã "Contas" (US63): primeiro as dos funcionários, depois as dos fornecedores,
     * cada uma com o tipo e o estado. Só o gestor pode ver.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @return as contas dos funcionários e dos fornecedores
     * @throws IllegalStateException se quem age não for o gestor
     */
    public List<Conta> listarContas(Utilizador quemAge) {
        Permissoes.exigirGestor(quemAge);
        List<Conta> contas = new ArrayList<>();
        for (Utilizador utilizador : utilizadorDAL.listar()) {
            contas.add(new Conta(utilizador));
        }
        for (Fornecedor fornecedor : fornecedorDAL.listar()) {
            contas.add(new Conta(fornecedor));
        }
        return contas;
    }

    /**
     * Desativa a conta escolhida na tabela das contas (US63), com as regras de
     * {@link #desativar(Utilizador, Utilizador)}.
     *
     * @param quemAge o utilizador com a sessão iniciada (tem de ser gestor)
     * @param conta   a conta escolhida
     * @throws IllegalArgumentException se a conta for de um fornecedor, que não se desativa aqui
     */
    public void desativarConta(Utilizador quemAge, Conta conta) {
        desativar(quemAge, utilizadorDe(conta));
    }

    /**
     * Reativa a conta escolhida na tabela das contas (US63), com as regras de
     * {@link #reativar(Utilizador, Utilizador)}.
     *
     * @param quemAge o utilizador com a sessão iniciada (tem de ser gestor)
     * @param conta   a conta escolhida
     * @throws IllegalArgumentException se a conta for de um fornecedor, que não se reativa aqui
     */
    public void reativarConta(Utilizador quemAge, Conta conta) {
        reativar(quemAge, utilizadorDe(conta));
    }

    private Utilizador utilizadorDe(Conta conta) {
        if (conta != null && conta.isDeFornecedor()) {
            throw new IllegalArgumentException("Só as contas dos funcionários podem ser desativadas ou reativadas.");
        }
        return conta == null ? null : conta.getUtilizador();
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
