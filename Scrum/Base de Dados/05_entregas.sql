IF OBJECT_ID('Fornecedor') IS NULL
CREATE TABLE Fornecedor (
    id           INT IDENTITY PRIMARY KEY,
    codigo       VARCHAR(30)  NOT NULL CONSTRAINT UQ_Fornecedor_codigo UNIQUE,
    nome         VARCHAR(100) NOT NULL,
    morada1      VARCHAR(200),
    morada2      VARCHAR(200),
    cidade       VARCHAR(100),
    codigoPostal VARCHAR(20),
    pais         VARCHAR(60),
    codigoPais   CHAR(2)
);
GO

IF OBJECT_ID('Material') IS NULL
CREATE TABLE Material (
    id           INT IDENTITY PRIMARY KEY,
    fornecedorId INT NOT NULL REFERENCES Fornecedor(id),
    codigo       VARCHAR(30)  NOT NULL,
    descricao    VARCHAR(200) NOT NULL,
    CONSTRAINT UQ_Material_fornecedor_codigo UNIQUE (fornecedorId, codigo)
);
GO

IF OBJECT_ID('Entrega') IS NULL
CREATE TABLE Entrega (
    id              INT IDENTITY PRIMARY KEY,
    fornecedorId    INT NOT NULL REFERENCES Fornecedor(id),
    numeroEncomenda VARCHAR(30) NOT NULL,
    dataEncomenda   DATE NOT NULL,
    total           DECIMAL(12,2) NOT NULL CHECK (total >= 0),
    registadaPor    INT NOT NULL REFERENCES Funcionario(pessoaId),
    registadaEm     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Entrega_fornecedor_numero UNIQUE (fornecedorId, numeroEncomenda)
);
GO

IF OBJECT_ID('LinhaEntrega') IS NULL
CREATE TABLE LinhaEntrega (
    entregaId     INT NOT NULL REFERENCES Entrega(id) ON DELETE CASCADE,
    numeroLinha   INT NOT NULL,
    materialId    INT NOT NULL REFERENCES Material(id),
    quantidade    INT NOT NULL CHECK (quantidade > 0),
    unidade       VARCHAR(10) NOT NULL CHECK (unidade IN ('BOX', 'UNIT', 'KILOGRAM')),
    precoUnitario DECIMAL(10,2) NOT NULL CHECK (precoUnitario >= 0),
    valorSemIva   DECIMAL(12,2) NOT NULL,
    taxaIva       INT NOT NULL,
    valorIva      DECIMAL(12,2) NOT NULL,
    total         DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (entregaId, numeroLinha)
);
GO
