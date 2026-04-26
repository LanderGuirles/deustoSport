package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.AbonoUsuarioRequest;
import com.deustosport.my_app.dto.AbonoUsuarioResponse;
import com.deustosport.my_app.dto.PlanAbonoResponse;
import com.deustosport.my_app.entity.AbonoUsuario;
import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.service.AbonoUsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/abonos")
@Tag(name = "Abonos usuario", description = "Opciones de los abonos a nivel de usuario")
public class AbonoUserController {

    @Autowired
    private AbonoUsuarioService abonoService;

    @GetMapping("/comprobar-plan")
    @io.swagger.v3.oas.annotations.Operation(summary = "Comprueba si el usuario puede acceder al plan y muestra la información del mismo")
    public ResponseEntity<?> comprobarPlan(
            @PathVariable Long usuarioId,
            @RequestParam Long planId) {
        try {
            PlanAbono p = abonoService.obtenerPlanAdecuado(usuarioId, planId);
            PlanAbonoResponse dto = new PlanAbonoResponse();
            dto.setPlanAbonoId(p.getId());
            dto.setNombrePlan(p.getNombre());
            dto.setDescripcion(p.getDescripcion());
            dto.setDuracion(p.getDuracion());
            dto.setCantidadPersonas(p.getCantidadPersonas());
            dto.setEdadMinima(p.getEdadMinima());
            dto.setEdadMax(p.getEdadMax());
            dto.setPrecio(p.getPrecio());
            dto.setDescuentoPistasPorcentaje(p.getDescuentoPistasPorcentaje());
            dto.setActivo(p.isActivo());
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/comprar")
    @io.swagger.v3.oas.annotations.Operation(summary = "Comprar el abono")
    public ResponseEntity<?> comprarAbono(
            @PathVariable Long usuarioId,
            @RequestBody AbonoUsuarioRequest request) {
        try {
            AbonoUsuario abono = abonoService.comprarAbono(usuarioId, request.getPlanAbonoId());
            return ResponseEntity.ok(mapearResponse(abono));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/activo")
    @io.swagger.v3.oas.annotations.Operation(summary = "Ver plan activo del usuario")
    public ResponseEntity<?> verAbonoActivo(@PathVariable Long usuarioId) {
        return abonoService.obtenerAbonoActivo(usuarioId)
                .map(abono -> ResponseEntity.ok(mapearResponse(abono)))
                .orElseGet(() -> ResponseEntity.ok().build());
    }

    private AbonoUsuarioResponse mapearResponse(AbonoUsuario a) {
        AbonoUsuarioResponse res = new AbonoUsuarioResponse();
        res.setId(a.getId());
        res.setNombrePlan(a.getPlan().getNombre());
        res.setDescripcion(a.getPlan().getDescripcion());
        res.setDuracion(a.getPlan().getDuracion());
        res.setPrecioPagado(a.getPlan().getPrecio());
        res.setFechaInicio(a.getFechaInicio());
        res.setFechaFin(a.getFechaFin());
        res.setActivo(a.isActivo());
        return res;
    }
}