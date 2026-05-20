package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.Bono;
import com.deustosport.my_app.service.BonoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bonos")
@Tag(name = "Bonos", description = "Gestión de bonos de sesiones")
public class BonoController {

    @Autowired
    private BonoService bonoService;

    @GetMapping
    @Operation(summary = "Listar bonos activos", description = "Devuelve todos los bonos que están marcados como activos")
    public ResponseEntity<List<Bono>> listarBonos() {
        return ResponseEntity.ok(bonoService.obtenerBonosActivos());
    }
}
