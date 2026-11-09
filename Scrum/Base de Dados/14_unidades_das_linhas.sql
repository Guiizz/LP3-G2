-- US49: total de cada linha da entrega em unidades (é este valor que entra no stock) (corre depois do 13)
-- Pode correr-se várias vezes: cada alteração só é feita se ainda não existir.

IF COL_LENGTH('LinhaEntrega', 'unidades') IS NULL
ALTER TABLE LinhaEntrega ADD unidades INT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_LinhaEntrega_unidades')
ALTER TABLE LinhaEntrega ADD CONSTRAINT CK_LinhaEntrega_unidades CHECK (unidades > 0);
GO

-- As linhas que já vinham em unidades têm tantas unidades como a quantidade.
-- As que vinham em caixas ou quilos ficam vazias: o ficheiro de então não guardou o total em unidades.
UPDATE LinhaEntrega SET unidades = quantidade WHERE unidades IS NULL AND unidade = 'UNIT' AND quantidade > 0;
GO
