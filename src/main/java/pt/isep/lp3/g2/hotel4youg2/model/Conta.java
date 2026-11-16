package pt.isep.lp3.g2.hotel4youg2.model;

public class Conta {

    public static final String FORNECEDOR = "Fornecedor";
    public static final String ATIVA = "Ativa";
    public static final String DESATIVADA = "Desativada";
    public static final String SEM_PASSWORD = "Sem password";

    private final String nome;
    private final String tipo;
    private final String estado;
    private final Utilizador utilizador;

    public Conta(Utilizador utilizador) {
        this.nome = utilizador.getNomeUtilizador();
        this.tipo = utilizador.getPerfil().name().charAt(0) + utilizador.getPerfil().name().substring(1).toLowerCase();
        this.estado = utilizador.isAtivo() ? ATIVA : DESATIVADA;
        this.utilizador = utilizador;
    }

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

    public boolean isDeFornecedor() {
        return utilizador == null;
    }

    @Override
    public String toString() {
        return nome + " (" + tipo + ", " + estado + ")";
    }
}
