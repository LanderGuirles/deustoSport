package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.HorarioInstalacionRequest;
import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.service.InstalacionService;
import com.deustosport.my_app.service.PistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/instalaciones")
@Tag(name = "Instalaciones", description = "Configuración de instalaciones deportivas")
public class InstalacionController {

    private final PistaService pistaService;
    private final InstalacionService instalacionService;

    public InstalacionController(InstalacionService instalacionService, PistaService pistaService) {
        this.instalacionService = instalacionService;
        this.pistaService = pistaService;
    }

    @GetMapping("/{instalacionId}/pistas")
    @io.swagger.v3.oas.annotations.Operation(summary = "Listar todas las pistas de una instalacion", description = "Devuelve todas las pistas activas de esa instalacion")
    public ResponseEntity<?> listarPistasDeUnaInstalacion(@PathVariable Long id) {
        try {
            List<Pista> pistas = pistaService.obtenerPistasPorInstalacionId(id);
            return ResponseEntity.ok(pistas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    

    @PutMapping("/{instalacionId}/horario-general")
    @Operation(summary = "Actualizar horario general", description = "Define la hora general de apertura y cierre del polideportivo")
    public ResponseEntity<?> actualizarHorarioGeneral(
            @PathVariable Long instalacionId,
            @Valid @RequestBody HorarioInstalacionRequest request) {
        try {
            Instalacion instalacion = instalacionService.actualizarHorarioGeneral(
                    instalacionId,
                    request.getHoraApertura(),
                    request.getHoraCierre());

            return ResponseEntity.ok(instalacion);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}