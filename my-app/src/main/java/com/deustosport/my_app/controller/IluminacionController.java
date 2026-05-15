package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.EstadoIluminacion;
import com.deustosport.my_app.service.IluminacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iluminacion")
@Tag(name = "Iluminación", description = "Gestión automática de la iluminación de pistas")
public class IluminacionController {

    private final IluminacionService iluminacionService;

    public IluminacionController(IluminacionService iluminacionService) {
        this.iluminacionService = iluminacionService;
    }

    @GetMapping
    @Operation(summary = "Estado de iluminación de todas las pistas",
               description = "Devuelve el estado actual (encendida/apagada) de la iluminación de cada pista activa.")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodosLosEstados() {
        return ResponseEntity.ok(iluminacionService.obtenerTodosLosEstados());
    }

    @GetMapping("/pista/{pistaId}")
    @Operation(summary = "Estado de iluminación de una pista concreta")
    public ResponseEntity<?> obtenerEstadoPista(@PathVariable("pistaId") Long pistaId) {
        try {
            EstadoIluminacion estado = iluminacionService.obtenerEstado(pistaId);
            return ResponseEntity.ok(Map.of(
                    "pistaId", estado.getPista().getId(),
                    "pistaNombre", estado.getPista().getNombre(),
                    "encendida", estado.isEncendida(),
                    "ultimoCambio", estado.getUltimoCambio() != null
                            ? estado.getUltimoCambio().toString() : "",
                    "motivo", estado.getMotivoUltimoCambio() != null
                            ? estado.getMotivoUltimoCambio() : "Sin actividad registrada"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
