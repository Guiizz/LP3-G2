-- US38 e US49: entregas do fornecedor com estado, conta do fornecedor e stock (corre depois do 05)
-- Pode correr-se várias vezes: cada alteração só é feita se ainda não existir.

SET QUOTED_IDENTIFIER ON;
GO

-- 1) Estado da entrega: PENDENTE (carregada pelo fornecedor), ACEITE ou RECUSADA.
--    As entregas que já existiam foram registadas pela receção, por isso ficam ACEITE.
IF COL_LENGTH('Entrega', 'estado') IS NULL
ALTER TABLE Entrega ADD estado VARCHAR(10) NOT NULL
    CONSTRAINT DF_Entrega_estado DEFAULT 'ACEITE';
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_Entrega_estado')
ALTER TABLE Entrega ADD CONSTRAINT CK_Entrega_estado
    CHECK (estado IN ('PENDENTE', 'ACEITE', 'RECUSADA'));
GO

-- 2) Quem aceitou ou recusou, e quando (fica no histórico).
IF COL_LENGTH('Entrega', 'decididaPor') IS NULL
ALTER TABLE Entrega ADD decididaPor INT NULL REFERENCES Funcionario(pessoaId);
GO

IF COL_LENGTH('Entrega', 'decididaEm') IS NULL
ALTER TABLE Entrega ADD decididaEm DATETIME2 NULL;
GO

-- 3) Quando o fornecedor carrega o ficheiro não há funcionário a registar a entrega.
IF EXISTS (SELECT 1 FROM sys.columns
           WHERE object_id = OBJECT_ID('Entrega') AND name = 'registadaPor' AND is_nullable = 0)
ALTER TABLE Entrega ALTER COLUMN registadaPor INT NULL;
GO

-- 4) O número de encomenda só tem de ser único enquanto a entrega não foi recusada,
--    para o fornecedor poder voltar a enviar uma entrega recusada.
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_Entrega_fornecedor_numero')
ALTER TABLE Entrega DROP CONSTRAINT UQ_Entrega_fornecedor_numero;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_Entrega_fornecedor_numero')
CREATE UNIQUE INDEX UQ_Entrega_fornecedor_numero
    ON Entrega (fornecedorId, numeroEncomenda) WHERE estado <> 'RECUSADA';
GO

-- 5) Conta do fornecedor: entra com o código e a password (só se guarda o hash e o salt,
--    como nas contas dos funcionários). Os fornecedores sem password não conseguem entrar.
IF COL_LENGTH('Fornecedor', 'passwordHash') IS NULL
ALTER TABLE Fornecedor ADD passwordHash CHAR(64) NULL;
GO

IF COL_LENGTH('Fornecedor', 'passwordSalt') IS NULL
ALTER TABLE Fornecedor ADD passwordSalt CHAR(32) NULL;
GO

-- 6) Stock: uma linha por material, que só aumenta quando uma entrega é aceite (US49).
IF OBJECT_ID('Stock') IS NULL
CREATE TABLE Stock (
    materialId INT NOT NULL PRIMARY KEY REFERENCES Material(id),
    quantidade INT NOT NULL DEFAULT 0 CHECK (quantidade >= 0)
);
GO
