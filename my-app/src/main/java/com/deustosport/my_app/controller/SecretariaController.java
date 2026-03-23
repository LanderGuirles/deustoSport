package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.SecretariaReservaResumenDto;
import com.deustosport.my_app.dto.SecretariaUsuarioResumenDto;
import com.deustosport.my_app.service.SecretariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secretaria")
@Tag(name = "Secretaría", description = "Panel de consulta rápida de usuarios y reservas")
public class SecretariaController {

    private final SecretariaService secretariaService;

    public SecretariaController(SecretariaService secretariaService) {
        this.secretariaService = secretariaService;
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Buscar usuarios", description = "Permite buscar usuarios por DNI")
    public ResponseEntity<List<SecretariaUsuarioResumenDto>> buscarUsuarios(
            @RequestParam(required = false) String dni) {
        return ResponseEntity.ok(secretariaService.buscarUsuarios(dni));
    }

    @GetMapping("/usuarios/{usuarioId}/reservas")
    @Operation(summary = "Consultar reservas de usuario", description = "Devuelve las reservas de un usuario para atención en secretaría")
    public ResponseEntity<List<SecretariaReservaResumenDto>> consultarReservasUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(secretariaService.obtenerReservasPorUsuario(usuarioId));
    }
}
