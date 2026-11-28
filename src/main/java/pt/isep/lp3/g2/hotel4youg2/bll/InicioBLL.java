package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.ResumoDoDia;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoEntrega;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class InicioBLL {

    private final ReservaBLL reservaBLL;
    private final PedidoExtraBLL pedidoExtraBLL;
    private final EntregaBLL entregaBLL;

    public InicioBLL() {
        this(new ReservaBLL(), new PedidoExtraBLL(), new EntregaBLL());
    }

    public InicioBLL(ReservaBLL reservaBLL, PedidoExtraBLL pedidoExtraBLL, EntregaBLL entregaBLL) {
        this.reservaBLL = reservaBLL;
        this.pedidoExtraBLL = pedidoExtraBLL;
        this.entregaBLL = entregaBLL;
    }

    public ResumoDoDia resumoDoDia(Utilizador quemAge) {
        return resumoDoDia(quemAge, LocalDate.now());
    }

    public ResumoDoDia resumoDoDia(Utilizador quemAge, LocalDate hoje) {
        exigirRececao(quemAge);
        List<Reserva> reservas = reservaBLL.listar();
        int pedidos = pedidoExtraBLL.listarPendentes(quemAge).size();
        int entregas = entregaBLL.listarParaHotel(quemAge, EstadoEntrega.PENDENTE).size();
        return new ResumoDoDia(ReservaBLL.contarCheckinsDeHoje(reservas, hoje),
                ReservaBLL.contarCheckoutsDeHoje(reservas, hoje),
                ReservaBLL.contarChegadasDeHoje(reservas, hoje),
                pedidos, entregas);
    }

    public List<Reserva> hojeNaRececao(Utilizador quemAge, LocalDate hoje) {
        exigirRececao(quemAge);
        return ReservaBLL.listarHojeNaRececao(reservaBLL.listar(), hoje);
    }

    public static String saudacao(LocalTime agora) {
        int hora = agora.getHour();
        if (hora >= 6 && hora < 12) {
            return "Bom dia";
        }
        if (hora >= 12 && hora < 20) {
            return "Boa tarde";
        }
        return "Boa noite";
    }

    private void exigirRececao(Utilizador quemAge) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Só a receção e o gestor veem o que há para fazer hoje.");
        }
    }
}