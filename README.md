# DeustoSport — Grupo 8

Sistema de gestión de reservas deportivas desarrollado con Spring Boot (backend REST) y Thymeleaf (frontend web). Arquitectura de 3 capas: presentación (`my-webapp`), lógica de negocio (`my-app`) y persistencia (PostgreSQL / H2).

Nota: se añadió un enlace de QR visible en las reservas confirmadas para facilitar el acceso rápido.

---

## Metodología SCRUM y Organización

Este proyecto sigue la metodología ágil SCRUM. Los detalles sobre los roles, el Product Backlog y el Sprint Backlog se encuentran en la carpeta `docs/`:

- [**Roles SCRUM**](docs/ROLES.md): Definición de responsabilidades del equipo.
- [**Backlog del Proyecto**](docs/BACKLOG.md): Historias de usuario, estimaciones (Story Points) y seguimiento de tareas.

---

## Prerrequisitos

| Herramienta | Versión mínima |
|-------------|----------------|
| Java (JDK)  | 21             |
| Maven       | 3.9+           |
| Gradle      | Incluido (wrapper `gradlew`) |
| PostgreSQL  | 14+ (solo para ejecución real; los tests usan H2 en memoria) |

---

## Construcción

El proyecto soporta tanto **Gradle** (recomendado) como **Maven** para cumplir con los criterios de evaluación.

### Usando Gradle
```bash
./gradlew build -x test
```

### Usando Maven
```bash
mvn clean install -DskipTests
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
# Gradle
./gradlew :my-app:bootRun

# Maven
mvn spring-boot:run -pl my-app
```

### Arrancar el frontend (Thymeleaf)

```bash
# Gradle
./gradlew :my-webapp:bootRun

# Maven
mvn spring-boot:run -pl my-webapp
```

---

## Tests y Cobertura

### Ejecución de tests

```bash
# Gradle
./gradlew test

# Maven
mvn test
```

### Reportes de Cobertura (JaCoCo)

La cobertura de código se genera automáticamente tras ejecutar los tests:
- **Gradle:** `my-app/build/reports/jacoco/test/html/index.html`
- **Maven:** `my-app/target/site/jacoco/index.html`

---

## Demo / Datos de Prueba

Para probar la funcionalidad completa del sistema sin registrar nuevos datos, se han pre-cargado los siguientes usuarios de demo:

| Rol | Email | Password |
|-----|-------|----------|
| **Cliente** | `juan@deustosport.com` | `password123` |
| **Cliente** | `laura@deustosport.com` | `password123` |
| **Secretaría** | `maria@deustosport.com` | `password123` |
| **Coordinador** | `carlos@deustosport.com` | `password123` |
| **Mantenimiento** | `iker.mantenimiento@gmail.com` | `mantenimiento` |
| **Ayuntamiento** | `ayuntamiento.bilbao@deustosport.com` | `bilbao` |

> **Nota:** La base de datos H2 se reinicia en cada arranque si no se configura una persistencia externa.

---

## Documentación Automática

El proyecto genera y publica documentación de forma automatizada:

1.  **API REST (Swagger/OpenAPI):** Disponible en `/swagger-ui.html` cuando la aplicación está en ejecución.
2.  **Reportes de Calidad:** Los reportes de JaCoCo se generan en cada build de CI y se adjuntan como artefactos en GitHub Actions.

