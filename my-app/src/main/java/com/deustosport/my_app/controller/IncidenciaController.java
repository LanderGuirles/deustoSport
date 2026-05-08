package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.IncidenciaResponse;
import com.deustosport.my_app.dto.RegistrarIncidenciaRequest;
import com.deustosport.my_app.enums.EstadoIncidencia;
import com.deustosport.my_app.service.IncidenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidencias")
@Tag(name = "Incidencias", description = "Registro y seguimiento de incidencias de mantenimiento")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @PostMapping
    @Operation(summary = "Registrar una incidencia de mantenimiento")
    public ResponseEntity<?> registrarIncidencia(@RequestBody RegistrarIncidenciaRequest request) {
        try {
            IncidenciaResponse response = incidenciaService.registrarIncidencia(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Listar incidencias registradas")
    public ResponseEntity<List<IncidenciaResponse>> listarIncidencias(
            @RequestParam(value = "estado", required = false) EstadoIncidencia estado) {
        return ResponseEntity.ok(incidenciaService.listarIncidencias(estado));
    }
}