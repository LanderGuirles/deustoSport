package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.service.AbonoConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/abonos")
@Tag(name = "Abonos administrador", description = "Configuración de la creacion/modificacion de los abonos")
public class AbonoConfigController {

    @Autowired
    private AbonoConfigService adminService;

    @Autowired
    private com.deustosport.my_app.repository.AyuntamientoRepository ayuntamientoRepository;

    @PostMapping("/planes")
    @io.swagger.v3.oas.annotations.Operation(summary = "Crear plan de abono")
    public ResponseEntity<?> crearPlan(@RequestBody Map<String, Object> payload) {
        try {
            PlanAbono plan = new PlanAbono();
            plan.setNombre((String) payload.get("nombre"));
            plan.setDescripcion((String) payload.get("descripcion"));
            plan.setCantidadPersonas((Integer) payload.get("cantidadPersonas"));
            plan.setEdadMinima((Integer) payload.get("edadMinima"));
            plan.setEdadMax((Integer) payload.get("edadMax"));
            plan.setPrecio(new java.math.BigDecimal(payload.get("precio").toString()));
            plan.setDuracion(com.deustosport.my_app.enums.DuracionAbonos.valueOf((String) payload.get("duracion")));
            plan.setAmbito(com.deustosport.my_app.enums.AmbitoAbono.valueOf((String) payload.get("ambito")));
            plan.setDescuentoPistasPorcentaje(new java.math.BigDecimal(payload.get("descuentoPistasPorcentaje").toString()));
            plan.setActivo(payload.get("activo") == null || (boolean) payload.get("activo"));

            if (payload.containsKey("ayuntamientoId")) {
                Long aytoId = Long.valueOf(payload.get("ayuntamientoId").toString());
                plan.setAyuntamiento(ayuntamientoRepository.findById(aytoId).orElse(null));
            }

            return ResponseEntity.ok(adminService.crearPlan(plan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/planes")
    @io.swagger.v3.oas.annotations.Operation(summary = "Listar planes de abono")
    public ResponseEntity<List<PlanAbono>> listarPlanes(@RequestParam(required = false) Long ayuntamientoId) {
        if (ayuntamientoId != null) {
            return ResponseEntity.ok(adminService.listarPlanesPorAyuntamiento(ayuntamientoId));
        }
        return ResponseEntity.ok(adminService.listarPlanes());
    }

    @PutMapping("/planes/{planId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Modificar plan")
    public ResponseEntity<PlanAbono> actualizarPlan(@PathVariable("planId") Long planId, @RequestBody PlanAbono detalles) {
        return ResponseEntity.ok(adminService.actualizarPlan(planId, detalles));
    }

    @DeleteMapping("/planes/{planId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Eliminar plan")
    public ResponseEntity<Void> eliminarPlan(@PathVariable("planId") Long planId) {
        adminService.eliminarPlan(planId);
        return ResponseEntity.noContent().build();
    }
}