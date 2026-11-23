package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.FornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.UtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Conta;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;

import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio das contas de utilizador: listar, desativar e reativar (US22).
 */
public class UtilizadorBLL {

    private final IUtilizadorDAL utilizadorDAL;
    private final IFornecedorDAL fornecedorDAL;
    private final PasswordUtils passwordUtils = new PasswordUtils();
    private final SecureRandom random = new SecureRandom();

    private static final String LETRAS_TEMPORARIAS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int TAMANHO_TEMPORARIA = 10;

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

    /**
     * Repõe a palavra-passe de um funcionário. No próximo login tem de escolher outra.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param conta   a conta escolhida
     * @return a palavra-passe temporária para dar ao funcionário
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se não houver conta, for de um fornecedor ou já não existir
     */
    public String reporPassword(Utilizador quemAge, Conta conta) {
        if (quemAge == null || quemAge.getPerfil() != Perfil.GESTOR) {
            throw new IllegalStateException("Só o gestor pode gerir contas.");
        }
        if (conta == null) {
            throw new IllegalArgumentException("Escolha uma conta.");
        }
        if (conta.isDeFornecedor()) {
            throw new IllegalArgumentException("Só se repõe a palavra-passe das contas dos funcionários.");
        }
        Utilizador utilizador = conta.getUtilizador();
        String temporaria = gerarTemporaria();
        String salt = passwordUtils.gerarSalt();
        String hash = passwordUtils.hashPassword(temporaria, salt);
        if (!utilizadorDAL.definirPassword(utilizador.getId(), hash, salt, true)) {
            throw new IllegalArgumentException("A conta não existe.");
        }
        utilizador.definirPassword(hash, salt, true);
        return temporaria;
    }

    private String gerarTemporaria() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < TAMANHO_TEMPORARIA; i++) {
            texto.append(LETRAS_TEMPORARIAS.charAt(random.nextInt(LETRAS_TEMPORARIAS.length())));
        }
        return texto.toString();
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
