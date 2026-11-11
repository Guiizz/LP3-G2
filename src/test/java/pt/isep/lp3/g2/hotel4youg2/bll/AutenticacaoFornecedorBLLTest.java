package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class AutenticacaoFornecedorBLLTest {

    /** DAL em memória, para testar as regras sem base de dados. */
    private static class FornecedorDALFalso implements IFornecedorDAL {
        private final Map<String, Fornecedor> fornecedores = new HashMap<>();

        @Override
        public Optional<Fornecedor> procurarPorCodigo(String codigo) {
            return Optional.ofNullable(fornecedores.get(codigo));
        }

        @Override
        public int criar(Fornecedor fornecedor) {
            fornecedores.put(fornecedor.getCodigo(), fornecedor);
            return fornecedor.getId();
        }

        @Override
        public boolean definirPassword(int fornecedorId, String passwordHash, String passwordSalt) {
            return false;
        }
    }

    private final PasswordUtils passwordUtils = new PasswordUtils();
    private FornecedorDALFalso fornecedorDAL;
    private AutenticacaoFornecedorBLL instance;
    private Fornecedor comConta;

    @BeforeEach
    void preparar() {
        fornecedorDAL = new FornecedorDALFalso();
        instance = new AutenticacaoFornecedorBLL(fornecedorDAL, passwordUtils);
        comConta = criarFornecedor("F-1", "Forn#2026");
        fornecedorDAL.fornecedores.put("F-1", comConta);
        fornecedorDAL.fornecedores.put("F-2", new Fornecedor(2, "F-2", "Sem conta", "", "", "", "", "", "PT"));
    }

    private Fornecedor criarFornecedor(String codigo, String password) {
        Fornecedor fornecedor = new Fornecedor(1, codigo, "Fornecedor " + codigo, "", "", "", "", "", "PT");
        String salt = passwordUtils.gerarSalt();
        fornecedor.definirPassword(passwordUtils.hashPassword(password, salt), salt);
        return fornecedor;
    }

    @Test
    public void testAutenticar() {
        System.out.println("autenticar");
        Fornecedor expResult = comConta;
        Fornecedor result = instance.autenticar("F-1", "Forn#2026");
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarIgnoraEspacosNoCodigo() {
        System.out.println("autenticar (espaços no código)");
        Fornecedor expResult = comConta;
        Fornecedor result = instance.autenticar("  F-1 ", "Forn#2026");
        assertEquals(expResult, result);
    }

    @Test
    public void testAutenticarPasswordErrada() {
        System.out.println("autenticar (password errada)");
        try {
            instance.autenticar("F-1", "errada");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("A password está errada.", expected.getMessage());
        }
    }

    @Test
    public void testAutenticarFornecedorQueNaoExiste() {
        System.out.println("autenticar (não existe)");
        try {
            instance.autenticar("XX", "Forn#2026");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Não existe nenhum fornecedor com o código \"XX\".", expected.getMessage());
        }
    }

    @Test
    public void testAutenticarSemConta() {
        System.out.println("autenticar (fornecedor sem password)");
        try {
            instance.autenticar("F-2", "qualquer");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Este fornecedor ainda não tem conta. Contacte o hotel.", expected.getMessage());
        }
    }

    @Test
    public void testAutenticarCamposVazios() {
        System.out.println("autenticar (campos vazios)");
        try {
            instance.autenticar(" ", "Forn#2026");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escreva o código de fornecedor.", expected.getMessage());
        }
        try {
            instance.autenticar("F-1", "");
            fail("Devia ter lançado IllegalArgumentException, mas não lançou");
        } catch (IllegalArgumentException expected) {
            assertEquals("Escreva a password.", expected.getMessage());
        }
    }
}
