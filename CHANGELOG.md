# Changelog

Todos los cambios notables de Virtual Clubs API se documentan aquí.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/),
y el proyecto sigue [Versionado Semántico](https://semver.org/lang/es/).

> **Criterio de versiones:**
> - `MAJOR.0.0` — cambio breaking en la API (contratos modificados, endpoints renombrados/eliminados)
> - `0.MINOR.0` — nuevo módulo o conjunto de endpoints
> - `0.0.PATCH` — correcciones, mejoras internas, actualizaciones sin cambio de contrato

---

## [Sin publicar]

---

## [0.0.2] — 2026-03-27

### Añadido
- Suite de tests unitarios e integración para el módulo de autenticación
- Sistema de tareas integrado con Notion (slash commands de Claude Code)
- Integración de Flyway para migraciones versionadas de base de datos

### Cambiado
- Dependencias actualizadas: JJWT migrado a 0.12.6

### Corregido
- Fallos de compilación y lógica en la suite de tests

---

## [0.0.1] — 2025-12-26

### Añadido
- Sistema completo de autenticación JWT multi-dispositivo
- Endpoints: `POST /v1/auth/login`, `POST /v1/auth/register`, `POST /v1/auth/refresh`, `DELETE /v1/auth/logout`
- Soporte OAuth2 con Google (verificación de `id_token`)
- Refresh tokens hasheados (SHA-256) en base de datos — nunca en texto plano
- Soporte multi-dispositivo: cada dispositivo gestiona su propio refresh token independiente
- Validación de contraseñas fuertes con `@StrongPassword` (2 mayúsc., 2 minúsc., 1 dígito)
- Respuestas API estandarizadas con `ApiResponse<T>`
- 27 códigos de error tipados en `ErrorType` para manejo en cliente mobile
- Configuración de perfiles `dev` / `prod` con niveles de log y actuator diferenciados
- CI/CD: build Docker multi-arquitectura (amd64/arm64) hacia GHCR en cada push a `development`
