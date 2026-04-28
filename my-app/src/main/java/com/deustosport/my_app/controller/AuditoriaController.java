package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.Auditoria;
import com.deustosport.my_app.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auditoria")
@Tag(name = "Auditoría", description = "Sistema de auditoría y logs del sistema")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /**
     * Obtener todas las acciones de auditoría
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todas las acciones de auditoría")
    public ResponseEntity<List<Auditoria>> obtenerTodasLasAcciones(
            @RequestParam(defaultValue = "100") int limit) {
        List<Auditoria> acciones = auditoriaService.obtenerAccionesRecientes(limit);
        return ResponseEntity.ok(acciones);
    }

    /**
     * Obtener acciones por usuario
     */
    @GetMapping("/usuario/{usuario}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener acciones de auditoría por usuario")
    public ResponseEntity<List<Auditoria>> obtenerAccionesPorUsuario(@PathVariable String usuario) {
        List<Auditoria> acciones = auditoriaService.obtenerAccionesPorUsuario(usuario);
        return ResponseEntity.ok(acciones);
    }

    /**
     * Obtener acciones por entidad
     */
    @GetMapping("/entidad/{entidad}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener acciones de auditoría por entidad")
    public ResponseEntity<List<Auditoria>> obtenerAccionesPorEntidad(@PathVariable String entidad) {
        List<Auditoria> acciones = auditoriaService.obtenerAccionesPorEntidad(entidad);
        return ResponseEntity.ok(acciones);
    }

    /**
     * Obtener acciones por rango de fechas
     */
    @GetMapping("/fecha")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener acciones de auditoría por rango de fechas")
    public ResponseEntity<List<Auditoria>> obtenerAccionesPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<Auditoria> acciones = auditoriaService.obtenerAccionesPorFecha(fechaInicio, fechaFin);
        return ResponseEntity.ok(acciones);
    }

    /**
     * Obtener estadísticas de auditoría
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener estadísticas de auditoría")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasAuditoria() {
        Map<String, Object> stats = Map.of(
                "totalAcciones", auditoriaService.contarAccionesPorTipo("TODAS"), // Placeholder
                "logins", auditoriaService.contarAccionesPorTipo("LOGIN"),
                "reservasCreadas", auditoriaService.contarAccionesPorTipo("CREAR"),
                "pagosRealizados", auditoriaService.contarAccionesPorTipo("PAGAR"),
                "usuariosMasActivos", auditoriaService.obtenerUsuariosMasActivos(5)
        );
        return ResponseEntity.ok(stats);
    }

    /**
     * Buscar en detalles de auditoría
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar en los detalles de auditoría")
    public ResponseEntity<List<Auditoria>> buscarEnAuditoria(@RequestParam String busqueda) {
        // Nota: Este método necesitaría ser implementado en el repository
        // Por ahora devolvemos lista vacía
        return ResponseEntity.ok(List.of());
    }

    /**
     * Limpiar registros antiguos
     */
    @DeleteMapping("/limpiar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Limpiar registros de auditoría antiguos")
    public ResponseEntity<?> limpiarRegistrosAntiguos(@RequestParam(defaultValue = "90") int diasMantener) {
        auditoriaService.limpiarRegistrosAntiguos(diasMantener);
        return ResponseEntity.ok(Map.of("mensaje", "Registros antiguos eliminados"));
    }

    /**
     * Obtener entidades auditadas
     */
    @GetMapping("/entidades")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener lista de entidades auditadas")
    public ResponseEntity<List<String>> obtenerEntidadesAuditadas() {
        // Este método necesitaría ser implementado en el repository
        return ResponseEntity.ok(List.of("Usuario", "Reserva", "Pago", "Notificacion"));
    }

    /**
     * Obtener tipos de acciones
     */
    @GetMapping("/acciones")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener lista de tipos de acciones")
    public ResponseEntity<List<String>> obtenerTiposAcciones() {
        return ResponseEntity.ok(List.of(
                "LOGIN", "LOGOUT", "CREAR", "MODIFICAR", "ELIMINAR",
                "PAGAR", "CANCELAR", "ENVIAR_EMAIL", "CAMBIAR_PASSWORD"
        ));
    }

    /**
     * Obtener resumen de actividad reciente
     */
    @GetMapping("/resumen")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener resumen de actividad reciente")
    public ResponseEntity<Map<String, Object>> obtenerResumenActividad() {
        LocalDateTime hace24Horas = LocalDateTime.now().minusHours(24);
        LocalDateTime hace7Dias = LocalDateTime.now().minusDays(7);

        Map<String, Object> resumen = Map.of(
                "accionesUltimas24Horas", auditoriaService.contarAccionesPorTipo("TODAS"), // Placeholder
                "accionesUltimos7Dias", auditoriaService.contarAccionesPorTipo("TODAS"), // Placeholder
                "usuariosActivosHoy", 0, // Placeholder
                "reservasCreadasHoy", 0 // Placeholder
        );

        return ResponseEntity.ok(resumen);
    }

    /**
     * Exportar auditoría (placeholder)
     */
    @GetMapping("/exportar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exportar registros de auditoría")
    public ResponseEntity<?> exportarAuditoria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "CSV") String formato) {

        // Implementación placeholder
        return ResponseEntity.ok(Map.of(
                "mensaje", "Exportación no implementada aún",
                "formato", formato,
                "fechaInicio", fechaInicio,
                "fechaFin", fechaFin
        ));
    }
}