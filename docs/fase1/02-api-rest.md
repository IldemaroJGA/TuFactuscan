# Fase 1 · Diseño de la API REST

La API tiene 28 endpoints bajo `/api`, agrupados en 8 recursos. Todos, salvo el login, exigen el token JWT en la cabecera `Authorization: Bearer <token>`, y todos trabajan solo con los datos de la empresa del usuario autenticado.

## Convenciones

- Formato JSON en peticiones y respuestas; fechas en ISO 8601 (`2026-11-02`, `2026-11-02T18:30:00Z`); importes como número con 2 decimales.
- Listados paginados con `?page=0&size=20&sort=fechaEmision,desc`. La respuesta incluye `content`, `totalElements` y `totalPages`.
- Errores con un cuerpo común: `{ "status": 400, "error": "VALIDACION", "mensaje": "El total no puede ser negativo", "campos": { "total": "debe ser >= 0" } }`.

| Código | Cuándo |
| --- | --- |
| 200 / 201 / 204 | Correcto / creado / sin contenido (borrados) |
| 400 | Datos no válidos |
| 401 | Sin token o token caducado |
| 403 | El rol no permite la operación |
| 404 | No existe o es de otra empresa (no se revela cuál de las dos) |
| 409 | Conflicto: NIF duplicado, vínculo no permitido |

## Endpoints

Rol: **Todos** = administrador y empleado; **Admin** = solo administrador; **Propio** = el empleado solo sobre lo que creó él.

### Autenticación

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | Público | Recibe `email` y `password`; devuelve `token`, caducidad y datos del usuario |
| GET | `/api/auth/me` | Todos | Datos del usuario y de su empresa |

### Usuarios

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| GET | `/api/usuarios` | Admin | Lista los usuarios de la empresa |
| POST | `/api/usuarios` | Admin | Crea un empleado con contraseña inicial |
| PUT | `/api/usuarios/{id}` | Admin | Edita nombre, rol o estado activo |
| PUT | `/api/usuarios/me/password` | Todos | Cambia la contraseña propia |
| DELETE | `/api/usuarios/{id}` | Admin | Desactiva el usuario (no lo borra) |

### Proveedores

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| GET | `/api/proveedores?q=` | Todos | Lista y busca por nombre o NIF |
| GET | `/api/proveedores/{id}` | Todos | Detalle |
| GET | `/api/proveedores/nif/{nif}` | Todos | Busca por NIF (la app lo usa tras el OCR para autoasignar proveedor) |
| POST | `/api/proveedores` | Todos | Crea un proveedor |
| PUT | `/api/proveedores/{id}` | Todos | Edita |
| DELETE | `/api/proveedores/{id}` | Admin | Borra (sus documentos quedan sin proveedor) |

### Documentos

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| GET | `/api/documentos` | Todos | Lista con filtros: `tipo`, `proveedorId`, `estado`, `desde`, `hasta`, `importeMin`, `importeMax`, `q` (número) |
| GET | `/api/documentos/{id}` | Todos | Detalle con líneas, imágenes y albaranes vinculados |
| POST | `/api/documentos` | Todos | Crea un documento (el `uuid` lo genera el móvil) |
| PUT | `/api/documentos/{id}` | Propio / Admin | Edita los datos |
| PATCH | `/api/documentos/{id}/estado` | Admin | Cambia el estado: `{ "estado": "PAGADO" }` |
| DELETE | `/api/documentos/{id}` | Propio / Admin | Borrado lógico |

### Vinculación albarán-factura

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| PUT | `/api/documentos/{facturaId}/albaranes` | Admin | Vincula albaranes: `{ "albaranIds": [2, 3] }`. Devuelve si la suma cuadra con el total de la factura |
| DELETE | `/api/documentos/{facturaId}/albaranes/{albaranId}` | Admin | Desvincula un albarán |

### Imágenes

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| POST | `/api/documentos/{id}/imagenes` | Propio / Admin | Sube una imagen escaneada o un PDF completo (`multipart/form-data`, JPEG, PNG o PDF, máx. 10 MB). Un PDF de varias páginas se guarda como un solo fichero |
| GET | `/api/imagenes/{id}` | Todos | Descarga la imagen |
| DELETE | `/api/imagenes/{id}` | Propio / Admin | Borra una página |

### Sincronización (app móvil)

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| POST | `/api/sync/subir` | Todos | Envía en lote los documentos creados o modificados sin conexión. El servidor identifica cada uno por su `uuid`: si existe lo actualiza, si no lo crea |
| GET | `/api/sync/cambios?desde=<fecha>` | Todos | Devuelve documentos y proveedores modificados desde esa fecha (incluidos los eliminados) |

### Informes y exportación

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| GET | `/api/informes/gasto?agrupar=proveedor\|mes&desde=&hasta=` | Admin | Gasto total agrupado |
| GET | `/api/exportar/documentos.csv` | Admin | Mismos filtros que el listado; descarga CSV (separador `;`, UTF-8 con BOM para que Excel muestre bien las tildes) |

## Ejemplo: crear un documento desde la app

```http
POST /api/documentos
Authorization: Bearer eyJhbGciOi...
Content-Type: application/json

{
  "uuid": "3b0c8e4a-9f1d-4c55-a7d2-1e6f0b9c2a77",
  "tipo": "FACTURA",
  "proveedorId": 3,
  "numero": "NE-00321",
  "fechaEmision": "2026-11-02",
  "baseImponible": 640.00,
  "cuotaIva": 134.40,
  "total": 774.40,
  "textoOcr": "NEUMATICOS EXPRESS S.L. CIF B33333333 ..."
}
```

Respuesta `201 Created` con el documento completo, su `id` y `estado: "PENDIENTE"`.

## Cómo encaja la sincronización

1. La app guarda cada escaneo en Room con un `uuid` y la marca «pendiente de subir».
2. Al tener conexión, llama a `POST /api/sync/subir` con todo lo pendiente. Como el `uuid` es único, repetir el envío no duplica documentos.
3. Después llama a `GET /api/sync/cambios?desde=<última sincronización>` y aplica en Room lo que han cambiado otros usuarios o el panel web.
4. Si un documento se cambió en los dos lados, gana la fecha de actualización más reciente.
