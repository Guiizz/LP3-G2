package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.ClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.ContaClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IContaClienteDAL;
import pt.isep.lp3.g2.hotel4youg2.exception.ValidacaoException;
import pt.isep.lp3.g2.hotel4youg2.model.Cliente;
import pt.isep.lp3.g2.hotel4youg2.model.ContaCliente;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.security.SecureRandom;

/**
 * Regras de negócio das contas dos clientes: criar a conta, entrar e escolher a palavra-passe.
 */
public class ContaClienteBLL {

    /** Número mínimo de caracteres da palavra-passe do cliente. */
    public static final int MINIMO_PALAVRA_PASSE = 6;

    private static final int LIMITE_CODIGO = 1000000;

    private final IContaClienteDAL contaClienteDAL;
    private final ClienteBLL clienteBLL;
    private final PasswordUtils passwordUtils;
    private final SecureRandom random = new SecureRandom();

    /**
     * Cria o BLL ligado à base de dados.
     */
    public ContaClienteBLL() {
        this(new ContaClienteDAL(), new ClienteDAL(), new PasswordUtils());
    }

    /**
     * Cria o BLL com outros DAL (usado nos testes).
     *
     * @param contaClienteDAL onde ficam as contas
     * @param clienteDAL      onde procurar os clientes
     * @param passwordUtils   quem gera o salt e o hash
     */
    public ContaClienteBLL(IContaClienteDAL contaClienteDAL, IClienteDAL clienteDAL, PasswordUtils passwordUtils) {
        this.contaClienteDAL = contaClienteDAL;
        this.clienteBLL = new ClienteBLL(clienteDAL);
        this.passwordUtils = passwordUtils;
    }

    /**
     * Cria a conta de um cliente e devolve o código do primeiro acesso.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param nif     o NIF do cliente
     * @return o código de 6 dígitos para dar ao cliente
     * @throws IllegalStateException se quem age não for da receção nem gestor
     * @throws ValidacaoException    se o cliente não existir ou já tiver conta
     */
    public String criarConta(Utilizador quemAge, String nif) {
        exigirRececao(quemAge);
        Cliente cliente = procurarCliente(nif);
        if (contaClienteDAL.procurarPorNif(cliente.getNif()) != null) {
            throw new ValidacaoException("O cliente " + cliente.getNome() + " já tem conta. Se ele se esqueceu "
                    + "da palavra-passe, gere um código novo.");
        }
        String codigo = gerarCodigo();
        String salt = passwordUtils.gerarSalt();
        ContaCliente conta = new ContaCliente(0, cliente.getId(), cliente.getNif(), cliente.getNome(),
                passwordUtils.hashPassword(codigo, salt), salt, true);
        contaClienteDAL.inserir(conta);
        return codigo;
    }

    /**
     * Gera um código novo para um cliente que se esqueceu da palavra-passe.
     *
     * @param quemAge o utilizador com a sessão iniciada
     * @param nif     o NIF do cliente
     * @return o código de 6 dígitos para dar ao cliente
     * @throws IllegalStateException se quem age não for da receção nem gestor
     * @throws ValidacaoException    se o cliente não existir ou ainda não tiver conta
     */
    public String gerarNovoCodigo(Utilizador quemAge, String nif) {
        exigirRececao(quemAge);
        Cliente cliente = procurarCliente(nif);
        ContaCliente conta = contaClienteDAL.procurarPorNif(cliente.getNif());
        if (conta == null) {
            throw new ValidacaoException("O cliente " + cliente.getNome() + " ainda não tem conta. Crie a conta primeiro.");
        }
        String codigo = gerarCodigo();
        String salt = passwordUtils.gerarSalt();
        if (!contaClienteDAL.novoCodigo(conta.getId(), passwordUtils.hashPassword(codigo, salt), salt)) {
            throw new ValidacaoException("A conta do cliente " + cliente.getNome() + " já não existe.");
        }
        return codigo;
    }

    /**
     * Verifica o NIF e a palavra-passe (ou o código do primeiro acesso) do cliente.
     *
     * @param nif      o NIF do cliente
     * @param password a palavra-passe ou o código
     * @return a conta do cliente
     * @throws IllegalArgumentException se faltar algum dado, não houver conta ou a palavra-passe estiver errada
     */
    public ContaCliente autenticar(String nif, String password) {
        if (nif == null || nif.isBlank()) {
            throw new IllegalArgumentException("Escreva o seu NIF.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Escreva a palavra-passe ou o código do primeiro acesso.");
        }
        String nifLimpo = nif.trim();
        ContaCliente conta = contaClienteDAL.procurarPorNif(nifLimpo);
        if (conta == null) {
            throw new IllegalArgumentException("Não existe nenhuma conta de cliente com o NIF " + nifLimpo
                    + ". Peça à receção para a criar.");
        }
        if (!passwordUtils.verificarPassword(password, conta.getPasswordSalt(), conta.getPasswordHash())) {
            throw new IllegalArgumentException(conta.isPrimeiroAcesso()
                    ? "O código do primeiro acesso está errado." : "A palavra-passe está errada.");
        }
        return conta;
    }

    /**
     * Guarda a palavra-passe escolhida pelo cliente no primeiro acesso.
     *
     * @param conta       a conta do cliente
     * @param nova        a palavra-passe nova
     * @param confirmacao a palavra-passe nova, outra vez
     * @throws IllegalStateException se não houver conta
     * @throws ValidacaoException    se a palavra-passe já foi escolhida, for curta, não coincidir ou for igual ao código
     */
    public void definirPalavraPasse(ContaCliente conta, String nova, String confirmacao) {
        if (conta == null) {
            throw new IllegalStateException("Entre com o NIF e o código do primeiro acesso.");
        }
        if (!conta.isPrimeiroAcesso()) {
            throw new ValidacaoException("A palavra-passe desta conta já foi escolhida.");
        }
        if (nova == null || nova.length() < MINIMO_PALAVRA_PASSE) {
            throw new ValidacaoException("A palavra-passe tem de ter pelo menos " + MINIMO_PALAVRA_PASSE
                    + " caracteres.");
        }
        if (!nova.equals(confirmacao)) {
            throw new ValidacaoException("As duas palavras-passe não são iguais.");
        }
        if (passwordUtils.verificarPassword(nova, conta.getPasswordSalt(), conta.getPasswordHash())) {
            throw new ValidacaoException("Escolha uma palavra-passe diferente do código que a receção lhe deu.");
        }
        String salt = passwordUtils.gerarSalt();
        String hash = passwordUtils.hashPassword(nova, salt);
        if (!contaClienteDAL.definirPassword(conta.getId(), hash, salt)) {
            throw new ValidacaoException("A palavra-passe desta conta já foi escolhida.");
        }
        conta.definirPassword(hash, salt, false);
    }

    private void exigirRececao(Utilizador quemAge) {
        if (!Permissoes.podeGerirReservas(quemAge)) {
            throw new IllegalStateException("Só a receção e o gestor podem tratar das contas dos clientes.");
        }
    }

    private Cliente procurarCliente(String nif) {
        Cliente cliente = clienteBLL.pesquisarPorNif(nif);
        if (cliente == null) {
            throw new ValidacaoException("Não existe nenhum cliente com o NIF " + nif.trim() + ".");
        }
        return cliente;
    }

    private String gerarCodigo() {
        return String.format("%06d", random.nextInt(LIMITE_CODIGO));
    }
}
