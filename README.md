# 🚀 RutaExpress — BFF (Backend For Frontend)

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)
![Spring Security](https://img.shields.io/badge/Spring%20Security-OAuth2-green.svg)
![Docker](https://img.shields.io/badge/Docker-Multi--stage-blue.svg)
![AWS](https://img.shields.io/badge/AWS-EC2-yellow.svg)
![Status](https://img.shields.io/badge/estado-funcionando-success.svg)

Backend For Frontend (BFF) de **RutaExpress**, implementado con **Spring Boot 4.x** como **OAuth2 Resource Server**. Valida los JWT emitidos por **Microsoft Entra ID** y expone endpoints protegidos por rol para el frontend Angular.

---

## 📑 Índice

1. [Arquitectura](#-arquitectura)
2. [Stack tecnológico](#-stack-tecnológico)
3. [Estructura del proyecto](#-estructura-del-proyecto)
4. [Seguridad y autenticación](#-seguridad-y-autenticación)
5. [Endpoints](#-endpoints)
6. [Puesta en marcha local](#-puesta-en-marcha-local)
7. [Puesta en marcha con Docker](#-puesta-en-marcha-con-docker)
8. [Variables de entorno](#-variables-de-entorno)
9. [Pipeline CI/CD](#-pipeline-cicd)
10. [Troubleshooting](#-troubleshooting)

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│  Angular Frontend                                           │
│  - MSAL obtiene JWT de Entra ID                             │
│  - MsalInterceptor inyecta Bearer en cada request           │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTPS + Authorization: Bearer <JWT>
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  AWS API Gateway (JWT Authorizer)                           │
│  - Valida firma, iss, aud, exp                              │
│  - Reenvía al BFF si es válido                              │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  BFF Spring Boot (este repositorio)                         │
│  - OAuth2 Resource Server                                   │
│  - Validación secundaria del JWT                            │
│  - Autorización por rol (@PreAuthorize)                     │
│  - CORS configurable                                        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  Microservicios de dominio (futuro)                         │
│  - shipments, catalog, notify, report, audit                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Stack tecnológico

| Componente | Versión | Uso |
| :--- | :--- | :--- |
| **Java** | 21 | Runtime |
| **Spring Boot** | 4.1.1 | Framework principal |
| **Spring Security** | 6.x | OAuth2 Resource Server |
| **spring-boot-starter-oauth2-resource-server** | 4.x | Validación JWT |
| **spring-boot-starter-actuator** | 4.x | Health checks |
| **Lombok** | Latest | Reducción de boilerplate |
| **Maven** | 3.9+ | Build y dependencias |
| **Docker** | Multi-stage | Empaquetado |

---

## 📁 Estructura del proyecto

```
ms-rutaexpress-bff/
├── src/
│   ├── main/
│   │   ├── java/com/rutaexpress/ms_rutaexpress_bff/
│   │   │   ├── MsRutaexpressBffApplication.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java      # Config OAuth2 + CORS + JWT
│   │   │   └── controller/
│   │   │       └── TestController.java      # Endpoints de prueba
│   │   └── resources/
│   │       └── application.yml              # Configuración
│   └── test/                                 # Tests (pendiente)
├── Dockerfile                                # Multi-stage build
├── pom.xml                                   # Dependencias Maven
├── mvnw / mvnw.cmd                           # Maven Wrapper
└── HELP.md
```

---

## 🔐 Seguridad y autenticación

### OAuth2 Resource Server

El BFF actúa como **Resource Server**: valida los JWT entrantes contra el JWKS de Entra ID sin mantener sesiones. Cada request pasa por:

1. **JwtDecoder** — valida firma, issuer, expiración y audiencia.
2. **JwtAuthenticationConverter** — extrae el claim `roles` y lo mapea a `GrantedAuthority`.
3. **@PreAuthorize** — autoriza según el rol requerido por endpoint.
4. **Fallback `ROLE_CLIENTE`** — si el token no trae `roles`, se asigna por defecto.

### Configuración del `JwtDecoder`

Debido a un bug conocido de Spring Boot 4.x con la propiedad `audiences`, el BFF define un `@Bean JwtDecoder` explícito:

```java
@Bean
public JwtDecoder jwtDecoder() {
    NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

    OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            new JwtIssuerValidator(issuerUri)
    );

    jwtDecoder.setJwtValidator(validator);
    return jwtDecoder;
}
```

### CORS configurable

Los orígenes permitidos se leen desde la variable de entorno `APP_CORS_ALLOWED_ORIGINS` (separados por coma):

```yaml
app:
  cors:
    allowed-origins: ${APP_CORS_ALLOWED_ORIGINS:http://localhost:4200}
```

---

## 🌐 Endpoints

| Endpoint | Método | Rol requerido | Descripción |
| :--- | :---: | :--- | :--- |
| `/actuator/health` | GET | — | Health check (público) |
| `/api/v1/public/status` | GET | — | Estado público del BFF |
| `/api/v1/clientes/dashboard` | GET | CLIENTE / OPERADOR / ADMIN | Datos del cliente |
| `/api/v1/operadores/panel` | GET | OPERADOR | Panel de operadores |

### Códigos de respuesta

| Código | Significado |
| :---: | :--- |
| `200` | OK — Token válido + rol autorizado |
| `401` | Sin token o token inválido (genera `WWW-Authenticate: Bearer`) |
| `403` | Token válido pero rol insuficiente |

---

## 🚀 Puesta en marcha local

### Prerrequisitos

- Java 21
- Maven 3.9+ (o usar el wrapper `./mvnw`)

### 1. Clonar

```bash
git clone https://github.com/RutaExpress/ms-rutaexpress-bff.git
cd ms-rutaexpress-bff
```

### 2. Verificar `application.yml`

```yaml
server:
  port: 8082

spring:
  application:
    name: ms-rutaexpress-bff

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://login.microsoftonline.com/70d90397-3549-4694-8a16-70f45020c6fa/v2.0
          audiences: 5421b650-58dc-4ae6-863e-a34f55e0177a

app:
  cors:
    allowed-origins: ${APP_CORS_ALLOWED_ORIGINS:http://localhost:4200}

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never
```

### 3. Levantar

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

**Esperado:**
```
Started MsRutaexpressBffApplication in X.XXX seconds
Tomcat started on port 8082
```

### 4. Verificar

```bash
curl -i http://localhost:8082/actuator/health
```

Debe responder `200 OK` con `{"status":"UP"}`.

---

## 🐳 Puesta en marcha con Docker

### 1. Build de la imagen

```bash
docker build -t rutaexpress-bff:latest .
```

### 2. Run del contenedor

```bash
docker run -p 8082:8082 \
  -e APP_CORS_ALLOWED_ORIGINS=http://localhost:4200 \
  rutaexpress-bff:latest
```

### 3. Con Docker Compose (recomendado)

```yaml
services:
  bff:
    image: 792951721857.dkr.ecr.us-east-1.amazonaws.com/rutaexpress-bff:latest
    container_name: rutaexpress-bff
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - APP_CORS_ALLOWED_ORIGINS=https://a9kmdqgude.execute-api.us-east-1.amazonaws.com,http://localhost:4200
    restart: unless-stopped
```

```bash
docker compose up -d
```

---

## ⚙️ Variables de entorno

| Variable | Descripción | Valor por defecto |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring | `prod` |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | Emisor del JWT | (YAML) |
| `APP_CORS_ALLOWED_ORIGINS` | Orígenes permitidos (separados por coma) | `http://localhost:4200` |

---

## 🔄 Pipeline CI/CD

Cada push a `main` dispara un workflow en GitHub Actions que:

1. Hace checkout del código.
2. Configura credenciales AWS.
3. Compila el proyecto con Maven (`./mvnw clean package`).
4. Construye la imagen Docker (multi-stage: Maven build → JRE alpine).
5. Publica en Amazon ECR como `rutaexpress-bff:latest`.

---

## 🐛 Troubleshooting

| Error | Causa | Solución |
| :--- | :--- | :--- |
| `invalid_token audience` | Audiencia mal configurada | Verificar `audiences` en `application.yml` |
| `401` con token válido | `issuer-uri` incorrecto | Debe terminar en `/v2.0` |
| `403` con rol correcto | Converter no lee `roles` | Verificar `JwtAuthenticationConverter` |
| CORS bloquea preflight | Origen no incluido | Agregar a `APP_CORS_ALLOWED_ORIGINS` |
| `ContainerConfig` error | docker-compose v1 | Migrar a `docker-compose-v2` |
| `Connection refused` desde EC2 | Contenedor caído | `docker compose up -d` |
| `voc-cancel-cred` en pipeline | Token AWS expirado | Renovar credenciales del lab |

---

## 📄 Licencia

Proyecto académico — Duoc UC, Subdirección de Diseño Instruccional, 2026.

---

**Última actualización:** Septiembre 2026