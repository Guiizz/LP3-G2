-- US75: materiais consumíveis e configuração dos materiais de cada tipo de quarto (corre depois do 21)
-- Pode correr-se várias vezes: cada alteração só é feita se ainda não existir.

SET QUOTED_IDENTIFIER ON;
GO

-- 1) Cada material é consumível (champô, gel de banho) ou não consumível (televisão, secador, cofre).
--    Os materiais que já existem ficam consumíveis até o gestor os mudar.
IF COL_LENGTH('Material', 'consumivel') IS NULL
ALTER TABLE Material ADD consumivel BIT NOT NULL
    CONSTRAINT DF_Material_consumivel DEFAULT 1;
GO

-- 2) Configuração de cada tipo de quarto: que materiais leva por defeito e quantas unidades de cada.
--    Um material aparece no máximo uma vez em cada tipo de quarto.
IF OBJECT_ID('ConfiguracaoQuarto') IS NULL
CREATE TABLE ConfiguracaoQuarto (
    tipoQuarto VARCHAR(10) NOT NULL CHECK (tipoQuarto IN ('INDIVIDUAL', 'DUPLO', 'FAMILIA')),
    materialId INT NOT NULL REFERENCES Material(id),
    quantidade INT NOT NULL CHECK (quantidade > 0),
    PRIMARY KEY (tipoQuarto, materialId)
);
GO
