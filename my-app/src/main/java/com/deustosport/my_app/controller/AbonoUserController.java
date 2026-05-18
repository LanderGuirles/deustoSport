package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.AbonoUsuarioRequest;
import com.deustosport.my_app.dto.AbonoUsuarioResponse;
import com.deustosport.my_app.dto.PlanAbonoResponse;
import com.deustosport.my_app.entity.AbonoUsuario;
import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.service.AbonoUsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/abonos")
@Tag(name = "Abonos usuario", description = "Opciones de los abonos a nivel de usuario")
public class AbonoUserController {

    @Autowired
    private AbonoUsuarioService abonoService;

    @GetMapping("/disponibles")
    @io.swagger.v3.oas.annotations.Operation(summary = "Listar los planes activos disponibles para comprar")
    public ResponseEntity<List<PlanAbonoResponse>> listarPlanesDisponibles(@PathVariable("usuarioId") Long usuarioId) {
        List<PlanAbonoResponse> activos = abonoService.obtenerPlanesActivos().stream().map(p -> {
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
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(activos);
    }

    @PostMapping("/comprar")
    @io.swagger.v3.oas.annotations.Operation(summary = "Comprar el abono")
    public ResponseEntity<?> comprarAbono(
            @PathVariable("usuarioId") Long usuarioId,
            @RequestBody AbonoUsuarioRequest request) {
        try {
            AbonoUsuario abono = abonoService.comprarAbono(
                    usuarioId, 
                    request.getPlanAbonoId(), 
                    request.getEmailsBeneficiarios(), 
                    request.getMetodoPago(),
                    request.getAmbito(),
                    request.getPolideportivoId(),
                    request.getAyuntamientoId()
            );
            return ResponseEntity.ok(mapearResponse(abono));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/activo")
    @io.swagger.v3.oas.annotations.Operation(summary = "Ver plan activo del usuario")
    public ResponseEntity<?> verAbonoActivo(@PathVariable("usuarioId") Long usuarioId) {
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
        res.setTitularEmail(a.getTitular().getEmail());
        
        List<String> correos = a.getBeneficiarios().stream()
                                .map(Usuario::getEmail)
                                .collect(Collectors.toList());
        res.setEmailsBeneficiarios(correos);
        
        return res;
    }
}