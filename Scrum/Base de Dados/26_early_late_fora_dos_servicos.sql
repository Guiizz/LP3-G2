UPDATE Servico SET ativo = 0
WHERE designacao IN ('Early check-in', 'Late check-out') AND ativo = 1;
GO
