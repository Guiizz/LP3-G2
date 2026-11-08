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
    categoriaId  INT           NOT NULL REFERENCES Categoria (id),
    ativo        BIT           NOT NULL DEFAULT 1,
    CONSTRAINT UQ_Servico_designacao UNIQUE (designacao)
);
GO

IF NOT EXISTS (SELECT 1 FROM Categoria)
INSERT Categoria (nome) VALUES ('Refeição'), ('Spa'), ('Quarto'), ('Lazer'), ('Outros');
GO

IF NOT EXISTS (SELECT 1 FROM Servico)
INSERT Servico (designacao, preco, categoriaId)
SELECT s.designacao, s.preco, c.id
FROM (VALUES ('Pequeno-almoço', 12.00, 'Refeição'),
             ('Almoço', 18.00, 'Refeição'),
             ('Jantar', 22.00, 'Refeição'),
             ('Massagem', 45.00, 'Spa'),
             ('Banho turco', 20.00, 'Spa'),
             ('Sauna', 15.00, 'Spa'),
             ('Mini-bar', 10.00, 'Quarto'),
             ('Limpeza extra', 15.00, 'Quarto'),
             ('Early check-in', 20.00, 'Quarto'),
             ('Late check-out', 20.00, 'Quarto'),
             ('Piscina', 5.00, 'Lazer'),
             ('Ginásio', 5.00, 'Lazer'),
             ('Lavandaria', 10.00, 'Outros'),
             ('Estacionamento', 8.00, 'Outros')) AS s (designacao, preco, categoria)
         JOIN Categoria c ON c.nome = s.categoria;
GO
