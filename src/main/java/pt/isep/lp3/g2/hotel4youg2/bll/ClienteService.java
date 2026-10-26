package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.ClienteRepositorio;
import pt.isep.lp3.g2.hotel4youg2.dal.ClienteRepositorioBD;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class ClienteService {
    private final ClienteRepositorio repositorio;

    public ClienteService() {
        this(new ClienteRepositorioBD());
    }

    public ClienteService(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Cliente registar(String nome, String nif, LocalDate dataNascimento,
                            String telemovel, String email, String cc, String morada) {
        nome = limpar(nome);
        nif = limpar(nif);
        telemovel = limpar(telemovel);
        email = limpar(email);
        cc = limpar(cc);
        morada = limpar(morada);

        if (nif == null) {
            throw new ValidacaoException("O NIF é obrigatório");
        }
        if (!nif.matches("\\d{9}")) {
            throw new ValidacaoException("O NIF tem de ter exatamente 9 dígitos");
        }
        validarDados(nome, dataNascimento, telemovel, email, cc, morada);

        if (repositorio.existeNif(nif)) {
            throw new ValidacaoException("Já existe uma pessoa registada com o NIF " + nif);
        }

        Cliente cliente = new Cliente(0, nome, cc, nif, morada, telemovel, dataNascimento, email);
        repositorio.inserirCliente(cliente);
        return cliente;
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
        if (!repositorio.atualizarCliente(alterado)) {
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

    public List<Cliente> pesquisar(String termo) {
        termo = limpar(termo);
        if (termo == null) {
            termo = "";
        }
        return repositorio.pesquisarClientes(termo);
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
