package pt.isep.lp3.g2.hotel4youg2.dal.interfaces;

import pt.isep.lp3.g2.hotel4youg2.model.Estadia;
import pt.isep.lp3.g2.hotel4youg2.model.Estatistica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IEstatisticaDAL {

    int contarQuartosAtivos();

    List<Estadia> listarEstadias(LocalDate inicio, LocalDate fim);

    List<Estatistica> receitaPorTipoQuarto(LocalDate inicio, LocalDate fim);

    List<Estatistica> receitaPorServico(LocalDate inicio, LocalDate fim);

    BigDecimal receitaPrevista(LocalDate inicio, LocalDate fim);

    int noitesVendidas(LocalDate inicio, LocalDate fim);

    BigDecimal duracaoMedia(LocalDate inicio, LocalDate fim);

    boolean guardaDataDeCriacao();

    BigDecimal antecedenciaMedia(LocalDate inicio, LocalDate fim);

    List<Estatistica> reservasPorEstado(LocalDate inicio, LocalDate fim);

    List<Estatistica> servicosMaisUsados(LocalDate inicio, LocalDate fim);

    List<Estatistica> materiaisMaisGastos(LocalDate inicio, LocalDate fim);
}
