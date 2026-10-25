package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.QuartoRepositorio;
import pt.isep.lp3.g2.hotel4youg2.dal.QuartoRepositorioBD;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.Vista;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QuartoService {
    private final QuartoRepositorio repositorio;

    public QuartoService() {
        this(new QuartoRepositorioBD());
    }

    public QuartoService(QuartoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

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
