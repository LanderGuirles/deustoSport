package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.PagoReservaRequest;
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
    @Operation(summary = "Crear nueva reserva")
    public ResponseEntity<?> crearReserva(@RequestBody ReservaRequest request) {
        try {
            if (request.getUsuarioId() == null || request.getPistaId() == null
                    || request.getFecha() == null || request.getHoraInicio() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Faltan datos obligatorios para la reserva"));
            }
            Integer duracion = request.getDuracionMinutos() != null
                    ? request.getDuracionMinutos() : 60;

            Reserva reserva = reservaService.crearReserva(
                    request.getUsuarioId(), request.getPistaId(),
                    request.getFecha(), request.getHoraInicio(), duracion);

            ReservaResponse dto = new ReservaResponse();
            dto.setId(reserva.getId());
            dto.setUsuarioId(reserva.getUsuario().getId());
            dto.setPistaId(reserva.getPista().getId());
            dto.setPistaNombre(reserva.getPista().getNombre());
            dto.setTipoDeporte(reserva.getPista().getTipoDeporte());
            dto.setFechaReserva(reserva.getFechaReserva());
            dto.setHoraInicio(reserva.getHoraInicio());
            dto.setHoraFin(reserva.getHoraFin());
            dto.setPrecioTotal(reserva.getPrecioTotal());
            dto.setEstado(reserva.getEstado());
            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error inesperado al procesar la reserva."));
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener reservas de un usuario")
    public ResponseEntity<List<ReservaResponse>> obtenerMisReservas(
            @PathVariable("usuarioId") Long usuarioId) {
        return ResponseEntity.ok(reservaService.obtenerMisReservas(usuarioId));
    }

    @PostMapping("/{reservaId}/cancelar")
    @Operation(summary = "Cancelar reserva")
    public ResponseEntity<?> cancelarReserva(
            @PathVariable("reservaId") Long reservaId,
            @RequestParam("usuarioId") Long usuarioId) {
        try {
            Reserva r = reservaService.cancelarReserva(reservaId, usuarioId);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{reservaId}/pagar")
    @Operation(summary = "Pagar reserva",
               description = "Métodos: TARJETA, BIZUM, TRANSFERENCIA (requiere IBAN).")
    public ResponseEntity<?> pagarReserva(
            @PathVariable("reservaId") Long reservaId,
            @RequestBody PagoReservaRequest request) {
        try {
            if (request.getUsuarioId() == null || request.getMetodoPago() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Debes indicar usuarioId y metodoPago."));
            }
            Reserva pagada = reservaService.pagarReserva(
                    reservaId, request.getUsuarioId(), request.getMetodoPago(),
                    request.getNumeroTarjeta(), request.getTitularTarjeta(),
                    request.getCaducidadTarjeta(), request.getCvv(),
                    request.getTelefonoBizum(), request.getIban());

            ReservaResponse resp = new ReservaResponse();
            resp.setId(pagada.getId());
            resp.setUsuarioId(pagada.getUsuario().getId());
            resp.setPistaId(pagada.getPista().getId());
            resp.setPistaNombre(pagada.getPista().getNombre());
            resp.setTipoDeporte(pagada.getPista().getTipoDeporte());
            resp.setFechaReserva(pagada.getFechaReserva());
            resp.setHoraInicio(pagada.getHoraInicio());
            resp.setHoraFin(pagada.getHoraFin());
            resp.setPrecioTotal(pagada.getPrecioTotal());
            resp.setEstado(pagada.getEstado());
            resp.setMetodoPago(pagada.getMetodoPago());
            resp.setReferenciaPago(pagada.getReferenciaPago());
            resp.setFechaPago(pagada.getFechaPago());
            return ResponseEntity.ok(resp);

        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error inesperado al procesar el pago."));
        }
    }

    @GetMapping("/disponibilidad")
    @Operation(summary = "Consultar disponibilidad de franja horaria")
    public ResponseEntity<?> consultarDisponibilidad(
            @RequestParam("pistaId") Long pistaId,
            @RequestParam("fecha") String fecha,
            @RequestParam("horaInicio") String horaInicio,
            @RequestParam(name = "duracionMinutos", defaultValue = "60") Integer duracionMinutos) {
        try {
            boolean disponible = reservaService.consultarDisponibilidad(
                    pistaId,
                    LocalDate.parse(fecha),
                    LocalTime.parse(horaInicio),
                    LocalTime.parse(horaInicio).plusMinutes(duracionMinutos));
            return ResponseEntity.ok(Map.of("disponible", disponible));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al consultar disponibilidad: " + e.getMessage()));
        }
    }

    @GetMapping("/pista/{pistaId}/semana")
    @Operation(summary = "Slots ocupados de una pista para los próximos 7 días",
               description = "Devuelve lista de {fecha, horaInicio, horaFin} con reservas activas.")
    public ResponseEntity<?> obtenerOcupadosSemana(
            @PathVariable("pistaId") Long pistaId,
            @RequestParam(name = "fecha", required = false) String fecha) {
        try {
            LocalDate inicio = (fecha != null && !fecha.isBlank())
                    ? LocalDate.parse(fecha)
                    : LocalDate.now();
            LocalDate fin = inicio.plusDays(6);
            return ResponseEntity.ok(
                    reservaService.obtenerOcupadosPorPistaYRango(pistaId, inicio, fin));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al obtener disponibilidad: " + e.getMessage()));
        }
    }
}