package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.ReservaRequest;
import com.deustosport.my_app.dto.ReservaResponse;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Gestión de reservas de pistas deportivas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping("/crear")
    @Operation(summary = "Crear nueva reserva", description = "Permite a un usuario reservar una pista en una fecha y hora específica")
    public ResponseEntity<?> crearReserva(@RequestBody ReservaRequest request) {
        try {
            // Validar campos obligatorios
            if (request.getUsuarioId() == null || request.getPistaId() == null || 
                request.getFecha() == null || request.getHoraInicio() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Faltan datos obligatorios para la reserva"));
            }

            // Establecer duración por defecto si no viene
            Integer duracion = request.getDuracionMinutos() != null ? request.getDuracionMinutos() : 60;

            Reserva reserva = reservaService.crearReserva(
                    request.getUsuarioId(),
                    request.getPistaId(),
                    request.getFecha(),
                    request.getHoraInicio(),
                    duracion
            );

            // mapeamos a dto para no devolver un entity
            ReservaResponse reservaResponseDto = new ReservaResponse();
            reservaResponseDto.setId(reserva.getId());
            reservaResponseDto.setUsuarioId(reserva.getUsuario().getId());
            reservaResponseDto.setPistaId(reserva.getPista().getId());
            reservaResponseDto.setFecha(reserva.getFechaReserva());
            reservaResponseDto.setHoraInicio(reserva.getHoraInicio());
            reservaResponseDto.setHoraFin(reserva.getHoraFin());
            reservaResponseDto.setPrecioTotal(reserva.getPrecioTotal());
            reservaResponseDto.setEstado(reserva.getEstado());

            return ResponseEntity.ok(reservaResponseDto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Captura errores de negocio controlados (400 Bad Request)
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Captura errores inesperados (500 Internal Server Error)
            return ResponseEntity.internalServerError().body(Map.of("error", "Ha ocurrido un error inesperado al procesar la reserva."));
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener mis reservas", description = "Devuelve el historial de reservas de un usuario")
    public ResponseEntity<List<Reserva>> obtenerMisReservas(@PathVariable Long usuarioId) {
        List<Reserva> reservas = reservaService.obtenerMisReservas(usuarioId);
        return ResponseEntity.ok(reservas);
    }

    @PostMapping("/{reservaId}/cancelar")
    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva existente si cumple las condiciones")
    public ResponseEntity<?> cancelarReserva(
            @PathVariable Long reservaId, 
            @RequestParam Long usuarioId) {
        try {
            Reserva reservaCancelada = reservaService.cancelarReserva(reservaId, usuarioId);
            return ResponseEntity.ok(reservaCancelada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/disponibilidad")
    @Operation(summary = "Consultar disponibilidad", description = "Verifica si una pista está libre en el horario indicado")
    public ResponseEntity<?> consultarDisponibilidad(
            @RequestParam Long pistaId,
            @RequestParam String fecha,
            @RequestParam String horaInicio,
            @RequestParam(defaultValue = "60") Integer duracionMinutos) {
        try {
            boolean disponible = reservaService.consultarDisponibilidad(
                    pistaId,
                    LocalDate.parse(fecha),
                    LocalTime.parse(horaInicio),
                    LocalTime.parse(horaInicio).plusMinutes(duracionMinutos)
            );
            return ResponseEntity.ok(Map.of("disponible", disponible));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al consultar disponibilidad: " + e.getMessage()));
        }
    }
}