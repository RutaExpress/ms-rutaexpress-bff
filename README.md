# ms-rutaexpress-bff

**Backend For Frontend** del caso RutaExpress: es la única puerta del front al backend. Valida el **JWT de Microsoft Entra ID**, revisa el **rol** y reenvía la petición al microservicio que corresponde.

> Arquitectura en capas (refactor desde hexagonal, rama `refactor/ArquitecturaCapas`).

## Tecnologías
Java 21 · Spring Boot 4.1 · Spring Security (OAuth2 Resource Server) · RestClient · JUnit 5 y Mockito

## Arquitectura en capas
Una petición pasa siempre por el mismo camino: **controller → client**. No tiene service: no tiene reglas de negocio propias, solo seguridad y reenvío.

```
src/main/java/com/rutaexpress/ms_rutaexpress_bff/
├── controller/   Rutas HTTP y rol permitido en cada una (@PreAuthorize)
├── client/       Llamadas REST a shipments, catalog, report y audit (RestClient)
├── dto/          Respuesta de /me y respuesta de los servicios (se reenvía tal cual)
├── exception/    Manejador global (503 si un servicio no responde)
└── config/       Seguridad: JWT de Entra ID y roles del claim «roles»
```

## Endpoints (puerto 8082)
| Método | Ruta | Roles |
|---|---|---|
| GET | `/api/v1/public/status` | público |
| GET | `/api/v1/me` | cualquier usuario con sesión |
| GET | `/api/v1/shipments` | CLIENTE (solo los suyos), OPERADOR, ADMIN |
| GET | `/api/v1/shipments/{id}` | CLIENTE (si es suyo), OPERADOR, ADMIN |
| POST | `/api/v1/shipments` | CLIENTE, OPERADOR |
| PUT | `/api/v1/shipments/{id}/status` | OPERADOR, ADMIN |
| GET | `/api/v1/catalog/services` | ADMIN, OPERADOR |
| POST · PUT | `/api/v1/catalog/services` · `/{id}` | ADMIN |
| GET | `/api/v1/report/kpis?range=` · `/top-services?range=` | ADMIN |
| GET | `/api/v1/audit/shipments/{id}` | ADMIN, AUDITOR |

Sin token → 401. Rol sin permiso → 403. Servicio caído → 503.

## Tests
```
cd ms-rutaexpress-bff
mvnw.cmd test
```
