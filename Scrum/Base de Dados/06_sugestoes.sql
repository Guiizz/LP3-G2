IF OBJECT_ID('SugestaoQueixa') IS NULL
CREATE TABLE SugestaoQueixa (
    id           INT IDENTITY PRIMARY KEY,
    clienteId    INT NOT NULL REFERENCES Cliente(pessoaId),
    tipo         VARCHAR(10) NOT NULL CHECK (tipo IN ('SUGESTAO', 'QUEIXA')),
    descricao    VARCHAR(1000) NOT NULL CHECK (LEN(LTRIM(RTRIM(descricao))) > 0),
    registadaPor INT NOT NULL REFERENCES Funcionario(pessoaId),
    registadaEm  DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
