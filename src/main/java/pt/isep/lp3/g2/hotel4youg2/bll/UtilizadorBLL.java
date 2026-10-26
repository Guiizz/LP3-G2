package pt.isep.lp3.g2.hotel4youg2.bll;

import jdk.jshell.execution.Util;
import pt.isep.lp3.g2.hotel4youg2.dal.IUtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.UtilizadorDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.sql.Struct;
import java.util.List;

public class UtilizadorBLL {

    private final IUtilizadorDAL utilizadorDAL;

    public UtilizadorBLL() {
        this(new UtilizadorDAL());
    }

    public UtilizadorBLL(IUtilizadorDAL utilizadorDAL) {
        this.utilizadorDAL = utilizadorDAL;
    }

    public List<Utilizador> listar() {
        return utilizadorDAL.listar();
    }

    public void desativar(Utilizador quemAge, Utilizador utilizador) {
        alterarEstado(quemAge, utilizador, false);
    }

    public void reativar(Utilizador quemAge, Utilizador utilizador) {
        alterarEstado(quemAge, utilizador, true);
    }

    private void alterarEstado(Utilizador quemAge, Utilizador utilizador, boolean ativo) {
        if (quemAge == null || quemAge.getPerfil() != Perfil.GESTOR) {
            throw new IllegalStateException("Só o gestor pode gerir contas.");
        }
        if (utilizador == null) {
            throw new IllegalArgumentException("Escolha uma conta.");
        }
        if (!ativo && quemAge.getId() == utilizador.getId()) {
            throw new IllegalArgumentException("Não pode desativar a sua própria conta.");
        }
        if (!utilizadorDAL.atualizarAtivo(utilizador.getId(), ativo)) {
            throw new IllegalArgumentException("A conta não existe.");
        }
    }
}
