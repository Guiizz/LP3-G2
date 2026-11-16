MERGE Stock AS s
USING (
    SELECT m.id AS materialId,
           ISNULL((SELECT SUM(l.unidades) FROM LinhaEntrega l JOIN Entrega e ON e.id = l.entregaId
                   WHERE l.materialId = m.id AND e.estado = 'ACEITE'), 0)
         - ISNULL((SELECT SUM(ms.quantidade) FROM MovimentoStock ms WHERE ms.materialId = m.id), 0) AS quantidade
    FROM Material m
) AS t
ON s.materialId = t.materialId
WHEN MATCHED AND t.quantidade >= 0 AND s.quantidade <> t.quantidade THEN
    UPDATE SET quantidade = t.quantidade
WHEN NOT MATCHED BY TARGET AND t.quantidade > 0 THEN
    INSERT (materialId, quantidade) VALUES (t.materialId, t.quantidade);
GO
