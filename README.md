# ms-rutaexpress-bff

**Backend For Frontend** de RutaExpress. Es la única puerta del front hacia el backend: valida el **JWT de Microsoft Entra ID** (segunda validación, después del API Gateway), aplica la **autorización por rol** y reenvía a los microservicios de dominio (hoy: `ms-rutaexpress-shipments`).

## Tecnologías
Java 21 · Spring Boot 4.1.1 · Spring Security (OAuth2 Resource Server) · RestClient · Actuator · Docker

## Arquitectura
```
Front Angular (MSAL) ──JWT──► API Gateway (CORS + JWT Authorizer) ──► BFF :8082 ──► shipments :8081 ──► RabbitMQ ──► notify
```

## Estructura
```
ms-rutaexpress-bff/src/main/java/com/rutaexpress/ms_rutaexpress_bff/
├── config/SecurityConfig          JWT (firma, expiración, issuer, audiencia), roles, CORS local
├── adapter/in/rest/
│   ├── PublicController           GET /api/v1/public/status
│   ├── MeController               GET /api/v1/me
│   ├── ShipmentController         /api/v1/shipments/** (roles + reenvío)
│   ├── UserIdentity               oid del token, rol CLIENTE
│   ├── dto/MeResponse
│   └── exception/                 GlobalExceptionHandler (404, 503)
├── port/out/                      ShipmentsPort, DownstreamResponse, ShipmentView
└── adapter/out/http/              ShipmentsRestClient (RestClient → shipments)
```

## Seguridad
| Validación | Dónde |
|---|---|
| Firma (JWKS de Entra ID), expiración, issuer `…/v2.0` | API Gateway **y** BFF |
| Audiencia = clientId de la app RutaExpress | API Gateway **y** BFF (`JWT_AUDIENCE`) |
| Rol (claim `roles`) | BFF con `@PreAuthorize`. **Sin rol no hay acceso** (no hay rol por defecto) |
| Identidad hacia shipments | Header `X-User-Id` = claim `oid` del token (el front nunca lo envía) |

## Endpoints (`/api/v1`)
| Verbo | Ruta | Roles | Respuesta |
|---|---|---|---|
| GET | `/public/status` | público | 200 |
| GET | `/me` | autenticado | 200 `{ userId, name, email, roles }` |
| GET | `/shipments?status=&from=&to=` | CLIENTE (solo los suyos), OPERADOR, ADMIN | 200 |
| GET | `/shipments/{id}` | CLIENTE (si es suyo), OPERADOR, ADMIN | 200 · 404 |
| POST | `/shipments` | CLIENTE, OPERADOR, ADMIN | 201 · 400 |
| PUT | `/shipments/{id}/status` | OPERADOR, ADMIN | 200 · 400 · 403 · 404 · 409 |
| GET | `/actuator/health` | público | 200 |

Sin token o token inválido → **401**. Rol insuficiente → **403**. Los errores de shipments (400, 404, 409) se devuelven tal cual. Shipments caído → **503**.

## Variables de entorno
| Variable | Por defecto | Uso |
|---|---|---|
| `JWT_ISSUER_URI` | `https://login.microsoftonline.com/<tenant>/v2.0` | Issuer de Entra ID (token v2) |
| `JWT_AUDIENCE` | clientId de RutaExpress | Audiencia esperada (vacío = no validar) |
| `SHIPMENTS_URL` | `http://localhost:8081` | En Docker: `http://shipments:8081` |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | CORS para desarrollo local (en la nube lo hace el API Gateway) |
| `PORT` | `8082` | Puerto HTTP |

## Ejecución
- Local: `mvnw.cmd spring-boot:run` (con shipments en 8081).
- Docker: `docker build -t ms-bff .` dentro de `ms-rutaexpress-bff/`.
- CI: `.github/workflows/ci.yml` compila, corre los tests y construye la imagen en cada PR.

## Avance
| Paso | Descripción | Estado |
|---|---|---|
| Entrega 1 | Resource Server, roles, CORS, despliegue EC2 + API Gateway | ✅ |
| F2 | Endpoints de demo reemplazados por `/me` y `/shipments`; audiencia validada; sin rol por defecto; config por variables de entorno; CI | ✅ |
