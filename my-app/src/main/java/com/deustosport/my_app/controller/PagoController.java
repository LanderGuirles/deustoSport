package com.deustosport.my_app.controller;

import com.deustosport.my_app.entity.Pago;
import com.deustosport.my_app.enums.MetodoPago;
import com.deustosport.my_app.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Pasarela de pago virtual para confirmar reservas")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping("/opciones")
    @Operation(summary = "Opciones de pago disponibles (#41)",
               description = "Devuelve los métodos de pago disponibles para el usuario")
    public ResponseEntity<?> obtenerOpcionesPago() {
        return ResponseEntity.ok(Map.of(
                "metodos", List.of(
                        Map.of("codigo", "TARJETA",      "descripcion", "Tarjeta de débito/crédito"),
                        Map.of("codigo", "BIZUM",        "descripcion", "Bizum"),
                        Map.of("codigo", "TRANSFERENCIA","descripcion", "Transferencia bancaria")
                ),
                "nota", "Se requiere IBAN bancario para procesar el pago"
        ));
    }

    @PostMapping("/procesar")
    @Operation(summary = "Procesar pago (#42 y #43)",
               description = "Simula la validación del pago y guarda el IBAN. Confirma la reserva si el pago es correcto.")
    public ResponseEntity<?> procesarPago(@RequestBody Map<String, String> request) {
        try {
            Long reservaId = Long.parseLong(request.get("reservaId"));
            String iban = request.get("iban");
            MetodoPago metodo = MetodoPago.valueOf(
                    request.getOrDefault("metodoPago", "TRANSFERENCIA").toUpperCase());

            Pago pago = pagoService.procesarPago(reservaId, iban, metodo);

            return ResponseEntity.ok(Map.of(
                    "mensaje",          "Pago procesado correctamente. Reserva confirmada.",
                    "referenciaPago",   pago.getReferenciaPago(),
                    "importe",          pago.getImporte(),
                    "metodoPago",       pago.getMetodoPago(),
                    "estadoPago",       pago.getEstadoPago(),
                    "fechaPago",        pago.getFechaPago().toString()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error inesperado al procesar el pago."));
        }
    }

    @GetMapping("/reserva/{reservaId}")
    @Operation(summary = "Consultar estado del pago de una reserva")
    public ResponseEntity<?> consultarPago(@PathVariable Long reservaId) {
        try {
            Pago pago = pagoService.obtenerPagoPorReserva(reservaId);
            return ResponseEntity.ok(Map.of(
                    "referenciaPago",   pago.getReferenciaPago(),
                    "importe",          pago.getImporte(),
                    "metodoPago",       pago.getMetodoPago(),
                    "estadoPago",       pago.getEstadoPago(),
                    "iban",             pago.getIban(),
                    "fechaPago",        pago.getFechaPago().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}