-- US10: tabelas de quartos e tarifas
-- Os valores dos CHECK espelham os enums TipoQuarto e Vista do model.

IF OBJECT_ID('Tarifa') IS NULL
CREATE TABLE Tarifa (
    tipo       VARCHAR(10) NOT NULL CHECK (tipo IN ('INDIVIDUAL', 'DUPLO', 'FAMILIA')),
    vista      VARCHAR(10) NOT NULL CHECK (vista IN ('NENHUMA', 'MAR', 'MONTANHA')),
    valorNoite DECIMAL(8,2) NOT NULL CHECK (valorNoite > 0),
    PRIMARY KEY (tipo, vista)
);
GO

IF OBJECT_ID('Quarto') IS NULL
CREATE TABLE Quarto (
    numero     INT PRIMARY KEY,
    piso       INT NOT NULL CHECK (piso >= 0),
    tipo       VARCHAR(10) NOT NULL CHECK (tipo IN ('INDIVIDUAL', 'DUPLO', 'FAMILIA')),
    vista      VARCHAR(10) NOT NULL CHECK (vista IN ('NENHUMA', 'MAR', 'MONTANHA')),
    capacidade INT NOT NULL CHECK (capacidade > 0),
    ativo      BIT NOT NULL DEFAULT 1,
    FOREIGN KEY (tipo, vista) REFERENCES Tarifa (tipo, vista)
);
GO