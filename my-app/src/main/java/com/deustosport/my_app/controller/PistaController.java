package com.deustosport.my_app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.deustosport.my_app.dto.PistaRequest;
import com.deustosport.my_app.dto.PistaResponse;
import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.service.PistaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;



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
    public ResponseEntity<java.util.List<PistaResponse>> listarTodas() {
        return ResponseEntity.ok(pistaService.obtenerTodasLasPistas());
    }


    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Añadir nueva pista", description = "Permite la creacion de nuevas pistas")
    public ResponseEntity<?> crearPista(@RequestBody PistaRequest pistaDto){

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

    @PutMapping("/{pistaId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Actualizar pistas", description = "Permite modificar los datos de las pistas")
    public ResponseEntity<?> actualizarPista(@PathVariable Long pistaId, @RequestBody PistaRequest pistaRequestDTO ) {
        
        try {
            PistaResponse pistaActualizada = pistaService.actualizarPista(pistaId, pistaRequestDTO);

            return ResponseEntity.ok(pistaActualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{pistaId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Eliminar pista", description = "Elimina la pista de la base de datos")
    public ResponseEntity<?> eliminarPista(@PathVariable Long pistaId) {
        try {
            pistaService.eliminarPista(pistaId);
            return ResponseEntity.ok("La pista con id: " + pistaId + " ha sido eliminada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
