package pt.isep.lp3.g2.hotel4youg2.model;

/**
 * Linha da tabela das contas (US63): a conta de um funcionário (gestor ou rececionista) ou de um fornecedor,
 * com o tipo e o estado.
 */
public class Conta {

    /** Tipo das contas dos fornecedores. */
    public static final String FORNECEDOR = "Fornecedor";
    /** Estado de uma conta que pode entrar na aplicação. */
    public static final String ATIVA = "Ativa";
    /** Estado de uma conta de funcionário que o gestor desativou. */
    public static final String DESATIVADA = "Desativada";
    /** Estado de um fornecedor que ainda não definiu a password. */
    public static final String SEM_PASSWORD = "Sem password";

    private final String nome;
    private final String tipo;
    private final String estado;
    private final Utilizador utilizador;

    /**
     * Cria a linha da conta de um funcionário.
     *
     * @param utilizador a conta do funcionário
     */
    public Conta(Utilizador utilizador) {
        this.nome = utilizador.getNomeUtilizador();
        this.tipo = utilizador.getPerfil().name().charAt(0) + utilizador.getPerfil().name().substring(1).toLowerCase();
        this.estado = utilizador.isAtivo() ? ATIVA : DESATIVADA;
        this.utilizador = utilizador;
    }

    /**
     * Cria a linha da conta de um fornecedor.
     *
     * @param fornecedor o fornecedor
     */
    public Conta(Fornecedor fornecedor) {
        this.nome = fornecedor.getCodigo() + " - " + fornecedor.getNome();
        this.tipo = FORNECEDOR;
        this.estado = fornecedor.getPasswordHash() == null ? SEM_PASSWORD : ATIVA;
        this.utilizador = null;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEstado() {
        return estado;
    }

    public Utilizador getUtilizador() {
        return utilizador;
    }

    /**
     * @return {@code true} se a conta for de um fornecedor (estas não se desativam no ecrã das contas)
     */
    public boolean isDeFornecedor() {
        return utilizador == null;
    }

    @Override
    public String toString() {
        return nome + " (" + tipo + ", " + estado + ")";
    }
}
