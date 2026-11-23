SET QUOTED_IDENTIFIER ON;
GO

IF COL_LENGTH('Utilizador', 'mudarPassword') IS NULL
ALTER TABLE Utilizador ADD mudarPassword BIT NOT NULL
    CONSTRAINT DF_Utilizador_mudarPassword DEFAULT 0;
GO
