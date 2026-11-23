package pt.isep.lp3.g2.hotel4youg2.dal;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FuncionarioDALTest {

    private FuncionarioDAL instance;
    private int pessoaId;
    private int numero;

    @BeforeEach
    public void setUp() {
        try {
            List<Integer> ids = ConexaoBD.select("SELECT TOP 1 pessoaId FROM Funcionario ORDER BY numeroFuncionario",
                    new InteiroMapper());
            Assumptions.assumeTrue(!ids.isEmpty(), "Faltam funcionários na BD");
            pessoaId = ids.get(0);
            numero = ConexaoBD.select("SELECT numeroFuncionario FROM Funcionario WHERE pessoaId = ?",
                    new InteiroMapper(), pessoaId).get(0);
        } catch (DataAccessException e) {
            Assumptions.abort("Sem acesso à BD neste computador: " + e.getMessage());
        }
        instance = new FuncionarioDAL();
    }

    @Test
    public void testProcurarPorNumero() {
        System.out.println("procurarPorNumero");
        int expResult = pessoaId;
        int result = instance.procurarPorNumero(numero).getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorNumeroTemOCartao() {
        System.out.println("procurarPorNumero (o cartão é F e o número de funcionário)");
        String expResult = "F" + numero;
        String result = instance.procurarPorNumero(numero).getCartao();
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorNumeroQueNaoExiste() {
        System.out.println("procurarPorNumero (número que não existe)");
        Funcionario expResult = null;
        Funcionario result = instance.procurarPorNumero(-1);
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorId() {
        System.out.println("procurarPorId");
        int expResult = numero;
        int result = instance.procurarPorId(pessoaId).getNumeroFuncionario();
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorIdQueNaoExiste() {
        System.out.println("procurarPorId (id que não existe)");
        Funcionario expResult = null;
        Funcionario result = instance.procurarPorId(-1);
        assertEquals(expResult, result);
    }

    private int funcionarioComConta(int ativa) {
        List<Integer> ids = ConexaoBD.select("SELECT TOP 1 funcionarioId FROM Utilizador WHERE ativo = ? "
                + "ORDER BY funcionarioId", new InteiroMapper(), ativa);
        Assumptions.assumeTrue(!ids.isEmpty(), "Não há nenhuma conta nesse estado na BD");
        return ids.get(0);
    }

    @Test
    public void testTemContaAtiva() {
        System.out.println("temContaAtiva (conta ativa)");
        boolean expResult = true;
        boolean result = instance.temContaAtiva(funcionarioComConta(1));
        assertEquals(expResult, result);
    }

    @Test
    public void testTemContaAtivaContaDesativada() {
        System.out.println("temContaAtiva (conta desativada)");
        boolean expResult = false;
        boolean result = instance.temContaAtiva(funcionarioComConta(0));
        assertEquals(expResult, result);
    }

    @Test
    public void testTemContaAtivaSemConta() {
        System.out.println("temContaAtiva (pessoa sem conta)");
        boolean expResult = false;
        boolean result = instance.temContaAtiva(-1);
        assertEquals(expResult, result);
    }

    private boolean contem(List<Funcionario> funcionarios, int id) {
        for (Funcionario funcionario : funcionarios) {
            if (funcionario.getId() == id) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testListarComContaAtiva() {
        System.out.println("listarComContaAtiva (traz quem tem a conta ativa)");
        boolean expResult = true;
        boolean result = contem(instance.listarComContaAtiva(), funcionarioComConta(1));
        assertEquals(expResult, result);
    }

    @Test
    public void testListarComContaAtivaSemContasDesativadas() {
        System.out.println("listarComContaAtiva (não traz as contas desativadas)");
        boolean expResult = false;
        boolean result = contem(instance.listarComContaAtiva(), funcionarioComConta(0));
        assertEquals(expResult, result);
    }

    @Test
    public void testListarComContaAtivaPorNumero() {
        System.out.println("listarComContaAtiva (por ordem do número de funcionário)");
        List<Integer> result = new ArrayList<>();
        for (Funcionario funcionario : instance.listarComContaAtiva()) {
            result.add(funcionario.getNumeroFuncionario());
        }
        List<Integer> expResult = new ArrayList<>(result);
        Collections.sort(expResult);
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorIdTemOPerfilDaConta() {
        System.out.println("procurarPorId (vem com o perfil da conta)");
        Funcionario funcionario = instance.procurarPorId(pessoaId);
        Assumptions.assumeTrue(funcionario.getPerfil() != null, "O funcionário não tem conta");
        int expResult = 1;
        int result = ConexaoBD.select("SELECT COUNT(*) FROM Utilizador WHERE funcionarioId = ? AND perfil = ?",
                new InteiroMapper(), pessoaId, funcionario.getPerfil().name()).get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testProcurarPorIdEPorNumero() {
        System.out.println("procurarPorId e procurarPorNumero (dão o mesmo funcionário)");
        Funcionario expResult = instance.procurarPorId(pessoaId);
        Funcionario result = instance.procurarPorNumero(numero);
        assertEquals(expResult, result);
    }
}
