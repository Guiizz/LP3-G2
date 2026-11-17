IF OBJECT_ID('ContaCliente') IS NULL
CREATE TABLE ContaCliente (
    id             INT IDENTITY PRIMARY KEY,
    clienteId      INT NOT NULL CONSTRAINT UQ_ContaCliente_cliente UNIQUE REFERENCES Cliente(pessoaId),
    passwordHash   CHAR(64) NOT NULL,
    passwordSalt   CHAR(32) NOT NULL,
    primeiroAcesso BIT NOT NULL DEFAULT 1,
    criadaEm       DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
