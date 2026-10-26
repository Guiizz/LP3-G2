package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.IFuncionarioDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioBLLTest {

    private static class DALFalsa implements IFuncionarioDAL {
        boolean nifExiste = false;
        boolean nomeExiste = false;
        int chamadas = 0;
        Funcionario funcionarioGravado;
        Utilizador contaGravada;

        @Override
        public boolean existeNif(String nif) {
            return nifExiste;
        }

        @Override
        public boolean existeNomeUtilizador(String nomeUtilizador) {
            return nomeExiste;
        }

        @Override
        public void criarConta(Funcionario funcionario, Utilizador utilizador) {
            chamadas++;
            funcionarioGravado = funcionario;
            contaGravada = utilizador;
        }
    }

    private final DALFalsa dal = new DALFalsa();
    private final PasswordUtils passwordUtils = new PasswordUtils();
    private final FuncionarioBLL bll = new FuncionarioBLL(dal, passwordUtils);
    private final Utilizador gestor = new Utilizador(1, 1, "gestor", "h", "s", Perfil.GESTOR, true);

    private static Funcionario dados(String nome, String nif, String email, LocalDate nascimento) {
        return new Funcionario(0, nome, null, nif, null, null, nascimento, email, 0, null);
    }

    private void criarValida() {
        bll.criarConta(gestor, dados("Ana Silva", "123456789", null, null),
                "ana.silva", "password123", Perfil.RECECIONISTA);
    }

    @Test
    void gestorCriaContaComSucesso() {
        criarValida();

        assertEquals(1, dal.chamadas);
        assertEquals("Ana Silva", dal.funcionarioGravado.getNome());
        assertEquals(LocalDate.now(), dal.funcionarioGravado.getDataAdmissao());
        assertEquals("ana.silva", dal.contaGravada.getNomeUtilizador());
        assertEquals(Perfil.RECECIONISTA, dal.contaGravada.getPerfil());
        assertTrue(dal.contaGravada.isAtivo());
    }

    @Test
    void passwordGuardadaComHashESaltNaoEmTexto() {
        criarValida();

        Utilizador conta = dal.contaGravada;
        assertNotEquals("password123", conta.getPasswordHash());
        assertEquals(64, conta.getPasswordHash().length());
        assertEquals(32, conta.getPasswordSalt().length());
        assertTrue(passwordUtils.verificarPassword("password123", conta.getPasswordSalt(), conta.getPasswordHash()));
    }

    @Test
    void camposOpcionaisVaziosFicamNulos() {
        bll.criarConta(gestor, new Funcionario(0, "Ana Silva", "  ", "123456789", "", " ", null, "", 0, null),
                "ana.silva", "password123", Perfil.RECECIONISTA);

        Funcionario f = dal.funcionarioGravado;
        assertNull(f.getCc());
        assertNull(f.getMorada());
        assertNull(f.getTelemovel());
        assertNull(f.getEmail());
        assertNull(f.getDataNascimento());
    }

    @Test
    void rececionistaNaoPodeCriarContas() {
        Utilizador rececionista = new Utilizador(2, 2, "rece", "h", "s", Perfil.RECECIONISTA, true);

        assertThrows(IllegalStateException.class, () -> bll.criarConta(rececionista,
                dados("Ana Silva", "123456789", null, null), "ana.silva", "password123", Perfil.RECECIONISTA));
        assertEquals(0, dal.chamadas);
    }

    @Test
    void nomeObrigatorio() {
        assertThrows(IllegalArgumentException.class, () -> bll.criarConta(gestor,
                dados("  ", "123456789", null, null), "ana.silva", "password123", Perfil.RECECIONISTA));
        assertEquals(0, dal.chamadas);
    }

    @Test
    void nifTemDeTerNoveAlgarismos() {
        assertThrows(IllegalArgumentException.class, () -> bll.criarConta(gestor,
                dados("Ana Silva", "12345", null, null), "ana.silva", "password123", Perfil.RECECIONISTA));
        assertThrows(IllegalArgumentException.class, () -> bll.criarConta(gestor,
                dados("Ana Silva", "12345678A", null, null), "ana.silva", "password123", Perfil.RECECIONISTA));
        assertEquals(0, dal.chamadas);
    }

    @Test
    void nifRepetidoDaErro() {
        dal.nifExiste = true;

        assertThrows(IllegalArgumentException.class, this::criarValida);
        assertEquals(0, dal.chamadas);
    }

    @Test
    void nomeDeUtilizadorRepetidoDaErro() {
        dal.nomeExiste = true;

        assertThrows(IllegalArgumentException.class, this::criarValida);
        assertEquals(0, dal.chamadas);
    }

    @Test
    void passwordCurtaDaErro() {
        assertThrows(IllegalArgumentException.class, () -> bll.criarConta(gestor,
                dados("Ana Silva", "123456789", null, null), "ana.silva", "1234567", Perfil.RECECIONISTA));
        assertEquals(0, dal.chamadas);
    }

    @Test
    void perfilObrigatorio() {
        assertThrows(IllegalArgumentException.class, () -> bll.criarConta(gestor,
                dados("Ana Silva", "123456789", null, null), "ana.silva", "password123", null));
    }

    @Test
    void emailInvalidoDaErro() {
        assertThrows(IllegalArgumentException.class, () -> bll.criarConta(gestor,
                dados("Ana Silva", "123456789", "sem-arroba", null), "ana.silva", "password123", Perfil.RECECIONISTA));
    }

    @Test
    void dataDeNascimentoNoFuturoDaErro() {
        assertThrows(IllegalArgumentException.class, () -> bll.criarConta(gestor,
                dados("Ana Silva", "123456789", null, LocalDate.now().plusDays(1)),
                "ana.silva", "password123", Perfil.RECECIONISTA));
    }
}