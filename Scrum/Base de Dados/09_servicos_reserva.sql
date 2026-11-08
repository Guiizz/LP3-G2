IF OBJECT_ID('ReservaServico') IS NULL
CREATE TABLE ReservaServico (
    id            INT IDENTITY PRIMARY KEY,
    reservaId     INT NOT NULL REFERENCES Reserva(id) ON DELETE CASCADE,
    servicoId     INT NOT NULL REFERENCES Servico(id),
    quantidade    INT NOT NULL CHECK (quantidade > 0),
    precoUnitario DECIMAL(8,2) NOT NULL CHECK (precoUnitario > 0),
    adicionadoEm  DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
