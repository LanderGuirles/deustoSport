package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.EstadisticasDTO;
import com.deustosport.my_app.service.EstadisticasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estadisticas")
@Tag(name = "Estadísticas", description = "Reportes y estadísticas del sistema")
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    public EstadisticasController(EstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas resumidas del sistema")
    public ResponseEntity<EstadisticasDTO> obtenerEstadisticas() {
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticas());
    }

    @GetMapping("/reservas-por-instalacion")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reservas agrupadas por instalación")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasReservasPorInstalacion() {
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticasReservasPorInstalacion());
    }

    @GetMapping("/reservas-por-dia")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reservas por día en los últimos N días")
    public ResponseEntity<List<Object[]>> obtenerEstadisticasReservasPorDia(
            @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticasReservasPorDia(dias));
    }

    /**
     * Obtener estadísticas generales
     */
    @GetMapping("/generales")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas generales del sistema")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasGenerales() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasGenerales();
        return ResponseEntity.ok(stats);
    }

    /**
     * Estadísticas de reservas por mes
     */
    @GetMapping("/reservas/mes/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de reservas por mes")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasReservasPorMes(@PathVariable int year) {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasReservasPorMes(year);
        return ResponseEntity.ok(stats);
    }

    /**
     * Estadísticas de uso de pistas
     */
    @GetMapping("/pistas/uso")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de uso de pistas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasUsoPistas() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasUsoPistas();
        return ResponseEntity.ok(stats);
    }

    /**
     * Top usuarios por reservas
     */
    @GetMapping("/usuarios/top")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Top usuarios por número de reservas")
    public ResponseEntity<Map<String, Object>> obtenerTopUsuariosReservas(
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> stats = estadisticasService.obtenerTopUsuariosReservas(limit);
        return ResponseEntity.ok(stats);
    }

    /**
     * Estadísticas de pagos
     */
    @GetMapping("/pagos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de métodos de pago")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPagos() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasPagos();
        return ResponseEntity.ok(stats);
    }

    /**
     * Estadísticas de ocupación semanal
     */
    @GetMapping("/ocupacion/semanal")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de ocupación por día de la semana")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasOcupacionSemanal() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasOcupacionSemanal();
        return ResponseEntity.ok(stats);
    }

    /**
     * Estadísticas por tipo de deporte
     */
    @GetMapping("/deportes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas por tipo de deporte")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorDeporte() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasPorDeporte();
        return ResponseEntity.ok(stats);
    }

    /**
     * Reporte de ingresos por rango de fechas
     */
    @GetMapping("/ingresos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reporte de ingresos por rango de fechas")
    public ResponseEntity<Map<String, Object>> obtenerReporteIngresos(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {
        Map<String, Object> reporte = estadisticasService.obtenerReporteIngresos(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    /**
     * Dashboard resumen para admin
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dashboard con métricas principales")
    public ResponseEntity<Map<String, Object>> obtenerDashboard() {
        Map<String, Object> dashboard = estadisticasService.obtenerEstadisticasGenerales();

        // Añadir algunas métricas adicionales
        dashboard.put("titulo", "Dashboard DeustoSport");
        dashboard.put("fechaGeneracion", LocalDate.now());

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Estadísticas de usuarios
     */
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de usuarios")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasUsuarios() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasGenerales();

        // Filtrar solo estadísticas de usuarios
        Map<String, Object> userStats = Map.of(
                "totalUsuarios", stats.get("totalUsuarios"),
                "usuariosActivos", stats.get("usuariosActivos"),
                "usuariosSocios", stats.get("usuariosSocios"),
                "porcentajeSocios", stats.containsKey("totalUsuarios") && (Long)stats.get("totalUsuarios") > 0 ?
                        (Long)stats.get("usuariosSocios") * 100.0 / (Long)stats.get("totalUsuarios") : 0.0
        );

        return ResponseEntity.ok(userStats);
    }

    /**
     * Estadísticas de reservas
     */
    @GetMapping("/reservas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de reservas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasReservas() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasGenerales();

        // Filtrar solo estadísticas de reservas
        Map<String, Object> reservaStats = Map.of(
                "totalReservas", stats.get("totalReservas"),
                "reservasConfirmadas", stats.get("reservasConfirmadas"),
                "reservasPendientes", stats.get("reservasPendientes"),
                "reservasCanceladas", stats.get("reservasCanceladas"),
                "tasaConfirmacion", stats.containsKey("totalReservas") && (Long)stats.get("totalReservas") > 0 ?
                        (Long)stats.get("reservasConfirmadas") * 100.0 / (Long)stats.get("totalReservas") : 0.0
        );

        return ResponseEntity.ok(reservaStats);
    }

    /**
     * Estadísticas financieras
     */
    @GetMapping("/financieras")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas financieras")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasFinancieras() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasGenerales();

        // Filtrar solo estadísticas financieras
        Map<String, Object> finStats = Map.of(
                "ingresosTotales", stats.get("ingresosTotales"),
                "ingresosMesActual", stats.get("ingresosMesActual")
        );

        return ResponseEntity.ok(finStats);
    }
}