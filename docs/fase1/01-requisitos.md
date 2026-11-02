# Fase 1 · Requisitos y casos de uso

TuFactuScan tiene dos actores, 15 requisitos funcionales y 8 no funcionales. Cada requisito funcional está ligado al objetivo de la memoria que cumple (OF = objetivo funcional, OT = objetivo técnico).

## Actores

| Actor | Descripción | Accede desde |
| --- | --- | --- |
| Administrador | Responsable de la empresa. Gestiona usuarios, proveedores y todos los documentos; consulta informes y exporta. | App Android y panel web |
| Empleado | Escanea documentos, revisa los datos extraídos y consulta los documentos de su empresa. | App Android (y panel web en modo consulta) |

## Requisitos funcionales

| Id | Requisito | Actor | Objetivo | Prioridad |
| --- | --- | --- | --- | --- |
| RF01 | Iniciar y cerrar sesión con email y contraseña | Ambos | OF7 | Imprescindible |
| RF02 | Gestionar usuarios de la empresa (alta, edición, desactivación, rol) | Admin | OF7 | Imprescindible |
| RF03 | Gestionar proveedores (alta, edición, baja, búsqueda) | Ambos | OF4 | Imprescindible |
| RF04 | Escanear un documento con la cámara: detección de bordes, recorte y corrección de perspectiva | Ambos | OF1 | Imprescindible |
| RF15 | Subir facturas y albaranes en PDF desde la tablet, el móvil o el panel web. Si el PDF tiene texto, se lee directamente; si es escaneado, se aplica OCR a cada página | Ambos | OF1, OF2 | Imprescindible |
| RF05 | Extraer por OCR proveedor, NIF/CIF, número, fecha, base imponible, IVA y total | Ambos | OF2 | Imprescindible |
| RF06 | Revisar y corregir los datos extraídos antes de guardar | Ambos | OF3 | Imprescindible |
| RF07 | Clasificar el documento como factura o albarán y asignarle proveedor | Ambos | OF4 | Imprescindible |
| RF08 | Vincular uno o varios albaranes a su factura | Admin | OF4 | Imprescindible |
| RF09 | Buscar y filtrar documentos por proveedor, fechas, importe, tipo y estado | Ambos | OF5 | Imprescindible |
| RF10 | Cambiar el estado de un documento (pendiente, revisado, pagado) | Admin | OF5 | Imprescindible |
| RF11 | Trabajar sin conexión en el móvil y sincronizar al recuperar la red | Ambos | OF6 | Imprescindible |
| RF12 | Ver el resumen de gasto por proveedor y por mes | Admin | OF8 | Deseable |
| RF13 | Exportar los documentos filtrados a CSV | Admin | OF9 | Imprescindible |
| RF14 | Descargar las imágenes de un documento y exportar a Excel | Admin | OF9 | Deseable |

Los requisitos «Deseable» son los primeros que se recortan si falta tiempo (ver planificación).

## Requisitos no funcionales

| Id | Requisito | Objetivo |
| --- | --- | --- |
| RNF01 | Contraseñas guardadas con hash BCrypt; nunca en texto plano | OT4 |
| RNF02 | Comunicación cliente-servidor por HTTPS y autenticación con token JWT con caducidad | OT2, OT4 |
| RNF03 | Aislamiento por empresa: un usuario nunca ve datos de otra empresa | OT4 |
| RNF04 | La app funciona en Android 8.0 (API 26) o superior | OT3 |
| RNF05 | El OCR se ejecuta en el dispositivo: las imágenes no se envían a servicios externos | OT4 |
| RNF06 | Todo el sistema servidor se levanta con un único `docker compose up` | OT6 |
| RNF07 | API documentada con OpenAPI/Swagger | OT2 |
| RNF08 | Interfaz en español, con mensajes de error comprensibles para el usuario | — |

## Casos de uso

| Id | Caso de uso | Actor | Requisitos | Flujo principal |
| --- | --- | --- | --- | --- |
| CU01 | Iniciar sesión | Ambos | RF01 | Introduce email y contraseña → la API devuelve un token → se abre la pantalla principal |
| CU02 | Escanear documento | Ambos | RF04–RF07 | Pulsa «Escanear» → encuadra el papel → la app recorta y aplica OCR → muestra el formulario con los campos propuestos → el usuario corrige, elige tipo y proveedor → guarda |
| CU11 | Subir PDF | Ambos | RF15, RF05–RF07 | Pulsa «Subir PDF» → elige el fichero → la app extrae el texto (o aplica OCR si es una imagen escaneada) → muestra el mismo formulario de revisión que al escanear → guarda |
| CU03 | Sincronizar | Sistema | RF11 | Al recuperar conexión, la app envía los documentos pendientes y descarga los cambios del servidor |
| CU04 | Consultar documentos | Ambos | RF09 | Abre el listado → aplica filtros → abre el detalle con la imagen |
| CU05 | Vincular albaranes a factura | Admin | RF08 | Abre una factura → «Vincular albaranes» → selecciona albaranes del mismo proveedor → la app indica si los importes cuadran |
| CU06 | Cambiar estado | Admin | RF10 | En el detalle, marca el documento como revisado o pagado |
| CU07 | Gestionar proveedores | Ambos | RF03 | Alta, edición o baja desde el listado de proveedores |
| CU08 | Gestionar usuarios | Admin | RF02 | Alta de un empleado con email y contraseña inicial; desactivarlo cuando deja la empresa |
| CU09 | Ver resumen de gasto | Admin | RF12 | Elige un periodo → ve el gasto por proveedor y por mes |
| CU10 | Exportar para la gestoría | Admin | RF13, RF14 | Aplica filtros → «Exportar CSV» → descarga el fichero |

## Reglas de negocio

1. Solo un albarán puede vincularse a una factura, y ambos deben ser del mismo proveedor y la misma empresa.
2. Un documento eliminado no se borra físicamente: se marca como eliminado para que el resto de dispositivos se enteren al sincronizar.
3. Si dos dispositivos modifican el mismo documento, se queda la versión con la fecha de actualización más reciente.
4. Un empleado solo puede editar o borrar los documentos que ha escaneado él. Cambiar el estado y vincular albaranes es exclusivo del administrador.
