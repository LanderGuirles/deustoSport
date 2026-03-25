package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.Tarifa;
import com.deustosport.my_app.service.TarifaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tarifas")
@Tag(name = "Tarifas", description = "Gestión de tarifas por horario y tipo de deporte")
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las tarifas")
    public ResponseEntity<List<Tarifa>> listarTodas() {
        return ResponseEntity.ok(tarifaService.obtenerTodas());
    }

    @GetMapping("/activas")
    @Operation(summary = "Listar tarifas activas")
    public ResponseEntity<List<Tarifa>> listarActivas() {
        return ResponseEntity.ok(tarifaService.obtenerActivas());
    }

    @GetMapping("/pista/{pistaId}")
    @Operation(summary = "Tarifas para una pista concreta",
               description = "Devuelve las tarifas activas del tipo de deporte de esa pista.")
    public ResponseEntity<?> listarPorPista(@PathVariable("pistaId") Long pistaId) {
        try {
            return ResponseEntity.ok(tarifaService.obtenerPorPistaId(pistaId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Crear tarifa")
    public ResponseEntity<?> crearTarifa(@RequestBody Tarifa tarifa) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(tarifaService.crearTarifa(tarifa));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarifa")
    public ResponseEntity<?> actualizarTarifa(@PathVariable("id") Long id,
                                              @RequestBody Tarifa tarifa) {
        try {
            return ResponseEntity.ok(tarifaService.actualizarTarifa(id, tarifa));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar tarifa")
    public ResponseEntity<?> desactivarTarifa(@PathVariable("id") Long id) {
        try {
            tarifaService.desactivarTarifa(id);
            return ResponseEntity.ok(Map.of("mensaje", "Tarifa desactivada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}