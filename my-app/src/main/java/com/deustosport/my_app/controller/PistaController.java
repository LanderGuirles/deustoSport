package com.deustosport.my_app.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.deustosport.my_app.dto.PistaDisponibleDTO;
import com.deustosport.my_app.dto.PistaRequest;
import com.deustosport.my_app.dto.PistaResponse;
import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.service.PistaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import java.time.LocalDate;
import java.time.LocalTime;



@RestController
@RequestMapping("/api/pistas")
@Tag(name = "Pistas", description = "Configuración de pistas deportivas")
public class PistaController {
    
    private final PistaService pistaService;

    public PistaController(PistaService pistaService){
        this.pistaService = pistaService;
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Listar todas las pistas", description = "Devuelve todas las pistas")
    public ResponseEntity<java.util.List<PistaResponse>> listarTodas() {
        return ResponseEntity.ok(pistaService.obtenerTodasLasPistas());
    }

    @GetMapping("/disponibles")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Buscar pistas disponibles por deporte, fecha y hora",
            description = "Devuelve todas las pistas activas del deporte indicado que no tienen reservas "
                    + "conflictivas en el horario solicitado y cuyo polideportivo está abierto en ese tramo.")
    public ResponseEntity<?> buscarDisponibles(
            @RequestParam TipoDeporte tipoDeporte,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam(defaultValue = "60") int duracionMinutos) {
        try {
            if (duracionMinutos <= 0 || duracionMinutos > 480) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("error", "La duración debe estar entre 1 y 480 minutos."));
            }
            LocalTime horaFin = horaInicio.plusMinutes(duracionMinutos);
            java.util.List<PistaDisponibleDTO> disponibles =
                    pistaService.buscarPistasDisponibles(tipoDeporte, fecha, horaInicio, horaFin);
            return ResponseEntity.ok(disponibles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{pistaId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener pista por ID", description = "Devuelve los detalles de una pista específica")
    public ResponseEntity<?> obtenerPorId(@PathVariable("pistaId") Long pistaId) {
        try {
            return ResponseEntity.ok(pistaService.obtenerPistaPorId(pistaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
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
            
            // Creamos un objeto Polideportivo solo con el ID
            Polideportivo poli = new Polideportivo();
            poli.setId(pistaDto.getPolideportivoId()); 
            nuevaPista.setPolideportivo(poli);

            // Llamamos al service
            Pista pistaGuardada = pistaService.registrarNuevaPista(nuevaPista);

            return new ResponseEntity<>(pistaGuardada, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear la pista: " + e.getMessage());
        }
    }

    @PutMapping("/{pistaId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Actualizar pistas", description = "Permite modificar los datos de las pistas")
    public ResponseEntity<?> actualizarPista(@PathVariable("pistaId") Long pistaId, @RequestBody PistaRequest pistaRequestDTO ) {
        
        try {
            PistaResponse pistaActualizada = pistaService.actualizarPista(pistaId, pistaRequestDTO);

            return ResponseEntity.ok(pistaActualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{pistaId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Eliminar pista", description = "Elimina la pista de la base de datos")
    public ResponseEntity<?> eliminarPista(@PathVariable("pistaId") Long pistaId) {
        try {
            pistaService.eliminarPista(pistaId);
            return ResponseEntity.ok("La pista con id: " + pistaId + " ha sido eliminada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping("/{pistaId}/bloquear")
    @io.swagger.v3.oas.annotations.Operation(summary = "Bloquear pista", description = "Bloquea la pista cambiando su estado")
    public ResponseEntity<?> bloquearPista(@PathVariable("pistaId") Long pistaId) {
        try {
            PistaResponse pistaBloqueada = pistaService.bloquearPista(pistaId);
            return ResponseEntity.ok(pistaBloqueada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
