package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.AbonoUsuarioRequest;
import com.deustosport.my_app.dto.AbonoUsuarioResponse;
import com.deustosport.my_app.dto.TarifaAbonoResponse;
import com.deustosport.my_app.entity.AbonoUsuario;
import com.deustosport.my_app.entity.TarifaAbono;
import com.deustosport.my_app.enums.DuracionAbonos;
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

    @GetMapping("/calcular-precio")
    @io.swagger.v3.oas.annotations.Operation(summary = "Calcular el importe a pagar por el abono dependiendo de plan y edad")
    public ResponseEntity<?> calcularPrecio(
            @PathVariable Long usuarioId,
            @RequestParam Long planId,
            @RequestParam DuracionAbonos duracion) {
        try {
            TarifaAbono t = abonoService.calcularTarifaParaUsuario(usuarioId, planId, duracion);
            TarifaAbonoResponse dto = new TarifaAbonoResponse();
            dto.setId(t.getId());
            dto.setPlanAbonoId(t.getPlanAbono().getId());
            dto.setNombrePlan(t.getPlanAbono().getNombre());
            dto.setDuracion(t.getDuracion());
            dto.setEdadMinima(t.getEdadMinima());
            dto.setEdadMax(t.getEdadMax());
            dto.setPrecio(t.getPrecio());
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
            AbonoUsuario abono = abonoService.comprarAbono(usuarioId, request.getTarifaAbonoId());
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
                .orElseGet(() -> ResponseEntity.ok().build()); // 200 OK vacío si no tiene
    }

    private AbonoUsuarioResponse mapearResponse(AbonoUsuario a) {
        AbonoUsuarioResponse res = new AbonoUsuarioResponse();
        res.setId(a.getId());
        res.setNombrePlan(a.getTarifa().getPlanAbono().getNombre());
        res.setDuracion(a.getTarifa().getDuracion());
        res.setPrecioPagado(a.getTarifa().getPrecio());
        res.setFechaInicio(a.getFechaInicio());
        res.setFechaFin(a.getFechaFin());
        res.setActivo(a.isActivo());
        return res;
    }
}