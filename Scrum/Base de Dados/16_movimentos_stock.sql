IF OBJECT_ID('MovimentoStock') IS NULL
CREATE TABLE MovimentoStock (
    id            INT IDENTITY PRIMARY KEY,
    materialId    INT NOT NULL REFERENCES Material(id),
    quantidade    INT NOT NULL CHECK (quantidade > 0),
    funcionarioId INT NOT NULL REFERENCES Funcionario(pessoaId),
    registadoEm   DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
