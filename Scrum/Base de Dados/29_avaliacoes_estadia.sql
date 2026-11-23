IF OBJECT_ID('AvaliacaoEstadia') IS NULL
CREATE TABLE AvaliacaoEstadia (
    id         INT IDENTITY PRIMARY KEY,
    reservaId  INT NOT NULL CONSTRAINT UQ_AvaliacaoEstadia_reserva UNIQUE REFERENCES Reserva(id) ON DELETE CASCADE,
    estrelas   INT NOT NULL CHECK (estrelas BETWEEN 1 AND 5),
    comentario VARCHAR(500) NULL,
    avaliadaEm DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
