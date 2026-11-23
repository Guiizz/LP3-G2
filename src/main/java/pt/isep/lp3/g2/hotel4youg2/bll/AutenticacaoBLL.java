package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.UtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.util.Optional;

/**
 * Regras de autenticação dos funcionários na aplicação.
 */
public class AutenticacaoBLL {

    private final IUtilizadorDAL utilizadorDAL;
    private final PasswordUtils passwordUtils;

    /**
     * Cria a camada de negócio da autenticação com acesso à base de dados.
     */
    public AutenticacaoBLL() {
        this(new UtilizadorDAL(), new PasswordUtils());
    }

    /**
     * Cria a camada de negócio da autenticação (usado nos testes, com um DAL em memória).
     *
     * @param utilizadorDAL onde procurar os utilizadores
     * @param passwordUtils como verificar as passwords
     */
    public AutenticacaoBLL(IUtilizadorDAL utilizadorDAL, PasswordUtils passwordUtils) {
        this.utilizadorDAL = utilizadorDAL;
        this.passwordUtils = passwordUtils;
    }

    /**
     * Autentica um funcionário com o nome de utilizador e a password.
     *
     * @param nomeUtilizador o nome de utilizador escrito no ecrã de login
     * @param password       a password escrita no ecrã de login
     * @return o utilizador autenticado
     * @throws IllegalArgumentException se algum campo estiver vazio, se a conta não existir,
     *                                  se a password estiver errada ou se a conta estiver desativada
     */
    public Utilizador autenticar(String nomeUtilizador, String password) {
        if (nomeUtilizador == null || nomeUtilizador.isBlank()) {
            throw new IllegalArgumentException("Escreva o nome de utilizador.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Escreva a password.");
        }

        String nome = nomeUtilizador.trim();
        Optional<Utilizador> encontrado = utilizadorDAL.procurarPorNomeUtilizador(nome);
        if (encontrado.isEmpty()) {
            throw new IllegalArgumentException("Não existe nenhuma conta com o nome de utilizador \"" + nome + "\".");
        }
        Utilizador utilizador = encontrado.get();

        if (!passwordUtils.verificarPassword(password, utilizador.getPasswordSalt(), utilizador.getPasswordHash())) {
            throw new IllegalArgumentException("A password está errada.");
        }
        if (!utilizador.isAtivo()) {
            throw new IllegalArgumentException("Esta conta está desativada. Contacte o gestor.");
        }
        return utilizador;
    }

    /**
     * Muda a palavra-passe temporária que o gestor repôs pela escolhida pelo funcionário.
     *
     * @param utilizador  o utilizador que entrou com a palavra-passe temporária
     * @param nova        a palavra-passe nova
     * @param confirmacao a palavra-passe nova, outra vez
     * @throws IllegalStateException    se não houver utilizador
     * @throws IllegalArgumentException se a palavra-passe não foi reposta, for curta, não coincidir
     *                                  ou for igual à temporária
     */
    public void mudarPassword(Utilizador utilizador, String nova, String confirmacao) {
        if (utilizador == null) {
            throw new IllegalStateException("Entre com a palavra-passe temporária.");
        }
        if (!utilizador.isMudarPassword()) {
            throw new IllegalArgumentException("A palavra-passe desta conta não foi reposta.");
        }
        if (nova == null || nova.length() < FuncionarioBLL.TAMANHO_MINIMO_PASSWORD) {
            throw new IllegalArgumentException("A palavra-passe tem de ter pelo menos "
                    + FuncionarioBLL.TAMANHO_MINIMO_PASSWORD + " caracteres.");
        }
        if (!nova.equals(confirmacao)) {
            throw new IllegalArgumentException("As duas palavras-passe não são iguais.");
        }
        if (passwordUtils.verificarPassword(nova, utilizador.getPasswordSalt(), utilizador.getPasswordHash())) {
            throw new IllegalArgumentException("Escolha uma palavra-passe diferente da temporária.");
        }
        String salt = passwordUtils.gerarSalt();
        String hash = passwordUtils.hashPassword(nova, salt);
        if (!utilizadorDAL.definirPassword(utilizador.getId(), hash, salt, false)) {
            throw new IllegalArgumentException("A conta já não existe.");
        }
        utilizador.definirPassword(hash, salt, false);
    }
}
