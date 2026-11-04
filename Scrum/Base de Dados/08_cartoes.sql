IF COL_LENGTH('Reserva', 'cartao') IS NULL
ALTER TABLE Reserva ADD cartao VARCHAR(10) NULL;
GO
