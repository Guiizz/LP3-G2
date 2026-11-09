IF EXISTS (SELECT 1 FROM sys.columns
           WHERE object_id = OBJECT_ID('SugestaoQueixa') AND name = 'registadaPor' AND is_nullable = 0)
ALTER TABLE SugestaoQueixa ALTER COLUMN registadaPor INT NULL;
GO
