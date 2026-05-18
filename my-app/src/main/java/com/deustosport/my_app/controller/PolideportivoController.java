package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.service.PolideportivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import com.deustosport.my_app.dto.HorarioInstalacionRequest;

@RestController
@RequestMapping("/api/polideportivos")
@Tag(name = "Polideportivos", description = "Gestión de centros polideportivos")
public class PolideportivoController {

    private final PolideportivoService polideportivoService;

    public PolideportivoController(PolideportivoService polideportivoService) {
        this.polideportivoService = polideportivoService;
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

    @PutMapping("/{polideportivoId}/horario")
    @Operation(summary = "Actualizar horario del polideportivo")
    public ResponseEntity<?> actualizarHorario(
            @PathVariable Long polideportivoId,
            @Valid @RequestBody HorarioInstalacionRequest request) {
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
