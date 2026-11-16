-- US53: moeda da entrega, lida do atributo CurrencyType do XML (corre depois do 17)
-- Pode correr-se várias vezes: só acrescenta a coluna se ainda não existir.
-- As entregas que já existiam ficam em EUR.

IF COL_LENGTH('Entrega', 'moeda') IS NULL
ALTER TABLE Entrega ADD moeda CHAR(3) NOT NULL
    CONSTRAINT DF_Entrega_moeda DEFAULT 'EUR';
GO
