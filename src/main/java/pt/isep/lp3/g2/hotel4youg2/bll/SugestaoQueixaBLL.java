package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.IReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ISugestaoQueixaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ReservaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.SugestaoQueixaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.Reserva;
import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoSugestao;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.util.List;

public class SugestaoQueixaBLL {

    public static final int MAXIMO_DESCRICAO = 1000;

    private final ISugestaoQueixaDAL sugestaoDAL;
    private final IReservaDAL reservaDAL;

    public SugestaoQueixaBLL() {
        this(new SugestaoQueixaDAL(), new ReservaDAL());
    }

    public SugestaoQueixaBLL(ISugestaoQueixaDAL sugestaoDAL) {
        this(sugestaoDAL, new ReservaDAL());
    }

    public SugestaoQueixaBLL(ISugestaoQueixaDAL sugestaoDAL, IReservaDAL reservaDAL) {
        this.sugestaoDAL = sugestaoDAL;
        this.reservaDAL = reservaDAL;
    }

    public SugestaoQueixa registar(Utilizador quemAge, Cliente cliente, TipoSugestao tipo, String descricao) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para registar sugestões e queixas.");
        }
        if (cliente == null) {
            throw new IllegalArgumentException("Procure primeiro o cliente pelo NIF.");
        }
        String texto = textoValidado(tipo, descricao);
        // a data fica a null: é a BD que a regista no momento em que grava
        SugestaoQueixa sugestao = new SugestaoQueixa(0, cliente.getId(), tipo, texto, quemAge.getFuncionarioId(), null);
        sugestaoDAL.inserir(sugestao);
        return sugestao;
    }

    public SugestaoQueixa registarPeloCartao(String cartao, TipoSugestao tipo, String descricao) {
        String numero = cartao == null ? "" : cartao.trim().toUpperCase();
        if (numero.isEmpty()) {
            throw new IllegalArgumentException("Escreva o número do cartão.");
        }
        Reserva reserva = reservaDAL.procurarEmCursoPorCartao(numero);
        if (reserva == null) {
            throw new IllegalArgumentException("O cartão " + numero
                    + " não é válido ou é de uma reserva que já não está em curso.");
        }
        String texto = textoValidado(tipo, descricao);
        SugestaoQueixa sugestao = new SugestaoQueixa(0, reserva.getClienteId(), tipo, texto, 0, null);
        sugestaoDAL.inserir(sugestao);
        return sugestao;
    }

    private String textoValidado(TipoSugestao tipo, String descricao) {
        if (tipo == null) {
            throw new IllegalArgumentException("Escolha o tipo: sugestão ou queixa.");
        }
        String texto = descricao == null ? "" : descricao.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("A descrição é obrigatória.");
        }
        if (texto.length() > MAXIMO_DESCRICAO) {
            throw new IllegalArgumentException("A descrição não pode ter mais de " + MAXIMO_DESCRICAO + " caracteres.");
        }
        return texto;
    }

    public List<SugestaoQueixa> listar(Utilizador quemAge, TipoSugestao tipo) {
        if (!Permissoes.eGestor(quemAge)) {
            throw new IllegalStateException("Só o gestor pode consultar as sugestões e queixas.");
        }
        return sugestaoDAL.listar(tipo);
    }
}
