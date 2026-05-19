package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.entity.EstadoIluminacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.EstadoIluminacionRepository;
import com.deustosport.my_app.repository.PistaRepository;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class IluminacionServiceTest {

    private static final Logger log = LoggerFactory.getLogger(IluminacionServiceTest.class);

    @Mock private EstadoIluminacionRepository estadoRepo;
    @Mock private PistaRepository pistaRepository;
    @Mock private ReservaRepository reservaRepository;

    @InjectMocks private IluminacionService iluminacionService;

    private Pista pista;
    private EstadoIluminacion estadoApagado;
    private EstadoIluminacion estadoEncendido;

    @BeforeEach
    void setUp() {
        Polideportivo poli = new Polideportivo();
        poli.setId(1L);
        poli.setNombre("Polideportivo Central");

        pista = new Pista();
        pista.setId(10L);
        pista.setNombre("Pista Pádel 1");
        pista.setTipoDeporte(TipoDeporte.PADEL);
        pista.setActiva(true);
        pista.setPolideportivo(poli);

        estadoApagado = new EstadoIluminacion();
        estadoApagado.setId(1L);
        estadoApagado.setPista(pista);
        estadoApagado.setEncendida(false);
        estadoApagado.setUltimoCambio(LocalDateTime.now().minusHours(2));
        estadoApagado.setMotivoUltimoCambio("Sin reservas");

        estadoEncendido = new EstadoIluminacion();
        estadoEncendido.setId(1L);
        estadoEncendido.setPista(pista);
        estadoEncendido.setEncendida(true);
        estadoEncendido.setUltimoCambio(LocalDateTime.now().minusMinutes(30));
        estadoEncendido.setMotivoUltimoCambio("Reserva activa");
    }

    // ── obtenerEstado ──────────────────────────────────────────────────────────

    @Test
    void obtenerEstado_pistaExiste_devuelveEstado() {
        log.info("[TEST] IluminacionService.obtenerEstado - pista existe");
        when(pistaRepository.findById(10L)).thenReturn(Optional.of(pista));
        when(estadoRepo.findByPistaId(10L)).thenReturn(Optional.of(estadoApagado));

        EstadoIluminacion resultado = iluminacionService.obtenerEstado(10L);

        assertNotNull(resultado);
        assertFalse(resultado.isEncendida());
        assertEquals(pista, resultado.getPista());
        log.info("[TEST] Estado iluminación: encendida={}", resultado.isEncendida());
    }

    @Test
    void obtenerEstado_pistaNoExiste_lanzaExcepcion() {
        log.info("[TEST] IluminacionService.obtenerEstado - pista 999 no existe");
        when(pistaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> iluminacionService.obtenerEstado(999L));
        log.info("[TEST] Excepción correcta al obtener estado de pista inexistente");
    }

    @Test
    void obtenerEstado_sinEstadoPrevio_creaEstadoNuevo() {
        log.info("[TEST] IluminacionService.obtenerEstado - sin estado previo, crea nuevo");
        when(pistaRepository.findById(10L)).thenReturn(Optional.of(pista));
        when(estadoRepo.findByPistaId(10L)).thenReturn(Optional.empty());
        when(estadoRepo.save(any())).thenReturn(estadoApagado);

        EstadoIluminacion resultado = iluminacionService.obtenerEstado(10L);

        assertNotNull(resultado);
        verify(estadoRepo).save(any(EstadoIluminacion.class));
        log.info("[TEST] Estado inicial creado: encendida={}", resultado.isEncendida());
    }

    // ── obtenerTodosLosEstados ─────────────────────────────────────────────────

    @Test
    void obtenerTodosLosEstados_sinPistasActivas_devuelveListaVacia() {
        log.info("[TEST] IluminacionService.obtenerTodosLosEstados - sin pistas activas");
        when(pistaRepository.findByActivaTrue()).thenReturn(List.of());

        List<Map<String, Object>> resultado = iluminacionService.obtenerTodosLosEstados();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        log.info("[TEST] Lista vacía sin pistas activas");
    }

    @Test
    void obtenerTodosLosEstados_unaActiva_devuelveEstado() {
        log.info("[TEST] IluminacionService.obtenerTodosLosEstados - una pista activa");
        when(pistaRepository.findByActivaTrue()).thenReturn(List.of(pista));
        when(estadoRepo.findByPistaId(10L)).thenReturn(Optional.of(estadoApagado));

        List<Map<String, Object>> resultado = iluminacionService.obtenerTodosLosEstados();

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).get("pistaId"));
        assertEquals("Pista Pádel 1", resultado.get(0).get("pistaNombre"));
        assertEquals(false, resultado.get(0).get("encendida"));
        log.info("[TEST] Estado de pista activa: {}", resultado.get(0));
    }

    // ── revisarIluminacion ────────────────────────────────────────────────────

    @Test
    void revisarIluminacion_sinPistasActivas_noHaceNada() {
        log.info("[TEST] IluminacionService.revisarIluminacion - sin pistas activas");
        when(pistaRepository.findByActivaTrue()).thenReturn(List.of());

        assertDoesNotThrow(() -> iluminacionService.revisarIluminacion());
        verify(estadoRepo, never()).save(any());
        log.info("[TEST] Sin pistas activas: no se modifica ningún estado");
    }

    @Test
    void revisarIluminacion_pistaConLuzEncendidaSinReservas_apagaLuz() {
        log.info("[TEST] IluminacionService.revisarIluminacion - luz encendida, sin reservas → apaga");
        when(pistaRepository.findByActivaTrue()).thenReturn(List.of(pista));
        when(estadoRepo.findByPistaId(10L)).thenReturn(Optional.of(estadoEncendido));
        when(reservaRepository.findActivasByPistaAndRango(eq(10L), any(), any()))
                .thenReturn(List.of());
        when(estadoRepo.save(any())).thenReturn(estadoEncendido);

        iluminacionService.revisarIluminacion();

        verify(estadoRepo).save(argThat(e -> !e.isEncendida()));
        log.info("[TEST] Luz apagada correctamente cuando no hay reservas");
    }

    @Test
    void revisarIluminacion_pistaConLuzApagadaSinReservas_noHaceNada() {
        log.info("[TEST] IluminacionService.revisarIluminacion - luz apagada, sin reservas → no cambia");
        when(pistaRepository.findByActivaTrue()).thenReturn(List.of(pista));
        when(estadoRepo.findByPistaId(10L)).thenReturn(Optional.of(estadoApagado));
        when(reservaRepository.findActivasByPistaAndRango(eq(10L), any(), any()))
                .thenReturn(List.of());

        iluminacionService.revisarIluminacion();

        verify(estadoRepo, never()).save(any());
        log.info("[TEST] Luz ya apagada y sin reservas: sin cambios");
    }

    @Test
    void revisarIluminacion_reservaPendienteNoEnciendeEn5Min_noModificaEstado() {
        log.info("[TEST] IluminacionService.revisarIluminacion - reserva PENDIENTE no activa iluminación");
        Reserva reservaPendiente = new Reserva();
        reservaPendiente.setId(50L);
        reservaPendiente.setHoraInicio(LocalTime.now().plusMinutes(3));
        reservaPendiente.setHoraFin(LocalTime.now().plusMinutes(63));
        reservaPendiente.setFechaReserva(LocalDate.now());
        reservaPendiente.setEstado(EstadoReserva.PENDIENTE);

        when(pistaRepository.findByActivaTrue()).thenReturn(List.of(pista));
        when(estadoRepo.findByPistaId(10L)).thenReturn(Optional.of(estadoApagado));
        when(reservaRepository.findActivasByPistaAndRango(eq(10L), any(), any()))
                .thenReturn(List.of(reservaPendiente));

        iluminacionService.revisarIluminacion();

        verify(estadoRepo, never()).save(any());
        log.info("[TEST] Reserva pendiente no activa iluminación");
    }

    @Test
    void revisarIluminacion_reservaConfirmadaEn5Min_enciendeLuz() {
        log.info("[TEST] IluminacionService.revisarIluminacion - reserva CONFIRMADA en 3 min → enciende");
        LocalTime ahora = LocalTime.now();
        Reserva reservaConfirmada = new Reserva();
        reservaConfirmada.setId(60L);
        reservaConfirmada.setHoraInicio(ahora.plusMinutes(3));
        reservaConfirmada.setHoraFin(ahora.plusMinutes(63));
        reservaConfirmada.setFechaReserva(LocalDate.now());
        reservaConfirmada.setEstado(EstadoReserva.CONFIRMADA);

        when(pistaRepository.findByActivaTrue()).thenReturn(List.of(pista));
        when(estadoRepo.findByPistaId(10L)).thenReturn(Optional.of(estadoApagado));
        when(reservaRepository.findActivasByPistaAndRango(eq(10L), any(), any()))
                .thenReturn(List.of(reservaConfirmada));
        when(estadoRepo.save(any())).thenReturn(estadoEncendido);

        iluminacionService.revisarIluminacion();

        verify(estadoRepo).save(argThat(EstadoIluminacion::isEncendida));
        log.info("[TEST] Luz encendida 3 min antes de reserva confirmada");
    }
}
