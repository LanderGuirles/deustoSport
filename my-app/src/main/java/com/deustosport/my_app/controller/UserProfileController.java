package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.ActualizarPerfilRequest;
import com.deustosport.my_app.dto.PerfilUsuarioResponse;
import com.deustosport.my_app.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Perfil Usuario", description = "Alias de endpoints de perfil de usuario")
public class UserProfileController {

    private final LoginService loginService;

    public UserProfileController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil de usuario")
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

    @PutMapping("/profile")
    @Operation(summary = "Actualizar perfil de usuario")
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
}
