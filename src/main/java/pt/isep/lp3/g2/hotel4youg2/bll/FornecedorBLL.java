package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.FornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.util.Optional;

/**
 * Regras de negócio das contas dos fornecedores: só o gestor as cria (US38).
 */
public class FornecedorBLL {

    private static final int TAMANHO_MAXIMO_CODIGO = 30;
    private static final int TAMANHO_MAXIMO_NOME = 100;
    private static final int TAMANHO_MINIMO_PASSWORD = 8;

    private final IFornecedorDAL fornecedorDAL;
    private final PasswordUtils passwordUtils;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public FornecedorBLL() {
        this(new FornecedorDAL(), new PasswordUtils());
    }

    /**
     * Cria o BLL com outro DAL (usado nos testes, com um DAL em memória).
     *
     * @param fornecedorDAL onde os fornecedores são gravados
     * @param passwordUtils para gerar o salt e o hash da password
     */
    public FornecedorBLL(IFornecedorDAL fornecedorDAL, PasswordUtils passwordUtils) {
        this.fornecedorDAL = fornecedorDAL;
        this.passwordUtils = passwordUtils;
    }

    /**
     * Cria a conta de um fornecedor. A password é guardada só em hash. Se o fornecedor já existir
     * por causa de uma entrega, mas ainda não tiver conta, define-lhe a password.
     *
     * @param quemAge  o utilizador com a sessão iniciada
     * @param codigo   o código do fornecedor, igual ao que vem nos ficheiros XML
     * @param nome     o nome do fornecedor
     * @param password a password (pelo menos 8 caracteres)
     * @return o fornecedor com a conta criada
     * @throws IllegalStateException    se quem age não for o gestor
     * @throws IllegalArgumentException se algum dado não for válido ou se o fornecedor já tiver conta
     */
    public Fornecedor criar(Utilizador quemAge, String codigo, String nome, String password) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode criar fornecedores.");
        }
        String codigoLimpo = limpar(codigo);
        String nomeLimpo = limpar(nome);
        if (codigoLimpo == null) {
            throw new IllegalArgumentException("Escreva o código do fornecedor.");
        }
        if (codigoLimpo.length() > TAMANHO_MAXIMO_CODIGO) {
            throw new IllegalArgumentException(
                    "O código pode ter no máximo " + TAMANHO_MAXIMO_CODIGO + " caracteres.");
        }
        if (nomeLimpo == null) {
            throw new IllegalArgumentException("Escreva o nome do fornecedor.");
        }
        if (nomeLimpo.length() > TAMANHO_MAXIMO_NOME) {
            throw new IllegalArgumentException("O nome pode ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }
        if (password == null || password.length() < TAMANHO_MINIMO_PASSWORD) {
            throw new IllegalArgumentException(
                    "A password tem de ter pelo menos " + TAMANHO_MINIMO_PASSWORD + " caracteres.");
        }

        String salt = passwordUtils.gerarSalt();
        String hash = passwordUtils.hashPassword(password, salt);

        Optional<Fornecedor> existente = fornecedorDAL.procurarPorCodigo(codigoLimpo);
        if (existente.isPresent()) {
            Fornecedor fornecedor = existente.get();
            if (fornecedor.getPasswordHash() != null) {
                throw new IllegalArgumentException("Já existe um fornecedor com o código " + codigoLimpo + ".");
            }
            if (!fornecedorDAL.definirPassword(fornecedor.getId(), hash, salt)) {
                throw new IllegalArgumentException("Já existe um fornecedor com o código " + codigoLimpo + ".");
            }
            fornecedor.definirPassword(hash, salt);
            return fornecedor;
        }

        Fornecedor fornecedor = new Fornecedor(0, codigoLimpo, nomeLimpo, null, null, null, null, null, null);
        fornecedor.definirPassword(hash, salt);
        fornecedorDAL.criar(fornecedor);
        return fornecedor;
    }

    private static String limpar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
