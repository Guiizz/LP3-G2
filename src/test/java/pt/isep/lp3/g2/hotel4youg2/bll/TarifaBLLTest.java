package pt.isep.lp3.g2.hotel4youg2.bll;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.dal.ITarifaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TarifaBLLTest {

    private static class TarifaDALFalsa implements ITarifaDAL {
        boolean tarifaExiste = true;
        int chamadas = 0;

        @Override
        public List<Tarifa> listar() {
            return new ArrayList<>();
        }

        @Override
        public boolean atualizarValor(TipoQuarto tipo, Vista vista, BigDecimal novoValor) {
            chamadas++;
            return tarifaExiste;
        }
    }

    @Test
    void gestorAlteraPrecoValido() {
        TarifaDALFalsa repo = new TarifaDALFalsa();
        new TarifaBLL(repo).alterarPreco(Perfil.GESTOR, TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("120.50"));
        assertEquals(1, repo.chamadas);
    }

    @Test
    void rececionistaNaoPodeAlterar() {
        TarifaDALFalsa repo = new TarifaDALFalsa();
        TarifaBLL servico = new TarifaBLL(repo);
        assertThrows(IllegalStateException.class,
                () -> servico.alterarPreco(Perfil.RECECIONISTA, TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("100")));
        assertEquals(0, repo.chamadas);
    }

    @Test
    void precoNegativoERecusado() {
        TarifaDALFalsa repo = new TarifaDALFalsa();
        TarifaBLL servico = new TarifaBLL(repo);
        assertThrows(IllegalArgumentException.class,
                () -> servico.alterarPreco(Perfil.GESTOR, TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("-5")));
        assertEquals(0, repo.chamadas);
    }

    @Test
    void precoZeroERecusado() {
        TarifaDALFalsa repo = new TarifaDALFalsa();
        TarifaBLL servico = new TarifaBLL(repo);
        assertThrows(IllegalArgumentException.class,
                () -> servico.alterarPreco(Perfil.GESTOR, TipoQuarto.DUPLO, Vista.MAR, BigDecimal.ZERO));
    }

    @Test
    void precoVazioERecusado() {
        TarifaDALFalsa repo = new TarifaDALFalsa();
        TarifaBLL servico = new TarifaBLL(repo);
        assertThrows(IllegalArgumentException.class,
                () -> servico.alterarPreco(Perfil.GESTOR, TipoQuarto.DUPLO, Vista.MAR, null));
    }

    @Test
    void tarifaInexistenteDaErro() {
        TarifaDALFalsa repo = new TarifaDALFalsa();
        repo.tarifaExiste = false;
        TarifaBLL servico = new TarifaBLL(repo);
        assertThrows(IllegalArgumentException.class,
                () -> servico.alterarPreco(Perfil.GESTOR, TipoQuarto.DUPLO, Vista.MAR, new BigDecimal("80")));
    }
}