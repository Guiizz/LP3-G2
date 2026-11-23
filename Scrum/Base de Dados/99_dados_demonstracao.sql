SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

IF OBJECT_ID('AvaliacaoEstadia') IS NULL OR COL_LENGTH('Utilizador', 'mudarPassword') IS NULL
BEGIN
    RAISERROR('Corra primeiro o 00_criar_base_de_dados.sql.', 16, 1);
    RETURN;
END

IF EXISTS (SELECT 1 FROM Pessoa WHERE nif = '246789123')
BEGIN
    PRINT 'Os dados de demonstração já estão nesta base de dados.';
    RETURN;
END

IF EXISTS (SELECT 1 FROM Pessoa WHERE nif <> '213456788') OR EXISTS (SELECT 1 FROM Fornecedor)
BEGIN
    RAISERROR('Esta base de dados já tem dados. O 99 só corre numa base de dados nova, criada com o 00.', 16, 1);
    RETURN;
END

DECLARE @gestor INT = (SELECT TOP 1 funcionarioId FROM Utilizador WHERE perfil = 'GESTOR' ORDER BY id);
IF @gestor IS NULL
BEGIN
    RAISERROR('Falta a conta do gestor criada pelo 00_criar_base_de_dados.sql.', 16, 1);
    RETURN;
END

BEGIN TRANSACTION;

DECLARE @hoje DATETIME2 = CAST(CAST(GETDATE() AS DATE) AS DATETIME2);

DECLARE @pessoas TABLE (nome VARCHAR(100), nif CHAR(9), nascimento DATE, telemovel VARCHAR(15), email VARCHAR(100),
                        morada VARCHAR(200), cliente BIT);
INSERT @pessoas VALUES
    ('Rita Almeida',     '224567896', '1990-04-12', '912345001', 'rita.almeida@hotel4you.pt',  'Rua das Flores 10, Porto',        0),
    ('Hugo Pinto',       '235678910', '1985-09-30', '912345002', 'hugo.pinto@saboresdoporto.pt', 'Rua de Cedofeita 52, Porto',    0),
    ('Ana Martins',      '246789123', '1992-02-03', '913000101', 'ana.martins@mail.pt',        'Avenida da Boavista 1200, Porto', 1),
    ('Carla Sousa',      '257891234', '1988-07-21', '913000102', 'carla.sousa@mail.pt',        'Rua Direita 5, Braga',            1),
    ('Bruno Costa',      '268912343', '1979-11-14', '913000103', 'bruno.costa@mail.pt',        'Rua do Sol 33, Aveiro',           1),
    ('Diogo Ferreira',   '279123450', '1995-05-09', '913000104', 'diogo.ferreira@mail.pt',     'Rua Nova 8, Viseu',               1),
    ('Eva Lopes',        '281234566', '1983-01-27', '913000105', 'eva.lopes@mail.pt',          'Praça da Liberdade 2, Porto',     1),
    ('Filipe Nunes',     '292345674', '1976-08-18', '913000106', 'filipe.nunes@mail.pt',       'Rua de Santa Catarina 400, Porto', 1),
    ('Gabriela Reis',    '213579138', '1998-12-01', '913000107', 'gabriela.reis@mail.pt',      'Rua do Carmo 14, Lisboa',         1),
    ('Henrique Matos',   '224680242', '1969-03-15', '913000108', 'henrique.matos@mail.pt',     'Avenida Central 77, Coimbra',     1),
    ('Inês Carvalho',    '235791350', '1987-06-06', '913000109', 'ines.carvalho@mail.pt',      'Rua da Praia 3, Vila do Conde',   1),
    ('João Ribeiro',     '246802464', '1993-10-22', '913000110', 'joao.ribeiro@mail.pt',       'Rua Alta 21, Guimarães',          1),
    ('Marta Fonseca',    '257913572', '1990-09-09', '913000111', 'marta.fonseca@mail.pt',      'Largo do Toural 9, Guimarães',    1);

INSERT Pessoa (nome, nif, dataNascimento, telemovel, email, morada)
SELECT nome, nif, nascimento, telemovel, email, morada FROM @pessoas;

INSERT Cliente (pessoaId)
SELECT p.id FROM Pessoa p JOIN @pessoas x ON x.nif = p.nif WHERE x.cliente = 1;

DECLARE @rececao INT = (SELECT id FROM Pessoa WHERE nif = '224567896');
DECLARE @restauracao INT = (SELECT id FROM Pessoa WHERE nif = '235678910');

INSERT Funcionario (pessoaId, numeroFuncionario, dataAdmissao, empresa) VALUES
    (@rececao, 2, DATEADD(YEAR, -2, CAST(@hoje AS DATE)), NULL),
    (@restauracao, 3, DATEADD(MONTH, -6, CAST(@hoje AS DATE)), 'Sabores do Porto, Lda.');

INSERT Utilizador (funcionarioId, nomeUtilizador, passwordHash, passwordSalt, perfil, mudarPassword) VALUES
    (@rececao, 'rececao', '3edbea6af2dc92586270c6fd3b4407e1321313fc6a2fcf1e350abc35ebbd1532',
     '53b0e424f3204f8cb4113580bbcf6364', 'RECECIONISTA', 0),
    (@restauracao, 'restauracao', 'e752be91a190b963874aab08d18d4998ed7c525cef83cf90733be956d1d8d393',
     '6b9c3268653e4473f0608996b2e701ed', 'RESTAURACAO', 0);

INSERT Fornecedor (codigo, nome, morada1, morada2, cidade, codigoPostal, pais, codigoPais, passwordHash, passwordSalt) VALUES
    ('11PT920', 'ProductHero', 'Rua da Alegria', 'nº1', 'Porto', '4000-032', 'Portugal', 'PT',
     'e2c6700f327873b11a3e216436290c9a2cdd09556af1cef0fdc8db089e24d3f2', 'efe1f2acb3fae1df92344d478e95dd3d'),
    ('964070328', 'L''industrie Alimentaire', 'Rue internationale de Paris 96', NULL, 'Paris', '75001', 'France', 'FR',
     '6b0025a3aa5a0dcdd91e54f6761ee44fd4fd995b5ecc7889990a84e76ec1a73d', 'c828acd50dc5e366d4c3579a53a54e8d');

DECLARE @productHero INT = (SELECT id FROM Fornecedor WHERE codigo = '11PT920');

INSERT Material (fornecedorId, codigo, descricao, consumivel) VALUES
    (@productHero, 'PZ-1989', 'Toalha de banho 400g cinza', 0),
    (@productHero, 'XQF6324', 'Rituals Condicionador/Champô de banho 200ml', 1),
    (@productHero, 'X569P', 'Secador de cabelo Philips Preto', 0),
    (@productHero, 'LG18964', 'LG 32LM6370PLA LED FullHD', 0),
    (@productHero, 'CF-9812', 'Cofre hotel ALPHA 25', 0);

DECLARE @toalha INT = (SELECT id FROM Material WHERE fornecedorId = @productHero AND codigo = 'PZ-1989');
DECLARE @champo INT = (SELECT id FROM Material WHERE fornecedorId = @productHero AND codigo = 'XQF6324');
DECLARE @secador INT = (SELECT id FROM Material WHERE fornecedorId = @productHero AND codigo = 'X569P');
DECLARE @televisao INT = (SELECT id FROM Material WHERE fornecedorId = @productHero AND codigo = 'LG18964');
DECLARE @cofre INT = (SELECT id FROM Material WHERE fornecedorId = @productHero AND codigo = 'CF-9812');

INSERT ConfiguracaoQuarto (tipoQuarto, materialId, quantidade) VALUES
    ('INDIVIDUAL', @toalha, 2), ('INDIVIDUAL', @champo, 1), ('INDIVIDUAL', @secador, 1),
    ('DUPLO',      @toalha, 4), ('DUPLO',      @champo, 2), ('DUPLO',      @secador, 1),
    ('FAMILIA',    @toalha, 8), ('FAMILIA',    @champo, 4), ('FAMILIA',    @secador, 1);

DECLARE @entrega INT;

INSERT Entrega (fornecedorId, numeroEncomenda, dataEncomenda, total, registadaPor, registadaEm, estado,
                decididaPor, decididaEm, motivoRecusa, moeda)
VALUES (@productHero, 'OC50001', CAST(DATEADD(DAY, -75, @hoje) AS DATE), 2964.30, NULL,
        DATEADD(MINUTE, 600, DATEADD(DAY, -74, @hoje)), 'ACEITE', @gestor,
        DATEADD(MINUTE, 660, DATEADD(DAY, -73, @hoje)), NULL, 'EUR');
SET @entrega = SCOPE_IDENTITY();
INSERT LinhaEntrega (entregaId, numeroLinha, materialId, quantidade, unidade, precoUnitario, valorSemIva, taxaIva,
                     valorIva, total, unidades) VALUES
    (@entrega, 1, @toalha,  50, 'BOX',  25.10, 1255.00, 23, 288.65, 1543.65, 500),
    (@entrega, 2, @champo,  20, 'BOX',  30.00,  600.00, 23, 138.00,  738.00, 400),
    (@entrega, 3, @secador, 30, 'UNIT', 18.50,  555.00, 23, 127.65,  682.65,  30);

INSERT Entrega (fornecedorId, numeroEncomenda, dataEncomenda, total, registadaPor, registadaEm, estado,
                decididaPor, decididaEm, motivoRecusa, moeda)
VALUES (@productHero, 'OC50002', CAST(DATEADD(DAY, -20, @hoje) AS DATE), 1536.89, NULL,
        DATEADD(MINUTE, 540, DATEADD(DAY, -19, @hoje)), 'RECUSADA', @rececao,
        DATEADD(MINUTE, 615, DATEADD(DAY, -19, @hoje)), 'As quantidades não batem certo com a encomenda.', 'EUR');
SET @entrega = SCOPE_IDENTITY();
INSERT LinhaEntrega (entregaId, numeroLinha, materialId, quantidade, unidade, precoUnitario, valorSemIva, taxaIva,
                     valorIva, total, unidades) VALUES
    (@entrega, 1, @televisao, 5, 'UNIT', 249.90, 1249.50, 23, 287.39, 1536.89, 5);

INSERT Entrega (fornecedorId, numeroEncomenda, dataEncomenda, total, registadaPor, registadaEm, estado,
                decididaPor, decididaEm, motivoRecusa, moeda)
VALUES (@productHero, 'OC50003', CAST(DATEADD(DAY, -2, @hoje) AS DATE), 856.08, NULL,
        DATEADD(MINUTE, 1020, DATEADD(DAY, -1, @hoje)), 'PENDENTE', NULL, NULL, NULL, 'EUR');
SET @entrega = SCOPE_IDENTITY();
INSERT LinhaEntrega (entregaId, numeroLinha, materialId, quantidade, unidade, precoUnitario, valorSemIva, taxaIva,
                     valorIva, total, unidades) VALUES
    (@entrega, 1, @toalha, 10, 'BOX',  25.10, 251.00, 23,  57.73, 308.73, 100),
    (@entrega, 2, @cofre,   5, 'UNIT', 89.00, 445.00, 23, 102.35, 547.35,   5);

DECLARE @reservas TABLE (chave INT PRIMARY KEY, nif CHAR(9), quarto INT, entrada INT, noites INT, estado VARCHAR(10),
                         hospedes INT, criadaAntes INT, late BIT, id INT NULL);
INSERT @reservas (chave, nif, quarto, entrada, noites, estado, hospedes, criadaAntes, late) VALUES
    ( 1, '246789123', 201,  -2, 4, 'EM_CURSO',   1, 20, 0),
    ( 2, '246789123', 206,  20, 3, 'CONFIRMADA', 1, 25, 0),
    ( 3, '246789123', 204, -40, 3, 'CONCLUIDA',  1, 30, 0),
    ( 4, '246789123', 207, -15, 2, 'CONCLUIDA',  1, 12, 0),
    ( 5, '268912343', 208,   0, 2, 'CONFIRMADA', 2,  7, 0),
    ( 6, '257891234', 302,  -1, 3, 'EM_CURSO',   2,  9, 0),
    ( 7, '279123450', 209,  -1, 2, 'CONFIRMADA', 2, 10, 0),
    ( 8, '281234566', 305, -55, 4, 'CONCLUIDA',  2, 40, 0),
    ( 9, '292345674', 308, -48, 5, 'CONCLUIDA',  4, 60, 0),
    (10, '213579138', 210, -35, 3, 'CONCLUIDA',  2, 15, 0),
    (11, '224680242', 303, -28, 2, 'CONCLUIDA',  2,  5, 0),
    (12, '235791350', 309, -21, 6, 'CONCLUIDA',  4, 45, 1),
    (13, '246802464', 202, -10, 3, 'CONCLUIDA',  1,  3, 0),
    (14, '257913572', 306,  -7, 4, 'CONCLUIDA',  2, 21, 0),
    (15, '281234566', 304,  -3, 2, 'CONCLUIDA',  2,  8, 0),
    (16, '224680242', 301,  10, 2, 'CANCELADA',  2, 14, 0),
    (17, '292345674', 310,   5, 3, 'CONFIRMADA', 4, 12, 0),
    (18, '213579138', 203, -60, 2, 'CONCLUIDA',  1, 10, 0),
    (19, '246802464', 307,   2, 3, 'CONFIRMADA', 2,  9, 0),
    (20, '235791350', 305, -18, 2, 'CANCELADA',  2, 20, 0);

INSERT Reserva (clienteId, quartoNumero, dataEntrada, dataSaida, numHospedes, estado, precoTotal, checkinEm,
                checkoutEm, cartao, earlyCheckin, lateCheckout, taxaHorario, criadaEm)
SELECT p.id, r.quarto,
       CAST(DATEADD(DAY, r.entrada, @hoje) AS DATE),
       CAST(DATEADD(DAY, r.entrada + r.noites, @hoje) AS DATE),
       r.hospedes, r.estado, r.noites * t.valorNoite,
       CASE WHEN r.estado IN ('EM_CURSO', 'CONCLUIDA')
            THEN DATEADD(MINUTE, 900 + r.chave * 7, DATEADD(DAY, r.entrada, @hoje)) END,
       CASE WHEN r.estado = 'CONCLUIDA'
            THEN DATEADD(MINUTE, CASE WHEN r.late = 1 THEN 810 ELSE 660 + r.chave * 3 END,
                         DATEADD(DAY, r.entrada + r.noites, @hoje)) END,
       CASE WHEN r.estado IN ('EM_CURSO', 'CONCLUIDA') THEN 'C' + CAST(r.quarto AS VARCHAR(5)) END,
       0, r.late, CASE WHEN r.late = 1 THEN 20.00 ELSE 0 END,
       DATEADD(MINUTE, 600 + r.chave * 11, DATEADD(DAY, r.entrada - r.criadaAntes, @hoje))
FROM @reservas r
JOIN Pessoa p ON p.nif = r.nif
JOIN Quarto q ON q.numero = r.quarto
JOIN Tarifa t ON t.tipo = q.tipo AND t.vista = q.vista
ORDER BY r.chave;

UPDATE r SET r.id = res.id
FROM @reservas r
JOIN Pessoa p ON p.nif = r.nif
JOIN Reserva res ON res.clienteId = p.id AND res.quartoNumero = r.quarto
                AND res.dataEntrada = CAST(DATEADD(DAY, r.entrada, @hoje) AS DATE);

DECLARE @servicos TABLE (chave INT, designacao VARCHAR(100), quantidade INT);
INSERT @servicos VALUES
    ( 1, 'Piscina', 2), ( 1, 'Jantar', 2), ( 1, 'Pequeno-almoço', 4),
    ( 2, 'Pequeno-almoço', 3),
    ( 3, 'Pequeno-almoço', 3),
    ( 5, 'Estacionamento', 2),
    ( 6, 'Pequeno-almoço', 6),
    ( 8, 'Jantar', 2), ( 8, 'Massagem', 1),
    ( 9, 'Pequeno-almoço', 10), ( 9, 'Piscina', 4),
    (10, 'Massagem', 2), (10, 'Sauna', 1),
    (11, 'Lavandaria', 1),
    (12, 'Pequeno-almoço', 12), (12, 'Jantar', 4), (12, 'Piscina', 4),
    (13, 'Ginásio', 2),
    (14, 'Jantar', 2), (14, 'Banho turco', 2),
    (15, 'Pequeno-almoço', 4),
    (17, 'Pequeno-almoço', 6),
    (18, 'Pequeno-almoço', 2),
    (19, 'Estacionamento', 3);

INSERT ReservaServico (reservaId, servicoId, quantidade, precoUnitario, adicionadoEm)
SELECT r.id, s.id, x.quantidade, s.preco, COALESCE(res.checkinEm, res.criadaEm)
FROM @servicos x
JOIN @reservas r ON r.chave = x.chave
JOIN Reserva res ON res.id = r.id
JOIN Servico s ON s.designacao = x.designacao;

INSERT MaterialReserva (reservaId, materialId, quantidade)
SELECT r.id, c.materialId, CASE WHEN c.materialId = @toalha THEN 6 ELSE c.quantidade END
FROM @reservas r
JOIN ConfiguracaoQuarto c ON c.tipoQuarto = 'DUPLO'
WHERE r.chave = 5;

INSERT MovimentoStock (materialId, quantidade, funcionarioId, registadoEm, tipo, motivo, descricaoMotivo,
                       devolvidaEm, devolvidaPor, reservaId)
SELECT c.materialId, c.quantidade, @rececao, res.checkinEm,
       CASE WHEN m.consumivel = 1 THEN 'DEFINITIVA' ELSE 'TEMPORARIA' END,
       CASE WHEN m.consumivel = 1 THEN 'CONSUMO_RESERVA' END,
       'Check-in da reserva ' + CAST(res.id AS VARCHAR(10)),
       CASE WHEN m.consumivel = 0 AND res.estado = 'CONCLUIDA' THEN res.checkoutEm END,
       CASE WHEN m.consumivel = 0 AND res.estado = 'CONCLUIDA' THEN @rececao END,
       res.id
FROM @reservas r
JOIN Reserva res ON res.id = r.id
JOIN Quarto q ON q.numero = res.quartoNumero
JOIN ConfiguracaoQuarto c ON c.tipoQuarto = q.tipo
JOIN Material m ON m.id = c.materialId
WHERE res.estado IN ('EM_CURSO', 'CONCLUIDA');

DECLARE @anaEmCurso INT = (SELECT id FROM @reservas WHERE chave = 1);
DECLARE @carlaEmCurso INT = (SELECT id FROM @reservas WHERE chave = 6);

INSERT PedidoExtra (reservaId, servicoId, materialId, quantidade, estado, motivoRecusa, pedidoEm, decididoEm, decididoPor)
VALUES
    (@anaEmCurso, (SELECT id FROM Servico WHERE designacao = 'Massagem'), NULL, 1, 'RECUSADO',
     'O spa está fechado para manutenção.', DATEADD(MINUTE, 600, DATEADD(DAY, -1, @hoje)),
     DATEADD(MINUTE, 620, DATEADD(DAY, -1, @hoje)), @rececao),
    (@anaEmCurso, NULL, @champo, 1, 'ACEITE', NULL, DATEADD(MINUTE, 1260, DATEADD(DAY, -1, @hoje)),
     DATEADD(MINUTE, 1270, DATEADD(DAY, -1, @hoje)), @rececao),
    (@carlaEmCurso, NULL, @toalha, 2, 'PENDENTE', NULL, DATEADD(HOUR, -2, SYSDATETIME()), NULL, NULL),
    (@carlaEmCurso, (SELECT id FROM Servico WHERE designacao = 'Limpeza extra'), NULL, 1, 'PENDENTE', NULL,
     DATEADD(HOUR, -1, SYSDATETIME()), NULL, NULL);

INSERT MovimentoStock (materialId, quantidade, funcionarioId, registadoEm, tipo, motivo, descricaoMotivo,
                       devolvidaEm, devolvidaPor, reservaId)
VALUES
    (@champo, 1, @rececao, DATEADD(MINUTE, 1270, DATEADD(DAY, -1, @hoje)), 'DEFINITIVA', 'CONSUMO_RESERVA',
     'Pedido extra da reserva ' + CAST(@anaEmCurso AS VARCHAR(10)), NULL, NULL, @anaEmCurso),
    (@champo, 3, @gestor, DATEADD(MINUTE, 600, DATEADD(DAY, -12, @hoje)), 'DEFINITIVA', 'ESTRAGADO',
     'Frascos partidos no armazém.', NULL, NULL, NULL),
    (@secador, 1, @gestor, DATEADD(MINUTE, 570, DATEADD(DAY, -3, @hoje)), 'TEMPORARIA', NULL,
     'Emprestado ao spa.', NULL, NULL, NULL);

INSERT Stock (materialId, quantidade)
SELECT m.id,
       ISNULL((SELECT SUM(l.unidades) FROM LinhaEntrega l JOIN Entrega e ON e.id = l.entregaId
               WHERE l.materialId = m.id AND e.estado = 'ACEITE'), 0)
     - ISNULL((SELECT SUM(ms.quantidade) FROM MovimentoStock ms
               WHERE ms.materialId = m.id AND (ms.tipo = 'DEFINITIVA' OR ms.devolvidaEm IS NULL)), 0)
FROM Material m
WHERE m.fornecedorId = @productHero;

INSERT ContaCliente (clienteId, passwordHash, passwordSalt, primeiroAcesso, criadaEm)
SELECT p.id, c.hash, c.salt, 0, DATEADD(DAY, -45, @hoje)
FROM (VALUES ('246789123', 'dd8c77962b47b17f5ffc1bfea85ac9e40273855847f0c8681af1072d733a8f55',
              '36d361902233d021f9026d6720a1b2a9'),
             ('257891234', '6df1b7c24c9bbbf7ad3acd4bf1ff03596090de6714a908f1ab9ec153f425ff73',
              '4a227ee80bf5539f09e8a52d762bfbc5')) AS c (nif, hash, salt)
JOIN Pessoa p ON p.nif = c.nif;

DECLARE @anaConcluida INT = (SELECT id FROM @reservas WHERE chave = 3);

INSERT AcessoCartao (cartao, localAcesso, quartoNumero, reservaId, registadoEm, funcionarioId) VALUES
    ('C201', 'QUARTO', 201, @anaEmCurso, DATEADD(MINUTE, 915, DATEADD(DAY, -2, @hoje)), NULL),
    ('C201', 'PISCINA', NULL, @anaEmCurso, DATEADD(MINUTE, 630, DATEADD(DAY, -1, @hoje)), NULL),
    ('C201', 'QUARTO', 201, @anaEmCurso, DATEADD(MINUTE, 780, DATEADD(DAY, -1, @hoje)), NULL),
    ('C201', 'RESTAURANTE', NULL, @anaEmCurso, DATEADD(MINUTE, 1205, DATEADD(DAY, -1, @hoje)), NULL),
    ('C201', 'QUARTO', 201, @anaEmCurso, DATEADD(MINUTE, 1360, DATEADD(DAY, -1, @hoje)), NULL),
    ('C204', 'QUARTO', 204, @anaConcluida, DATEADD(MINUTE, 935, DATEADD(DAY, -40, @hoje)), NULL),
    ('C302', 'QUARTO', 302, @carlaEmCurso, DATEADD(MINUTE, 950, DATEADD(DAY, -1, @hoje)), NULL),
    ('C302', 'RESTAURANTE', NULL, @carlaEmCurso, DATEADD(MINUTE, 1230, DATEADD(DAY, -1, @hoje)), NULL),
    ('F2', 'BAR', NULL, NULL, DATEADD(MINUTE, 550, DATEADD(DAY, -1, @hoje)), @rececao),
    ('F2', 'SALA_CONFERENCIAS', NULL, NULL, DATEADD(MINUTE, 840, DATEADD(DAY, -1, @hoje)), @rececao),
    ('R3', 'COZINHA', NULL, NULL, DATEADD(MINUTE, 450, DATEADD(DAY, -1, @hoje)), @restauracao),
    ('R3', 'RESTAURANTE', NULL, NULL, DATEADD(MINUTE, 725, DATEADD(DAY, -1, @hoje)), @restauracao),
    ('C000', 'ARMAZEM', NULL, NULL, DATEADD(MINUTE, 660, DATEADD(DAY, -1, @hoje)), NULL);

INSERT SugestaoQueixa (clienteId, tipo, descricao, registadaPor, registadaEm, reservaId)
SELECT p.id, x.tipo, x.descricao, x.registadaPor,
       COALESCE(DATEADD(HOUR, 1, res.checkoutEm), DATEADD(MINUTE, 600, DATEADD(DAY, -30, @hoje))), r.id
FROM (VALUES ('246789123', 'SUGESTAO', 'Seria bom haver mais opções vegetarianas ao jantar.', NULL, 3),
             ('224680242', 'QUEIXA', 'Muito barulho no corredor durante a noite.', @rececao, 11),
             ('292345674', 'QUEIXA', 'A água da piscina estava fria.', @rececao, 9),
             ('213579138', 'SUGESTAO', 'Podiam emprestar bicicletas aos hóspedes.', @rececao, NULL))
     AS x (nif, tipo, descricao, registadaPor, chave)
JOIN Pessoa p ON p.nif = x.nif
LEFT JOIN @reservas r ON r.chave = x.chave
LEFT JOIN Reserva res ON res.id = r.id;

INSERT AvaliacaoEstadia (reservaId, estrelas, comentario, avaliadaEm)
SELECT r.id, x.estrelas, x.comentario, DATEADD(HOUR, 3, res.checkoutEm)
FROM (VALUES ( 3, 5, 'Quarto impecável e pessoal muito simpático.'),
             ( 8, 4, 'Jantar excelente.'),
             ( 9, 3, 'A piscina estava fria, de resto correu tudo bem.'),
             (10, 5, 'A massagem foi ótima!'),
             (11, 2, 'Muito barulho à noite.'),
             (12, 5, 'Perfeito para a família.'),
             (13, 4, NULL),
             (14, 4, 'Boa vista de mar.')) AS x (chave, estrelas, comentario)
JOIN @reservas r ON r.chave = x.chave
JOIN Reserva res ON res.id = r.id;

COMMIT TRANSACTION;

PRINT 'Dados de demonstração criados.';
GO
