# DeustoSport — Grupo 8

Sistema de gestión de reservas deportivas desarrollado con Spring Boot (backend REST) y Thymeleaf (frontend web). Arquitectura de 3 capas: presentación (`my-webapp`), lógica de negocio (`my-app`) y persistencia (PostgreSQL / H2).

---

## Prerrequisitos

| Herramienta | Versión mínima |
|-------------|----------------|
| Java (JDK)  | 21             |
| Gradle      | Incluido (wrapper `gradlew`) |
| PostgreSQL  | 14+ (solo para ejecución real; los tests usan H2 en memoria) |

---

## Construcción

```bash
# Compilar ambos módulos sin ejecutar tests
./gradlew build -x test
```

---

## Ejecución

### Variables de entorno (PostgreSQL)

```bash
export DB_URL="jdbc:postgresql://localhost:5432/deustosport"
export DB_DRIVER="org.postgresql.Driver"
export DB_USER="postgres"
export DB_PASSWORD="postgres"
```

> Si no se definen, la app usa H2 en memoria (útil para desarrollo local).

### Arrancar el backend (API REST)

```bash
./gradlew :my-app:bootRun
```

### Arrancar el frontend (Thymeleaf)

```bash
./gradlew :my-webapp:bootRun
```

**Windows (PowerShell):**

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/deustosport"
$env:DB_DRIVER="org.postgresql.Driver"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
.\gradlew.bat :my-app:bootRun
```

---

## Tests

El proyecto divide las pruebas en tres fases independientes.

### 1. Tests unitarios

Pruebas aisladas con Mockito. No requieren servidor ni base de datos externa.

```bash
./gradlew :my-app:test
```

Genera reporte de cobertura JaCoCo en:
`my-app/build/reports/jacoco/test/html/index.html`

### 2. Tests de integración

Arrancan el contexto completo de Spring Boot con H2 en memoria y realizan llamadas HTTP reales al servidor embebido.

```bash
./gradlew :my-app:integrationTest
```

### 3. Tests de rendimiento (ContiPerf)

Requieren el servidor arrancado en `localhost:8080`. Ejecutar en una terminal separada:

```bash
# Terminal 1 — arrancar servidor
./gradlew :my-app:bootRun

# Terminal 2 — ejecutar tests de rendimiento
./gradlew :my-app:performanceTest
```

Genera reporte en: `my-app/build/reports/tests/performanceTest/index.html`

### Ejecutar todas las fases de una vez

```bash
./gradlew :my-app:test :my-app:integrationTest
```

---

## Cobertura de código

El plugin **JaCoCo** genera el informe automáticamente al ejecutar `test`. Abre el informe HTML:

```
my-app/build/reports/jacoco/test/html/index.html
```

---

## API y Swagger UI

Con el servidor arrancado, la documentación interactiva está disponible en:

```
http://localhost:8080/swagger-ui.html
```

---

## Principales endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/auth/registro` | Registro de usuario |
| `POST` | `/api/auth/login` | Autenticación |
| `GET`  | `/api/pistas` | Listado de pistas |
| `GET`  | `/api/instalaciones` | Listado de instalaciones |
| `GET`  | `/api/tarifas` | Listado de tarifas |
| `POST` | `/api/reservas` | Crear reserva |
| `PUT`  | `/api/instalaciones/{id}/horario-general` | Configurar horario |
| `GET`  | `/api/secretaria/usuarios` | Búsqueda por DNI (secretaría) |

---

## Módulos del proyecto

```
deustoSport/
├── my-app/          # Backend REST (Spring Boot 3.4.3, Java 21)
│   ├── controller/  # Capa de presentación (REST controllers)
│   ├── service/     # Capa de negocio
│   └── repository/  # Capa de persistencia (Spring Data JPA)
└── my-webapp/       # Frontend web (Thymeleaf + HTML/JS/CSS)
```
