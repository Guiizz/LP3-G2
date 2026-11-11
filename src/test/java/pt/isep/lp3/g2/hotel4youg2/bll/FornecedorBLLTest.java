package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IFornecedorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FornecedorBLLTest {

    /** DAL em memória, para testar as regras sem base de dados. */
    private static class FornecedorDALFalso implements IFornecedorDAL {
        private final Map<String, Fornecedor> fornecedores = new HashMap<>();
        private int passwordsDefinidas;
        private boolean passwordFoiDefinida = true;

        @Override
        public Optional<Fornecedor> procurarPorCodigo(String codigo) {
            return Optional.ofNullable(fornecedores.get(codigo));
        }

        @Override
        public int criar(Fornecedor fornecedor) {
            fornecedor.setId(fornecedores.size() + 1);
            fornecedores.put(fornecedor.getCodigo(), fornecedor);
            return fornecedor.getId();
        }

        @Override
        public boolean definirPassword(int fornecedorId, String passwordHash, String passwordSalt) {
            passwordsDefinidas++;
            return passwordFoiDefinida;
        }
    }

    private static final Utilizador GESTOR = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);
    private static final Utilizador RECECAO = new Utilizador(2, 2, "rececao", "h", "s", Perfil.RECECIONISTA, true);

    private final PasswordUtils passwordUtils = new PasswordUtils();
    private FornecedorDALFalso fornecedorDAL;
    private FornecedorBLL instance;

    @BeforeEach
    void preparar() {
        fornecedorDAL = new FornecedorDALFalso();
        instance = new FornecedorBLL(fornecedorDAL, passwordUtils);
    }

    // tenta criar o fornecedor e devolve a mensagem do erro; devolve null se não houve erro
    private String mensagemDoErro(Utilizador quemAge, String codigo, String nome, String password) {
        try {
            instance.criar(quemAge, codigo, nome, password);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
        return null;
    }

    @Test
    public void testCriar() {
        System.out.println("criar");
        Fornecedor result = instance.criar(GESTOR, " F-1 ", "Fornecedor Um", "Forn#2026");
        assertEquals("F-1", result.getCodigo());
        assertEquals("Fornecedor Um", result.getNome());
        assertEquals(result, fornecedorDAL.fornecedores.get("F-1"));
    }

    @Test
    public void testCriarGuardaSoOHashDaPassword() {
        System.out.println("criar (a password não fica em claro)");
        Fornecedor result = instance.criar(GESTOR, "F-1", "Fornecedor Um", "Forn#2026");
        assertNotNull(result.getPasswordSalt());
        assertTrue(passwordUtils.verificarPassword("Forn#2026", result.getPasswordSalt(), result.getPasswordHash()));
        assertFalse(result.getPasswordHash().contains("Forn#2026"));
    }

    @Test
    public void testCriarSoOGestor() {
        System.out.println("criar (rececionista não pode)");
        String expResult = "Só o gestor pode criar fornecedores.";
        String result = mensagemDoErro(RECECAO, "F-1", "Fornecedor Um", "Forn#2026");
        assertEquals(expResult, result);
        assertEquals(0, fornecedorDAL.fornecedores.size());
    }

    @Test
    public void testCriarSemSessao() {
        System.out.println("criar (sem sessão)");
        String expResult = "Só o gestor pode criar fornecedores.";
        String result = mensagemDoErro(null, "F-1", "Fornecedor Um", "Forn#2026");
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarComCodigoRepetido() {
        System.out.println("criar (código já tem conta)");
        instance.criar(GESTOR, "F-1", "Fornecedor Um", "Forn#2026");
        String expResult = "Já existe um fornecedor com o código F-1.";
        String result = mensagemDoErro(GESTOR, "F-1", "Outro nome", "Forn#2027");
        assertEquals(expResult, result);
        assertEquals(1, fornecedorDAL.fornecedores.size());
    }

    @Test
    public void testCriarParaFornecedorSemConta() {
        System.out.println("criar (fornecedor vindo de uma entrega, sem conta)");
        Fornecedor semConta = new Fornecedor(5, "F-2", "Fornecedor Dois", "Rua A", "", "Porto", "4000", "Portugal", "PT");
        fornecedorDAL.fornecedores.put("F-2", semConta);
        Fornecedor result = instance.criar(GESTOR, "F-2", "Fornecedor Dois", "Forn#2026");
        assertEquals(1, fornecedorDAL.passwordsDefinidas);
        assertNotNull(result.getPasswordHash());
        assertEquals(1, fornecedorDAL.fornecedores.size());
    }

    @Test
    public void testCriarQuandoOutroGestorDefiniuAPasswordEntretanto() {
        System.out.println("criar (outro gestor definiu a password ao mesmo tempo)");
        Fornecedor semConta = new Fornecedor(5, "F-2", "Fornecedor Dois", "Rua A", "", "Porto", "4000", "Portugal", "PT");
        fornecedorDAL.fornecedores.put("F-2", semConta);
        fornecedorDAL.passwordFoiDefinida = false;
        String expResult = "Já existe um fornecedor com o código F-2.";
        String result = mensagemDoErro(GESTOR, "F-2", "Fornecedor Dois", "Forn#2026");
        assertEquals(expResult, result);
    }

    @Test
    public void testCriarSemCodigo() {
        System.out.println("criar (sem código)");
        assertEquals("Escreva o código do fornecedor.", mensagemDoErro(GESTOR, " ", "Nome", "Forn#2026"));
    }

    @Test
    public void testCriarSemNome() {
        System.out.println("criar (sem nome)");
        assertEquals("Escreva o nome do fornecedor.", mensagemDoErro(GESTOR, "F-1", "", "Forn#2026"));
    }

    @Test
    public void testCriarComPasswordCurta() {
        System.out.println("criar (password curta)");
        assertEquals("A password tem de ter pelo menos 8 caracteres.", mensagemDoErro(GESTOR, "F-1", "Nome", "curta"));
    }

    @Test
    public void testCriarComCodigoDemasiadoLongo() {
        System.out.println("criar (código com mais de 30 caracteres)");
        String codigoLongo = "X".repeat(31);
        assertEquals("O código pode ter no máximo 30 caracteres.",
                mensagemDoErro(GESTOR, codigoLongo, "Nome", "Forn#2026"));
        assertEquals(0, fornecedorDAL.fornecedores.size());
    }
}
