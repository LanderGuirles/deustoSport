package com.deustosport.my_app.controller;
 
import com.deustosport.my_app.dto.CambioPasswordRequest;
import com.deustosport.my_app.dto.LoginRequest;
import com.deustosport.my_app.dto.LoginResponse;
import com.deustosport.my_app.dto.ActualizarPerfilRequest;
import com.deustosport.my_app.dto.PerfilUsuarioResponse;
import com.deustosport.my_app.dto.RegistroRequest;
import com.deustosport.my_app.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para login, registro y gestión de sesiones")
public class LoginController {
 
    @Autowired
    private LoginService loginService;

    @Autowired
    private com.deustosport.my_app.repository.UsuarioRepository usuarioRepository;
 
    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario con email y contraseña")
    public ResponseEntity<LoginResponse> registro(@Valid @RequestBody RegistroRequest solicitud) {
        LoginResponse response = loginService.registrarUsuario(solicitud);
        return response.isExitoso() ?
                ResponseEntity.status(HttpStatus.CREATED).body(response) :
                ResponseEntity.badRequest().body(response);
    }
 
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con email y contraseña")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest solicitud) {
        if (solicitud.getEmail() == null || solicitud.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(null, null, null, null, false,
                            null, "Email y contraseña son requeridos", false));
        }
 
        LoginResponse response = loginService.iniciarSesion(solicitud);
        return response.isExitoso() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
 
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Cierra la sesión del usuario autenticado")
    public ResponseEntity<LoginResponse> logout(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(null, null, null, null, false,
                            null, "ID de usuario no válido", false));
        }
 
        LoginResponse response = loginService.cerrarSesion(usuarioId);
        return response.isExitoso() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }
 
    @PostMapping("/solicitar-recuperacion")
    public ResponseEntity<LoginResponse> solicitarRecuperacion(@RequestParam("email") String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(null, null, null, null, false,
                            null, "Email es requerido", false));
        }
 
        LoginResponse response = loginService.solicitarRecuperacion(email);
        return ResponseEntity.ok(response);
    }
 
    @PostMapping("/restablecer-password")
    public ResponseEntity<LoginResponse> restablecerPassword(@RequestBody CambioPasswordRequest solicitud) {
        if (solicitud.getEmailOToken() == null || solicitud.getPasswordNueva() == null) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(null, null, null, null, false,
                            null, "Token y contraseña nueva son requeridos", false));
        }
 
        LoginResponse response = loginService.restablecerPassword(solicitud);
        return response.isExitoso() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
 
    @PostMapping("/cambiar-password")
    public ResponseEntity<LoginResponse> cambiarPassword(
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestBody CambioPasswordRequest solicitud) {
 
        if (usuarioId == null || usuarioId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(null, null, null, null, false,
                            null, "ID de usuario no válido", false));
        }
 
        if (solicitud.getEmailOToken() == null || solicitud.getPasswordNueva() == null) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(null, null, null, null, false,
                            null, "Contraseña actual y nueva son requeridas", false));
        }
 
        LoginResponse response = loginService.cambiarPassword(usuarioId,
                solicitud.getEmailOToken(), solicitud.getPasswordNueva());
 
        return response.isExitoso() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

        @GetMapping("/perfil")
        @Operation(summary = "Obtener perfil", description = "Obtiene los datos del perfil del usuario autenticado")
        public ResponseEntity<PerfilUsuarioResponse> obtenerPerfil(@RequestHeader("X-Usuario-Id") Long usuarioId) {
                if (usuarioId == null || usuarioId <= 0) {
                        return ResponseEntity.badRequest().body(
                                        new PerfilUsuarioResponse(null, null, null, null, null, false, null, "ID de usuario no válido", false));
                }

                PerfilUsuarioResponse response = loginService.obtenerPerfil(usuarioId);
                return response.isExitoso()
                                ? ResponseEntity.ok(response)
                                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        @PutMapping("/perfil")
        @Operation(summary = "Actualizar perfil", description = "Actualiza nombre y teléfono del usuario autenticado")
        public ResponseEntity<PerfilUsuarioResponse> actualizarPerfil(
                        @RequestHeader("X-Usuario-Id") Long usuarioId,
                        @Valid @RequestBody ActualizarPerfilRequest solicitud) {

                if (usuarioId == null || usuarioId <= 0) {
                        return ResponseEntity.badRequest().body(
                                        new PerfilUsuarioResponse(null, null, null, null, null, false, null, "ID de usuario no válido", false));
                }

                PerfilUsuarioResponse response = loginService.actualizarPerfil(usuarioId, solicitud);
                return response.isExitoso()
                                ? ResponseEntity.ok(response)
                                : ResponseEntity.badRequest().body(response);
        }

        @PostMapping("/recargar-billetera")
        @Operation(summary = "Recargar billetera", description = "Añade saldo a la billetera del usuario")
        public ResponseEntity<?> recargarBilletera(@RequestBody java.util.Map<String, Object> body) {
                try {
                        Long usuarioId = Long.valueOf(body.get("usuarioId").toString());
                        java.math.BigDecimal cantidad = new java.math.BigDecimal(body.get("cantidad").toString());

                        if (cantidad.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                                return ResponseEntity.badRequest()
                                        .body(java.util.Map.of("error", "La cantidad debe ser mayor que 0"));
                        }

                        var usuarioOpt = loginService.obtenerPerfil(usuarioId);
                        if (!usuarioOpt.isExitoso()) {
                                return ResponseEntity.badRequest()
                                        .body(java.util.Map.of("error", "Usuario no encontrado"));
                        }

                        // Recargar directamente
                        com.deustosport.my_app.entity.Usuario usuario = usuarioRepository.findById(usuarioId)
                                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
                        usuario.setBilletera(usuario.getBilletera().add(cantidad));
                        usuarioRepository.save(usuario);

                        return ResponseEntity.ok(java.util.Map.of(
                                "mensaje", "Billetera recargada correctamente",
                                "billetera", usuario.getBilletera()
                        ));
                } catch (Exception e) {
                        return ResponseEntity.badRequest()
                                .body(java.util.Map.of("error", "Error al recargar: " + e.getMessage()));
                }
        }
}