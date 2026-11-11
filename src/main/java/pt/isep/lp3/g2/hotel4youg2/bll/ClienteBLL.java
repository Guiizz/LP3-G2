package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.ClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Regras de negócio dos clientes: registar (US13), pesquisar (US14), editar (US19)
 * e registar como cliente um funcionário do hotel (US43).
 */
public class ClienteBLL {
    private final IClienteDAL clienteDAL;

    /**
     * Cria o serviço ligado à base de dados.
     */
    public ClienteBLL() {
        this(new ClienteDAL());
    }

    /**
     * Cria o serviço com outro repositório (usado nos testes).
     *
     * @param clienteDAL de onde vêm e para onde vão os clientes
     */
    public ClienteBLL(IClienteDAL clienteDAL) {
        this.clienteDAL = clienteDAL;
    }

    /**
     * Regista um cliente novo (US13). Se o NIF for de um funcionário que ainda não é cliente,
     * a pessoa fica também cliente: o nome dela não muda, os campos vazios não apagam o que já
     * existe e a conta dela não muda (US43).
     *
     * @param nome           o nome (obrigatório)
     * @param nif            o NIF, com 9 dígitos, ainda não registado ou de um funcionário
     * @param dataNascimento a data de nascimento (obrigatória, pelo menos 18 anos)
     * @param telemovel      o telemóvel (obrigatório se não houver email)
     * @param email          o email (obrigatório se não houver telemóvel)
     * @param cc             o número do CC (opcional)
     * @param morada         a morada (opcional)
     * @return o cliente gravado, já com o id
     * @throws ValidacaoException se algum dado não for válido ou o NIF já existir
     */
    public Cliente registar(String nome, String nif, LocalDate dataNascimento,
                            String telemovel, String email, String cc, String morada) {
        nome = limpar(nome);
        nif = limpar(nif);
        telemovel = limpar(telemovel);
        email = limpar(email);
        cc = limpar(cc);
        morada = limpar(morada);

        validarNif(nif);
        validarDados(nome, dataNascimento, telemovel, email, cc, morada);

        Cliente funcionario = clienteDAL.procurarFuncionarioNaoCliente(nif);
        if (funcionario != null) {
            Cliente cliente = new Cliente(funcionario.getId(), funcionario.getNome(), primeiroPreenchido(cc, funcionario.getCc()),
                    nif, primeiroPreenchido(morada, funcionario.getMorada()),
                    primeiroPreenchido(telemovel, funcionario.getTelemovel()), dataNascimento,
                    primeiroPreenchido(email, funcionario.getEmail()));
            if (!clienteDAL.tornarCliente(cliente)) {
                throw new ValidacaoException("Este funcionário já foi registado como cliente");
            }
            return cliente;
        }
        if (clienteDAL.existeNif(nif)) {
            throw new ValidacaoException("Já existe uma pessoa registada com o NIF " + nif);
        }

        Cliente cliente = new Cliente(0, nome, cc, nif, morada, telemovel, dataNascimento, email);
        clienteDAL.inserirCliente(cliente);
        return cliente;
    }

    /**
     * Procura um funcionário com este NIF que ainda não seja cliente (US43), para o formulário
     * "Registar cliente" o preencher com os dados que o hotel já tem.
     *
     * @param nif o NIF escrito no formulário
     * @return o funcionário, com os seus dados, ou {@code null} se o NIF não tiver 9 algarismos,
     *         não for de um funcionário ou ele já for cliente
     */
    public Cliente funcionarioComNif(String nif) {
        nif = limpar(nif);
        if (nif == null || !nif.matches("\\d{9}")) {
            return null;
        }
        return clienteDAL.procurarFuncionarioNaoCliente(nif);
    }

    private static String primeiroPreenchido(String doFormulario, String jaGuardado) {
        return doFormulario != null ? doFormulario : jaGuardado;
    }

    /**
     * Altera os dados de um cliente já registado (US19).
     * O NIF não pode ser alterado e aplicam-se as mesmas validações do registo.
     * Se alguma validação falhar, o cliente fica como estava.
     *
     * @param cliente        o cliente escolhido na pesquisa
     * @param nome           o nome (obrigatório)
     * @param dataNascimento a data de nascimento (obrigatória, pelo menos 18 anos)
     * @param telemovel      o telemóvel (obrigatório se não houver email)
     * @param email          o email (obrigatório se não houver telemóvel)
     * @param cc             o número do CC (opcional)
     * @param morada         a morada (opcional)
     * @return o mesmo cliente, já com os dados novos
     * @throws ValidacaoException se não houver cliente escolhido, algum dado for inválido
     *                            ou o cliente já não existir na base de dados
     */
    public Cliente editar(Cliente cliente, String nome, LocalDate dataNascimento,
                          String telemovel, String email, String cc, String morada) {
        if (cliente == null) {
            throw new ValidacaoException("Escolha um cliente na pesquisa");
        }
        nome = limpar(nome);
        telemovel = limpar(telemovel);
        email = limpar(email);
        cc = limpar(cc);
        morada = limpar(morada);
        validarDados(nome, dataNascimento, telemovel, email, cc, morada);

        Cliente alterado = new Cliente(cliente.getId(), nome, cc, cliente.getNif(), morada, telemovel,
                dataNascimento, email);
        if (!clienteDAL.atualizarCliente(alterado)) {
            throw new ValidacaoException("O cliente já não existe");
        }

        cliente.setNome(nome);
        cliente.setDataNascimento(dataNascimento);
        cliente.setTelemovel(telemovel);
        cliente.setEmail(email);
        cliente.setCc(cc);
        cliente.setMorada(morada);
        return cliente;
    }

    /** Validações comuns ao registo e à edição (tudo menos o NIF). */
    private void validarDados(String nome, LocalDate dataNascimento, String telemovel,
                              String email, String cc, String morada) {
        if (nome == null) {
            throw new ValidacaoException("O nome é obrigatório");
        }
        validarTamanho(nome, 100, "O nome");

        if (dataNascimento == null) {
            throw new ValidacaoException("A data de nascimento é obrigatória");
        }
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new ValidacaoException("A data de nascimento não pode ser no futuro");
        }
        if (Period.between(dataNascimento, LocalDate.now()).getYears() < 18) {
            throw new ValidacaoException("O cliente tem de ter pelo menos " + 18 + " anos");
        }

        if (telemovel == null && email == null) {
            throw new ValidacaoException("Indique pelo menos o telemóvel ou o email");
        }
        if (telemovel != null && !telemovel.matches("9\\d{8}")) {
            throw new ValidacaoException("O telemóvel tem de ter 9 dígitos e começar por 9");
        }
        if (email != null && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ValidacaoException("O email não é válido");
        }
        validarTamanho(email, 100, "O email");
        validarTamanho(cc, 12, "O CC");
        validarTamanho(morada, 200, "A morada");
    }

    /**
     * Pesquisa clientes por parte do nome ou pelo NIF completo (US14).
     *
     * @param termo o texto a procurar; vazio mostra todos os clientes
     * @return os clientes encontrados, ordenados por nome
     */
    public List<Cliente> pesquisar(String termo) {
        termo = limpar(termo);
        if (termo == null) {
            termo = "";
        }
        return clienteDAL.pesquisarClientes(termo);
    }

    /**
     * Procura um cliente pelo NIF (US14). O formato do NIF é validado
     * antes de ir à base de dados.
     *
     * @param nif o NIF a procurar
     * @return o cliente com este NIF, ou {@code null} se não existir
     * @throws ValidacaoException se o NIF estiver vazio ou não tiver 9 dígitos
     */
    public Cliente pesquisarPorNif(String nif) {
        nif = limpar(nif);
        validarNif(nif);
        return clienteDAL.procurarPorNif(nif);
    }

    private void validarNif(String nif) {
        if (nif == null) {
            throw new ValidacaoException("O NIF é obrigatório");
        }
        if (!nif.matches("\\d{9}")) {
            throw new ValidacaoException("O NIF tem de ter exatamente 9 dígitos");
        }
    }

    private String limpar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private void validarTamanho(String texto, int maximo, String campo) {
        if (texto != null && texto.length() > maximo) {
            throw new ValidacaoException(campo + " não pode ter mais de " + maximo + " caracteres");
        }
    }
}
