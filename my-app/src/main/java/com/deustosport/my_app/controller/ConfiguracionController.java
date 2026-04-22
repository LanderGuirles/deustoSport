package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.Configuracion;
import com.deustosport.my_app.service.ConfiguracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@Tag(name = "Configuración", description = "Configuración global del sistema")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping("/descuento-socio")
    @Operation(summary = "Obtener descuento de socio",
               description = "Devuelve el porcentaje de descuento configurado para socios.")
    public ResponseEntity<?> obtenerDescuentoSocio() {
        BigDecimal porcentaje = configuracionService.obtenerDescuentoSocioPorcentaje();
        return ResponseEntity.ok(Map.of(
                "porcentaje", porcentaje,
                "descripcion", "Porcentaje de descuento aplicado a socios en reservas"
        ));
    }

    @PutMapping("/descuento-socio")
    @Operation(summary = "Actualizar descuento de socio",
               description = "Establece el porcentaje de descuento para socios (0-100).")
    public ResponseEntity<?> actualizarDescuentoSocio(@RequestBody Map<String, Object> body) {
        try {
            Object val = body.get("porcentaje");
            if (val == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'porcentaje' es obligatorio."));
            }
            BigDecimal porcentaje = new BigDecimal(val.toString());
            Configuracion config = configuracionService.actualizarDescuentoSocio(porcentaje);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Descuento de socio actualizado correctamente.",
                    "porcentaje", new BigDecimal(config.getValor())
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error inesperado al actualizar la configuración."));
        }
    }
}
