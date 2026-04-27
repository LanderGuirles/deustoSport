package com.deustosport.my_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.deustosport.my_app.entity.Tarifa;
import com.deustosport.my_app.service.TarifaService;
import java.util.List;
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
class TarifaControllerTest {

    private static final Logger log = LoggerFactory.getLogger(TarifaControllerTest.class);

    @Mock
    private TarifaService tarifaService;

    @InjectMocks
    private TarifaController tarifaController;

    @Test
    void listarTodas_devuelveOkConLista() {
        log.info("[TEST] listarTodas_devuelveOkConLista");
        Tarifa tarifa = new Tarifa();
        tarifa.setId(1L);
        when(tarifaService.obtenerTodas()).thenReturn(List.of(tarifa));

        ResponseEntity<List<Tarifa>> response = tarifaController.listarTodas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        log.info("[TEST] Tarifas devueltas: {}", response.getBody().size());
    }

    @Test
    void crearTarifa_siServicioFalla_devuelveBadRequest() {
        log.info("[TEST] crearTarifa_siServicioFalla_devuelveBadRequest");
        when(tarifaService.crearTarifa(any(Tarifa.class)))
                .thenThrow(new IllegalArgumentException("dato invalido"));

        ResponseEntity<?> response = tarifaController.crearTarifa(new Tarifa());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("error")).contains("dato invalido"));
        log.info("[TEST] Error capturado correctamente: {}", body.get("error"));
    }
}
