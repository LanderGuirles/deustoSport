package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.EstadisticasDTO;
import com.deustosport.my_app.dto.ReporteUsoPistaDTO;
import com.deustosport.my_app.dto.ReporteUsoPistasDTO;
import com.deustosport.my_app.service.EstadisticasService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstadisticasController.class)
@WithMockUser(roles = "ADMIN")
class EstadisticasControllerTest {

    private static final Logger log = LoggerFactory.getLogger(EstadisticasControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstadisticasService estadisticasService;

    @Test
    void obtenerEstadisticas_debeRetornarEstadisticas() throws Exception {
        EstadisticasDTO estadisticas = new EstadisticasDTO();
        estadisticas.setTotalReservasConfirmadas(150L);
        estadisticas.setTotalReservasPendientes(25L);
        estadisticas.setReservasMesActual(120L);
        estadisticas.setIngresosMesActual(new BigDecimal("2400.00"));
        estadisticas.setTotalSocios(200L);
        estadisticas.setSaldoTotalBilleteras(new BigDecimal("1500.00"));

        when(estadisticasService.obtenerEstadisticas()).thenReturn(estadisticas);

        mockMvc.perform(get("/api/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReservasConfirmadas").value(150))
                .andExpect(jsonPath("$.totalReservasPendientes").value(25))
                .andExpect(jsonPath("$.reservasMesActual").value(120))
                .andExpect(jsonPath("$.ingresosMesActual").value(2400.00))
                .andExpect(jsonPath("$.totalSocios").value(200))
                .andExpect(jsonPath("$.saldoTotalBilleteras").value(1500.00));
    }

    @Test
    void obtenerEstadisticasReservasPorPolideportivo_debeRetornarMapa() throws Exception {
        Map<String, Long> estadisticasPorPolideportivo = Map.of(
            "Pista Tenis 1", 45L,
            "Pista Pádel 2", 30L
        );

        when(estadisticasService.obtenerEstadisticasReservasPorPolideportivo()).thenReturn(estadisticasPorPolideportivo);

        mockMvc.perform(get("/api/estadisticas/reservas-por-polideportivo"))                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['Pista Tenis 1']").value(45))
                .andExpect(jsonPath("$.['Pista Pádel 2']").value(30));
    }

    @Test
    void obtenerEstadisticasReservasPorDia_debeRetornarLista() throws Exception {
        when(estadisticasService.obtenerEstadisticasReservasPorDia(7)).thenReturn(List.of());

        mockMvc.perform(get("/api/estadisticas/reservas-por-dia")
                .param("dias", "7"))
                .andExpect(status().isOk());
    }

    // ── HU3: estadísticas por deporte ────────────────────────────────────────

    @Test
    void obtenerEstadisticasPorDeporte_devuelveOk() throws Exception {
        log.info("[TEST] GET /api/estadisticas/deportes - HU3");
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("totalReservas", 100L);
        datos.put("deportes", List.of());
        when(estadisticasService.obtenerEstadisticasPorDeporte()).thenReturn(datos);

        mockMvc.perform(get("/api/estadisticas/deportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReservas").value(100));
        log.info("[TEST] /deportes devuelve 200 con totalReservas=100");
    }

    // ── HU3: top usuarios ─────────────────────────────────────────────────────

    @Test
    void obtenerTopUsuariosReservas_devuelveOk() throws Exception {
        log.info("[TEST] GET /api/estadisticas/usuarios/top?limit=5 - HU3");
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("limit", 5);
        datos.put("topUsuarios", List.of());
        when(estadisticasService.obtenerTopUsuariosReservas(5)).thenReturn(datos);

        mockMvc.perform(get("/api/estadisticas/usuarios/top").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(5));
        log.info("[TEST] /usuarios/top devuelve 200 con limit=5");
    }

    // ── HU3: uso de pistas ────────────────────────────────────────────────────

    @Test
    void obtenerEstadisticasUsoPistas_devuelveOk() throws Exception {
        log.info("[TEST] GET /api/estadisticas/pistas/uso - HU3");
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("pistasConReservas", 4);
        datos.put("pistas", List.of());
        when(estadisticasService.obtenerEstadisticasUsoPistas()).thenReturn(datos);

        mockMvc.perform(get("/api/estadisticas/pistas/uso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pistasConReservas").value(4));
        log.info("[TEST] /pistas/uso devuelve 200 con pistasConReservas=4");
    }

    // ── reporte pista ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "COORDINADOR")
    void obtenerReporteUsoPista_devuelveOk() throws Exception {
        log.info("[TEST] GET /api/estadisticas/reportes/pista/1 - COORDINADOR");
        ReporteUsoPistaDTO reporte = new ReporteUsoPistaDTO();
        reporte.setPistaId(1L);
        reporte.setPistaNombre("Pista Pádel 1");
        reporte.setTotalReservas(15L);

        when(estadisticasService.generarReporteUsoPista(eq(1L), any(), any())).thenReturn(reporte);

        mockMvc.perform(get("/api/estadisticas/reportes/pista/1")
                .param("fechaInicio", LocalDate.now().withDayOfMonth(1).toString())
                .param("fechaFin", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pistaId").value(1))
                .andExpect(jsonPath("$.totalReservas").value(15));
        log.info("[TEST] Reporte pista: totalReservas=15");
    }

    @Test
    @WithMockUser(roles = "COORDINADOR")
    void obtenerReporteUsoPista_noExistente_devuelveNotFound() throws Exception {
        log.info("[TEST] GET /api/estadisticas/reportes/pista/999 - pista no encontrada");
        when(estadisticasService.generarReporteUsoPista(eq(999L), any(), any())).thenReturn(null);

        mockMvc.perform(get("/api/estadisticas/reportes/pista/999")
                .param("fechaInicio", LocalDate.now().withDayOfMonth(1).toString())
                .param("fechaFin", LocalDate.now().toString()))
                .andExpect(status().isNotFound());
        log.info("[TEST] 404 correcto para pista inexistente");
    }

    // ── reporte uso pistas semana/mes ─────────────────────────────────────────

    @Test
    @WithMockUser(roles = "COORDINADOR")
    void obtenerReporteSemanaActual_devuelveOk() throws Exception {
        log.info("[TEST] GET /api/estadisticas/reportes/uso-pistas/semana-actual");
        ReporteUsoPistasDTO reporte = new ReporteUsoPistasDTO();
        reporte.setTotalReservas(30L);
        reporte.setReportesPorPista(List.of());
        when(estadisticasService.generarReporteUsoPistas(any(), any())).thenReturn(reporte);

        mockMvc.perform(get("/api/estadisticas/reportes/uso-pistas/semana-actual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReservas").value(30));
        log.info("[TEST] Reporte semana actual: 200 OK");
    }

    @Test
    @WithMockUser(roles = "COORDINADOR")
    void obtenerReporteMesActual_devuelveOk() throws Exception {
        log.info("[TEST] GET /api/estadisticas/reportes/uso-pistas/mes-actual");
        ReporteUsoPistasDTO reporte = new ReporteUsoPistasDTO();
        reporte.setTotalReservas(120L);
        reporte.setReportesPorPista(List.of());
        when(estadisticasService.generarReporteUsoPistas(any(), any())).thenReturn(reporte);

        mockMvc.perform(get("/api/estadisticas/reportes/uso-pistas/mes-actual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReservas").value(120));
        log.info("[TEST] Reporte mes actual: 200 OK");
    }
}
