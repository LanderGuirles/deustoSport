package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.EstadisticasDTO;
import com.deustosport.my_app.service.EstadisticasService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstadisticasController.class)
@WithMockUser(roles = "ADMIN")
class EstadisticasControllerTest {

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
        when(estadisticasService.obtenerEstadisticasReservasPorDia(7)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/estadisticas/reservas-por-dia")
                .param("dias", "7"))
                .andExpect(status().isOk());
    }
}
