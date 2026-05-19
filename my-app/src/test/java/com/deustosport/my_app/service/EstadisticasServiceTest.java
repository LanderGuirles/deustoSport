package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.EstadisticasDTO;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstadisticasServiceTest {

    private static final Logger log = LoggerFactory.getLogger(EstadisticasServiceTest.class);

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PistaRepository pistaRepository;

    @InjectMocks
    private EstadisticasService estadisticasService;

    @Test
    void obtenerEstadisticas_debeRetornarEstadisticasCorrectas() {
        log.info("[TEST] obtenerEstadisticas - verifica cálculo de estadísticas del mes");
        // Given
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();

        when(reservaRepository.countByEstado("CONFIRMADA")).thenReturn(150L);
        when(reservaRepository.countByEstado("PENDIENTE")).thenReturn(25L);
        when(reservaRepository.countByEstado("CANCELADA")).thenReturn(10L);
        when(reservaRepository.countReservasMesActual(inicioMes, finMes)).thenReturn(120L);
        when(reservaRepository.sumIngresosMesActual(inicioMes, finMes)).thenReturn(new BigDecimal("2400.00"));
        when(reservaRepository.countReservasHoy(LocalDate.now())).thenReturn(15L);
        when(reservaRepository.sumIngresosHoy(LocalDate.now())).thenReturn(new BigDecimal("300.00"));
        when(usuarioRepository.countByEsSocio(true)).thenReturn(200L);
        when(usuarioRepository.countByEsSocio(false)).thenReturn(50L);
        when(usuarioRepository.sumBilleteraTotal()).thenReturn(new BigDecimal("1500.00"));

        // When
        EstadisticasDTO result = estadisticasService.obtenerEstadisticas();

        // Then
        assertNotNull(result);
        assertEquals(150L, result.getTotalReservasConfirmadas());
        assertEquals(25L, result.getTotalReservasPendientes());
        assertEquals(10L, result.getTotalReservasCanceladas());
        assertEquals(120L, result.getReservasMesActual());
        assertEquals(new BigDecimal("2400.00"), result.getIngresosMesActual());
        assertEquals(15L, result.getReservasHoy());
        assertEquals(new BigDecimal("300.00"), result.getIngresosHoy());
        assertEquals(200L, result.getTotalSocios());
        assertEquals(50L, result.getTotalNoSocios());
        assertEquals(new BigDecimal("1500.00"), result.getSaldoTotalBilleteras());
    }

    @Test
    void obtenerEstadisticasReservasPorPolideportivo_debeRetornarMapaCorrecto() {
        // Given
        when(reservaRepository.countReservasPorPolideportivo()).thenReturn(java.util.Map.of(
            "Polideportivo A", 45L,
            "Polideportivo B", 30L,
            "Polideportivo C", 25L
        ));

        // When
        var result = estadisticasService.obtenerEstadisticasReservasPorPolideportivo();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(45L, result.get("Polideportivo A"));
        assertEquals(30L, result.get("Polideportivo B"));
        assertEquals(25L, result.get("Polideportivo C"));
    }

    @Test
    void obtenerEstadisticasReservasPorDia_debeRetornarListaCorrecta() {
        // Given
        LocalDate hoy = LocalDate.now();
        when(reservaRepository.countReservasPorDia(any(), any())).thenReturn(java.util.Arrays.asList(
            new Object[]{hoy, 15L},
            new Object[]{hoy.minusDays(1), 12L},
            new Object[]{hoy.minusDays(2), 18L}
        ));

        // When
        var result = estadisticasService.obtenerEstadisticasReservasPorDia(7);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    // ── obtenerEstadisticasPorDeporte (HU3) ──────────────────────────────────

    @Test
    void obtenerEstadisticasPorDeporte_conDatos_devuelveDeportesConPorcentaje() {
        log.info("[TEST] EstadisticasService.obtenerEstadisticasPorDeporte - con datos reales");
        Object[] padelRow = new Object[]{TipoDeporte.PADEL, 60L};
        Object[] tenisRow = new Object[]{TipoDeporte.TENIS, 40L};
        when(reservaRepository.countReservasPorTipoDeporte()).thenReturn(List.of(padelRow, tenisRow));

        Map<String, Object> resultado = estadisticasService.obtenerEstadisticasPorDeporte();

        assertNotNull(resultado);
        assertEquals(100L, resultado.get("totalReservas"));
        List<Map<String, Object>> deportes = (List<Map<String, Object>>) resultado.get("deportes");
        assertEquals(2, deportes.size());
        assertEquals(60L, deportes.get(0).get("totalReservas"));
        assertEquals(60.0, deportes.get(0).get("porcentaje"));
        assertEquals(40.0, deportes.get(1).get("porcentaje"));
        log.info("[TEST] Deportes: PADEL={}%, TENIS={}%",
                deportes.get(0).get("porcentaje"), deportes.get(1).get("porcentaje"));
    }

    @Test
    void obtenerEstadisticasPorDeporte_sinDatos_devuelveVacio() {
        log.info("[TEST] EstadisticasService.obtenerEstadisticasPorDeporte - sin datos");
        when(reservaRepository.countReservasPorTipoDeporte()).thenReturn(List.of());

        Map<String, Object> resultado = estadisticasService.obtenerEstadisticasPorDeporte();

        assertNotNull(resultado);
        assertEquals(0L, resultado.get("totalReservas"));
        assertTrue(((List<?>) resultado.get("deportes")).isEmpty());
        log.info("[TEST] Sin datos de deportes: totalReservas=0");
    }

    @Test
    void obtenerEstadisticasPorDeporte_unSoloDeporte_porcentaje100() {
        log.info("[TEST] EstadisticasService.obtenerEstadisticasPorDeporte - un solo deporte → 100%");
        List<Object[]> filasSingle = new java.util.ArrayList<>();
        filasSingle.add(new Object[]{TipoDeporte.FUTBOL, 25L});
        when(reservaRepository.countReservasPorTipoDeporte()).thenReturn(filasSingle);

        Map<String, Object> resultado = estadisticasService.obtenerEstadisticasPorDeporte();

        List<Map<String, Object>> deportes = (List<Map<String, Object>>) resultado.get("deportes");
        assertEquals(100.0, deportes.get(0).get("porcentaje"));
        log.info("[TEST] Un solo deporte → 100%");
    }

    // ── obtenerTopUsuariosReservas (HU3) ──────────────────────────────────────

    @Test
    void obtenerTopUsuariosReservas_conDatos_devuelveListaOrdenada() {
        log.info("[TEST] EstadisticasService.obtenerTopUsuariosReservas - top 3");
        Object[] u1 = new Object[]{1L, "Ana García", 30L};
        Object[] u2 = new Object[]{2L, "Luis López", 20L};
        Object[] u3 = new Object[]{3L, "Marta Ruiz", 10L};
        when(reservaRepository.findTopUsuariosPorReservas(any())).thenReturn(List.of(u1, u2, u3));

        Map<String, Object> resultado = estadisticasService.obtenerTopUsuariosReservas(3);

        assertNotNull(resultado);
        assertEquals(3, resultado.get("limit"));
        List<Map<String, Object>> top = (List<Map<String, Object>>) resultado.get("topUsuarios");
        assertEquals(3, top.size());
        assertEquals("Ana García", top.get(0).get("nombreCompleto"));
        assertEquals(30L, top.get(0).get("totalReservas"));
        log.info("[TEST] Top 3 usuarios: {}", top.stream().map(u -> u.get("nombreCompleto")).toList());
    }

    @Test
    void obtenerTopUsuariosReservas_sinDatos_devuelveListaVacia() {
        log.info("[TEST] EstadisticasService.obtenerTopUsuariosReservas - sin datos");
        when(reservaRepository.findTopUsuariosPorReservas(any())).thenReturn(List.of());

        Map<String, Object> resultado = estadisticasService.obtenerTopUsuariosReservas(10);

        assertNotNull(resultado);
        assertTrue(((List<?>) resultado.get("topUsuarios")).isEmpty());
        log.info("[TEST] Sin datos → lista vacía de top usuarios");
    }

    @Test
    void obtenerTopUsuariosReservas_limiteCero_usaLimit1() {
        log.info("[TEST] EstadisticasService.obtenerTopUsuariosReservas - límite 0 usa 1");
        when(reservaRepository.findTopUsuariosPorReservas(any())).thenReturn(List.of());

        Map<String, Object> resultado = estadisticasService.obtenerTopUsuariosReservas(0);

        assertEquals(1, resultado.get("limit"));
        log.info("[TEST] Límite 0 normalizado a 1");
    }

    @Test
    void obtenerTopUsuariosReservas_limiteExcesivo_acotaA100() {
        log.info("[TEST] EstadisticasService.obtenerTopUsuariosReservas - límite >100 acota a 100");
        when(reservaRepository.findTopUsuariosPorReservas(any())).thenReturn(List.of());

        Map<String, Object> resultado = estadisticasService.obtenerTopUsuariosReservas(500);

        assertEquals(100, resultado.get("limit"));
        log.info("[TEST] Límite 500 acotado a 100");
    }

    // ── obtenerEstadisticasUsoPistas (HU3) ───────────────────────────────────

    @Test
    void obtenerEstadisticasUsoPistas_conDatos_devuelvePistasConPorcentaje() {
        log.info("[TEST] EstadisticasService.obtenerEstadisticasUsoPistas - con datos");
        Object[] pista1 = new Object[]{1L, "Pista Pádel 1", TipoDeporte.PADEL, 40L};
        Object[] pista2 = new Object[]{2L, "Pista Tenis 1", TipoDeporte.TENIS, 10L};
        when(reservaRepository.countReservasPorPista()).thenReturn(List.of(pista1, pista2));
        when(reservaRepository.count()).thenReturn(50L);

        Map<String, Object> resultado = estadisticasService.obtenerEstadisticasUsoPistas();

        assertNotNull(resultado);
        assertEquals(2, resultado.get("pistasConReservas"));
        assertEquals(50L, resultado.get("totalReservasContabilizadas"));
        List<Map<String, Object>> pistas = (List<Map<String, Object>>) resultado.get("pistas");
        assertEquals(2, pistas.size());
        assertEquals(80.0, pistas.get(0).get("porcentajeUso"));
        assertEquals(20.0, pistas.get(1).get("porcentajeUso"));
        log.info("[TEST] Uso pistas: Pádel={}%, Tenis={}%",
                pistas.get(0).get("porcentajeUso"), pistas.get(1).get("porcentajeUso"));
    }

    @Test
    void obtenerEstadisticasUsoPistas_sinReservas_porcentajeCero() {
        log.info("[TEST] EstadisticasService.obtenerEstadisticasUsoPistas - sin reservas");
        when(reservaRepository.countReservasPorPista()).thenReturn(List.of());
        when(reservaRepository.count()).thenReturn(0L);

        Map<String, Object> resultado = estadisticasService.obtenerEstadisticasUsoPistas();

        assertEquals(0, resultado.get("pistasConReservas"));
        assertTrue(((List<?>) resultado.get("pistas")).isEmpty());
        log.info("[TEST] Sin reservas: pistasConReservas=0");
    }
}
