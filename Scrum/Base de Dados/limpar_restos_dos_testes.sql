SET QUOTED_IDENTIFIER ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;
    IF OBJECT_ID('AcessoCartao') IS NOT NULL
        DELETE FROM AcessoCartao WHERE cartao LIKE 'T[0-9]%' OR reservaId IN (SELECT id FROM Reserva WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %')) OR quartoNumero IN (SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999)) OR funcionarioId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %')) OR quartoNumero IN (SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999);
    IF OBJECT_ID('PedidoExtra') IS NOT NULL
        DELETE FROM PedidoExtra WHERE reservaId IN (SELECT id FROM Reserva WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %')) OR quartoNumero IN (SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999)) OR materialId IN (SELECT id FROM Material WHERE fornecedorId IN (SELECT id FROM Fornecedor WHERE codigo LIKE 'T[0-9]%' OR codigo LIKE 'T[QRS][0-9]%')) OR servicoId IN (SELECT id FROM Servico WHERE designacao LIKE 'Teste %' OR categoriaId IN (SELECT id FROM Categoria WHERE nome LIKE 'Teste %'));
    IF OBJECT_ID('AvaliacaoEstadia') IS NOT NULL
        DELETE FROM AvaliacaoEstadia WHERE reservaId IN (SELECT id FROM Reserva WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %')) OR quartoNumero IN (SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999));
    IF OBJECT_ID('SugestaoQueixa') IS NOT NULL
        DELETE FROM SugestaoQueixa WHERE descricao LIKE 'Teste %' OR reservaId IN (SELECT id FROM Reserva WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %')) OR quartoNumero IN (SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999)) OR clienteId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %'));
    IF OBJECT_ID('MaterialReserva') IS NOT NULL
        DELETE FROM MaterialReserva WHERE reservaId IN (SELECT id FROM Reserva WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %')) OR quartoNumero IN (SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999)) OR materialId IN (SELECT id FROM Material WHERE fornecedorId IN (SELECT id FROM Fornecedor WHERE codigo LIKE 'T[0-9]%' OR codigo LIKE 'T[QRS][0-9]%'));
    IF OBJECT_ID('MovimentoStock') IS NOT NULL
        DELETE FROM MovimentoStock WHERE reservaId IN (SELECT id FROM Reserva WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %')) OR quartoNumero IN (SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999)) OR materialId IN (SELECT id FROM Material WHERE fornecedorId IN (SELECT id FROM Fornecedor WHERE codigo LIKE 'T[0-9]%' OR codigo LIKE 'T[QRS][0-9]%'));
    IF OBJECT_ID('ReservaServico') IS NOT NULL
        DELETE FROM ReservaServico WHERE reservaId IN (SELECT id FROM Reserva WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %')) OR quartoNumero IN (SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999)) OR servicoId IN (SELECT id FROM Servico WHERE designacao LIKE 'Teste %' OR categoriaId IN (SELECT id FROM Categoria WHERE nome LIKE 'Teste %'));
    IF OBJECT_ID('ContaCliente') IS NOT NULL
        DELETE FROM ContaCliente WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %')) OR passwordHash IN (REPLICATE('a', 64), REPLICATE('c', 64));
    IF OBJECT_ID('Reserva') IS NOT NULL
        DELETE FROM Reserva WHERE id IN (SELECT id FROM Reserva WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %')) OR quartoNumero IN (SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999));
    IF OBJECT_ID('Utilizador') IS NOT NULL
        DELETE FROM Utilizador WHERE funcionarioId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %'));
    IF OBJECT_ID('Funcionario') IS NOT NULL
        DELETE FROM Funcionario WHERE pessoaId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %'));
    IF OBJECT_ID('Cliente') IS NOT NULL
        DELETE FROM Cliente WHERE pessoaId IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %'));
    IF OBJECT_ID('Pessoa') IS NOT NULL
        DELETE FROM Pessoa WHERE id IN (SELECT id FROM Pessoa WHERE nif LIKE '9%' AND (nome LIKE 'Teste [0-9]%' OR nome LIKE 'Teste SR %' OR nome LIKE 'Teste FC %'));
    IF OBJECT_ID('LinhaEntrega') IS NOT NULL
        DELETE FROM LinhaEntrega WHERE entregaId IN (SELECT id FROM Entrega WHERE fornecedorId IN (SELECT id FROM Fornecedor WHERE codigo LIKE 'T[0-9]%' OR codigo LIKE 'T[QRS][0-9]%'));
    IF OBJECT_ID('Entrega') IS NOT NULL
        DELETE FROM Entrega WHERE fornecedorId IN (SELECT id FROM Fornecedor WHERE codigo LIKE 'T[0-9]%' OR codigo LIKE 'T[QRS][0-9]%');
    IF OBJECT_ID('ConfiguracaoQuarto') IS NOT NULL
        DELETE FROM ConfiguracaoQuarto WHERE materialId IN (SELECT id FROM Material WHERE fornecedorId IN (SELECT id FROM Fornecedor WHERE codigo LIKE 'T[0-9]%' OR codigo LIKE 'T[QRS][0-9]%'));
    IF OBJECT_ID('Stock') IS NOT NULL
        DELETE FROM Stock WHERE materialId IN (SELECT id FROM Material WHERE fornecedorId IN (SELECT id FROM Fornecedor WHERE codigo LIKE 'T[0-9]%' OR codigo LIKE 'T[QRS][0-9]%'));
    IF OBJECT_ID('Material') IS NOT NULL
        DELETE FROM Material WHERE id IN (SELECT id FROM Material WHERE fornecedorId IN (SELECT id FROM Fornecedor WHERE codigo LIKE 'T[0-9]%' OR codigo LIKE 'T[QRS][0-9]%'));
    IF OBJECT_ID('Fornecedor') IS NOT NULL
        DELETE FROM Fornecedor WHERE id IN (SELECT id FROM Fornecedor WHERE codigo LIKE 'T[0-9]%' OR codigo LIKE 'T[QRS][0-9]%');
    IF OBJECT_ID('Servico') IS NOT NULL
        DELETE FROM Servico WHERE id IN (SELECT id FROM Servico WHERE designacao LIKE 'Teste %' OR categoriaId IN (SELECT id FROM Categoria WHERE nome LIKE 'Teste %'));
    IF OBJECT_ID('Categoria') IS NOT NULL
        DELETE FROM Categoria WHERE nome LIKE 'Teste %';
    IF OBJECT_ID('Quarto') IS NOT NULL
        DELETE FROM Quarto WHERE numero IN (SELECT numero FROM Quarto WHERE numero BETWEEN 901 AND 999);
    IF OBJECT_ID('SugestaoQueixa') IS NOT NULL
        DELETE FROM SugestaoQueixa WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif = '111111111' AND nome = 'Teste Editado' AND id NOT IN (SELECT clienteId FROM Reserva) AND id NOT IN (SELECT pessoaId FROM Funcionario));
    IF OBJECT_ID('ContaCliente') IS NOT NULL
        DELETE FROM ContaCliente WHERE clienteId IN (SELECT id FROM Pessoa WHERE nif = '111111111' AND nome = 'Teste Editado' AND id NOT IN (SELECT clienteId FROM Reserva) AND id NOT IN (SELECT pessoaId FROM Funcionario));
    IF OBJECT_ID('Cliente') IS NOT NULL
        DELETE FROM Cliente WHERE pessoaId IN (SELECT id FROM Pessoa WHERE nif = '111111111' AND nome = 'Teste Editado' AND id NOT IN (SELECT clienteId FROM Reserva) AND id NOT IN (SELECT pessoaId FROM Funcionario));
    IF OBJECT_ID('Pessoa') IS NOT NULL
        DELETE FROM Pessoa WHERE id IN (SELECT id FROM Pessoa WHERE nif = '111111111' AND nome = 'Teste Editado' AND id NOT IN (SELECT clienteId FROM Reserva) AND id NOT IN (SELECT pessoaId FROM Funcionario));
    IF OBJECT_ID('Fornecedor') IS NOT NULL
        DELETE FROM Fornecedor WHERE codigo IN ('11PT290', 'F-TESTE', '10PT920') AND id NOT IN (SELECT fornecedorId FROM Entrega) AND id NOT IN (SELECT fornecedorId FROM Material);
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
