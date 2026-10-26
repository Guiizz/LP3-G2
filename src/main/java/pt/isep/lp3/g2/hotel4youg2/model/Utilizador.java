package pt.isep.lp3.g2.hotel4youg2.model;

/**
 * Conta de acesso de um funcionário à aplicação.
 * A password nunca é guardada: só o hash (SHA-256) e o salt, em hexadecimal.
 */
public class Utilizador {

    private int id;
    private int funcionarioId;
    private String nomeUtilizador;
    private String passwordHash;
    private String passwordSalt;
    private Perfil perfil;
    private boolean ativo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(int funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public String getNomeUtilizador() {
        return nomeUtilizador;
    }

    public void setNomeUtilizador(String nomeUtilizador) {
        this.nomeUtilizador = nomeUtilizador;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /**
     * @param id             o identificador da conta
     * @param funcionarioId  o id do funcionário a quem a conta pertence
     * @param nomeUtilizador o nome usado para entrar
     * @param passwordHash   o hash da password
     * @param passwordSalt   o salt usado no hash
     * @param perfil         o perfil, que decide o que a pessoa pode fazer
     * @param ativo          {@code false} se a conta estiver desativada
     */
    public Utilizador(int id, int funcionarioId, String nomeUtilizador, String passwordHash, String passwordSalt, Perfil perfil, boolean ativo) {
        this.id = id;
        this.funcionarioId = funcionarioId;
        this.nomeUtilizador = nomeUtilizador;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.perfil = perfil;
        this.ativo = ativo;
    }
}
