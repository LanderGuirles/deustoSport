package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.dto.FestivoRequest;
import com.deustosport.my_app.dto.FestivoResponse;
import com.deustosport.my_app.entity.Festivo;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.repository.FestivoRepository;
import com.deustosport.my_app.repository.ReservaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class FestivoServiceTest {

    private static final Logger log = LoggerFactory.getLogger(FestivoServiceTest.class);

    @Mock private FestivoRepository festivoRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaService reservaService;

    @InjectMocks private FestivoService festivoService;

    private FestivoRequest requestValido;
    private Festivo festivoGuardado;

    @BeforeEach
    void setUp() {
        requestValido = new FestivoRequest();
        requestValido.setNombre("Día del Trabajo");
        requestValido.setFechaInicio(LocalDate.of(2026, 5, 1));
        requestValido.setFechaFin(LocalDate.of(2026, 5, 1));

        festivoGuardado = new Festivo();
        festivoGuardado.setId(1L);
        festivoGuardado.setNombre("Día del Trabajo");
        festivoGuardado.setFechaInicio(LocalDate.of(2026, 5, 1));
        festivoGuardado.setFechaFin(LocalDate.of(2026, 5, 1));
    }

    // ── programarFestivo ──────────────────────────────────────────────────────

    @Test
    void programarFestivo_unDia_sinReservas_guardaCorrectamente() {
        log.info("[TEST] FestivoService.programarFestivo - un día, sin reservas conflictivas");
        when(festivoRepository.save(any())).thenReturn(festivoGuardado);
        when(reservaRepository.findActivasByRango(any(), any())).thenReturn(List.of());

        FestivoResponse resp = festivoService.programarFestivo(requestValido);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("Día del Trabajo", resp.getNombre());
        verify(festivoRepository).save(any(Festivo.class));
        verify(reservaService, never()).cancelarReservaPorBloqueo(any());
        log.info("[TEST] Festivo guardado: id={}, nombre={}", resp.getId(), resp.getNombre());
    }

    @Test
    void programarFestivo_conReservasEnPeriodo_cancelaReservas() {
        log.info("[TEST] FestivoService.programarFestivo - reservas en el periodo del festivo deben cancelarse");
        Reserva reservaEnFestivo = new Reserva();
        reservaEnFestivo.setId(100L);
        reservaEnFestivo.setFechaReserva(LocalDate.of(2026, 5, 1));

        // La query findActivasByRango ya filtra por rango: solo devuelve las del periodo
        when(festivoRepository.save(any())).thenReturn(festivoGuardado);
        when(reservaRepository.findActivasByRango(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 1)))
                .thenReturn(List.of(reservaEnFestivo));
        doNothing().when(reservaService).cancelarReservaPorBloqueo(anyLong());

        festivoService.programarFestivo(requestValido);

        verify(reservaService, times(1)).cancelarReservaPorBloqueo(100L);
        log.info("[TEST] Reserva en festivo cancelada mediante query eficiente");
    }

    @Test
    void programarFestivo_rangoMultiDia_cancelaTodasEnRango() {
        log.info("[TEST] FestivoService.programarFestivo - rango multi-día cancela reservas en todo el rango");
        FestivoRequest rangoRequest = new FestivoRequest();
        rangoRequest.setNombre("Semana Santa");
        rangoRequest.setFechaInicio(LocalDate.of(2026, 4, 2));
        rangoRequest.setFechaFin(LocalDate.of(2026, 4, 5));

        Festivo festivoRango = new Festivo();
        festivoRango.setId(2L);
        festivoRango.setNombre("Semana Santa");
        festivoRango.setFechaInicio(LocalDate.of(2026, 4, 2));
        festivoRango.setFechaFin(LocalDate.of(2026, 4, 5));

        Reserva r1 = new Reserva(); r1.setId(1L); r1.setFechaReserva(LocalDate.of(2026, 4, 2));
        Reserva r2 = new Reserva(); r2.setId(2L); r2.setFechaReserva(LocalDate.of(2026, 4, 5));

        // findActivasByRango devuelve solo las reservas dentro del rango (la DB filtra)
        when(festivoRepository.save(any())).thenReturn(festivoRango);
        when(reservaRepository.findActivasByRango(
                LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 5)))
                .thenReturn(List.of(r1, r2));
        doNothing().when(reservaService).cancelarReservaPorBloqueo(anyLong());

        festivoService.programarFestivo(rangoRequest);

        verify(reservaService).cancelarReservaPorBloqueo(1L);
        verify(reservaService).cancelarReservaPorBloqueo(2L);
        log.info("[TEST] Rango multi-día: 2 reservas canceladas en rango");
    }

    @Test
    void programarFestivo_fechaFinAntesQueInicio_lanzaExcepcion() {
        log.info("[TEST] FestivoService.programarFestivo - fechaFin anterior a fechaInicio");
        FestivoRequest invalido = new FestivoRequest();
        invalido.setNombre("Festivo Inválido");
        invalido.setFechaInicio(LocalDate.of(2026, 5, 10));
        invalido.setFechaFin(LocalDate.of(2026, 5, 1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> festivoService.programarFestivo(invalido));
        assertTrue(ex.getMessage().toLowerCase().contains("fin"));
        verify(festivoRepository, never()).save(any());
        log.info("[TEST] Excepción correcta: {}", ex.getMessage());
    }

    @Test
    void programarFestivo_mismaFechaInicioYFin_esValido() {
        log.info("[TEST] FestivoService.programarFestivo - mismo día inicio y fin es válido");
        when(festivoRepository.save(any())).thenReturn(festivoGuardado);
        when(reservaRepository.findActivasByRango(any(), any())).thenReturn(List.of());

        assertDoesNotThrow(() -> festivoService.programarFestivo(requestValido));
        verify(festivoRepository).save(any());
        log.info("[TEST] Festivo de un solo día programado correctamente");
    }

    // ── listarFestivos ────────────────────────────────────────────────────────

    @Test
    void listarFestivos_devuelveLista() {
        log.info("[TEST] FestivoService.listarFestivos - lista con un festivo");
        when(festivoRepository.findAll()).thenReturn(List.of(festivoGuardado));

        List<FestivoResponse> resultado = festivoService.listarFestivos();

        assertEquals(1, resultado.size());
        assertEquals("Día del Trabajo", resultado.get(0).getNombre());
        log.info("[TEST] Festivos listados: {}", resultado.size());
    }

    @Test
    void listarFestivos_listaVacia_devuelveVacio() {
        log.info("[TEST] FestivoService.listarFestivos - lista vacía");
        when(festivoRepository.findAll()).thenReturn(new ArrayList<>());

        List<FestivoResponse> resultado = festivoService.listarFestivos();

        assertTrue(resultado.isEmpty());
        log.info("[TEST] Lista vacía devuelta correctamente");
    }

    @Test
    void listarFestivos_multipleFestivos_devuelveOrdenados() {
        log.info("[TEST] FestivoService.listarFestivos - múltiples festivos");
        Festivo f2 = new Festivo();
        f2.setId(2L);
        f2.setNombre("Asunción");
        f2.setFechaInicio(LocalDate.of(2026, 8, 15));
        f2.setFechaFin(LocalDate.of(2026, 8, 15));

        when(festivoRepository.findAll()).thenReturn(List.of(festivoGuardado, f2));

        List<FestivoResponse> resultado = festivoService.listarFestivos();

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
        log.info("[TEST] {} festivos listados", resultado.size());
    }

    // ── eliminarFestivo ───────────────────────────────────────────────────────

    @Test
    void eliminarFestivo_llamaDeleteById() {
        log.info("[TEST] FestivoService.eliminarFestivo - id=1");
        doNothing().when(festivoRepository).deleteById(1L);

        assertDoesNotThrow(() -> festivoService.eliminarFestivo(1L));
        verify(festivoRepository).deleteById(1L);
        log.info("[TEST] deleteById(1) invocado correctamente");
    }

    @Test
    void eliminarFestivo_idDistinto_llamaDeleteConEseId() {
        log.info("[TEST] FestivoService.eliminarFestivo - id=42");
        doNothing().when(festivoRepository).deleteById(42L);

        festivoService.eliminarFestivo(42L);

        verify(festivoRepository).deleteById(42L);
        verify(festivoRepository, never()).deleteById(1L);
        log.info("[TEST] deleteById(42) invocado correctamente, deleteById(1) no invocado");
    }

    // ── esFechaFestiva ────────────────────────────────────────────────────────

    @Test
    void esFechaFestiva_fechaEsFestiva_devuelveTrue() {
        log.info("[TEST] FestivoService.esFechaFestiva - fecha festiva devuelve true");
        LocalDate fechaFestiva = LocalDate.of(2026, 5, 1);
        when(festivoRepository.findFestivosEnFecha(fechaFestiva)).thenReturn(List.of(festivoGuardado));

        assertTrue(festivoService.esFechaFestiva(fechaFestiva));
        log.info("[TEST] Fecha festiva detectada correctamente: {}", fechaFestiva);
    }

    @Test
    void esFechaFestiva_fechaNoEsFestiva_devuelveFalse() {
        log.info("[TEST] FestivoService.esFechaFestiva - fecha normal devuelve false");
        LocalDate fechaNormal = LocalDate.of(2026, 6, 15);
        when(festivoRepository.findFestivosEnFecha(fechaNormal)).thenReturn(List.of());

        assertFalse(festivoService.esFechaFestiva(fechaNormal));
        log.info("[TEST] Fecha no festiva detectada correctamente: {}", fechaNormal);
    }

    @Test
    void esFechaFestiva_multipleFestivosEnFecha_devuelveTrue() {
        log.info("[TEST] FestivoService.esFechaFestiva - múltiples festivos en la misma fecha");
        LocalDate fecha = LocalDate.of(2026, 12, 25);
        Festivo f2 = new Festivo();
        f2.setId(2L);
        f2.setNombre("Navidad Local");
        f2.setFechaInicio(fecha);
        f2.setFechaFin(fecha);

        when(festivoRepository.findFestivosEnFecha(fecha)).thenReturn(List.of(festivoGuardado, f2));

        assertTrue(festivoService.esFechaFestiva(fecha));
        log.info("[TEST] Fecha con múltiples festivos: true");
    }
}
