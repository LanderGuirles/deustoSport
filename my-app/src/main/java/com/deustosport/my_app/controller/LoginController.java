package com.deustosport.my_app.controller;
 
import com.deustosport.my_app.dto.CambioPasswordRequest;
import com.deustosport.my_app.dto.LoginRequest;
import com.deustosport.my_app.dto.LoginResponse;
import com.deustosport.my_app.dto.RegistroRequest;
import com.deustosport.my_app.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
                    // Añadimos un null extra para el campo 'rol'
                    .body(new LoginResponse(null, null, null, null, 
                            "Email y contraseña son requeridos", false));
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
                    // Añadimos un null extra para el campo 'rol'
                    .body(new LoginResponse(null, null, null, null, 
                            "ID de usuario no válido", false));
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
                    // Añadimos un null extra para el campo 'rol'
                    .body(new LoginResponse(null, null, null, null, 
                            "Email es requerido", false));
        }
 
        LoginResponse response = loginService.solicitarRecuperacion(email);
        return ResponseEntity.ok(response);
    }
 
    @PostMapping("/restablecer-password")
    public ResponseEntity<LoginResponse> restablecerPassword(@RequestBody CambioPasswordRequest solicitud) {
        if (solicitud.getEmailOToken() == null || solicitud.getPasswordNueva() == null) {
            return ResponseEntity.badRequest()
                    // Añadimos un null extra para el campo 'rol'
                    .body(new LoginResponse(null, null, null, null, 
                            "Token y contraseña nueva son requeridos", false));
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
                    // Añadimos un null extra para el campo 'rol'
                    .body(new LoginResponse(null, null, null, null, 
                            "ID de usuario no válido", false));
        }
 
        if (solicitud.getEmailOToken() == null || solicitud.getPasswordNueva() == null) {
            return ResponseEntity.badRequest()
                    // Añadimos un null extra para el campo 'rol'
                    .body(new LoginResponse(null, null, null, null, 
                            "Contraseña actual y nueva son requeridas", false));
        }
 
        LoginResponse response = loginService.cambiarPassword(usuarioId,
                solicitud.getEmailOToken(), solicitud.getPasswordNueva());
 
        return response.isExitoso() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }
}