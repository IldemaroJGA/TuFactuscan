# TuFactuScan

Aplicación multiplataforma para digitalizar facturas y albaranes en pequeñas empresas. Proyecto final del ciclo de Desarrollo de Aplicaciones Multiplataforma (DAM).

## Estructura

```
tufactuscan/
├── android/           App Android (Kotlin + Jetpack Compose)        → fase 3
├── backend/           API REST + panel web (Spring Boot)            → fases 2 y 4
├── database/          Scripts SQL de MySQL
├── docs/              Documentación por fases
└── docker-compose.yml Servicios del servidor
```

## Planificación

| Fase | Fechas | Estado |
| --- | --- | --- |
| 1. Análisis y diseño | 2–4 nov 2026 | En validación |
| 2. Backend (API REST) | 5–11 nov | Pendiente |
| 3. App Android | 12–19 nov | Pendiente |
| 4. Panel web | 20–24 nov | Pendiente |
| 5. Pruebas y memoria | 25–28 nov | Pendiente |
| Margen y entrega | 29–30 nov | — |

## Arrancar la base de datos

Requisitos: Docker Desktop en marcha.

```bash
docker compose up -d
docker compose ps            # espera a que mysql aparezca como "healthy"
```

Conexión: `localhost:3306`, usuario `root`, contraseña `root_dev_2026`, base de datos `tufactuscan`.

Para comprobar el esquema, ejecuta las consultas de validación:

```bash
docker exec -i tufactuscan-mysql mysql -uroot -proot_dev_2026 < database/03_consultas_validacion.sql
```

Para empezar de cero (borra todos los datos y vuelve a cargar los scripts):

```bash
docker compose down -v && docker compose up -d
```
