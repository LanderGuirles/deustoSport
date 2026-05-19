package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.EstadisticasDTO;
import com.deustosport.my_app.dto.ReporteUsoPistaDTO;
import com.deustosport.my_app.dto.ReporteUsoPistasDTO;
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

    @GetMapping("/reservas-por-polideportivo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reservas agrupadas por polideportivo")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasReservasPorPolideportivo() {
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticasReservasPorPolideportivo());
    }

    @GetMapping("/reservas-por-dia")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reservas por día en los últimos N días")
    public ResponseEntity<List<Object[]>> obtenerEstadisticasReservasPorDia(
            @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticasReservasPorDia(dias));
    }

    @GetMapping("/generales")
    @Operation(summary = "Estadísticas generales del sistema")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasGenerales() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasGenerales();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reservas/mes/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de reservas por mes")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasReservasPorMes(@PathVariable("year") int year) {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasReservasPorMes(year);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/pistas/uso")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de uso de pistas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasUsoPistas() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasUsoPistas();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/usuarios/top")
    @Operation(summary = "Top usuarios por número de reservas")
    public ResponseEntity<Map<String, Object>> obtenerTopUsuariosReservas(
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> stats = estadisticasService.obtenerTopUsuariosReservas(limit);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/pagos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de métodos de pago")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPagos() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasPagos();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/ocupacion/semanal")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de ocupación por día de la semana")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasOcupacionSemanal() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasOcupacionSemanal();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/deportes")
    @Operation(summary = "Estadísticas por tipo de deporte")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorDeporte() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasPorDeporte();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/ingresos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reporte de ingresos por rango de fechas")
    public ResponseEntity<Map<String, Object>> obtenerReporteIngresos(
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("fechaFin") LocalDate fechaFin) {
        Map<String, Object> reporte = estadisticasService.obtenerReporteIngresos(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dashboard con métricas principales")
    public ResponseEntity<Map<String, Object>> obtenerDashboard() {
        Map<String, Object> dashboard = estadisticasService.obtenerEstadisticasGenerales();
        dashboard.put("titulo", "Dashboard DeustoSport");
        dashboard.put("fechaGeneracion", LocalDate.now());
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de usuarios")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasUsuarios() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasGenerales();
        Map<String, Object> userStats = Map.of(
                "totalUsuarios", stats.get("totalUsuarios"),
                "usuariosActivos", stats.get("usuariosActivos"),
                "usuariosSocios", stats.get("usuariosSocios"),
                "porcentajeSocios", stats.containsKey("totalUsuarios") && (Long)stats.get("totalUsuarios") > 0 ?
                        (Long)stats.get("usuariosSocios") * 100.0 / (Long)stats.get("totalUsuarios") : 0.0
        );
        return ResponseEntity.ok(userStats);
    }

    @GetMapping("/reservas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas de reservas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasReservas() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasGenerales();
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

    @GetMapping("/financieras")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estadísticas financieras")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasFinancieras() {
        Map<String, Object> stats = estadisticasService.obtenerEstadisticasGenerales();
        Map<String, Object> finStats = Map.of(
                "ingresosTotales", stats.get("ingresosTotales"),
                "ingresosMesActual", stats.get("ingresosMesActual")
        );
        return ResponseEntity.ok(finStats);
    }

    @GetMapping("/reportes/pista/{pistaId}")
    @PreAuthorize("hasRole('COORDINADOR') or hasRole('ADMIN')")
    @Operation(summary = "Reporte de uso y rentabilidad de una pista")
    public ResponseEntity<ReporteUsoPistaDTO> obtenerReporteUsoPista(
            @PathVariable("pistaId") Long pistaId,
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("fechaFin") LocalDate fechaFin) {
        ReporteUsoPistaDTO reporte = estadisticasService.generarReporteUsoPista(pistaId, fechaInicio, fechaFin);
        if (reporte == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/reportes/uso-pistas")
    @PreAuthorize("hasRole('COORDINADOR') or hasRole('ADMIN')")
    @Operation(summary = "Reporte consolidado de uso y rentabilidad de todas las pistas")
    public ResponseEntity<ReporteUsoPistasDTO> obtenerReporteUsoPistas(
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("fechaFin") LocalDate fechaFin) {
        ReporteUsoPistasDTO reporte = estadisticasService.generarReporteUsoPistas(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/reportes/uso-pistas/semana-actual")
    @PreAuthorize("hasRole('COORDINADOR') or hasRole('ADMIN')")
    @Operation(summary = "Reporte de uso y rentabilidad para la semana actual")
    public ResponseEntity<ReporteUsoPistasDTO> obtenerReporteSemanaActual() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1); // Lunes
        LocalDate finSemana = inicioSemana.plusDays(6); // Domingo
        
        ReporteUsoPistasDTO reporte = estadisticasService.generarReporteUsoPistas(inicioSemana, finSemana);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/reportes/uso-pistas/mes-actual")
    @PreAuthorize("hasRole('COORDINADOR') or hasRole('ADMIN')")
    @Operation(summary = "Reporte de uso y rentabilidad para el mes actual")
    public ResponseEntity<ReporteUsoPistasDTO> obtenerReporteMesActual() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.plusMonths(1).withDayOfMonth(1).minusDays(1);
        
        ReporteUsoPistasDTO reporte = estadisticasService.generarReporteUsoPistas(inicioMes, finMes);
        return ResponseEntity.ok(reporte);
    }
}
