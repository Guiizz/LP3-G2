package pt.isep.lp3.g2.hotel4youg2.dal;

import jdk.jshell.execution.Util;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

public interface IFuncionarioDAL {

    boolean existeNif(String nif);

    boolean existeNomeUtilizador(String nomeUtilizador);

    void criarConta(Funcionario funcionario, Utilizador utilizador);
}
