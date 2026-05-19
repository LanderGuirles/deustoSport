package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.dto.PistaResponse;
import com.deustosport.my_app.dto.ResumenSemanalPolideportivoDTO;
import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.PolideportivoRepository;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PolideportivoYPistaServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PolideportivoYPistaServiceTest.class);

    // ── PolideportivoService ──────────────────────────────────────────────────
    @Mock private PolideportivoRepository polideportivoRepository;
    @Mock private ReservaRepository reservaRepository;
    @InjectMocks private PolideportivoService polideportivoService;

    // ── PistaService ──────────────────────────────────────────────────────────
    @Mock private PistaRepository pistaRepository;
    @Mock private ReservaService reservaService;
    @InjectMocks private PistaService pistaService;

    private Polideportivo polideportivo;
    private Pista pista;

    @BeforeEach
    void setUp() {
        polideportivo = new Polideportivo();
        polideportivo.setId(10L);
        polideportivo.setNombre("Polideportivo Central");
        polideportivo.setHoraApertura(LocalTime.of(8, 0));
        polideportivo.setHoraCierre(LocalTime.of(22, 0));

        pista = new Pista();
        pista.setId(5L);
        pista.setNombre("Pista Pádel 1");
        pista.setTipoDeporte(TipoDeporte.PADEL);
        pista.setMaxJugadores(4);
        pista.setActiva(true);
        pista.setPolideportivo(polideportivo);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PolideportivoService
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void polideportivo_actualizarHorario_exitoso() {
        log.info("[TEST] PolideportivoService.actualizarHorarioGeneral - 09:00-21:00");
        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));
        when(polideportivoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Polideportivo resultado = polideportivoService.actualizarHorarioGeneral(
                10L, LocalTime.of(9, 0), LocalTime.of(21, 0));

        assertEquals(LocalTime.of(9, 0), resultado.getHoraApertura());
        assertEquals(LocalTime.of(21, 0), resultado.getHoraCierre());
        log.info("[TEST] Horario polideportivo actualizado: {}-{}", resultado.getHoraApertura(), resultado.getHoraCierre());
    }

    @Test
    void polideportivo_actualizarHorario_cierreMenorQueApertura_lanzaExcepcion() {
        log.info("[TEST] PolideportivoService.actualizarHorarioGeneral - cierre <= apertura");
        assertThrows(IllegalArgumentException.class,
                () -> polideportivoService.actualizarHorarioGeneral(10L, LocalTime.of(20, 0), LocalTime.of(8, 0)));
    }

    @Test
    void polideportivo_actualizarHorario_noExiste_lanzaExcepcion() {
        log.info("[TEST] PolideportivoService.actualizarHorarioGeneral - polideportivo 999 no existe");
        when(polideportivoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> polideportivoService.actualizarHorarioGeneral(999L, LocalTime.of(8, 0), LocalTime.of(22, 0)));
    }

    @Test
    void polideportivo_obtenerPistas_devuelveLista() {
        log.info("[TEST] PolideportivoService.obtenerPistasByPolideportivo");
        polideportivo.setPistas(List.of(pista));
        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));

        List<Pista> resultado = polideportivoService.obtenerPistasByPolideportivo(10L);

        assertEquals(1, resultado.size());
        assertEquals(pista.getId(), resultado.get(0).getId());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PistaService
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void pista_obtenerTodas_devuelveListaDeDto() {
        log.info("[TEST] PistaService.obtenerTodasLasPistas");
        when(pistaRepository.findAll()).thenReturn(List.of(pista));

        List<PistaResponse> resultado = pistaService.obtenerTodasLasPistas();

        assertEquals(1, resultado.size());
        assertEquals("Pista Pádel 1", resultado.get(0).getNombre());
        log.info("[TEST] Pistas devueltas: {}", resultado.size());
    }

    @Test
    void pista_registrarNueva_exitosa() {
        log.info("[TEST] PistaService.registrarNuevaPista - nueva pista");
        Pista nueva = new Pista();
        nueva.setNombre("Pista Tenis 1");
        nueva.setTipoDeporte(TipoDeporte.TENIS);
        nueva.setMaxJugadores(2);
        nueva.setPolideportivo(polideportivo);

        when(pistaRepository.existsByNombreIgnoreCase("Pista Tenis 1")).thenReturn(false);
        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));
        when(pistaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pista resultado = pistaService.registrarNuevaPista(nueva);

        assertNotNull(resultado);
        assertTrue(resultado.isActiva());
        log.info("[TEST] Pista registrada: '{}'", resultado.getNombre());
    }

    @Test
    void pista_registrarNueva_nombreDuplicado_lanzaExcepcion() {
        log.info("[TEST] PistaService.registrarNuevaPista - nombre duplicado");
        when(pistaRepository.existsByNombreIgnoreCase("Pista Pádel 1")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pistaService.registrarNuevaPista(pista));
        assertTrue(ex.getMessage().contains("nombre"));
        log.info("[TEST] Excepción nombre duplicado: {}", ex.getMessage());
    }

    @Test
    void pista_eliminar_noExiste_lanzaExcepcion() {
        log.info("[TEST] PistaService.eliminarPista - pistaId=999 no existe");
        when(pistaRepository.existsById(999L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> pistaService.eliminarPista(999L));
    }

    @Test
    void pista_bloquear_alternaBloqueada_yCancelaReservas() {
        log.info("[TEST] PistaService.bloquearPista - pista activa → bloqueada (cancela reservas)");
        when(pistaRepository.findById(5L)).thenReturn(Optional.of(pista));
        when(pistaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        
        // Mock de reservas futuras
        com.deustosport.my_app.entity.Reserva r1 = new com.deustosport.my_app.entity.Reserva();
        r1.setId(100L);
        when(reservaRepository.findActivasByPistaAndRango(eq(5L), any(), any()))
                .thenReturn(List.of(r1));

        PistaResponse resultado = pistaService.bloquearPista(5L);

        assertFalse(resultado.isActiva(), "La pista debería quedar bloqueada (inactiva)");
        verify(reservaService, times(1)).cancelarReservaPorBloqueo(100L);
        log.info("[TEST] Estado tras bloquear: activa={}. Reserva 100 cancelada.", resultado.isActiva());
    }

    @Test
    void pista_desbloquear_noCancelaReservas() {
        log.info("[TEST] PistaService.bloquearPista - pista bloqueada → activa");
        pista.setActiva(false);
        when(pistaRepository.findById(5L)).thenReturn(Optional.of(pista));
        when(pistaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PistaResponse resultado = pistaService.bloquearPista(5L);

        assertTrue(resultado.isActiva(), "La pista debería quedar activa");
        verify(reservaRepository, never()).findActivasByPistaAndRango(any(), any(), any());
        verify(reservaService, never()).cancelarReservaPorBloqueo(any());
    }

    @Test
    void pista_eliminar_conExito_llamaDeleteById() {
        log.info("[TEST] PistaService.eliminarPista - eliminar pista existente");
        when(pistaRepository.existsById(5L)).thenReturn(true);
        doNothing().when(pistaRepository).deleteById(5L);

        assertDoesNotThrow(() -> pistaService.eliminarPista(5L));
        verify(pistaRepository).deleteById(5L);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PolideportivoService – eliminarPolideportivo (HU1)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void polideportivo_eliminar_sinPistas_eliminaCorrectamente() {
        log.info("[TEST] PolideportivoService.eliminarPolideportivo - sin pistas, elimina ok");
        polideportivo.setPistas(new ArrayList<>());
        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));
        doNothing().when(polideportivoRepository).delete(polideportivo);

        assertDoesNotThrow(() -> polideportivoService.eliminarPolideportivo(10L));
        verify(polideportivoRepository).delete(polideportivo);
        verify(reservaRepository, never()).countActivasFuturasByPistaIds(anyList(), any());
        log.info("[TEST] Polideportivo sin pistas eliminado correctamente");
    }

    @Test
    void polideportivo_eliminar_conPistasSinReservas_eliminaCorrectamente() {
        log.info("[TEST] PolideportivoService.eliminarPolideportivo - con pistas pero sin reservas activas");
        polideportivo.setPistas(List.of(pista));
        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));
        when(reservaRepository.countActivasFuturasByPistaIds(List.of(5L), LocalDate.now())).thenReturn(0L);
        doNothing().when(polideportivoRepository).delete(polideportivo);

        assertDoesNotThrow(() -> polideportivoService.eliminarPolideportivo(10L));
        verify(polideportivoRepository).delete(polideportivo);
        log.info("[TEST] Polideportivo con pistas sin reservas eliminado correctamente");
    }

    @Test
    void polideportivo_eliminar_conReservasActivas_lanzaExcepcion() {
        log.info("[TEST] PolideportivoService.eliminarPolideportivo - con reservas activas, debe lanzar excepción");
        polideportivo.setPistas(List.of(pista));
        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));
        when(reservaRepository.countActivasFuturasByPistaIds(List.of(5L), LocalDate.now())).thenReturn(3L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> polideportivoService.eliminarPolideportivo(10L));
        assertTrue(ex.getMessage().contains("3"));
        verify(polideportivoRepository, never()).delete(any());
        log.info("[TEST] Excepción correcta al intentar eliminar con {} reservas activas: {}", 3, ex.getMessage());
    }

    @Test
    void polideportivo_eliminar_noExiste_lanzaExcepcion() {
        log.info("[TEST] PolideportivoService.eliminarPolideportivo - polideportivo 999 no existe");
        when(polideportivoRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> polideportivoService.eliminarPolideportivo(999L));
        assertTrue(ex.getMessage().contains("999"));
        verify(polideportivoRepository, never()).delete(any());
        log.info("[TEST] Excepción correcta al eliminar polideportivo inexistente: {}", ex.getMessage());
    }

    @Test
    void polideportivo_eliminar_idNull_lanzaNullPointer() {
        log.info("[TEST] PolideportivoService.eliminarPolideportivo - id null lanza NullPointerException");
        assertThrows(NullPointerException.class,
                () -> polideportivoService.eliminarPolideportivo(null));
        log.info("[TEST] NullPointerException lanzada correctamente para id null");
    }

    @Test
    void polideportivo_crearDuplicado_lanzaExcepcion() {
        log.info("[TEST] PolideportivoService.crearPolideportivo - nombre+dirección duplicada");
        Polideportivo duplicado = new Polideportivo();
        duplicado.setNombre("Polideportivo Central");
        duplicado.setDireccion("Calle Principal 1");
        when(polideportivoRepository.existsByNombreAndDireccionIgnoreCase(
                "Polideportivo Central", "Calle Principal 1")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> polideportivoService.crearPolideportivo(duplicado));
        assertTrue(ex.getMessage().toLowerCase().contains("existe"));
        log.info("[TEST] Excepción duplicado al crear: {}", ex.getMessage());
    }

    @Test
    void polideportivo_obtenerTodos_devuelveLista() {
        log.info("[TEST] PolideportivoService.obtenerTodos");
        when(polideportivoRepository.findAll()).thenReturn(List.of(polideportivo));

        List<Polideportivo> resultado = polideportivoService.obtenerTodos();

        assertEquals(1, resultado.size());
        assertEquals("Polideportivo Central", resultado.get(0).getNombre());
        log.info("[TEST] Lista de polideportivos: {} elementos", resultado.size());
    }

    @Test
    void polideportivo_obtenerPorAyuntamiento_devuelveLista() {
        log.info("[TEST] PolideportivoService.obtenerPorAyuntamiento - ayuntamientoId=1");
        when(polideportivoRepository.findByAyuntamientoId(1L)).thenReturn(List.of(polideportivo));

        List<Polideportivo> resultado = polideportivoService.obtenerPorAyuntamiento(1L);

        assertEquals(1, resultado.size());
        log.info("[TEST] Polideportivos por ayuntamiento: {}", resultado.size());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PolideportivoService – obtenerDisponibilidadPolideportivo (HU2)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void disponibilidad_polideportivoSinPistas_devuelveDtoVacio() {
        log.info("[TEST] PolideportivoService.obtenerDisponibilidadPolideportivo - sin pistas");
        polideportivo.setPistas(new ArrayList<>());
        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));

        com.deustosport.my_app.dto.DisponibilidadPolideportivoDTO resultado =
                polideportivoService.obtenerDisponibilidadPolideportivo(10L, LocalDate.now().plusDays(1));

        assertNotNull(resultado);
        assertEquals(10L, resultado.getPolideportivoId());
        assertEquals(0, resultado.getTotalPistas());
        assertTrue(resultado.getPistas().isEmpty());
        log.info("[TEST] DTO sin pistas correcto: totalPistas={}", resultado.getTotalPistas());
    }

    @Test
    void disponibilidad_polideportivoConPista_devuelveSlots() {
        log.info("[TEST] PolideportivoService.obtenerDisponibilidadPolideportivo - pista con reservas");
        polideportivo.setPistas(List.of(pista));
        LocalDate fecha = LocalDate.now().plusDays(1);

        com.deustosport.my_app.entity.Reserva reserva = new com.deustosport.my_app.entity.Reserva();
        reserva.setHoraInicio(java.time.LocalTime.of(10, 0));
        reserva.setHoraFin(java.time.LocalTime.of(11, 0));
        reserva.setEstado(com.deustosport.my_app.enums.EstadoReserva.CONFIRMADA);

        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));
        when(reservaRepository.findActivasByPistaAndRango(5L, fecha, fecha))
                .thenReturn(List.of(reserva));

        com.deustosport.my_app.dto.DisponibilidadPolideportivoDTO resultado =
                polideportivoService.obtenerDisponibilidadPolideportivo(10L, fecha);

        assertEquals(1, resultado.getTotalPistas());
        assertEquals(1, resultado.getPistasActivas());
        assertEquals(1, resultado.getPistas().get(0).getSlotsOcupados().size());
        assertEquals("10:00", resultado.getPistas().get(0).getSlotsOcupados().get(0).get("horaInicio"));
        log.info("[TEST] DTO con 1 slot ocupado: {}", resultado.getPistas().get(0).getSlotsOcupados());
    }

    @Test
    void disponibilidad_polideportivoNoExiste_lanzaExcepcion() {
        log.info("[TEST] PolideportivoService.obtenerDisponibilidadPolideportivo - no existe");
        when(polideportivoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> polideportivoService.obtenerDisponibilidadPolideportivo(999L, LocalDate.now()));
        log.info("[TEST] Excepción correcta para polideportivo inexistente");
    }

    @Test
    void disponibilidad_fechaNull_lanzaNullPointer() {
        log.info("[TEST] PolideportivoService.obtenerDisponibilidadPolideportivo - fecha null");
        assertThrows(NullPointerException.class,
                () -> polideportivoService.obtenerDisponibilidadPolideportivo(10L, null));
        log.info("[TEST] NullPointerException lanzada correctamente para fecha null");
    }

    @Test
    void disponibilidad_conPistasBloqueadas_reflejaEstado() {
        log.info("[TEST] PolideportivoService.obtenerDisponibilidadPolideportivo - pista bloqueada");
        pista.setActiva(false);
        polideportivo.setPistas(List.of(pista));
        LocalDate fecha = LocalDate.now().plusDays(1);

        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));
        when(reservaRepository.findActivasByPistaAndRango(5L, fecha, fecha))
                .thenReturn(List.of());

        com.deustosport.my_app.dto.DisponibilidadPolideportivoDTO resultado =
                polideportivoService.obtenerDisponibilidadPolideportivo(10L, fecha);

        assertEquals(1, resultado.getTotalPistas());
        assertEquals(0, resultado.getPistasActivas());
        assertFalse(resultado.getPistas().get(0).isActiva());
        log.info("[TEST] Pista bloqueada reflejada en DTO: activa={}", resultado.getPistas().get(0).isActiva());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PolideportivoService – obtenerResumenSemanal (HU4)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void resumenSemanal_sinPistas_devuelveTotalesCero() {
        log.info("[TEST] PolideportivoService.obtenerResumenSemanal - polideportivo sin pistas");
        polideportivo.setPistas(new ArrayList<>());
        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));

        ResumenSemanalPolideportivoDTO resultado =
                polideportivoService.obtenerResumenSemanal(10L);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getPolideportivoId());
        assertEquals(0, resultado.getTotalPistas());
        assertEquals(0L, resultado.getTotalReservasSemana());
        assertEquals(BigDecimal.ZERO, resultado.getIngresosTotalesSemana());
        assertTrue(resultado.getDetallesPorPista().isEmpty());
        log.info("[TEST] Resumen sin pistas: totalReservas={}, ingresos={}",
                resultado.getTotalReservasSemana(), resultado.getIngresosTotalesSemana());
    }

    @Test
    void resumenSemanal_conUnaReservaConfirmada_refleja_ingresos() {
        log.info("[TEST] PolideportivoService.obtenerResumenSemanal - reserva confirmada contabilizada");
        polideportivo.setPistas(List.of(pista));
        Reserva r = new Reserva();
        r.setId(1L);
        r.setEstado(EstadoReserva.CONFIRMADA);
        r.setPrecioTotal(new BigDecimal("20.00"));
        r.setHoraInicio(LocalTime.of(10, 0));
        r.setHoraFin(LocalTime.of(11, 0));

        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));
        when(reservaRepository.findActivasByPistaAndRango(eq(5L), any(), any()))
                .thenReturn(List.of(r));

        ResumenSemanalPolideportivoDTO resultado =
                polideportivoService.obtenerResumenSemanal(10L);

        assertEquals(1L, resultado.getTotalReservasSemana());
        assertEquals(1L, resultado.getReservasConfirmadasSemana());
        assertEquals(new BigDecimal("20.00"), resultado.getIngresosTotalesSemana());
        assertEquals(1, resultado.getDetallesPorPista().size());
        assertEquals(1L, resultado.getDetallesPorPista().get(0).getReservasConfirmadas());
        log.info("[TEST] Reserva confirmada: ingresos={}", resultado.getIngresosTotalesSemana());
    }

    @Test
    void resumenSemanal_conReservaCancelada_noContabilizaIngresos() {
        log.info("[TEST] PolideportivoService.obtenerResumenSemanal - reserva cancelada no suma ingresos");
        polideportivo.setPistas(List.of(pista));
        Reserva r = new Reserva();
        r.setId(2L);
        r.setEstado(EstadoReserva.CANCELADA);
        r.setPrecioTotal(new BigDecimal("15.00"));
        r.setHoraInicio(LocalTime.of(12, 0));
        r.setHoraFin(LocalTime.of(13, 0));

        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));
        when(reservaRepository.findActivasByPistaAndRango(eq(5L), any(), any()))
                .thenReturn(List.of(r));

        ResumenSemanalPolideportivoDTO resultado =
                polideportivoService.obtenerResumenSemanal(10L);

        // findActivasByPistaAndRango excluye canceladas; el service las contaría como canceladas
        // pero como la query no devuelve canceladas, el total seria 1 con 0 confirmadas
        assertEquals(0L, resultado.getReservasConfirmadasSemana());
        assertEquals(BigDecimal.ZERO, resultado.getIngresosTotalesSemana());
        log.info("[TEST] Reserva cancelada: ingresos={}", resultado.getIngresosTotalesSemana());
    }

    @Test
    void resumenSemanal_polideportivoNoExiste_lanzaExcepcion() {
        log.info("[TEST] PolideportivoService.obtenerResumenSemanal - polideportivo 999 no existe");
        when(polideportivoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> polideportivoService.obtenerResumenSemanal(999L));
        log.info("[TEST] Excepción correcta al pedir resumen de polideportivo inexistente");
    }

    @Test
    void resumenSemanal_horarioCustom_calculaTasaOcupacion() {
        log.info("[TEST] PolideportivoService.obtenerResumenSemanal - tasa ocupación con horario 8:00-20:00");
        polideportivo.setHoraApertura(LocalTime.of(8, 0));
        polideportivo.setHoraCierre(LocalTime.of(20, 0));
        polideportivo.setPistas(List.of(pista));

        Reserva r = new Reserva();
        r.setId(3L);
        r.setEstado(EstadoReserva.CONFIRMADA);
        r.setPrecioTotal(new BigDecimal("12.00"));
        r.setHoraInicio(LocalTime.of(10, 0));
        r.setHoraFin(LocalTime.of(12, 0)); // 2 horas

        when(polideportivoRepository.findById(10L)).thenReturn(Optional.of(polideportivo));
        when(reservaRepository.findActivasByPistaAndRango(eq(5L), any(), any()))
                .thenReturn(List.of(r));

        ResumenSemanalPolideportivoDTO resultado =
                polideportivoService.obtenerResumenSemanal(10L);

        // 2 horas reservadas / (12 h/dia * 7 dias) = 2/84 ≈ 2.38%
        double tasa = resultado.getDetallesPorPista().get(0).getTasaOcupacion();
        assertTrue(tasa > 0.0 && tasa < 5.0, "Tasa de ocupación esperada ~2.38%: fue " + tasa);
        log.info("[TEST] Tasa de ocupación calculada: {}%", tasa);
    }
}
