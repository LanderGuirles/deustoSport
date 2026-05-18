package com.deustosport.my_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import com.deustosport.my_app.service.PagoService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked"})
class PagoControllerTest {

    private static final Logger log = LoggerFactory.getLogger(PagoControllerTest.class);

    @Mock
    private PagoService pagoService;

    @InjectMocks
    private PagoController pagoController;

    @Test
    void obtenerOpcionesPago_devuelveTresMetodos() {
        log.info("[TEST] obtenerOpcionesPago_devuelveTresMetodos");
        ResponseEntity<?> response = pagoController.obtenerOpcionesPago();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        java.util.List<?> metodos = (java.util.List<?>) body.get("metodos");
        assertEquals(3, metodos.size());
        log.info("[TEST] Métodos de pago devueltos: {}", metodos.size());
    }

    @Test
    void consultarPago_siServicioFalla_devuelveBadRequest() {
        log.info("[TEST] consultarPago_siServicioFalla_devuelveBadRequest - reservaId=9");
        when(pagoService.obtenerPagoPorReserva(9L)).thenThrow(new IllegalArgumentException("No existe"));

        ResponseEntity<?> response = pagoController.consultarPago(9L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("error")).contains("No existe"));
        log.info("[TEST] Respuesta correcta: {}", response.getStatusCode());
    }

    @Test
    void obtenerRecaudacionMesActual_devuelveOkConDatos() {
        log.info("[TEST] obtenerRecaudacionMesActual_devuelveOkConDatos");
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
