package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.ModificarReservaRequest;
import com.deustosport.my_app.dto.SecretariaReservaResumenDto;
import com.deustosport.my_app.dto.SecretariaUsuarioResumenDto;
import com.deustosport.my_app.service.SecretariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(name = "dni", required = false) String dni) {
        return ResponseEntity.ok(secretariaService.buscarUsuarios(dni));
    }

    @GetMapping("/usuarios/{usuarioId}/reservas")
    @Operation(summary = "Consultar reservas de usuario", description = "Devuelve las reservas de un usuario para atención en secretaría")
    public ResponseEntity<List<SecretariaReservaResumenDto>> consultarReservasUsuario(@PathVariable("usuarioId") Long usuarioId) {
        return ResponseEntity.ok(secretariaService.obtenerReservasPorUsuario(usuarioId));
    }

    @GetMapping("/reservas")
    @Operation(summary = "Consultar todas las reservas", description = "Devuelve todas las reservas del sistema")
    public ResponseEntity<List<SecretariaReservaResumenDto>> consultarTodasLasReservas() {
        return ResponseEntity.ok(secretariaService.obtenerTodasLasReservas());
    }

    @PutMapping("/reservas/{reservaId}")
    @Operation(summary = "Modificar reserva por secretaría", description = "Permite a la secretaría modificar cualquier reserva sin restricción de 24h")
    public ResponseEntity<?> modificarReservaPorSecretaria(
            @PathVariable("reservaId") Long reservaId,
            @RequestBody ModificarReservaRequest request) {
        try {
            if (request.getFecha() == null || request.getHoraInicio() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Debes indicar nueva fecha y nueva hora de inicio."));
            }
            secretariaService.modificarReservaPorSecretaria(reservaId, request);
            return ResponseEntity.ok(Map.of("message", "Reserva modificada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping(value = "/reservas/hoy/exportar", produces = "text/csv")
    @Operation(summary = "Exportar reservas de hoy", description = "Genera y descarga un CSV con todas las reservas del día actual")
    public ResponseEntity<byte[]> exportarReservasDeHoyCsv() {
        String nombreArchivo = "reservas_hoy_" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(secretariaService.exportarReservasDeHoyCsv());
    }
}
