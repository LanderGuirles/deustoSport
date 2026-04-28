package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.dto.PistaResponse;
import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.InstalacionRepository;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.service.ReservaService;
import java.time.LocalTime;
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
class InstalacionYPistaServiceTest {

    private static final Logger log = LoggerFactory.getLogger(InstalacionYPistaServiceTest.class);

    // ── InstalacionService ────────────────────────────────────────────────────
    @Mock private InstalacionRepository instalacionRepository;
    @InjectMocks private InstalacionService instalacionService;

    // ── PistaService ──────────────────────────────────────────────────────────
    @Mock private PistaRepository pistaRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaService reservaService;
    @InjectMocks private PistaService pistaService;

    private Instalacion instalacion;
    private Pista pista;

    @BeforeEach
    void setUp() {
        instalacion = new Instalacion();
        instalacion.setId(1L);
        instalacion.setNombre("Polideportivo Central");
        instalacion.setHoraApertura(LocalTime.of(8, 0));
        instalacion.setHoraCierre(LocalTime.of(22, 0));

        pista = new Pista();
        pista.setId(5L);
        pista.setNombre("Pista Pádel 1");
        pista.setTipoDeporte(TipoDeporte.PADEL);
        pista.setMaxJugadores(4);
        pista.setActiva(true);
        pista.setInstalacion(instalacion);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  InstalacionService
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void instalacion_obtenerTodas_devuelveLista() {
        log.info("[TEST] InstalacionService.obtenerTodas");
        when(instalacionRepository.findAll()).thenReturn(List.of(instalacion));

        List<Instalacion> resultado = instalacionService.obtenerTodas();

        assertEquals(1, resultado.size());
        log.info("[TEST] Instalaciones: {}", resultado.size());
    }

    @Test
    void instalacion_actualizarHorario_exitoso() {
        log.info("[TEST] InstalacionService.actualizarHorarioGeneral - 09:00-21:00");
        when(instalacionRepository.findById(1L)).thenReturn(Optional.of(instalacion));
        when(instalacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Instalacion resultado = instalacionService.actualizarHorarioGeneral(
                1L, LocalTime.of(9, 0), LocalTime.of(21, 0));

        assertEquals(LocalTime.of(9, 0), resultado.getHoraApertura());
        assertEquals(LocalTime.of(21, 0), resultado.getHoraCierre());
        log.info("[TEST] Horario actualizado: {}-{}", resultado.getHoraApertura(), resultado.getHoraCierre());
    }

    @Test
    void instalacion_actualizarHorario_cierreMenorQueApertura_lanzaExcepcion() {
        log.info("[TEST] InstalacionService.actualizarHorarioGeneral - cierre <= apertura");
        assertThrows(IllegalArgumentException.class,
                () -> instalacionService.actualizarHorarioGeneral(1L, LocalTime.of(20, 0), LocalTime.of(8, 0)));
    }

    @Test
    void instalacion_actualizarHorario_noExiste_lanzaExcepcion() {
        log.info("[TEST] InstalacionService.actualizarHorarioGeneral - instalacion 999 no existe");
        when(instalacionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> instalacionService.actualizarHorarioGeneral(999L, LocalTime.of(8, 0), LocalTime.of(22, 0)));
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
        nueva.setInstalacion(instalacion);

        when(pistaRepository.existsByNombreIgnoreCase("Pista Tenis 1")).thenReturn(false);
        when(instalacionRepository.findById(1L)).thenReturn(Optional.of(instalacion));
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
    void instalacion_existeInstalacion_devuelveTrue() {
        log.info("[TEST] InstalacionService.existeInstalacion - id=1 existe");
        when(instalacionRepository.existsById(1L)).thenReturn(true);

        assertTrue(instalacionService.existeInstalacion(1L));
    }

    @Test
    void pista_obtenerPorInstalacion_noExiste_lanzaExcepcion() {
        log.info("[TEST] PistaService.obtenerPistasPorInstalacionId - instalacion 99 no existe");
        when(instalacionRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> pistaService.obtenerPistasPorInstalacionId(99L));
    }

    @Test
    void pista_eliminar_conExito_llamaDeleteById() {
        log.info("[TEST] PistaService.eliminarPista - eliminar pista existente");
        when(pistaRepository.existsById(5L)).thenReturn(true);
        doNothing().when(pistaRepository).deleteById(5L);

        assertDoesNotThrow(() -> pistaService.eliminarPista(5L));
        verify(pistaRepository).deleteById(5L);
    }
}
