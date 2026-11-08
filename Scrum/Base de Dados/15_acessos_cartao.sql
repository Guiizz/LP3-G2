IF OBJECT_ID('AcessoCartao') IS NULL
CREATE TABLE AcessoCartao (
    id           INT IDENTITY PRIMARY KEY,
    cartao       VARCHAR(10) NOT NULL,
    localAcesso  VARCHAR(12) NOT NULL CHECK (localAcesso IN ('QUARTO', 'RESTAURANTE', 'PISCINA')),
    quartoNumero INT NULL REFERENCES Quarto(numero),
    reservaId    INT NULL REFERENCES Reserva(id) ON DELETE CASCADE,
    registadoEm  DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT CK_AcessoCartao_quarto CHECK ((localAcesso = 'QUARTO' AND quartoNumero IS NOT NULL)
        OR (localAcesso <> 'QUARTO' AND quartoNumero IS NULL))
);
GO
