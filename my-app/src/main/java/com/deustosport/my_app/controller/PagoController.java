package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.PagoResponse;
import com.deustosport.my_app.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Consulta de pagos y opciones disponibles")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping("/opciones")
    @Operation(summary = "Opciones de pago disponibles",
               description = "Devuelve los métodos de pago: TARJETA, BIZUM y TRANSFERENCIA (requiere IBAN)")
    public ResponseEntity<?> obtenerOpcionesPago() {
        return ResponseEntity.ok(Map.of(
                "metodos", List.of(
                        Map.of("codigo", "TARJETA",       "descripcion", "Tarjeta de débito/crédito (16 dígitos + CVV)"),
                        Map.of("codigo", "BIZUM",         "descripcion", "Bizum (teléfono español)"),
                        Map.of("codigo", "TRANSFERENCIA", "descripcion", "Transferencia bancaria (requiere IBAN)")
                )
        ));
    }

    @GetMapping("/reserva/{reservaId}")
    @Operation(summary = "Consultar pago de una reserva")
    public ResponseEntity<?> consultarPago(@PathVariable("reservaId") Long reservaId) {
        try {
            PagoResponse pagoResponseDto = pagoService.obtenerPagoPorReserva(reservaId);

            return ResponseEntity.ok(pagoResponseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}