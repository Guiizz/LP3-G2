IF COL_LENGTH('Servico', 'tipoCobranca') IS NOT NULL
BEGIN
    EXEC('UPDATE s SET designacao = LEFT(s.designacao, 83) + '' ('' + CASE s.tipoCobranca
              WHEN ''POR_NOITE'' THEN ''por noite''
              WHEN ''POR_ESTADIA'' THEN ''por estadia''
              ELSE ''por utilização'' END + '')''
          FROM Servico s
          WHERE EXISTS (SELECT 1 FROM Servico o WHERE LOWER(o.designacao) = LOWER(s.designacao) AND o.id < s.id)');

    IF OBJECT_ID('UQ_Servico_designacao_cobranca', 'UQ') IS NOT NULL
        ALTER TABLE Servico DROP CONSTRAINT UQ_Servico_designacao_cobranca;

    DECLARE @check SYSNAME = (SELECT cc.name
                              FROM sys.check_constraints cc
                                       JOIN sys.columns c ON c.object_id = cc.parent_object_id
                                  AND c.column_id = cc.parent_column_id
                              WHERE cc.parent_object_id = OBJECT_ID('Servico') AND c.name = 'tipoCobranca');
    IF @check IS NOT NULL
    BEGIN
        DECLARE @sql NVARCHAR(200) = N'ALTER TABLE Servico DROP CONSTRAINT ' + QUOTENAME(@check);
        EXEC(@sql);
    END

    EXEC('ALTER TABLE Servico DROP COLUMN tipoCobranca');
END
GO

IF OBJECT_ID('UQ_Servico_designacao', 'UQ') IS NULL
ALTER TABLE Servico ADD CONSTRAINT UQ_Servico_designacao UNIQUE (designacao);
GO
