package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.HorarioInstalacionRequest;
import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.service.InstalacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instalaciones")
@Tag(name = "Instalaciones", description = "Configuración de instalaciones deportivas")
public class InstalacionController {

    private final InstalacionService instalacionService;

    public InstalacionController(InstalacionService instalacionService) {
        this.instalacionService = instalacionService;
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

            // ¡Magia! Al devolver el objeto, Jackson usa los @JsonFormat de tu entidad
            return ResponseEntity.ok(instalacion);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}