-- =====================================================================
-- TuFactuScan - Esquema de base de datos (MySQL 8.x)
-- Fase 1: Análisis y diseño
-- =====================================================================
-- Convenciones:
--   * Tablas y columnas en español, snake_case, nombres en singular.
--   * Claves primarias BIGINT autoincrementales (id).
--   * Importes en DECIMAL(12,2): nunca FLOAT/DOUBLE para dinero.
--   * Toda tabla de negocio cuelga de "empresa" (multiempresa): la API
--     filtra SIEMPRE por la empresa del usuario autenticado.
--   * Sincronización con el móvil: uuid generado en el dispositivo,
--     fecha de actualización y borrado lógico (eliminado).
-- =====================================================================

DROP DATABASE IF EXISTS tufactuscan;
CREATE DATABASE tufactuscan
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE tufactuscan;

-- ---------------------------------------------------------------------
-- EMPRESA: cada cliente de TuFactuScan (autónomo o pyme)
-- ---------------------------------------------------------------------
CREATE TABLE empresa (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(150)  NOT NULL,
  nif           VARCHAR(15)   NOT NULL,
  email         VARCHAR(150)  NULL,
  telefono      VARCHAR(20)   NULL,
  direccion     VARCHAR(255)  NULL,
  creado_en     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_empresa     PRIMARY KEY (id),
  CONSTRAINT uq_empresa_nif UNIQUE (nif)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- USUARIO: personas que usan la app/panel, con rol
--   ADMIN    -> gestiona la empresa, usuarios y todos los documentos
--   EMPLEADO -> escanea y consulta documentos
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  empresa_id     BIGINT        NOT NULL,
  nombre         VARCHAR(100)  NOT NULL,
  email          VARCHAR(150)  NOT NULL,
  password_hash  VARCHAR(100)  NOT NULL,           -- hash BCrypt, nunca la contraseña
  rol            ENUM('ADMIN','EMPLEADO') NOT NULL DEFAULT 'EMPLEADO',
  activo         BOOLEAN       NOT NULL DEFAULT TRUE,
  creado_en      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ultimo_acceso  DATETIME      NULL,
  CONSTRAINT pk_usuario         PRIMARY KEY (id),
  CONSTRAINT uq_usuario_email   UNIQUE (email),
  CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id)
    REFERENCES empresa (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- PROVEEDOR: emisores de las facturas/albaranes de cada empresa
-- ---------------------------------------------------------------------
CREATE TABLE proveedor (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  empresa_id  BIGINT        NOT NULL,
  nombre      VARCHAR(150)  NOT NULL,
  nif         VARCHAR(15)   NULL,
  email       VARCHAR(150)  NULL,
  telefono    VARCHAR(20)   NULL,
  direccion   VARCHAR(255)  NULL,
  creado_en   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_proveedor         PRIMARY KEY (id),
  -- Un mismo NIF no se repite dentro de una empresa (sí entre empresas)
  CONSTRAINT uq_proveedor_nif     UNIQUE (empresa_id, nif),
  CONSTRAINT fk_proveedor_empresa FOREIGN KEY (empresa_id)
    REFERENCES empresa (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- DOCUMENTO: factura o albarán escaneado
--   - Un ALBARÁN puede vincularse a la FACTURA que lo agrupa (factura_id).
--   - Los importes los propone el OCR y los confirma el usuario.
-- ---------------------------------------------------------------------
CREATE TABLE documento (
  id              BIGINT         NOT NULL AUTO_INCREMENT,
  uuid            CHAR(36)       NOT NULL,          -- generado en el móvil (sincronización)
  empresa_id      BIGINT         NOT NULL,
  proveedor_id    BIGINT         NULL,              -- puede faltar hasta que se revise
  usuario_id      BIGINT         NULL,              -- quién lo escaneó
  factura_id      BIGINT         NULL,              -- solo para albaranes
  tipo            ENUM('FACTURA','ALBARAN') NOT NULL,
  numero          VARCHAR(50)    NULL,
  fecha_emision   DATE           NULL,
  base_imponible  DECIMAL(12,2)  NULL,
  cuota_iva       DECIMAL(12,2)  NULL,
  total           DECIMAL(12,2)  NULL,
  estado          ENUM('PENDIENTE','REVISADO','PAGADO') NOT NULL DEFAULT 'PENDIENTE',
  texto_ocr       TEXT           NULL,              -- texto bruto reconocido (auditoría)
  observaciones   VARCHAR(500)   NULL,
  creado_en       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  eliminado       BOOLEAN        NOT NULL DEFAULT FALSE,   -- borrado lógico para sincronizar
  CONSTRAINT pk_documento           PRIMARY KEY (id),
  CONSTRAINT uq_documento_uuid      UNIQUE (uuid),
  CONSTRAINT fk_documento_empresa   FOREIGN KEY (empresa_id)
    REFERENCES empresa (id) ON DELETE CASCADE,
  CONSTRAINT fk_documento_proveedor FOREIGN KEY (proveedor_id)
    REFERENCES proveedor (id) ON DELETE SET NULL,
  CONSTRAINT fk_documento_usuario   FOREIGN KEY (usuario_id)
    REFERENCES usuario (id) ON DELETE SET NULL,
  -- Sin acción ON DELETE: MySQL no permite acciones referenciales en
  -- columnas usadas en un CHECK. La API desvincula antes de borrar.
  CONSTRAINT fk_documento_factura   FOREIGN KEY (factura_id)
    REFERENCES documento (id),
  CONSTRAINT ck_documento_factura   CHECK (tipo = 'ALBARAN' OR factura_id IS NULL),
  CONSTRAINT ck_documento_importes  CHECK (
    (base_imponible IS NULL OR base_imponible >= 0) AND
    (cuota_iva      IS NULL OR cuota_iva      >= 0) AND
    (total          IS NULL OR total          >= 0)
  )
) ENGINE = InnoDB;

-- Índices para los filtros del panel web (proveedor, fechas, estado)
CREATE INDEX ix_documento_empresa_fecha     ON documento (empresa_id, fecha_emision);
CREATE INDEX ix_documento_empresa_proveedor ON documento (empresa_id, proveedor_id);
CREATE INDEX ix_documento_empresa_estado    ON documento (empresa_id, estado);
CREATE INDEX ix_documento_empresa_actualiz  ON documento (empresa_id, actualizado_en);  -- sincronización

-- ---------------------------------------------------------------------
-- LINEA_DOCUMENTO: detalle opcional de conceptos (si el OCR los detecta)
-- ---------------------------------------------------------------------
CREATE TABLE linea_documento (
  id               BIGINT         NOT NULL AUTO_INCREMENT,
  documento_id     BIGINT         NOT NULL,
  orden            SMALLINT       NOT NULL DEFAULT 1,
  descripcion      VARCHAR(255)   NOT NULL,
  cantidad         DECIMAL(10,3)  NOT NULL DEFAULT 1,
  precio_unitario  DECIMAL(12,4)  NULL,
  iva_porcentaje   DECIMAL(5,2)   NULL,              -- 21.00, 10.00, 4.00, 0.00
  importe          DECIMAL(12,2)  NULL,
  CONSTRAINT pk_linea_documento   PRIMARY KEY (id),
  CONSTRAINT fk_linea_documento   FOREIGN KEY (documento_id)
    REFERENCES documento (id) ON DELETE CASCADE,
  CONSTRAINT ck_linea_cantidad    CHECK (cantidad > 0),
  CONSTRAINT ck_linea_iva         CHECK (iva_porcentaje IS NULL OR iva_porcentaje BETWEEN 0 AND 100)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- IMAGEN_DOCUMENTO: páginas escaneadas (el fichero se guarda en disco;
-- en la base de datos solo la ruta y sus metadatos)
-- ---------------------------------------------------------------------
CREATE TABLE imagen_documento (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  documento_id  BIGINT        NOT NULL,
  pagina        SMALLINT      NOT NULL DEFAULT 1,
  ruta_fichero  VARCHAR(255)  NOT NULL,
  tipo_mime     VARCHAR(50)   NOT NULL,              -- image/jpeg, application/pdf
  tamano_bytes  BIGINT        NOT NULL,
  creado_en     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_imagen_documento PRIMARY KEY (id),
  CONSTRAINT uq_imagen_pagina    UNIQUE (documento_id, pagina),
  CONSTRAINT fk_imagen_documento FOREIGN KEY (documento_id)
    REFERENCES documento (id) ON DELETE CASCADE,
  CONSTRAINT ck_imagen_tamano    CHECK (tamano_bytes > 0)
) ENGINE = InnoDB;
