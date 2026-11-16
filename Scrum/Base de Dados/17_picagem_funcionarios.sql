IF COL_LENGTH('AcessoCartao', 'funcionarioId') IS NULL
ALTER TABLE AcessoCartao ADD funcionarioId INT NULL REFERENCES Funcionario(pessoaId);
GO

DECLARE @apagarRestricaoAntiga NVARCHAR(300) = (
    SELECT 'ALTER TABLE AcessoCartao DROP CONSTRAINT ' + QUOTENAME(name) FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('AcessoCartao')
      AND name NOT IN ('CK_AcessoCartao_quarto', 'CK_AcessoCartao_local', 'CK_AcessoCartao_dono')
      AND definition LIKE '%localAcesso%');
IF @apagarRestricaoAntiga IS NOT NULL
EXEC(@apagarRestricaoAntiga);
GO

IF COL_LENGTH('AcessoCartao', 'localAcesso') < 20
ALTER TABLE AcessoCartao ALTER COLUMN localAcesso VARCHAR(20) NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_AcessoCartao_local')
ALTER TABLE AcessoCartao ADD CONSTRAINT CK_AcessoCartao_local CHECK (localAcesso IN
    ('QUARTO', 'BAR', 'SALA_CONFERENCIAS', 'RESTAURANTE', 'PISCINA', 'COZINHA', 'GARAGEM', 'ARMAZEM'));
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_AcessoCartao_dono')
ALTER TABLE AcessoCartao ADD CONSTRAINT CK_AcessoCartao_dono
    CHECK (reservaId IS NULL OR funcionarioId IS NULL);
GO
