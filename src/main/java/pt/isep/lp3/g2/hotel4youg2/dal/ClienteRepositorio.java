package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.util.List;

/**
 * Acesso aos dados dos clientes.
 */
public interface ClienteRepositorio {

    /**
     * Grava um cliente novo (na Pessoa e no Cliente) e atualiza o id do objeto.
     *
     * @param c o cliente a gravar
     * @return o id gerado
     */
    int inserirCliente(Cliente c);

    /**
     * @param nif o NIF a procurar
     * @return {@code true} se já existir uma pessoa com este NIF
     */
    boolean existeNif(String nif);

    /**
     * @param termo parte do nome ou o NIF completo
     * @return os clientes encontrados, ordenados por nome
     */
    List<Cliente> pesquisarClientes(String termo);
}
