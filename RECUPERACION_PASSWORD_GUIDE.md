# Guía de Recuperación de Contraseña - DeustoSport

## Descripción General

Se ha implementado una funcionalidad completa de recuperación de contraseña que incluye:

1. **EmailService**: Servicio para enviar emails de recuperación
2. **LoginService**: Integración de la recuperación en el flujo de autenticación
3. **Frontend**: Interfaz de usuario con modal para solicitar y restablecer contraseña
4. **Configuración**: Propiedades de Spring Mail para envío de emails

## Flujo de Recuperación

### 1. Solicitar Recuperación
- El usuario hace clic en "¿Olvidaste tu contraseña?" en la página de login
- Se abre un modal que solicita el email
- Al enviar, se genera un token único y se guarda en la base de datos
- Se envía un email con el token (o se simula en consola si email está deshabilitado)

### 2. Restablecer Contraseña
- El usuario recibe el email con el código de recuperación
- Introduce el código en el modal junto con la nueva contraseña
- El sistema valida el token (debe estar vigente, máximo 24 horas)
- Si es válido, se actualiza la contraseña y se limpia el token

## Configuración

### En Desarrollo (Simulación)
Por defecto, el sistema simula el envío de emails y los muestra en la consola. Esto es útil para desarrollo.

```
# application.properties (valores por defecto)
app.email.enabled=false
```

Cuando está deshabilitado, verás en la consola algo como:
```
=========================================================
📧 SIMULACIÓN DE ENVÍO DE EMAIL (Desarrollo)
Destinatario: usuario@example.com
Asunto: DeustoSport - Recuperación de Contraseña
Contenido: 
Hola,
Hemos recibido una solicitud para restablecer tu contraseña en DeustoSport.
Tu código de seguridad de un solo uso es:
[12345-67890-...]
...
=========================================================
```

### En Producción (Email Real)

Para habilitar el envío de emails reales, configura las variables de entorno:

```bash
# Variables de entorno o en application.properties
EMAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-contraseña-app

# Ejemplo con Gmail:
# 1. Habilitar 2FA en tu cuenta de Google
# 2. Generar una contraseña de aplicación en https://myaccount.google.com/apppasswords
# 3. Usar esa contraseña aquí
```

## Estructura de Clases

### EmailService
```java
// Envía emails de recuperación
public void enviarEmailRecuperacion(String destinatario, String token)
```

**Características:**
- Detecta automáticamente si Spring Mail está disponible
- Simula en consola si no está habilitado
- Genera contenido HTML para el email
- Maneja excepciones de envío de manera elegante

### LoginService - Métodos Relacionados

```java
// Solicita la recuperación (genera token y envía email)
public LoginResponse solicitarRecuperacion(String email)

// Restablece la contraseña con el token
public LoginResponse restablecerPassword(CambioPasswordRequest solicitud)
```

## Endpoints API

### POST /api/auth/solicitar-recuperacion
```bash
curl -X POST "http://localhost:8080/api/auth/solicitar-recuperacion?email=usuario@example.com"
```

**Respuesta exitosa:**
```json
{
  "usuarioId": 1,
  "nombreCompleto": "Juan Pérez",
  "email": "usuario@example.com",
  "rol": "CLIENTE",
  "mensaje": "Instrucciones enviadas al email",
  "exitoso": true
}
```

### POST /api/auth/restablecer-password
```bash
curl -X POST "http://localhost:8080/api/auth/restablecer-password" \
  -H "Content-Type: application/json" \
  -d '{
    "emailOToken": "12345-67890-abcdef...",
    "passwordNueva": "nuevaPassword123"
  }'
```

**Respuesta exitosa:**
```json
{
  "usuarioId": 1,
  "nombreCompleto": "Juan Pérez",
  "email": "usuario@example.com",
  "rol": "CLIENTE",
  "mensaje": "Contraseña actualizada exitosamente",
  "exitoso": true
}
```

## Frontend - login.html

### Nuevas Funcionalidades

#### Modal de Recuperación
- Se abre con el enlace "¿Olvidaste tu contraseña?"
- Paso 1: Solicitar recuperación (email)
- Paso 2: Restablecer contraseña (token + nueva contraseña)

#### Validaciones
- Email debe estar registrado
- Token no debe estar expirado (24 horas)
- Contraseña nueva debe coincidir con la confirmación
- Mínimo 6 caracteres

#### Mensajes de Usuario
- Confirmación de envío de email
- Errores claros si el token es inválido o expirado
- Notificación de cambio exitoso

## Base de Datos

Las siguientes columnas de la tabla `credenciales` ya existen:

```sql
token_recuperacion VARCHAR(255) -- Token único para recuperación
fecha_expiracion_token TIMESTAMP -- Expira en 24 horas
```

## Seguridad

### Token de Recuperación
- Se genera con `UUID.randomUUID()` (altamente seguro)
- Se valida que no sea nulo y no esté expirado
- Se elimina inmediatamente después de restablecer la contraseña
- Válido solo por 24 horas

### Contraseña
- Se encripta con BCrypt
- Nunca se envía en plain text
- Se valida que coincida con la confirmación

### CORS
Asegúrate de que CORS está habilitado para los endpoints de autenticación en `SecurityConfig.java`

## Troubleshooting

### "Token inválido o expirado"
- Verifica que el token sea correcto (sin espacios extras)
- Comprueba que no hayan pasado más de 24 horas
- En desarrollo, copia exactamente el token de la consola

### "Email o contraseña no coinciden"
- Asegúrate de escribir las dos contraseñas igual
- Mínimo 6 caracteres

### No recibo el email (en producción)
- Verifica las credenciales SMTP
- Con Gmail, usa contraseña de aplicación, no la contraseña de la cuenta
- Revisa la carpeta de spam
- Comprueba los logs de la aplicación

### Error de conexión CORS
- Verifica que `SecurityConfig.java` permita los endpoints `/api/auth/*`
- En desarrollo, debería funcionar con `http://localhost:8080`

## Pasos para Probar

### Desarrollo (con simulación)

1. **Iniciar la aplicación**
   ```bash
   cd my-app
   gradle bootRun
   ```

2. **Ir a la página de login**
   ```
   http://localhost:8080/login.html
   (desde my-webapp)
   ```

3. **Hacer clic en "¿Olvidaste tu contraseña?"**

4. **Introducir un email registrado**
   - Se verá el token en la consola de la aplicación

5. **Copiar el token y pegarla en el modal**

6. **Introducir una nueva contraseña**

7. **Verificar que funciona el nuevo login**

### Producción (con email real)

1. **Configurar variables de entorno**
   ```bash
   export EMAIL_ENABLED=true
   export MAIL_HOST=smtp.gmail.com
   export MAIL_PORT=587
   export MAIL_USERNAME=your-email@gmail.com
   export MAIL_PASSWORD=your-app-password
   ```

2. **Reiniciar la aplicación**

3. **El email debería llegar a la bandeja del usuario**

## Dependencias Añadidas

En `my-app/build.gradle`:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-mail'
```

Esto proporciona:
- `JavaMailSender`: Interface para enviar emails
- `SimpleMailMessage`: Clase para construir mensajes de email
- Soporte para protocolos SMTP

## Archivos Modificados

1. **EmailService.java** - Mejorado con JavaMailSender
2. **LoginService.java** - Integración de EmailService
3. **login.html** - Nuevo modal y JavaScript para recuperación
4. **application.properties** - Configuración de email
5. **build.gradle** - Dependencia de Spring Mail

## Notas Importantes

- El sistema usa transacciones `@Transactional` para garantizar consistencia
- Los tokens se validan en tiempo real
- Las excepciones se manejan gracefully
- El frontend no expone información sensible (mensajes genéricos si email no existe)
- Compatible con todos los roles de usuario (CLIENTE, SECRETARIA, COORDINADOR, AYUNTAMIENTO)

---

**Versión:** 1.0  
**Fecha:** 25/03/2026  
**Estado:** Implementado y listo para usar
