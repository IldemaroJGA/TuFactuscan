# TuFactuScan · Backend

API REST y, más adelante, panel web de TuFactuScan, hechos con Spring Boot 4.1 y Java 21 o superior.

## Estructura modular

Cada paquete agrupa todo lo de una parte del negocio. Cuando se añadan los controladores y servicios, irán dentro de su módulo.

```
src/main/java/com/tufactuscan/
├── TuFactuScanApplication.java   Punto de entrada
├── comun/
│   ├── config/                   Cifrado de contraseñas (BCrypt)
│   ├── seguridad/                Spring Security, JWT, usuario actual, respuestas 401/403
│   └── error/                    Formato común de errores y manejador global
├── auth/                         Login (POST /api/auth/login) y GET /api/auth/me
├── empresa/                      Empresa + repositorio
├── usuario/                      Usuario, Rol, repositorio, servicio y GET /api/usuarios (ADMIN)
├── proveedor/                    Proveedor + repositorio
├── documento/                    Documento, líneas, imágenes, tipos, estados + repositorio
├── inicio/                       Usuarios de prueba creados al arrancar
└── estado/                       GET /api/estado (diagnóstico)
```

## Ejecutar en desarrollo

1. Arranca MySQL desde la raíz del proyecto: `docker compose up -d`.
2. Abre la carpeta `backend` en IntelliJ IDEA: *File › Open*, elige `backend/pom.xml` y luego *Open as Project*.
3. Espera a que Maven descargue las dependencias (barra inferior).
4. Ejecuta `TuFactuScanApplication` con el botón ▶.
5. Abre `http://localhost:8080/api/estado`.

## Usuarios de prueba

Se crean solos al arrancar. Son solo para desarrollo.

| Email | Contraseña | Rol | Empresa |
| --- | --- | --- | --- |
| admin@tallergotera.es | Admin1234! | ADMIN | Taller Mecánico Gotera S.L. |
| empleado@tallergotera.es | Empleado1234! | EMPLEADO | Taller Mecánico Gotera S.L. |
| lahuerta@email.es | Huerta1234! | ADMIN | Frutería La Huerta |

## Fase 2: bloques

| Bloque | Contenido | Estado |
| --- | --- | --- |
| 1 | Esqueleto, conexión a MySQL, entidades JPA, repositorios | Validado |
| 2 | Seguridad: login, JWT, roles, aislamiento por empresa | En validación |
| 3 | Endpoints de proveedores, documentos y vinculación | Pendiente |
| 4 | Subida de imágenes y PDF, sincronización, Swagger, Docker | Pendiente |

## Probar la seguridad (bloque 2)

Con el backend arrancado, desde la carpeta `backend`:

```powershell
powershell -ExecutionPolicy Bypass -File pruebas\prueba-seguridad.ps1
```

Deben salir 11 comprobaciones en verde.

## Variables de entorno

| Variable | Por defecto | Uso |
| --- | --- | --- |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | MySQL local de Docker | Conexión a la base de datos |
| `JWT_SECRETO` | Clave de desarrollo | Firma de los tokens. **Obligatorio cambiarla en producción** (mínimo 32 caracteres) |
| `JWT_EXPIRACION_HORAS` | 8 | Duración de la sesión |
| `DATOS_INICIALES` | true | Crear los usuarios de prueba al arrancar |
