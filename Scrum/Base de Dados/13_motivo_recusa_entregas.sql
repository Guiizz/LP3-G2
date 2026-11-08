-- US49: motivo (opcional) quando uma entrega é recusada (corre depois do 12)
-- Pode correr-se várias vezes: a coluna só é criada se ainda não existir.

IF COL_LENGTH('Entrega', 'motivoRecusa') IS NULL
ALTER TABLE Entrega ADD motivoRecusa VARCHAR(200) NULL;
GO
