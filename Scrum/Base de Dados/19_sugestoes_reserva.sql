IF COL_LENGTH('SugestaoQueixa', 'reservaId') IS NULL
ALTER TABLE SugestaoQueixa ADD reservaId INT NULL REFERENCES Reserva(id) ON DELETE SET NULL;
GO
