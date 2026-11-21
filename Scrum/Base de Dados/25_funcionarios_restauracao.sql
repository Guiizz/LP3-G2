SET QUOTED_IDENTIFIER ON;
GO

IF COL_LENGTH('Funcionario', 'empresa') IS NULL
ALTER TABLE Funcionario ADD empresa VARCHAR(100) NULL;
GO

DECLARE @apagarRestricaoAntiga NVARCHAR(300) = (
    SELECT 'ALTER TABLE Utilizador DROP CONSTRAINT ' + QUOTENAME(name) FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('Utilizador')
      AND name <> 'CK_Utilizador_perfil'
      AND definition LIKE '%perfil%');
IF @apagarRestricaoAntiga IS NOT NULL
EXEC(@apagarRestricaoAntiga);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_Utilizador_perfil')
ALTER TABLE Utilizador ADD CONSTRAINT CK_Utilizador_perfil
    CHECK (perfil IN ('GESTOR', 'RECECIONISTA', 'RESTAURACAO'));
GO
