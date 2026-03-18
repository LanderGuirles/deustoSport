# Grupo8

## Base de datos (PostgreSQL)

El proyecto está preparado para usar PostgreSQL en ejecución normal y H2 en memoria por defecto (útil para tests/local rápido).

### Dependencias añadidas

- `spring-boot-starter-data-jpa`
- `org.postgresql:postgresql`
- `com.h2database:h2` (en test runtime)

### Variables de entorno para PostgreSQL

Configura estas variables antes de arrancar cualquiera de los módulos:

- `DB_URL` (ejemplo: `jdbc:postgresql://localhost:5432/deustosport`)
- `DB_DRIVER` (valor: `org.postgresql.Driver`)
- `DB_USER` (ejemplo: `postgres`)
- `DB_PASSWORD` (tu contraseña)

Si no defines variables, la app usa H2 en memoria por defecto.

### Ejemplo en PowerShell (Windows)

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/deustosport"
$env:DB_DRIVER="org.postgresql.Driver"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
.\gradlew.bat :my-app:bootRun
```

Para el otro módulo:

```powershell
.\gradlew.bat :my-webapp:bootRun
```

## Horario general del polideportivo

El módulo `my-app` permite definir un horario general por instalación. Las reservas y las consultas de disponibilidad quedan bloqueadas fuera de esa franja.

Endpoint:

```http
PUT /api/instalaciones/{instalacionId}/horario-general
Content-Type: application/json

{
	"horaApertura": "08:00:00",
	"horaCierre": "22:00:00"
}
```

Si una reserva empieza antes de la apertura o termina después del cierre, la API responde con error y no la registra.