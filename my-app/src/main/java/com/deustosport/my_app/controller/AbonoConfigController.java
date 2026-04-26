package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.entity.TarifaAbono;
import com.deustosport.my_app.service.AbonoConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/abonos")
@Tag(name = "Abonos administrador", description = "Configuración de la creacion/modificacion de los abonos")
public class AbonoConfigController {

    @Autowired
    private AbonoConfigService adminService;

    @PostMapping("/planes")
    @io.swagger.v3.oas.annotations.Operation(summary = "Crear plan de abono")
    public ResponseEntity<PlanAbono> crearPlan(@RequestBody PlanAbono plan) {
        return ResponseEntity.ok(adminService.crearPlan(plan));
    }

    @GetMapping("/planes")
    @io.swagger.v3.oas.annotations.Operation(summary = "Listar planes de abono")
    public ResponseEntity<List<PlanAbono>> listarPlanes() {
        return ResponseEntity.ok(adminService.listarPlanes());
    }

    @PutMapping("/planes/{planId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Modificar plan")
    public ResponseEntity<PlanAbono> actualizarPlan(@PathVariable Long planId, @RequestBody PlanAbono detalles) {
        return ResponseEntity.ok(adminService.actualizarPlan(planId, detalles));
    }

    @DeleteMapping("/planes/{planId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Eliminar plan")
    public ResponseEntity<Void> eliminarPlan(@PathVariable Long planId) {
        adminService.eliminarPlan(planId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/planes/{planId}/tarifas")
    @io.swagger.v3.oas.annotations.Operation(summary = "Ponerle precio a un plan de abono")
    public ResponseEntity<?> añadirTarifa(@PathVariable Long planId, @RequestBody TarifaAbono tarifa) {
        try {
            return ResponseEntity.ok(adminService.añadirTarifaAPlan(planId, tarifa));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/planes/{planId}/tarifas")
    @io.swagger.v3.oas.annotations.Operation(summary = "Ver el precio de un plan")
    public ResponseEntity<List<TarifaAbono>> listarTarifas(@PathVariable Long planId) {
        return ResponseEntity.ok(adminService.listarTarifasDePlan(planId));
    }

    @PutMapping("/tarifas/{tarifaId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Modificar la tarifa de un plan")
    public ResponseEntity<TarifaAbono> actualizarTarifa(@PathVariable Long tarifaId, @RequestBody TarifaAbono detalles) {
        return ResponseEntity.ok(adminService.actualizarTarifa(tarifaId, detalles));
    }

    @DeleteMapping("/tarifas/{tarifaId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Eliminar la tarifa de un plan")
    public ResponseEntity<Void> eliminarTarifa(@PathVariable Long tarifaId) {
        adminService.eliminarTarifa(tarifaId);
        return ResponseEntity.noContent().build();
    }
}