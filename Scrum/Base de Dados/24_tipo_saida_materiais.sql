-- US77: tipo de cada saída de material (corre depois do 23)
-- Uma saída é TEMPORARIA (o material volta, por exemplo de uma reparação) ou DEFINITIVA (com o motivo).
-- Uma saída temporária pode ser dada como devolvida: fica a data e quem a recebeu, e o material volta ao stock.
-- As saídas que já existiam ficam definitivas, sem motivo.
-- Pode correr-se várias vezes: cada alteração só é feita se ainda não existir.

SET QUOTED_IDENTIFIER ON;
GO

-- 1) Tipo da saída
IF COL_LENGTH('MovimentoStock', 'tipo') IS NULL
ALTER TABLE MovimentoStock ADD tipo VARCHAR(12) NOT NULL
    CONSTRAINT DF_MovimentoStock_tipo DEFAULT 'DEFINITIVA';
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_MovimentoStock_tipo')
ALTER TABLE MovimentoStock ADD CONSTRAINT CK_MovimentoStock_tipo
    CHECK (tipo IN ('TEMPORARIA', 'DEFINITIVA'));
GO

-- 2) Motivo da saída definitiva (o CONSUMO_RESERVA é posto pelo check-in, na US76)
IF COL_LENGTH('MovimentoStock', 'motivo') IS NULL
ALTER TABLE MovimentoStock ADD motivo VARCHAR(25) NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_MovimentoStock_motivo')
ALTER TABLE MovimentoStock ADD CONSTRAINT CK_MovimentoStock_motivo
    CHECK (motivo IS NULL OR motivo IN ('ESTRAGADO', 'ROUBADO', 'DEVOLVIDO_FORNECEDOR', 'OUTRO', 'CONSUMO_RESERVA'));
GO

-- 3) Texto do motivo: obrigatório no "Outro", opcional nos restantes (por exemplo "Reparação" numa temporária)
IF COL_LENGTH('MovimentoStock', 'descricaoMotivo') IS NULL
ALTER TABLE MovimentoStock ADD descricaoMotivo VARCHAR(200) NULL;
GO

-- 4) Devolução de uma saída temporária: quando e quem a recebeu
IF COL_LENGTH('MovimentoStock', 'devolvidaEm') IS NULL
ALTER TABLE MovimentoStock ADD devolvidaEm DATETIME2 NULL;
GO

IF COL_LENGTH('MovimentoStock', 'devolvidaPor') IS NULL
ALTER TABLE MovimentoStock ADD devolvidaPor INT NULL REFERENCES Funcionario(pessoaId);
GO

-- só as saídas temporárias podem ser devolvidas
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_MovimentoStock_devolucao')
ALTER TABLE MovimentoStock ADD CONSTRAINT CK_MovimentoStock_devolucao
    CHECK (devolvidaEm IS NULL OR tipo = 'TEMPORARIA');
GO
