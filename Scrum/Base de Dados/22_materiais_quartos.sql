SET QUOTED_IDENTIFIER ON;
GO

IF COL_LENGTH('Material', 'consumivel') IS NULL
ALTER TABLE Material ADD consumivel BIT NOT NULL
    CONSTRAINT DF_Material_consumivel DEFAULT 1;
GO

IF OBJECT_ID('ConfiguracaoQuarto') IS NULL
CREATE TABLE ConfiguracaoQuarto (
    tipoQuarto VARCHAR(10) NOT NULL CHECK (tipoQuarto IN ('INDIVIDUAL', 'DUPLO', 'FAMILIA')),
    materialId INT NOT NULL REFERENCES Material(id),
    quantidade INT NOT NULL CHECK (quantidade > 0),
    PRIMARY KEY (tipoQuarto, materialId)
);
GO
