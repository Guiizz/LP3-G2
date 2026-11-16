package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.FornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.util.Optional;

/**
 * Regras de autenticação dos fornecedores, que entram com o código e a password (US38).
 */
public class AutenticacaoFornecedorBLL {

    private final IFornecedorDAL fornecedorDAL;
    private final PasswordUtils passwordUtils;

    /**
     * Cria a camada de negócio da autenticação dos fornecedores, ligada à base de dados.
     */
    public AutenticacaoFornecedorBLL() {
        this(new FornecedorDAL(), new PasswordUtils());
    }

    /**
     * Cria a camada de negócio com outro DAL (usado nos testes, com um DAL em memória).
     *
     * @param fornecedorDAL onde procurar os fornecedores
     * @param passwordUtils como verificar as passwords
     */
    public AutenticacaoFornecedorBLL(IFornecedorDAL fornecedorDAL, PasswordUtils passwordUtils) {
        this.fornecedorDAL = fornecedorDAL;
        this.passwordUtils = passwordUtils;
    }

    /**
     * Autentica um fornecedor com o código e a password.
     *
     * @param codigo   o código do fornecedor escrito no ecrã de login
     * @param password a password escrita no ecrã de login
     * @return o fornecedor autenticado
     * @throws IllegalArgumentException se algum campo estiver vazio, se o fornecedor não existir,
     *                                  se ainda não tiver conta ou se a password estiver errada
     */
    public Fornecedor autenticar(String codigo, String password) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Escreva o código de fornecedor.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Escreva a password.");
        }

        String codigoLimpo = codigo.trim();
        Optional<Fornecedor> encontrado = fornecedorDAL.procurarPorCodigo(codigoLimpo);
        if (encontrado.isEmpty()) {
            throw new IllegalArgumentException("Não existe nenhum fornecedor com o código \"" + codigoLimpo + "\".");
        }
        Fornecedor fornecedor = encontrado.get();

        if (fornecedor.getPasswordHash() == null || fornecedor.getPasswordSalt() == null) {
            throw new IllegalArgumentException("Este fornecedor ainda não tem conta. Contacte o hotel.");
        }
        if (!passwordUtils.verificarPassword(password, fornecedor.getPasswordSalt(), fornecedor.getPasswordHash())) {
            throw new IllegalArgumentException("A password está errada.");
        }
        return fornecedor;
    }
}
