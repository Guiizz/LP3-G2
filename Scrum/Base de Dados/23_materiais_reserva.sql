SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID('MaterialReserva') IS NULL
CREATE TABLE MaterialReserva (
    reservaId  INT NOT NULL REFERENCES Reserva(id) ON DELETE CASCADE,
    materialId INT NOT NULL REFERENCES Material(id),
    quantidade INT NOT NULL CHECK (quantidade BETWEEN 0 AND 99),
    PRIMARY KEY (reservaId, materialId)
);
GO
