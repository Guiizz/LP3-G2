IF
OBJECT_ID('Categoria') IS NULL
CREATE TABLE Categoria
(
    id   INT IDENTITY PRIMARY KEY,
    nome VARCHAR(50) NOT NULL
        CONSTRAINT UQ_Categoria_nome UNIQUE
);
GO

IF OBJECT_ID('Servico') IS NULL
CREATE TABLE Servico
(
    id           INT IDENTITY PRIMARY KEY,
    designacao   VARCHAR(100)  NOT NULL,
    preco        DECIMAL(8, 2) NOT NULL CHECK (preco > 0),
    tipoCobranca VARCHAR(15)   NOT NULL
        CHECK (tipoCobranca IN ('POR_NOITE', 'POR_ESTADIA', 'POR_UTILIZACAO')),
    categoriaId  INT           NOT NULL REFERENCES Categoria (id),
    ativo        BIT           NOT NULL DEFAULT 1,
    CONSTRAINT UQ_Servico_designacao_cobranca UNIQUE (designacao, tipoCobranca)
);
GO

IF NOT EXISTS (SELECT 1 FROM Categoria)
INSERT Categoria (nome) VALUES ('Refeição'), ('Spa'), ('Quarto'), ('Lazer'), ('Outros');
GO

IF NOT EXISTS (SELECT 1 FROM Servico)
INSERT Servico (designacao, preco, tipoCobranca, categoriaId)
SELECT s.designacao, s.preco, s.tipoCobranca, c.id
FROM (VALUES ('Pequeno-almoço', 12.00, 'POR_UTILIZACAO', 'Refeição'),
             ('Almoço', 18.00, 'POR_UTILIZACAO', 'Refeição'),
             ('Jantar', 22.00, 'POR_UTILIZACAO', 'Refeição'),
             ('Massagem', 45.00, 'POR_UTILIZACAO', 'Spa'),
             ('Banho turco', 20.00, 'POR_UTILIZACAO', 'Spa'),
             ('Sauna', 15.00, 'POR_UTILIZACAO', 'Spa'),
             ('Mini-bar', 10.00, 'POR_NOITE', 'Quarto'),
             ('Limpeza extra', 15.00, 'POR_UTILIZACAO', 'Quarto'),
             ('Early check-in', 20.00, 'POR_ESTADIA', 'Quarto'),
             ('Late check-out', 20.00, 'POR_ESTADIA', 'Quarto'),
             ('Piscina', 5.00, 'POR_UTILIZACAO', 'Lazer'),
             ('Ginásio', 5.00, 'POR_UTILIZACAO', 'Lazer'),
             ('Lavandaria', 10.00, 'POR_UTILIZACAO', 'Outros'),
             ('Estacionamento', 8.00, 'POR_NOITE', 'Outros')) AS s (designacao, preco, tipoCobranca, categoria)
         JOIN Categoria c ON c.nome = s.categoria;
GO
