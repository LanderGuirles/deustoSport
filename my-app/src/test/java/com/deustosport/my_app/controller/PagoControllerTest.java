package com.deustosport.my_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.deustosport.my_app.dto.PagoResponse;
import com.deustosport.my_app.service.PagoService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PagoControllerTest {

    @Mock
    private PagoService pagoService;

    @InjectMocks
    private PagoController pagoController;

    @Test
    void obtenerOpcionesPago_devuelveTresMetodos() {
        ResponseEntity<?> response = pagoController.obtenerOpcionesPago();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        java.util.List<?> metodos = (java.util.List<?>) body.get("metodos");
        assertEquals(3, metodos.size());
    }

    @Test
    void consultarPago_siServicioFalla_devuelveBadRequest() {
        when(pagoService.obtenerPagoPorReserva(9L)).thenThrow(new IllegalArgumentException("No existe"));

        ResponseEntity<?> response = pagoController.consultarPago(9L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("error")).contains("No existe"));
    }

    @Test
    void obtenerRecaudacionMesActual_devuelveOkConDatos() {
        when(pagoService.obtenerRecaudacionMesActual()).thenReturn(Map.of(
                "anio", 2026,
                "mes", 4,
                "mesNombre", "abril",
                "totalRecaudado", new BigDecimal("99.00"),
                "totalPagos", 3L,
                "moneda", "EUR"));

        ResponseEntity<?> response = pagoController.obtenerRecaudacionMesActual();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("EUR", body.get("moneda"));
    }
}
