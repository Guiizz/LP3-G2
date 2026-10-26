package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.TarifaRepositorio;
import pt.isep.lp3.g2.hotel4youg2.dal.TarifaRepositorioBD;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Tarifa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.math.BigDecimal;
import java.util.List;

/**
 * Regras de negócio das tarifas: consultar e alterar o preço por noite.
 */
public class TarifaService {

    private final TarifaRepositorio repositorio;

    /**
     * Cria o serviço ligado à base de dados.
     */
    public TarifaService() {
        this(new TarifaRepositorioBD());
    }

    /**
     * Cria o serviço com outro repositório (usado nos testes).
     *
     * @param repositorio de onde vêm as tarifas
     */
    public TarifaService(TarifaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * @return todas as tarifas
     */
    public List<Tarifa> listar() {
        return repositorio.listar();
    }

    /**
     * Altera o preço por noite de uma tarifa. Só o gestor o pode fazer.
     *
     * @param perfilAtual o perfil de quem está a usar a aplicação
     * @param tipo        o tipo de quarto da tarifa
     * @param vista       a vista da tarifa
     * @param novoValor   o novo preço por noite (maior que zero)
     * @throws IllegalStateException    se quem altera não for gestor
     * @throws IllegalArgumentException se o preço não for válido ou a tarifa não existir
     */
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
