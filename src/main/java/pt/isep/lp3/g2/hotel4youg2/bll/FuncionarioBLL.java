package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.FuncionarioDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IFuncionarioDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Funcionario;
import pt.isep.lp3.g2.hotel4youg2.model.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;
import pt.isep.lp3.g2.hotel4youg2.util.PasswordUtils;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Regras de negócio para criar contas de funcionário.
 */
public class FuncionarioBLL {

    private static final int TAMANHO_MINIMO_PASSWORD = 8;
    private static final Pattern NIF = Pattern.compile("\\d{9}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    private final IFuncionarioDAL funcionarioDAL;
    private final PasswordUtils passwordUtils;

    /**
     * Cria o BLL ligado à base de dados.
     */
    public FuncionarioBLL() {
        this(new FuncionarioDAL(), new PasswordUtils());
    }

    /**
     * Cria o BLL com outro DAL.
     *
     * @param funcionarioDAL onde as contas são gravadas
     * @param passwordUtils  para gerar o salt e o hash da password
     */
    public FuncionarioBLL(IFuncionarioDAL funcionarioDAL, PasswordUtils passwordUtils) {
        this.funcionarioDAL = funcionarioDAL;
        this.passwordUtils = passwordUtils;
    }

    /**
     * Cria um funcionário com a sua conta de acesso. A password é guardada
     * só como hash e salt, nunca em texto.
     *
     * @param quemAge        o utilizador com a sessão iniciada (tem de ser gestor)
     * @param dados          os dados pessoais do funcionário (nome e NIF obrigatórios)
     * @param nomeUtilizador o nome para entrar (3 a 50 caracteres, sem repetir)
     * @param password       a password (pelo menos 8 caracteres)
     * @param perfil         o perfil da conta
     * @throws IllegalStateException    se quem age não for gestor
     * @throws IllegalArgumentException se algum dado não for válido, o NIF ou o nome de utilizador já existirem
     */
    public void criarConta(Utilizador quemAge, Funcionario dados, String nomeUtilizador,
                           String password, Perfil perfil) {
        if (quemAge == null || quemAge.getPerfil() != Perfil.GESTOR) {
            throw new IllegalStateException("Só o gestor pode criar contas.");
        }
        if (dados == null) {
            throw new IllegalArgumentException("Faltam os dados do funcionário.");
        }

        String nome = limpar(dados.getNome());
        String nif = limpar(dados.getNif());
        String cc = limpar(dados.getCc());
        String morada = limpar(dados.getMorada());
        String telemovel = limpar(dados.getTelemovel());
        String email = limpar(dados.getEmail());
        LocalDate dataNascimento = dados.getDataNascimento();
        String utilizador = limpar(nomeUtilizador);

        if (nome == null || nome.length() > 100) {
            throw new IllegalArgumentException("O nome é obrigatório (até 100 caracteres).");
        }
        if (nif == null || !NIF.matcher(nif).matches()) {
            throw new IllegalArgumentException("O NIF tem de ter 9 algarismos.");
        }
        if (cc != null && cc.length() > 12) {
            throw new IllegalArgumentException("O CC tem no máximo 12 caracteres.");
        }
        if (morada != null && morada.length() > 200) {
            throw new IllegalArgumentException("A morada tem no máximo 200 caracteres.");
        }
        if (telemovel != null && telemovel.length() > 15) {
            throw new IllegalArgumentException("O telemóvel tem no máximo 15 caracteres.");
        }
        if (email != null && (email.length() > 100 || !EMAIL.matcher(email).matches())) {
            throw new IllegalArgumentException("O email não é válido.");
        }
        if (dataNascimento != null && dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de nascimento não pode ser no futuro.");
        }
        if (utilizador == null || utilizador.length() < 3 || utilizador.length() > 50) {
            throw new IllegalArgumentException("O nome de utilizador tem de ter entre 3 e 50 caracteres.");
        }
        if (password == null || password.length() < TAMANHO_MINIMO_PASSWORD) {
            throw new IllegalArgumentException(
                    "A password tem de ter pelo menos " + TAMANHO_MINIMO_PASSWORD + " caracteres.");
        }
        if (perfil == null) {
            throw new IllegalArgumentException("Escolha o perfil.");
        }
        if (funcionarioDAL.existeNif(nif)) {
            throw new IllegalArgumentException("Já existe uma pessoa com esse NIF.");
        }
        if (funcionarioDAL.existeNomeUtilizador(utilizador)) {
            throw new IllegalArgumentException("Esse nome de utilizador já existe.");
        }

        String salt = passwordUtils.gerarSalt();
        String hash = passwordUtils.hashPassword(password, salt);

        Funcionario funcionario = new Funcionario(0, nome, cc, nif, morada, telemovel,
                dataNascimento, email, 0, LocalDate.now());
        Utilizador conta = new Utilizador(0, 0, utilizador, hash, salt, perfil, true);
        funcionarioDAL.criarConta(funcionario, conta);
    }

    private static String limpar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}