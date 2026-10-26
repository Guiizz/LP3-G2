package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.UtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

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
        Utilizador utilizador = utilizadorDAL.procurarPorNomeUtilizador(nome)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Não existe nenhuma conta com o nome de utilizador \"" + nome + "\"."));

        if (!passwordUtils.verificarPassword(password, utilizador.getPasswordSalt(), utilizador.getPasswordHash())) {
            throw new IllegalArgumentException("A password está errada.");
        }
        if (!utilizador.isAtivo()) {
            throw new IllegalArgumentException("Esta conta está desativada. Contacte o gestor.");
        }
        return utilizador;
    }
}
