package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.QuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.interfaces.IQuartoDAL;
import pt.isep.lp3.g2.hotel4youg2.model.Quarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Perfil;
import pt.isep.lp3.g2.hotel4youg2.model.enums.TipoQuarto;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Vista;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Regras de negócio dos quartos: consultar a lista, pesquisar os que estão disponíveis e criar quartos novos.
 *
 */
public class QuartoBLL {
    private final IQuartoDAL quartoDAL;
    private static final int PRIMEIRO_PISO_DE_QUARTOS = 2;
    private static final int ULTIMO_PISO = 9;


    /**
     * Cria o serviço ligado à base de dados.
     */
    public QuartoBLL() {
        this(new QuartoDAL());
    }

    /**
     * Cria o serviço com outro repositório (usado nos testes).
     *
     * @param quartoDAL de onde vêm os quartos
     */
    public QuartoBLL(IQuartoDAL quartoDAL) {
        this.quartoDAL = quartoDAL;
    }

    /**
     * Lista os quartos, com filtros opcionais.
     *
     * @param tipo o tipo de quarto, ou {@code null} para todos
     * @param piso o piso, ou {@code null} para todos
     * @return os quartos que cumprem os filtros, com a sua tarifa
     */
    public List<Quarto> listar(TipoQuarto tipo, Integer piso) {
        List<Quarto> resultado = new ArrayList<>();
        for (Quarto quarto : quartoDAL.listarComTarifa()) {
            boolean tipoCerto = tipo == null || quarto.getTipo() == tipo;
            boolean pisoCerto = piso == null || quarto.getPiso() == piso;
            if (tipoCerto && pisoCerto) {
                resultado.add(quarto);
            }
        }
        return resultado;
    }

    /**
     * Pesquisa os quartos livres entre duas datas onde cabem os hóspedes.
     *
     * @param entrada  o dia de chegada (não pode ser no passado)
     * @param saida    o dia de partida (tem de ser depois da entrada)
     * @param hospedes o número de hóspedes (pelo menos 1)
     * @param tipo     o tipo de quarto, ou {@code null} para todos
     * @param vista    a vista, ou {@code null} para todas
     * @return os quartos disponíveis (vazia se não houver nenhum)
     * @throws IllegalArgumentException se as datas ou o número de hóspedes não forem válidos
     */
    public List<Quarto> pesquisarDisponiveis(LocalDate entrada, LocalDate saida, int hospedes, TipoQuarto tipo, Vista vista) {
        validarPesquisa(entrada, saida, hospedes);
        List<Quarto> resultado = new ArrayList<>();
        for (Quarto quarto : quartoDAL.listarDisponiveis(entrada, saida)) {
            boolean cabem = quarto.getCapacidade() >= hospedes;
            boolean tipoCerto = tipo == null || quarto.getTipo() == tipo;
            boolean vistaCerta = vista == null || quarto.getVista() == vista;
            if (cabem && tipoCerto && vistaCerta) {
                resultado.add(quarto);
            }
        }
        return resultado;
    }

    /**
     * Lista todos os quartos, ativos e desativados, para o ecrã "Gestão de quartos". Só o gestor.
     *
     * @param perfilAtual o perfil do utilizador com a sessão iniciada
     * @return todos os quartos
     * @throws IllegalStateException se o perfil não for o de gestor
     */
    public List<Quarto> listarTodos(Perfil perfilAtual) {
        exigirGestor(perfilAtual);
        return quartoDAL.listarTodos();
    }

    /**
     * Desativa um quarto, para deixar de aparecer nas pesquisas e nas reservas novas. Só o gestor.
     *
     * @param perfilAtual o perfil do utilizador com a sessão iniciada
     * @param numero      o número do quarto
     * @throws IllegalStateException    se o perfil não for o de gestor
     * @throws IllegalArgumentException se o quarto tiver reservas futuras ou não existir
     */
    public void desativar(Perfil perfilAtual, int numero) {
        exigirGestor(perfilAtual);
        if (quartoDAL.temReservasFuturas(numero)) {
            throw new IllegalArgumentException("Não é possível desativar o quarto " + numero +
                    ": tem reservas futuras");
        }
        if (!quartoDAL.atualizarAtivo(numero, false)) {
            throw new IllegalArgumentException("O quarto " + numero + " não existe");
        }
    }

    /**
     * Volta a ativar um quarto desativado. Só o gestor.
     *
     * @param perfilAtual o perfil do utilizador com a sessão iniciada
     * @param numero      o número do quarto
     * @throws IllegalStateException    se o perfil não for o de gestor
     * @throws IllegalArgumentException se o quarto não existir
     */
    public void reativar(Perfil perfilAtual, int numero) {
        exigirGestor(perfilAtual);
        if (!quartoDAL.atualizarAtivo(numero, true)) {
            throw new IllegalArgumentException("O quarto " + numero + " não existe");
        }
    }

    public List<Integer> listarPisos() {
        List<Integer> pisos = new ArrayList<>();
        for (Quarto quarto : quartoDAL.listarComTarifa()) {
            if (!pisos.contains(quarto.getPiso())) {
                pisos.add(quarto.getPiso());
            }
        }
        Collections.sort(pisos);
        return pisos;
    }

    public Quarto criar(Perfil perfilAtual, String numero, String piso, TipoQuarto tipo, Vista vista) {
        exigirGestor(perfilAtual);
        int numeroQuarto = lerInteiro(numero, "Indique o número do quarto.", "O número do quarto tem de ser um número inteiro.");
        int pisoQuarto = lerInteiro(piso, "Indique o piso do quarto.", "O piso tem de ser um número inteiro.");
        if (pisoQuarto < PRIMEIRO_PISO_DE_QUARTOS || pisoQuarto > ULTIMO_PISO) {
            throw new IllegalArgumentException("Os quartos ficam do piso " + PRIMEIRO_PISO_DE_QUARTOS + " ao " + ULTIMO_PISO + ": os pisos −1, 0 e 1 são dos serviços do hotel.");
        }
        if (numeroQuarto / 100 != pisoQuarto || numeroQuarto % 100 == 0) {
            throw new IllegalArgumentException("No piso " + pisoQuarto + " o número do quarto vai de "
                    + pisoQuarto + "01 a " + pisoQuarto + "99 (o número começa pelo piso).");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Escolha o tipo de quarto.");
        }
        if (vista == null) {
            throw new IllegalArgumentException("Escolha a vista do quarto.");
        }
        Quarto quarto = new Quarto(numeroQuarto, pisoQuarto, tipo, vista, tipo.getCapacidade());
        if (!quartoDAL.inserir(quarto)) {
            throw new IllegalArgumentException("Já existe um quarto com o número " + numeroQuarto + ".");
        }
        return quarto;
    }

    private static int lerInteiro(String texto, String vazio, String invalido) {
        String limpo = texto == null ? "" : texto.trim();
        if (limpo.isEmpty()) {
            throw new IllegalArgumentException(vazio);
        }
        if (!limpo.matches("-?\\d{1,9}")) {
            throw new IllegalArgumentException(invalido);
        }
        return Integer.parseInt(limpo);
    }

    private void exigirGestor(Perfil perfilAtual) {
        if (perfilAtual != Perfil.GESTOR) {
            throw new IllegalStateException("Só o gestor pode gerir quartos");
        }
    }

    private void validarPesquisa(LocalDate entrada, LocalDate saida, int hospedes) {
        if (entrada == null || saida == null) {
            throw new IllegalArgumentException("Indique as datas de entrada e de saída.");
        }
        if (entrada.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data de entrada não pode ser no passado.");
        }
        if (!saida.isAfter(entrada)) {
            throw new IllegalArgumentException("A data de saída tem de ser posterior à de entrada.");
        }
        if (hospedes < 1) {
            throw new IllegalArgumentException("O número de hóspedes tem de ser pelo menos 1.");
        }
    }
}
