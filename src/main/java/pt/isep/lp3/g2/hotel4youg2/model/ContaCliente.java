package pt.isep.lp3.g2.hotel4youg2.model;

import java.util.Objects;

/**
 * Conta de um cliente na aplicação. O cliente entra com o NIF e a palavra-passe.
 */
public class ContaCliente {

    private int id;
    private final int clienteId;
    private final String nif;
    private final String nome;
    private String passwordHash;
    private String passwordSalt;
    private boolean primeiroAcesso;

    /**
     * Cria a conta de um cliente.
     *
     * @param id             o id da conta (0 se ainda não foi gravada)
     * @param clienteId      o id do cliente
     * @param nif            o NIF do cliente
     * @param nome           o nome do cliente
     * @param passwordHash   o hash da palavra-passe
     * @param passwordSalt   o salt da palavra-passe
     * @param primeiroAcesso se o cliente ainda tem de escolher a palavra-passe
     */
    public ContaCliente(int id, int clienteId, String nif, String nome, String passwordHash, String passwordSalt,
                        boolean primeiroAcesso) {
        this.id = id;
        this.clienteId = clienteId;
        this.nif = nif;
        this.nome = nome;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.primeiroAcesso = primeiroAcesso;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public String getNif() {
        return nif;
    }

    public String getNome() {
        return nome;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public boolean isPrimeiroAcesso() {
        return primeiroAcesso;
    }

    /**
     * Muda a palavra-passe da conta.
     *
     * @param passwordHash   o hash da palavra-passe nova
     * @param passwordSalt   o salt da palavra-passe nova
     * @param primeiroAcesso se o cliente ainda tem de escolher a palavra-passe
     */
    public void definirPassword(String passwordHash, String passwordSalt, boolean primeiroAcesso) {
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.primeiroAcesso = primeiroAcesso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContaCliente outra = (ContaCliente) o;
        return id == outra.id && clienteId == outra.clienteId && primeiroAcesso == outra.primeiroAcesso
                && Objects.equals(nif, outra.nif) && Objects.equals(nome, outra.nome)
                && Objects.equals(passwordHash, outra.passwordHash)
                && Objects.equals(passwordSalt, outra.passwordSalt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clienteId, nif, nome, passwordHash, passwordSalt, primeiroAcesso);
    }

    @Override
    public String toString() {
        return nome + " (NIF " + nif + ")";
    }
}
