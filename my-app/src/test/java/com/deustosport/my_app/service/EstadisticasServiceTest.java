package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.EstadisticasDTO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstadisticasServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EstadisticasService estadisticasService;

    @Test
    void obtenerEstadisticas_debeRetornarEstadisticasCorrectas() {
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
    void obtenerEstadisticasReservasPorInstalacion_debeRetornarMapaCorrecto() {
        // Given
        when(reservaRepository.countReservasPorInstalacion()).thenReturn(java.util.Map.of(
            "Pista Tenis 1", 45L,
            "Pista Pádel 2", 30L,
            "Campo Fútbol", 25L
        ));

        // When
        var result = estadisticasService.obtenerEstadisticasReservasPorInstalacion();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(45L, result.get("Pista Tenis 1"));
        assertEquals(30L, result.get("Pista Pádel 2"));
        assertEquals(25L, result.get("Campo Fútbol"));
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
}