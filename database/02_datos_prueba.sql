-- =====================================================================
-- TuFactuScan - Datos de prueba
-- Los usuarios NO se insertan aquí: sus contraseñas deben guardarse con
-- hash BCrypt, así que los crea el backend al arrancar (fase 2).
-- Por eso los documentos de ejemplo tienen usuario_id = NULL.
-- =====================================================================
USE tufactuscan;

INSERT INTO empresa (id, nombre, nif, email, telefono, direccion) VALUES
  (1, 'Taller Mecánico Gotera S.L.', 'B12345678', 'admin@tallergotera.es', '600111222', 'C/ Mayor 10, Madrid'),
  (2, 'Frutería La Huerta',           '12345678Z', 'lahuerta@email.es',    '600333444', 'Av. del Mercado 5, Getafe');

INSERT INTO proveedor (id, empresa_id, nombre, nif, email, telefono) VALUES
  (1, 1, 'Recambios Norte S.A.',        'A11111111', 'pedidos@recambiosnorte.es', '911000001'),
  (2, 1, 'Lubricantes Ibéricos S.L.',   'B22222222', 'ventas@lubricantes.es',     '911000002'),
  (3, 1, 'Neumáticos Express S.L.',     'B33333333', NULL,                        '911000003'),
  (4, 2, 'Mercamadrid Distribución',    'B44444444', 'info@mercadist.es',         '911000004');

-- Factura de Recambios Norte que agrupa dos albaranes
INSERT INTO documento (id, uuid, empresa_id, proveedor_id, tipo, numero, fecha_emision,
                       base_imponible, cuota_iva, total, estado) VALUES
  (1, '7f1c2a10-0000-4000-8000-000000000001', 1, 1, 'FACTURA', 'F-2026-0153', '2026-10-31', 500.00, 105.00, 605.00, 'REVISADO');

INSERT INTO documento (id, uuid, empresa_id, proveedor_id, factura_id, tipo, numero, fecha_emision,
                       base_imponible, cuota_iva, total, estado) VALUES
  (2, '7f1c2a10-0000-4000-8000-000000000002', 1, 1, 1, 'ALBARAN', 'A-8841', '2026-10-10', 300.00, 63.00, 363.00, 'REVISADO'),
  (3, '7f1c2a10-0000-4000-8000-000000000003', 1, 1, 1, 'ALBARAN', 'A-8902', '2026-10-24', 200.00, 42.00, 242.00, 'REVISADO');

-- Otros documentos
INSERT INTO documento (id, uuid, empresa_id, proveedor_id, tipo, numero, fecha_emision,
                       base_imponible, cuota_iva, total, estado) VALUES
  (4, '7f1c2a10-0000-4000-8000-000000000004', 1, 2, 'FACTURA', '2026/774',   '2026-10-15', 120.50, 25.31, 145.81, 'PAGADO'),
  (5, '7f1c2a10-0000-4000-8000-000000000005', 1, 3, 'FACTURA', 'NE-00321',   '2026-11-02', 640.00, 134.40, 774.40, 'PENDIENTE'),
  (6, '7f1c2a10-0000-4000-8000-000000000006', 2, 4, 'ALBARAN', 'MD-55120',   '2026-11-01', 85.00,  3.40,  88.40,  'PENDIENTE');

INSERT INTO linea_documento (documento_id, orden, descripcion, cantidad, precio_unitario, iva_porcentaje, importe) VALUES
  (2, 1, 'Pastillas de freno delanteras', 4, 45.0000, 21.00, 180.00),
  (2, 2, 'Discos de freno',               2, 60.0000, 21.00, 120.00),
  (3, 1, 'Filtro de aceite',             10, 20.0000, 21.00, 200.00),
  (6, 1, 'Tomate rama (kg)',             50,  1.7000,  4.00,  85.00);

INSERT INTO imagen_documento (documento_id, pagina, ruta_fichero, tipo_mime, tamano_bytes) VALUES
  (1, 1, 'empresa-1/2026/10/7f1c2a10-0000-4000-8000-000000000001-p1.jpg', 'image/jpeg', 245760),
  (2, 1, 'empresa-1/2026/10/7f1c2a10-0000-4000-8000-000000000002-p1.jpg', 'image/jpeg', 198400);
