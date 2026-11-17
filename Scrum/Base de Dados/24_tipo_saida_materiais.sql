SET QUOTED_IDENTIFIER ON;
GO

IF COL_LENGTH('MovimentoStock', 'tipo') IS NULL
ALTER TABLE MovimentoStock ADD tipo VARCHAR(12) NOT NULL
    CONSTRAINT DF_MovimentoStock_tipo DEFAULT 'DEFINITIVA';
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_MovimentoStock_tipo')
ALTER TABLE MovimentoStock ADD CONSTRAINT CK_MovimentoStock_tipo
    CHECK (tipo IN ('TEMPORARIA', 'DEFINITIVA'));
GO

IF COL_LENGTH('MovimentoStock', 'motivo') IS NULL
ALTER TABLE MovimentoStock ADD motivo VARCHAR(25) NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_MovimentoStock_motivo')
ALTER TABLE MovimentoStock ADD CONSTRAINT CK_MovimentoStock_motivo
    CHECK (motivo IS NULL OR motivo IN ('ESTRAGADO', 'ROUBADO', 'DEVOLVIDO_FORNECEDOR', 'OUTRO', 'CONSUMO_RESERVA'));
GO

IF COL_LENGTH('MovimentoStock', 'descricaoMotivo') IS NULL
ALTER TABLE MovimentoStock ADD descricaoMotivo VARCHAR(200) NULL;
GO

IF COL_LENGTH('MovimentoStock', 'devolvidaEm') IS NULL
ALTER TABLE MovimentoStock ADD devolvidaEm DATETIME2 NULL;
GO

IF COL_LENGTH('MovimentoStock', 'devolvidaPor') IS NULL
ALTER TABLE MovimentoStock ADD devolvidaPor INT NULL REFERENCES Funcionario(pessoaId);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_MovimentoStock_devolucao')
ALTER TABLE MovimentoStock ADD CONSTRAINT CK_MovimentoStock_devolucao
    CHECK (devolvidaEm IS NULL OR tipo = 'TEMPORARIA');
GO
