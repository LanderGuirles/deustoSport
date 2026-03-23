package com.deustosport.my_app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deustosport.my_app.dto.PistaRequestDTO;
import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.service.PistaService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;



@RestController
@RequestMapping("/api/pistas")
@Tag(name = "Pistas", description = "Configuración de pistas deportivas")
public class PistaController {
    
    private final PistaService pistaService;

    public PistaController(PistaService pistaService){
        this.pistaService = pistaService;
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Listar todas las pistas", description = "Devuelve todas las pistas activas")
    public ResponseEntity<java.util.List<Pista>> listarTodas() {
        return ResponseEntity.ok(pistaService.obtenerTodasLasPistas());
    }

    

    @PostMapping
    public ResponseEntity<?> crearPista(@RequestBody PistaRequestDTO pistaDto){

        try {
            // Convertimos el DTO a Entidad antes de pasarla al servicio
            Pista nuevaPista = new Pista();
            nuevaPista.setNombre(pistaDto.getNombre());
            nuevaPista.setTipoDeporte(pistaDto.getTipoDeporte());
            nuevaPista.setMaxJugadores(pistaDto.getMaxJugadores());
            
            // Creamos un objeto Instalacion solo con el ID
            Instalacion inst = new Instalacion();
            inst.setId(pistaDto.getInstalacionId()); 
            nuevaPista.setInstalacion(inst);

            // Llamamos al service
            Pista pistaGuardada = pistaService.registrarNuevaPista(nuevaPista);

            return new ResponseEntity<>(pistaGuardada, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear la pista: " + e.getMessage());
        }
    }
}
