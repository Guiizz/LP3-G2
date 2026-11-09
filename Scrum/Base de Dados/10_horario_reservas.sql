IF OBJECT_ID('HorarioReserva') IS NULL
CREATE TABLE HorarioReserva (
    id               INT PRIMARY KEY CHECK (id = 1),
    horaCheckin      TIME NOT NULL,
    horaCheckout     TIME NOT NULL,
    horaEarlyCheckin TIME NOT NULL,
    horaLateCheckout TIME NOT NULL,
    taxaEarlyCheckin DECIMAL(8,2) NOT NULL CHECK (taxaEarlyCheckin >= 0),
    taxaLateCheckout DECIMAL(8,2) NOT NULL CHECK (taxaLateCheckout >= 0),
    CHECK (horaEarlyCheckin < horaCheckin),
    CHECK (horaLateCheckout > horaCheckout)
);
GO

IF NOT EXISTS (SELECT 1 FROM HorarioReserva)
INSERT HorarioReserva (id, horaCheckin, horaCheckout, horaEarlyCheckin, horaLateCheckout,
                       taxaEarlyCheckin, taxaLateCheckout)
VALUES (1, '15:00', '12:00', '13:00', '14:00', 20.00, 20.00);
GO

IF COL_LENGTH('Reserva', 'earlyCheckin') IS NULL
ALTER TABLE Reserva ADD earlyCheckin BIT NOT NULL CONSTRAINT DF_Reserva_earlyCheckin DEFAULT 0;
GO

IF COL_LENGTH('Reserva', 'lateCheckout') IS NULL
ALTER TABLE Reserva ADD lateCheckout BIT NOT NULL CONSTRAINT DF_Reserva_lateCheckout DEFAULT 0;
GO

IF COL_LENGTH('Reserva', 'taxaHorario') IS NULL
ALTER TABLE Reserva ADD taxaHorario DECIMAL(10,2) NOT NULL CONSTRAINT DF_Reserva_taxaHorario DEFAULT 0;
GO
