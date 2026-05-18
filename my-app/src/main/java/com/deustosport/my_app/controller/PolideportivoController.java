package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Ayuntamiento;
import com.deustosport.my_app.service.PolideportivoService;
import com.deustosport.my_app.repository.AyuntamientoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import com.deustosport.my_app.dto.HorarioPolideportivoRequest;

@RestController
@RequestMapping("/api/polideportivos")
@Tag(name = "Polideportivos", description = "Gestión de centros polideportivos")
public class PolideportivoController {

    private final PolideportivoService polideportivoService;
    private final AyuntamientoRepository ayuntamientoRepository;

    public PolideportivoController(PolideportivoService polideportivoService, AyuntamientoRepository ayuntamientoRepository) {
        this.polideportivoService = polideportivoService;
        this.ayuntamientoRepository = ayuntamientoRepository;
    }

    @GetMapping
    @Operation(summary = "Listar polideportivos", description = "Devuelve todos los polideportivos registrados")
    public ResponseEntity<List<Polideportivo>> listarPolideportivos() {
        return ResponseEntity.ok(polideportivoService.obtenerTodos());
    }

    @GetMapping("/ayuntamiento/{ayuntamientoId}")
    @Operation(summary = "Listar polideportivos por ayuntamiento")
    public ResponseEntity<List<Polideportivo>> listarPorAyuntamiento(@PathVariable Long ayuntamientoId) {
        return ResponseEntity.ok(polideportivoService.obtenerPorAyuntamiento(ayuntamientoId));
    }

    @PostMapping
    @Operation(summary = "Crear polideportivo")
    public ResponseEntity<?> crearPolideportivo(@RequestBody Map<String, Object> payload) {
        try {
            Polideportivo poli = new Polideportivo();
            poli.setNombre((String) payload.get("nombre"));
            poli.setDireccion((String) payload.get("direccion"));
            
            Object aytoIdObj = payload.get("ayuntamientoId");
            if (aytoIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "El ayuntamientoId es obligatorio"));
            }

            Long ayuntamientoId;
            if (aytoIdObj instanceof Number) {
                ayuntamientoId = ((Number) aytoIdObj).longValue();
            } else {
                ayuntamientoId = Long.valueOf(aytoIdObj.toString());
            }

            Ayuntamiento ayu = ayuntamientoRepository.findById(ayuntamientoId)
                    .orElseThrow(() -> new IllegalArgumentException("Ayuntamiento no encontrado con ID: " + ayuntamientoId));
            poli.setAyuntamiento(ayu);

            return ResponseEntity.ok(polideportivoService.crearPolideportivo(poli));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{polideportivoId}")
    @Operation(summary = "Actualizar polideportivo")
    public ResponseEntity<?> actualizarPolideportivo(
            @PathVariable Long polideportivoId,
            @RequestBody Map<String, String> payload) {
        try {
            Polideportivo poli = polideportivoService.actualizarPolideportivo(
                    polideportivoId,
                    payload.get("nombre"),
                    payload.get("direccion"));
            return ResponseEntity.ok(poli);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{polideportivoId}/pistas")
    @Operation(summary = "Listar pistas de un polideportivo")
    public ResponseEntity<List<Pista>> listarPistas(@PathVariable Long polideportivoId) {
        return ResponseEntity.ok(polideportivoService.obtenerPistasByPolideportivo(polideportivoId));
    }

    @PutMapping("/{polideportivoId}/horario")
    @Operation(summary = "Actualizar horario del polideportivo")
    public ResponseEntity<?> actualizarHorario(
            @PathVariable Long polideportivoId,
            @Valid @RequestBody HorarioPolideportivoRequest request) {
        try {
            Polideportivo polideportivo = polideportivoService.actualizarHorarioGeneral(
                    polideportivoId,
                    request.getHoraApertura(),
                    request.getHoraCierre());
            return ResponseEntity.ok(polideportivo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
