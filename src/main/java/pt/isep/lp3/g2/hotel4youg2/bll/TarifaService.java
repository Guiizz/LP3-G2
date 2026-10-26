package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.TarifaRepositorio;
import pt.isep.lp3.g2.hotel4youg2.dal.TarifaRepositorioBD;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.math.BigDecimal;
import java.util.List;

public class TarifaService {

    private final TarifaRepositorio repositorio;

    public TarifaService() {
        this(new TarifaRepositorioBD());
    }

    public TarifaService(TarifaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Tarifa> listar() {
        return repositorio.listar();
    }

    public void alterarPreco(Perfil perfilAtual, TipoQuarto tipo, Vista vista, BigDecimal novoValor) {
        if (perfilAtual != Perfil.GESTOR) {
            throw new IllegalStateException("Só o gestor pode alterar tarifas");
        }
        if (novoValor == null) {
            throw new IllegalArgumentException("O preço é obrigatório");
        }
        if (novoValor.signum() <= 0) {
            throw new IllegalArgumentException("O preço tem de ser maior que zero");
        }
        if (!repositorio.atualizarValor(tipo, vista, novoValor)) {
            throw new IllegalArgumentException("A tarifa não existe");
        }
    }
}
