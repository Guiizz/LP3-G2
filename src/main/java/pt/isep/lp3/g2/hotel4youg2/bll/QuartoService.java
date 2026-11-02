package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.QuartoRepositorio;
import pt.isep.lp3.g2.hotel4youg2.dal.QuartoRepositorioBD;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio dos quartos: consultar a lista e pesquisar os que estão disponíveis.
 */
public class QuartoService {
    private final QuartoRepositorio repositorio;

    /**
     * Cria o serviço ligado à base de dados.
     */
    public QuartoService() {
        this(new QuartoRepositorioBD());
    }

    /**
     * Cria o serviço com outro repositório (usado nos testes).
     *
     * @param repositorio de onde vêm os quartos
     */
    public QuartoService(QuartoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Lista os quartos, com filtros opcionais.
     *
     * @param tipo o tipo de quarto, ou {@code null} para todos
     * @param piso o piso, ou {@code null} para todos
     * @return os quartos que cumprem os filtros, com a sua tarifa
     */
    public List<Quarto> listar(TipoQuarto tipo, Integer piso) {
        List<Quarto> resultado = new ArrayList<>();
        for (Quarto quarto : repositorio.listarComTarifa()) {
            boolean tipoCerto = tipo == null || quarto.getTipo() == tipo;
            boolean pisoCerto = piso == null || quarto.getPiso() == piso;
            if (tipoCerto && pisoCerto) {
                resultado.add(quarto);
            }
        }
        return resultado;
    }

    /**
     * Pesquisa os quartos livres entre duas datas onde cabem os hóspedes.
     *
     * @param entrada  o dia de chegada (não pode ser no passado)
     * @param saida    o dia de partida (tem de ser depois da entrada)
     * @param hospedes o número de hóspedes (pelo menos 1)
     * @param tipo     o tipo de quarto, ou {@code null} para todos
     * @param vista    a vista, ou {@code null} para todas
     * @return os quartos disponíveis (vazia se não houver nenhum)
     * @throws IllegalArgumentException se as datas ou o número de hóspedes não forem válidos
     */
    public List<Quarto> pesquisarDisponiveis(LocalDate entrada, LocalDate saida, int hospedes, TipoQuarto tipo, Vista vista) {
        validarPesquisa(entrada, saida, hospedes);
        List<Quarto> resultado = new ArrayList<>();
        for (Quarto quarto : repositorio.listarDisponiveis(entrada, saida)) {
            boolean cabem = quarto.getCapacidade() >= hospedes;
            boolean tipoCerto = tipo == null || quarto.getTipo() == tipo;
            boolean vistaCerta = vista == null || quarto.getVista() == vista;
            if (cabem && tipoCerto && vistaCerta) {
                resultado.add(quarto);
            }
        }
        return resultado;
    }

    public List<Quarto> listarTodos(Perfil perfilAtual) {
        exigirGestor(perfilAtual);
        return repositorio.listarTodos();
    }

    public void desativar(Perfil perfilAtual, int numero) {
        exigirGestor(perfilAtual);
        if (repositorio.temReservasFuturas(numero)) {
            throw new IllegalArgumentException("Não é possível desativar o quarto " + numero +
                    ": tem reservas futuras");
        }
        if (!repositorio.atualizarAtivo(numero, false)) {
            throw new IllegalArgumentException("O quarto " + numero + " não existe");
        }
    }

    public void reativar(Perfil perfilAtual, int numero) {
        exigirGestor(perfilAtual);
        if (!repositorio.atualizarAtivo(numero, true)) {
            throw new IllegalArgumentException("O quarto " + numero + " não existe");
        }
    }

    private void exigirGestor(Perfil perfilAtual) {
        if (perfilAtual != Perfil.GESTOR) {
            throw new IllegalStateException("Só o gestor pode gerir quartos");        }
    }

    private void validarPesquisa(LocalDate entrada, LocalDate saida, int hospedes) {
        if (entrada == null || saida == null) {
            throw new IllegalArgumentException("Indique as datas de entrada e de saída.");
        }
        if (entrada.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data de entrada não pode ser no passado.");
        }
        if (!saida.isAfter(entrada)) {
            throw new IllegalArgumentException("A data de saída tem de ser posterior à de entrada.");
        }
        if (hospedes < 1) {
            throw new IllegalArgumentException("O número de hóspedes tem de ser pelo menos 1.");
        }
    }
}
