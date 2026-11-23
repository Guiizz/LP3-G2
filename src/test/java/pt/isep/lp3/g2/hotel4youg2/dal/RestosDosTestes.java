package pt.isep.lp3.g2.hotel4youg2.dal;

import pt.isep.lp3.g2.hotel4youg2.exception.DataAccessException;

public final class RestosDosTestes {

    static final String PESSOAS = "(SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' "
            + "OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %'))";
    static final String QUARTOS = "(SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999)";
    static final String RESERVAS = "(SELECT id FROM Reserva WHERE clienteId IN " + PESSOAS
            + " OR quartoNumero IN " + QUARTOS + ")";
    static final String FORNECEDORES = "(SELECT id FROM Fornecedor WHERE codigo LIKE 'T[0-9]%' "
            + "OR codigo LIKE 'T[QRS][0-9]%')";
    static final String MATERIAIS = "(SELECT id FROM Material WHERE fornecedorId IN " + FORNECEDORES + ")";
    static final String SERVICOS = "(SELECT id FROM Servico WHERE designacao LIKE 'Teste %' "
            + "OR categoriaId IN (SELECT id FROM Categoria WHERE nome LIKE 'Teste %'))";

    static final String[] APAGAR = {
        "DELETE FROM AcessoCartao WHERE cartao LIKE 'T[0-9]%' OR reservaId IN " + RESERVAS
                + " OR funcionarioId IN " + PESSOAS + " OR quartoNumero IN " + QUARTOS,
        "DELETE FROM PedidoExtra WHERE reservaId IN " + RESERVAS + " OR materialId IN " + MATERIAIS
                + " OR servicoId IN " + SERVICOS,
        "DELETE FROM AvaliacaoEstadia WHERE reservaId IN " + RESERVAS,
        "DELETE FROM SugestaoQueixa WHERE descricao LIKE 'Teste %' OR reservaId IN " + RESERVAS
                + " OR clienteId IN " + PESSOAS,
        "DELETE FROM MaterialReserva WHERE reservaId IN " + RESERVAS + " OR materialId IN " + MATERIAIS,
        "DELETE FROM MovimentoStock WHERE reservaId IN " + RESERVAS + " OR materialId IN " + MATERIAIS,
        "DELETE FROM ReservaServico WHERE reservaId IN " + RESERVAS + " OR servicoId IN " + SERVICOS,
        "DELETE FROM ContaCliente WHERE clienteId IN " + PESSOAS
                + " OR passwordHash IN (REPLICATE('a', 64), REPLICATE('c', 64))",
        "DELETE FROM Reserva WHERE id IN " + RESERVAS,
        "DELETE FROM Utilizador WHERE funcionarioId IN " + PESSOAS,
        "DELETE FROM Funcionario WHERE pessoaId IN " + PESSOAS,
        "DELETE FROM Cliente WHERE pessoaId IN " + PESSOAS,
        "DELETE FROM Pessoa WHERE id IN " + PESSOAS,
        "DELETE FROM LinhaEntrega WHERE entregaId IN (SELECT id FROM Entrega WHERE fornecedorId IN "
                + FORNECEDORES + ")",
        "DELETE FROM Entrega WHERE fornecedorId IN " + FORNECEDORES,
        "DELETE FROM ConfiguracaoQuarto WHERE materialId IN " + MATERIAIS,
        "DELETE FROM Stock WHERE materialId IN " + MATERIAIS,
        "DELETE FROM Material WHERE id IN " + MATERIAIS,
        "DELETE FROM Fornecedor WHERE id IN " + FORNECEDORES,
        "DELETE FROM Servico WHERE id IN " + SERVICOS,
        "DELETE FROM Categoria WHERE nome LIKE 'Teste %'",
        "DELETE FROM Quarto WHERE numero IN " + QUARTOS
    };

    private RestosDosTestes() {
    }

    public static void limpar() {
        try {
            ConexaoBD.select("SELECT 1", new InteiroMapper());
        } catch (DataAccessException e) {
            return;
        }
        for (String sql : APAGAR) {
            try {
                ConexaoBD.executar(sql);
            } catch (DataAccessException e) {
                System.out.println("Não foi possível apagar restos de testes: " + e.getMessage());
            }
        }
    }
}
