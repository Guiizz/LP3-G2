SET QUOTED_IDENTIFIER ON;
GO

IF COL_LENGTH('MovimentoStock', 'reservaId') IS NULL
ALTER TABLE MovimentoStock ADD reservaId INT NULL REFERENCES Reserva(id);
GO

IF OBJECT_ID('PedidoExtra') IS NULL
CREATE TABLE PedidoExtra (
    id           INT IDENTITY PRIMARY KEY,
    reservaId    INT NOT NULL REFERENCES Reserva(id) ON DELETE CASCADE,
    servicoId    INT NULL REFERENCES Servico(id),
    materialId   INT NULL REFERENCES Material(id),
    quantidade   INT NOT NULL CHECK (quantidade BETWEEN 1 AND 99),
    estado       VARCHAR(10) NOT NULL DEFAULT 'PENDENTE' CHECK (estado IN ('PENDENTE', 'ACEITE', 'RECUSADO')),
    motivoRecusa VARCHAR(200) NULL,
    pedidoEm     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    decididoEm   DATETIME2 NULL,
    decididoPor  INT NULL REFERENCES Funcionario(pessoaId),
    CONSTRAINT CK_PedidoExtra_servicoOuMaterial CHECK ((servicoId IS NOT NULL AND materialId IS NULL)
        OR (servicoId IS NULL AND materialId IS NOT NULL)),
    CONSTRAINT CK_PedidoExtra_motivo CHECK (estado <> 'RECUSADO' OR motivoRecusa IS NOT NULL)
);
GO
