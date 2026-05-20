package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.entity.Bono;
import com.deustosport.my_app.repository.BonoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class BonoServiceTest {

    private static final Logger log = LoggerFactory.getLogger(BonoServiceTest.class);

    @Mock
    private BonoRepository bonoRepository;

    @InjectMocks
    private BonoService bonoService;

    private Bono bonoBase() {
        Bono b = new Bono();
        b.setId(1L);
        b.setNombre("Bono Básico");
        b.setCreditosIncluidos(5);
        b.setPrecio(new BigDecimal("20.00"));
        b.setValidezDias(30);
        b.setActivo(true);
        return b;
    }

    @Test
    void obtenerBonosActivos_devuelveLista() {
        log.info("[TEST] BonoService.obtenerBonosActivos");
        when(bonoRepository.findByActivoTrue()).thenReturn(List.of(bonoBase()));

        List<Bono> bonos = bonoService.obtenerBonosActivos();

        assertEquals(1, bonos.size());
        assertEquals("Bono Básico", bonos.get(0).getNombre());
        log.info("[TEST] Bonos activos devueltos: {}", bonos.size());
    }

    @Test
    void obtenerTodos_devuelveLista() {
        log.info("[TEST] BonoService.obtenerTodos");
        when(bonoRepository.findAll()).thenReturn(List.of(bonoBase()));

        List<Bono> bonos = bonoService.obtenerTodos();

        assertEquals(1, bonos.size());
        log.info("[TEST] Todos los bonos devueltos: {}", bonos.size());
    }
}
