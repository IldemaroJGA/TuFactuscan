-- =====================================================================
-- Consultas para validar el esquema (ejecutar tras cargar 01 y 02)
-- =====================================================================
USE tufactuscan;

-- 1. Tablas creadas (deben salir 6)
SHOW TABLES;

-- 2. Albaranes vinculados a cada factura y si los importes cuadran
SELECT f.numero                 AS factura,
       f.total                  AS total_factura,
       COUNT(a.id)              AS num_albaranes,
       SUM(a.total)             AS suma_albaranes,
       f.total = SUM(a.total)   AS cuadra
FROM documento f
JOIN documento a ON a.factura_id = f.id
WHERE f.tipo = 'FACTURA'
GROUP BY f.id, f.numero, f.total;

-- 3. Gasto por proveedor y mes (lo que mostrará el panel web)
SELECT p.nombre                              AS proveedor,
       DATE_FORMAT(d.fecha_emision, '%Y-%m') AS mes,
       SUM(d.total)                          AS gasto
FROM documento d
JOIN proveedor p ON p.id = d.proveedor_id
WHERE d.empresa_id = 1 AND d.tipo = 'FACTURA' AND d.eliminado = FALSE
GROUP BY p.nombre, mes
ORDER BY mes, gasto DESC;

-- 4. Las restricciones funcionan: estas sentencias DEBEN fallar
-- (descoméntalas de una en una para comprobarlo)
-- Una factura no puede vincularse a otra factura (ck_documento_factura):
-- UPDATE documento SET factura_id = 1 WHERE id = 4;
-- Importes negativos (ck_documento_importes):
-- UPDATE documento SET total = -5 WHERE id = 5;
-- NIF de proveedor repetido en la misma empresa (uq_proveedor_nif):
-- INSERT INTO proveedor (empresa_id, nombre, nif) VALUES (1, 'Duplicado', 'A11111111');
