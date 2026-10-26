-- US10: dados iniciais dos quartos e das tarifas (corre depois do 01)
-- Os preços e a distribuição dos quartos são os acordados pela equipa.

IF NOT EXISTS (SELECT 1 FROM Tarifa)
INSERT Tarifa (tipo, vista, valorNoite) VALUES
    ('INDIVIDUAL', 'NENHUMA',  50.00), ('INDIVIDUAL', 'MAR',  65.00), ('INDIVIDUAL', 'MONTANHA',  60.00),
    ('DUPLO',      'NENHUMA',  80.00), ('DUPLO',      'MAR', 100.00), ('DUPLO',      'MONTANHA',  90.00),
    ('FAMILIA',    'NENHUMA', 120.00), ('FAMILIA',    'MAR', 150.00), ('FAMILIA',    'MONTANHA', 135.00);
GO

-- 20 quartos: 7 individuais, 10 duplos e 3 de família, nos pisos 2 e 3
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