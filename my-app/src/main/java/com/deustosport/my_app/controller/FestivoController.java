package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.FestivoRequest;
import com.deustosport.my_app.dto.FestivoResponse;
import com.deustosport.my_app.service.FestivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/festivos")
@Tag(name = "Festivos", description = "Gestión de cierres por festividad")
public class FestivoController {

    private final FestivoService festivoService;

    public FestivoController(FestivoService festivoService) {
        this.festivoService = festivoService;
    }

    @PostMapping
    @Operation(summary = "Programar un nuevo cierre por festivo")
    public ResponseEntity<FestivoResponse> programarFestivo(@RequestBody FestivoRequest request) {
        return ResponseEntity.ok(festivoService.programarFestivo(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos los festivos programados")
    public ResponseEntity<List<FestivoResponse>> listarFestivos() {
        return ResponseEntity.ok(festivoService.listarFestivos());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un festivo programado")
    public ResponseEntity<Void> eliminarFestivo(@PathVariable Long id) {
        festivoService.eliminarFestivo(id);
        return ResponseEntity.noContent().build();
    }
}
