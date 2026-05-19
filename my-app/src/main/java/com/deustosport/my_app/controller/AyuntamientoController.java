package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.Ayuntamiento;
import com.deustosport.my_app.repository.AyuntamientoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ayuntamientos")
@Tag(name = "Ayuntamientos", description = "Gestión de datos de ayuntamientos")
public class AyuntamientoController {

    private final AyuntamientoRepository ayuntamientoRepository;

    public AyuntamientoController(AyuntamientoRepository ayuntamientoRepository) {
        this.ayuntamientoRepository = ayuntamientoRepository;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener datos de un ayuntamiento")
    public ResponseEntity<Ayuntamiento> obtenerAyuntamiento(@PathVariable("id") Long id) {
        return ayuntamientoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un ayuntamiento")
    public ResponseEntity<?> actualizarAyuntamiento(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        return ayuntamientoRepository.findById(id)
                .map(ayu -> {
                    ayu.setNombre(payload.get("nombre"));
                    ayu.setCif(payload.get("cif"));
                    return ResponseEntity.ok(ayuntamientoRepository.save(ayu));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
