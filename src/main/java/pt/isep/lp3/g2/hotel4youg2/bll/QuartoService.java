package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.QuartoRepositorio;
import pt.isep.lp3.g2.hotel4youg2.dal.QuartoRepositorioBD;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.TipoQuarto;

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

}
