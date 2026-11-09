package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.util.List;

/**
 * Acesso aos dados dos clientes.
 */
public interface IClienteDAL {

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

    /**
     * @param nif o NIF do cliente
     * @return o cliente com este NIF, ou {@code null} se não existir
     */
    Cliente procurarPorNif(String nif);

    /**
     * Grava as alterações aos dados de um cliente que já existe.
     * O NIF não é alterado.
     *
     * @param c o cliente, com o id e os dados novos
     * @return {@code true} se o cliente existia e foi alterado
     */
    boolean atualizarCliente(Cliente c);

    /**
     * Procura um funcionário com este NIF que ainda não seja cliente (US43).
     *
     * @param nif o NIF, com 9 algarismos
     * @return o funcionário com os seus dados pessoais, ou {@code null} se não existir ou já for cliente
     */
    Cliente procurarFuncionarioNaoCliente(String nif);

    /**
     * Regista um funcionário como cliente, sem criar outra pessoa (US43). Os dados vazios do formulário
     * não apagam os que o funcionário já tem, e o nome não muda. Só regista se ainda não for cliente,
     * mesmo que outro rececionista o faça ao mesmo tempo.
     *
     * @param c o funcionário, com o id da pessoa e os dados do formulário
     * @return {@code true} se passou a cliente; {@code false} se já era cliente
     */
    boolean tornarCliente(Cliente c);
}
