SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
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
    dataAdmissao      DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    empresa           VARCHAR(100) NULL
);
GO

IF OBJECT_ID('Utilizador') IS NULL
CREATE TABLE Utilizador (
    id             INT IDENTITY PRIMARY KEY,
    funcionarioId  INT NOT NULL UNIQUE REFERENCES Funcionario(pessoaId),
    nomeUtilizador VARCHAR(50) NOT NULL CONSTRAINT UQ_Utilizador_nome UNIQUE,
    passwordHash   CHAR(64) NOT NULL,
    passwordSalt   CHAR(32) NOT NULL,
    perfil         VARCHAR(12) NOT NULL
                   CONSTRAINT CK_Utilizador_perfil CHECK (perfil IN ('GESTOR', 'RECECIONISTA', 'RESTAURACAO')),
    ativo          BIT NOT NULL DEFAULT 1,
    mudarPassword  BIT NOT NULL CONSTRAINT DF_Utilizador_mudarPassword DEFAULT 0
);
GO

IF OBJECT_ID('Reserva') IS NULL
CREATE TABLE Reserva (
    id           INT IDENTITY PRIMARY KEY,
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
    cartao       VARCHAR(10) NULL,
    earlyCheckin BIT NOT NULL CONSTRAINT DF_Reserva_earlyCheckin DEFAULT 0,
    lateCheckout BIT NOT NULL CONSTRAINT DF_Reserva_lateCheckout DEFAULT 0,
    taxaHorario  DECIMAL(10,2) NOT NULL CONSTRAINT DF_Reserva_taxaHorario DEFAULT 0,
    criadaEm     DATETIME2 NULL CONSTRAINT DF_Reserva_criadaEm DEFAULT SYSDATETIME(),
    CHECK (dataSaida > dataEntrada),
    CHECK (checkoutEm IS NULL OR (checkinEm IS NOT NULL AND checkoutEm >= checkinEm))
);
GO

IF OBJECT_ID('Categoria') IS NULL
CREATE TABLE Categoria (
    id   INT IDENTITY PRIMARY KEY,
    nome VARCHAR(50) NOT NULL CONSTRAINT UQ_Categoria_nome UNIQUE
);
GO

IF OBJECT_ID('Servico') IS NULL
CREATE TABLE Servico (
    id          INT IDENTITY PRIMARY KEY,
    designacao  VARCHAR(100) NOT NULL CONSTRAINT UQ_Servico_designacao UNIQUE,
    preco       DECIMAL(8,2) NOT NULL CHECK (preco > 0),
    categoriaId INT NOT NULL REFERENCES Categoria(id),
    ativo       BIT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('ReservaServico') IS NULL
CREATE TABLE ReservaServico (
    id            INT IDENTITY PRIMARY KEY,
    reservaId     INT NOT NULL REFERENCES Reserva(id) ON DELETE CASCADE,
    servicoId     INT NOT NULL REFERENCES Servico(id),
    quantidade    INT NOT NULL CHECK (quantidade > 0),
    precoUnitario DECIMAL(8,2) NOT NULL CHECK (precoUnitario > 0),
    adicionadoEm  DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

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

IF OBJECT_ID('Fornecedor') IS NULL
CREATE TABLE Fornecedor (
    id           INT IDENTITY PRIMARY KEY,
    codigo       VARCHAR(30) NOT NULL CONSTRAINT UQ_Fornecedor_codigo UNIQUE,
    nome         VARCHAR(100) NOT NULL,
    morada1      VARCHAR(200),
    morada2      VARCHAR(200),
    cidade       VARCHAR(100),
    codigoPostal VARCHAR(20),
    pais         VARCHAR(60),
    codigoPais   CHAR(2),
    passwordHash CHAR(64) NULL,
    passwordSalt CHAR(32) NULL
);
GO

IF OBJECT_ID('Material') IS NULL
CREATE TABLE Material (
    id           INT IDENTITY PRIMARY KEY,
    fornecedorId INT NOT NULL REFERENCES Fornecedor(id),
    codigo       VARCHAR(30) NOT NULL,
    descricao    VARCHAR(200) NOT NULL,
    consumivel   BIT NOT NULL CONSTRAINT DF_Material_consumivel DEFAULT 1,
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
    registadaPor    INT NULL REFERENCES Funcionario(pessoaId),
    registadaEm     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    estado          VARCHAR(10) NOT NULL CONSTRAINT DF_Entrega_estado DEFAULT 'ACEITE'
                    CONSTRAINT CK_Entrega_estado CHECK (estado IN ('PENDENTE', 'ACEITE', 'RECUSADA')),
    decididaPor     INT NULL REFERENCES Funcionario(pessoaId),
    decididaEm      DATETIME2 NULL,
    motivoRecusa    VARCHAR(200) NULL,
    moeda           CHAR(3) NOT NULL CONSTRAINT DF_Entrega_moeda DEFAULT 'EUR'
);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_Entrega_fornecedor_numero')
CREATE UNIQUE INDEX UQ_Entrega_fornecedor_numero
    ON Entrega (fornecedorId, numeroEncomenda) WHERE estado <> 'RECUSADA';
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
    unidades      INT NULL CONSTRAINT CK_LinhaEntrega_unidades CHECK (unidades > 0),
    PRIMARY KEY (entregaId, numeroLinha)
);
GO

IF OBJECT_ID('Stock') IS NULL
CREATE TABLE Stock (
    materialId INT NOT NULL PRIMARY KEY REFERENCES Material(id),
    quantidade INT NOT NULL DEFAULT 0 CHECK (quantidade >= 0)
);
GO

IF OBJECT_ID('MovimentoStock') IS NULL
CREATE TABLE MovimentoStock (
    id              INT IDENTITY PRIMARY KEY,
    materialId      INT NOT NULL REFERENCES Material(id),
    quantidade      INT NOT NULL CHECK (quantidade > 0),
    funcionarioId   INT NOT NULL REFERENCES Funcionario(pessoaId),
    registadoEm     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    tipo            VARCHAR(12) NOT NULL CONSTRAINT DF_MovimentoStock_tipo DEFAULT 'DEFINITIVA'
                    CONSTRAINT CK_MovimentoStock_tipo CHECK (tipo IN ('TEMPORARIA', 'DEFINITIVA')),
    motivo          VARCHAR(25) NULL
                    CONSTRAINT CK_MovimentoStock_motivo CHECK (motivo IS NULL OR motivo IN
                        ('ESTRAGADO', 'ROUBADO', 'DEVOLVIDO_FORNECEDOR', 'OUTRO', 'CONSUMO_RESERVA')),
    descricaoMotivo VARCHAR(200) NULL,
    devolvidaEm     DATETIME2 NULL,
    devolvidaPor    INT NULL REFERENCES Funcionario(pessoaId),
    reservaId       INT NULL REFERENCES Reserva(id),
    CONSTRAINT CK_MovimentoStock_devolucao CHECK (devolvidaEm IS NULL OR tipo = 'TEMPORARIA')
);
GO

IF OBJECT_ID('SugestaoQueixa') IS NULL
CREATE TABLE SugestaoQueixa (
    id           INT IDENTITY PRIMARY KEY,
    clienteId    INT NOT NULL REFERENCES Cliente(pessoaId),
    tipo         VARCHAR(10) NOT NULL CHECK (tipo IN ('SUGESTAO', 'QUEIXA')),
    descricao    VARCHAR(1000) NOT NULL CHECK (LEN(LTRIM(RTRIM(descricao))) > 0),
    registadaPor INT NULL REFERENCES Funcionario(pessoaId),
    registadaEm  DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    reservaId    INT NULL REFERENCES Reserva(id) ON DELETE SET NULL
);
GO

IF OBJECT_ID('AcessoCartao') IS NULL
CREATE TABLE AcessoCartao (
    id            INT IDENTITY PRIMARY KEY,
    cartao        VARCHAR(10) NOT NULL,
    localAcesso   VARCHAR(20) NOT NULL
                  CONSTRAINT CK_AcessoCartao_local CHECK (localAcesso IN
                      ('QUARTO', 'BAR', 'SALA_CONFERENCIAS', 'RESTAURANTE', 'PISCINA', 'COZINHA', 'GARAGEM', 'ARMAZEM')),
    quartoNumero  INT NULL REFERENCES Quarto(numero),
    reservaId     INT NULL REFERENCES Reserva(id) ON DELETE CASCADE,
    registadoEm   DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    funcionarioId INT NULL REFERENCES Funcionario(pessoaId),
    CONSTRAINT CK_AcessoCartao_quarto CHECK ((localAcesso = 'QUARTO' AND quartoNumero IS NOT NULL)
        OR (localAcesso <> 'QUARTO' AND quartoNumero IS NULL)),
    CONSTRAINT CK_AcessoCartao_dono CHECK (reservaId IS NULL OR funcionarioId IS NULL)
);
GO

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

IF OBJECT_ID('ConfiguracaoQuarto') IS NULL
CREATE TABLE ConfiguracaoQuarto (
    tipoQuarto VARCHAR(10) NOT NULL CHECK (tipoQuarto IN ('INDIVIDUAL', 'DUPLO', 'FAMILIA')),
    materialId INT NOT NULL REFERENCES Material(id),
    quantidade INT NOT NULL CHECK (quantidade > 0),
    PRIMARY KEY (tipoQuarto, materialId)
);
GO

IF OBJECT_ID('MaterialReserva') IS NULL
CREATE TABLE MaterialReserva (
    reservaId  INT NOT NULL REFERENCES Reserva(id) ON DELETE CASCADE,
    materialId INT NOT NULL REFERENCES Material(id),
    quantidade INT NOT NULL CHECK (quantidade BETWEEN 0 AND 99),
    PRIMARY KEY (reservaId, materialId)
);
GO

IF OBJECT_ID('PedidoExtra') IS NULL
CREATE TABLE PedidoExtra (
    id           INT IDENTITY PRIMARY KEY,
    reservaId    INT NOT NULL REFERENCES Reserva(id) ON DELETE CASCADE,
    servicoId    INT NULL REFERENCES Servico(id),
    materialId   INT NULL REFERENCES Material(id),
    quantidade   INT NOT NULL CHECK (quantidade BETWEEN 1 AND 99),
    estado       VARCHAR(10) NOT NULL DEFAULT 'PENDENTE' CHECK (estado IN ('PENDENTE', 'ACEITE', 'RECUSADO')),
    motivoRecusa VARCHAR(200) NULL,
    pedidoEm     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    decididoEm   DATETIME2 NULL,
    decididoPor  INT NULL REFERENCES Funcionario(pessoaId),
    CONSTRAINT CK_PedidoExtra_servicoOuMaterial CHECK ((servicoId IS NOT NULL AND materialId IS NULL)
        OR (servicoId IS NULL AND materialId IS NOT NULL)),
    CONSTRAINT CK_PedidoExtra_motivo CHECK (estado <> 'RECUSADO' OR motivoRecusa IS NOT NULL)
);
GO

IF OBJECT_ID('AvaliacaoEstadia') IS NULL
CREATE TABLE AvaliacaoEstadia (
    id         INT IDENTITY PRIMARY KEY,
    reservaId  INT NOT NULL CONSTRAINT UQ_AvaliacaoEstadia_reserva UNIQUE REFERENCES Reserva(id) ON DELETE CASCADE,
    estrelas   INT NOT NULL CHECK (estrelas BETWEEN 1 AND 5),
    comentario VARCHAR(500) NULL,
    avaliadaEm DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

IF NOT EXISTS (SELECT 1 FROM Tarifa)
INSERT Tarifa (tipo, vista, valorNoite) VALUES
    ('INDIVIDUAL', 'NENHUMA',  50.00), ('INDIVIDUAL', 'MAR',  65.00), ('INDIVIDUAL', 'MONTANHA',  60.00),
    ('DUPLO',      'NENHUMA',  80.00), ('DUPLO',      'MAR', 100.00), ('DUPLO',      'MONTANHA',  90.00),
    ('FAMILIA',    'NENHUMA', 120.00), ('FAMILIA',    'MAR', 150.00), ('FAMILIA',    'MONTANHA', 135.00);
GO

IF NOT EXISTS (SELECT 1 FROM Quarto)
INSERT Quarto (numero, piso, tipo, vista, capacidade) VALUES
    (201, 2, 'INDIVIDUAL', 'NENHUMA',  1), (202, 2, 'INDIVIDUAL', 'NENHUMA',  1),
    (203, 2, 'INDIVIDUAL', 'NENHUMA',  1), (204, 2, 'INDIVIDUAL', 'MONTANHA', 1),
    (205, 2, 'INDIVIDUAL', 'MONTANHA', 1), (206, 2, 'INDIVIDUAL', 'MAR',      1),
    (207, 2, 'INDIVIDUAL', 'MAR',      1), (208, 2, 'DUPLO',      'NENHUMA',  2),
    (209, 2, 'DUPLO',      'MONTANHA', 2), (210, 2, 'DUPLO',      'MAR',      2),
    (301, 3, 'DUPLO',      'NENHUMA',  2), (302, 3, 'DUPLO',      'NENHUMA',  2),
    (303, 3, 'DUPLO',      'MONTANHA', 2), (304, 3, 'DUPLO',      'MONTANHA', 2),
    (305, 3, 'DUPLO',      'MAR',      2), (306, 3, 'DUPLO',      'MAR',      2),
    (307, 3, 'DUPLO',      'MAR',      2), (308, 3, 'FAMILIA',    'MONTANHA', 4),
    (309, 3, 'FAMILIA',    'MAR',      4), (310, 3, 'FAMILIA',    'MAR',      4);
GO

IF NOT EXISTS (SELECT 1 FROM Categoria)
INSERT Categoria (nome) VALUES ('Refeição'), ('Spa'), ('Quarto'), ('Lazer'), ('Outros');
GO

IF NOT EXISTS (SELECT 1 FROM Servico)
INSERT Servico (designacao, preco, categoriaId, ativo)
SELECT s.designacao, s.preco, c.id, s.ativo
FROM (VALUES ('Pequeno-almoço', 12.00, 'Refeição', 1),
             ('Almoço',         18.00, 'Refeição', 1),
             ('Jantar',         22.00, 'Refeição', 1),
             ('Massagem',       45.00, 'Spa',      1),
             ('Banho turco',    20.00, 'Spa',      1),
             ('Sauna',          15.00, 'Spa',      1),
             ('Mini-bar',       10.00, 'Quarto',   1),
             ('Limpeza extra',  15.00, 'Quarto',   1),
             ('Early check-in', 20.00, 'Quarto',   0),
             ('Late check-out', 20.00, 'Quarto',   0),
             ('Piscina',         5.00, 'Lazer',    1),
             ('Ginásio',         5.00, 'Lazer',    1),
             ('Lavandaria',     10.00, 'Outros',   1),
             ('Estacionamento',  8.00, 'Outros',   1)) AS s (designacao, preco, categoria, ativo)
JOIN Categoria c ON c.nome = s.categoria;
GO

IF NOT EXISTS (SELECT 1 FROM HorarioReserva)
INSERT HorarioReserva (id, horaCheckin, horaCheckout, horaEarlyCheckin, horaLateCheckout,
                       taxaEarlyCheckin, taxaLateCheckout)
VALUES (1, '15:00', '12:00', '13:00', '14:00', 20.00, 20.00);
GO

IF NOT EXISTS (SELECT 1 FROM Utilizador WHERE perfil = 'GESTOR')
BEGIN
    DECLARE @gestor INT = (SELECT id FROM Pessoa WHERE nif = '213456788');
    IF @gestor IS NULL
    BEGIN
        INSERT Pessoa (nome, nif, email) VALUES ('Gestor do Hotel', '213456788', 'gestor@hotel4you.pt');
        SET @gestor = SCOPE_IDENTITY();
    END
    IF NOT EXISTS (SELECT 1 FROM Funcionario WHERE pessoaId = @gestor)
        INSERT Funcionario (pessoaId, numeroFuncionario)
        VALUES (@gestor, (SELECT ISNULL(MAX(numeroFuncionario), 0) + 1 FROM Funcionario));
    INSERT Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil, mudarPassword)
    VALUES (@gestor, 'gestor', 'de426e53d60ce4436b8e869f8440b950266ceeef2aaafd946e49b5a095d48340',
            '401100f48168203644108ca1e5eb38d0', 'GESTOR', 1);
END
GO
