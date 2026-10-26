package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutenticacaoBLLTest {

    /** DAL em memória, para testar as regras sem base de dados. */
    private static class StubUtilizadorDAL implements IUtilizadorDAL {
        private final Map<String, Utilizador> utilizadores = new HashMap<>();

        void adicionar(Utilizador u) {
            utilizadores.put(u.getNomeUtilizador(), u);
        }

        @Override
        public Optional<Utilizador> procurarPorNomeUtilizador(String nomeUtilizador) {
            return Optional.ofNullable(utilizadores.get(nomeUtilizador));
        }

        @Override
        public List<Utilizador> listar() {
        return List.of();
        }

        @Override
        public boolean atualizarAtivo(int utilizadorId, boolean ativo) {
            return false;
        }
    }

    private final PasswordUtils passwordUtils = new PasswordUtils();
    private StubUtilizadorDAL utilizadorDAL;
    private AutenticacaoBLL autenticacaoBLL;

    @BeforeEach
    void preparar() {
        utilizadorDAL = new StubUtilizadorDAL();
        autenticacaoBLL = new AutenticacaoBLL(utilizadorDAL, passwordUtils);
        utilizadorDAL.adicionar(criarUtilizador(1, "gestor", "Gestor#2026", Perfil.GESTOR, true));
        utilizadorDAL.adicionar(criarUtilizador(2, "antigo", "Antigo#2026", Perfil.RECECIONISTA, false));
    }

    private Utilizador criarUtilizador(int funcionarioId, String nome, String password, Perfil perfil, boolean ativo) {
        String salt = passwordUtils.gerarSalt();
        return new Utilizador(funcionarioId, funcionarioId, nome, passwordUtils.hashPassword(password, salt), salt, perfil, ativo);
    }

    private String erro(String nome, String password) {
        return assertThrows(IllegalArgumentException.class, () -> autenticacaoBLL.autenticar(nome, password)).getMessage();
    }

    @Test
    void entraComCredenciaisCorretas() {
        Utilizador u = autenticacaoBLL.autenticar("gestor", "Gestor#2026");
        assertEquals(Perfil.GESTOR, u.getPerfil());
        assertEquals(1, u.getFuncionarioId());
    }

    @Test
    void ignoraEspacosAntesEDepoisDoNomeDeUtilizador() {
        assertEquals("gestor", autenticacaoBLL.autenticar("  gestor ", "Gestor#2026").getNomeUtilizador());
    }

    @Test
    void nomeVazioPedeONome() {
        assertEquals("Escreva o nome de utilizador.", erro("", "Gestor#2026"));
        assertEquals("Escreva o nome de utilizador.", erro(null, "Gestor#2026"));
    }

    @Test
    void passwordVaziaPedeAPassword() {
        assertEquals("Escreva a password.", erro("gestor", ""));
    }

    @Test
    void contaInexistenteDizQueNaoExiste() {
        assertEquals("Não existe nenhuma conta com o nome de utilizador \"naoexiste\".", erro("naoexiste", "Gestor#2026"));
    }

    @Test
    void passwordErradaDizQueEstaErrada() {
        assertEquals("A password está errada.", erro("gestor", "errada"));
    }

    @Test
    void contaDesativadaNaoEntra() {
        assertEquals("Esta conta está desativada. Contacte o gestor.", erro("antigo", "Antigo#2026"));
    }
}
