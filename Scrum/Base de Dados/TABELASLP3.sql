IF OBJECT_ID('Pessoa') IS NULL
CREATE TABLE Pessoa (
    id             INT IDENTITY PRIMARY KEY,
    nome           VARCHAR(100) NOT NULL,
    nif            CHAR(9) NOT NULL CONSTRAINT UQ_Pessoa_nif UNIQUE
                   CHECK (nif LIKE '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'),
    dataNascimento DATE,
    telemovel      VARCHAR(15),
    email          VARCHAR(100),
    cc             VARCHAR(12),
    morada         VARCHAR(200)
);
GO

IF OBJECT_ID('Cliente') IS NULL
CREATE TABLE Cliente (
    pessoaId INT PRIMARY KEY REFERENCES Pessoa(id)
);
GO

IF OBJECT_ID('Funcionario') IS NULL
CREATE TABLE Funcionario (
    pessoaId          INT PRIMARY KEY REFERENCES Pessoa(id),
    numeroFuncionario INT NOT NULL CONSTRAINT UQ_Funcionario_numero UNIQUE,
    dataAdmissao      DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE)
);
GO

IF OBJECT_ID('Utilizador') IS NULL
CREATE TABLE Utilizador (
    id             INT IDENTITY PRIMARY KEY,
    funcionarioId  INT NOT NULL UNIQUE REFERENCES Funcionario(pessoaId),
    nomeUtilizador VARCHAR(50) NOT NULL CONSTRAINT UQ_Utilizador_nome UNIQUE,
    passwordHash   CHAR(64) NOT NULL,
    passwordSalt   CHAR(32) NOT NULL,
    perfil         VARCHAR(12) NOT NULL CHECK (perfil IN ('GESTOR', 'RECECIONISTA')),
    ativo          BIT NOT NULL DEFAULT 1
);
GO

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

IF OBJECT_ID('Reserva') IS NULL
CREATE TABLE Reserva (
    id INT IDENTITY PRIMARY KEY,
    clienteId    INT NOT NULL REFERENCES Cliente(pessoaId),
    quartoNumero INT NOT NULL REFERENCES Quarto(numero),
    dataEntrada  DATE NOT NULL,
    dataSaida    DATE NOT NULL,
    numHospedes  INT NOT NULL CHECK (numHospedes > 0),
    estado       VARCHAR(10) NOT NULL DEFAULT 'CONFIRMADA'
                 CHECK (estado IN ('CONFIRMADA', 'EM_CURSO', 'CONCLUIDA', 'CANCELADA')),
    precoTotal   DECIMAL(10,2) NOT NULL CHECK (precoTotal >= 0),
    checkinEm    DATETIME2,
    checkoutEm   DATETIME2,
    CHECK (dataSaida > dataEntrada),
    CHECK (checkoutEm IS NULL OR (checkinEm IS NOT NULL AND checkoutEm >= checkinEm))
);
GO