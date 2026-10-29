package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.ISugestaoQueixaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.SugestaoQueixaDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.SugestaoQueixa;
import pt.isep.lp3.g2.hotel4youg2.model.TipoSugestao;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

public class SugestaoQueixaBLL {

    public static final int MAXIMO_DESCRICAO = 1000;

    private final ISugestaoQueixaDAL sugestaoDAL;

    public SugestaoQueixaBLL() {
        this(new SugestaoQueixaDAL());
    }

    public SugestaoQueixaBLL(ISugestaoQueixaDAL sugestaoDAL) {
        this.sugestaoDAL = sugestaoDAL;
    }

    public SugestaoQueixa registar(Utilizador quemAge, Cliente cliente, TipoSugestao tipo, String descricao) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para registar sugestões e queixas.");
        }
        if (cliente == null) {
            throw new IllegalArgumentException("Procure primeiro o cliente pelo NIF.");
        }
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
        // a data fica a null: é a BD que a regista no momento em que grava
        SugestaoQueixa sugestao = new SugestaoQueixa(0, cliente.getId(), tipo, texto, quemAge.getFuncionarioId(), null);
        sugestaoDAL.inserir(sugestao);
        return sugestao;
    }
}
