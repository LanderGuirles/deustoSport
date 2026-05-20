package com.deustosport.my_app.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.entity.Bono;
import com.deustosport.my_app.service.BonoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class BonoControllerTest {

    private static final Logger log = LoggerFactory.getLogger(BonoControllerTest.class);

    @Mock private BonoService bonoService;
    @InjectMocks private BonoController bonoController;

    private Bono bono;

    @BeforeEach
    void setUp() {
        bono = new Bono();
        bono.setId(1L);
        bono.setNombre("Bono Básico");
        bono.setCreditosIncluidos(5);
        bono.setPrecio(new BigDecimal("20.00"));
        bono.setValidezDias(30);
        bono.setActivo(true);
    }

    @Test
    void listarBonos_devuelveListaOk() {
        log.info("[TEST] BonoController.listarBonos");
        when(bonoService.obtenerBonosActivos()).thenReturn(List.of(bono));

        ResponseEntity<List<Bono>> resp = bonoController.listarBonos();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
        assertEquals("Bono Básico", resp.getBody().get(0).getNombre());
        log.info("[TEST] Bonos listados: {}", resp.getBody().size());
    }

    @Test
    void listarBonos_listaVacia_devuelveOk() {
        log.info("[TEST] BonoController.listarBonos - lista vacía");
        when(bonoService.obtenerBonosActivos()).thenReturn(List.of());

        ResponseEntity<List<Bono>> resp = bonoController.listarBonos();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
        log.info("[TEST] Lista vacía devuelta correctamente");
    }
}
