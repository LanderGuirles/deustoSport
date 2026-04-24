package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.EnviarComunicadoRequest;
import com.deustosport.my_app.dto.NotificacionResponse;
import com.deustosport.my_app.dto.NotificarIncidenciaReservaRequest;
import com.deustosport.my_app.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Centro de notificaciones del usuario")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar notificaciones de un usuario")
    public ResponseEntity<List<NotificacionResponse>> listarPorUsuario(@PathVariable("usuarioId") Long usuarioId) {
        return ResponseEntity.ok(notificacionService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/contador-no-leidas")
    @Operation(summary = "Contar notificaciones no leidas")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(@PathVariable("usuarioId") Long usuarioId) {
        return ResponseEntity.ok(Map.of("noLeidas", notificacionService.contarNoLeidas(usuarioId)));
    }

    @PatchMapping("/{notificacionId}/leer")
    @Operation(summary = "Marcar una notificacion como leida")
    public ResponseEntity<?> marcarComoLeida(@PathVariable("notificacionId") Long notificacionId,
                                             @RequestParam("usuarioId") Long usuarioId) {
        try {
            notificacionService.marcarComoLeida(notificacionId, usuarioId);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/usuario/{usuarioId}/leer-todas")
    @Operation(summary = "Marcar todas las notificaciones como leidas")
    public ResponseEntity<Map<String, Integer>> marcarTodasComoLeidas(@PathVariable("usuarioId") Long usuarioId) {
        int actualizadas = notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok(Map.of("actualizadas", actualizadas));
    }

    @PostMapping("/comunicado")
    @Operation(summary = "Enviar comunicado administrativo")
    public ResponseEntity<?> enviarComunicado(@RequestBody EnviarComunicadoRequest request) {
        try {
            int total = notificacionService.enviarComunicado(
                    request.getTitulo(),
                    request.getMensaje(),
                    request.getDestinatarios(),
                    request.isEnviarEmail());
            return ResponseEntity.ok(Map.of("enviadas", total));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/incidencia/reserva/{reservaId}")
    @Operation(summary = "Notificar incidencia de una reserva")
    public ResponseEntity<?> notificarIncidenciaReserva(
            @PathVariable("reservaId") Long reservaId,
            @RequestBody(required = false) NotificarIncidenciaReservaRequest request) {
        try {
            NotificarIncidenciaReservaRequest body = request != null ? request : new NotificarIncidenciaReservaRequest();
            NotificacionResponse notificacion = notificacionService.notificarIncidenciaReserva(
                    reservaId,
                    body.getTitulo(),
                    body.getMensaje(),
                    body.isEnviarEmail());
            return ResponseEntity.ok(notificacion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
